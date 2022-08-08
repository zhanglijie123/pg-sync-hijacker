package com.github.royalflushdtd.pgsynchijacker;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.omg.Messaging.SYNC_WITH_TRANSPORT;
import org.postgresql.PGConnection;
import org.postgresql.PGProperty;
import org.postgresql.replication.LogSequenceNumber;
import org.postgresql.replication.PGReplicationStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.royalflushdtd.pgsynchijacker.config.JdbcConfig;
import com.github.royalflushdtd.pgsynchijacker.config.SubscribeConfig;
import com.github.royalflushdtd.pgsynchijacker.lock.ZkLock;
import com.github.royalflushdtd.pgsynchijacker.model.InvokeContext;
import com.github.royalflushdtd.pgsynchijacker.parse.EventParser;
import com.github.royalflushdtd.pgsynchijacker.parse.IEventParser;
import com.github.royalflushdtd.pgsynchijacker.utils.TimeUtils;

/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public class PgIncrementSyncServer {

    private static final Logger         /**/ log = LoggerFactory.getLogger(PgIncrementSyncServer.class);

    private final String                /**/ serverId;
    private final SubscribeConfig          /**/ config;
    private final JdbcConfig            /**/ jdbcConfig;
    private final IEventParser          /**/ eventParser = new EventParser();
    private final ZkLock                /**/ zkLock;
    private final String                /**/ slotName;
    private final ReentrantLock         /**/ lock = new ReentrantLock();

    private final Thread                /**/ startThread;
    private final Thread                /**/ receiveThread;

    private volatile boolean            /**/ started = false;
    private Connection                  /**/ connection;
    private PGConnection                /**/ rplConnection;
    private PGReplicationStream         /**/ stream;

    public PgIncrementSyncServer(SubscribeConfig config) {
        this.serverId = config.getServerId();
        this.config = config;
        this.jdbcConfig = config.getJdbcConfig();
        this.slotName = this.jdbcConfig.getSlotName();
        this.zkLock = new ZkLock(this.config.getZkConfig().getAddress(), generateLockKey(config.getJdbcConfig()));
        this.startThread = new Thread(new StartTask(), "PgIncrementSyncStartThread-" + this.slotName);
        this.receiveThread = new Thread(new ReceiveTask(), "PgIncrementSyncReceiveThread-" + this.slotName);
    }

    private static String generateLockKey(JdbcConfig config) {
        return "/com/github/pgSync/lock/" + config.getHost() + ":" + config.getPort() + "/" + config.getSchema() + "/" + config.getSlotName();
    }
    private static void closeClosable(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                //
            }
        }
    }

    public void start() {
        this.startThread.start();
    }

    public void shutdown()   {
        started = false;
        try {
          //  dropSlot();
        }catch (Exception e){
            log.info("{}",e);
        }
        closeClosable(this.stream);
        closeClosable(this.connection);
        zkLock.unlock();
        zkLock.close();
    }

    public String getServerId() {
        return serverId;
    }
    private void dropSlot() throws SQLException {
        this.rplConnection.getReplicationAPI().dropReplicationSlot(this.slotName );
        log.info("drop slot {} success",slotName);
    }
    private void createRplConn() throws SQLException {
        String url = this.jdbcConfig.getUrl();
        Properties props = new Properties();
        PGProperty.USER.set(props, this.jdbcConfig.getUsername());
        PGProperty.PASSWORD.set(props, this.jdbcConfig.getPassword());
        PGProperty.ASSUME_MIN_SERVER_VERSION.set(props, this.jdbcConfig.getMinVersion());
        PGProperty.REPLICATION.set(props, this.jdbcConfig.getRplLevel());
        PGProperty.PREFER_QUERY_MODE.set(props, "simple");

        this.connection = DriverManager.getConnection(url, props);
        this.rplConnection = this.connection.unwrap(PGConnection.class);
        log.info("GetRplConnection success,slot:{}", this.slotName);
    }

    private void createRplSlot() throws SQLException {
        try {
            this.rplConnection.getReplicationAPI()
                    .createReplicationSlot()
                    .logical()
                    .withSlotName(this.jdbcConfig.getSlotName())
                    .withOutputPlugin("test_decoding")
                    .make();
        } catch (SQLException e) {
            String msg = "ERROR: replication slot \"" + this.jdbcConfig.getSlotName() + "\" already exists";
          //   if (msg.equals(e.getMessage())) {
          //       return;
          //   }
          // throw e;
          log.info("{}",msg);
        }
        log.info("GetRplSlot success,slot:{}", this.slotName);
    }

    private void createRplStream() throws SQLException {
        this.stream = this.rplConnection.getReplicationAPI()
                .replicationStream()
                .logical()
                .withSlotName(this.jdbcConfig.getSlotName())
                .withSlotOption("include-xids", true)
                .withSlotOption("skip-empty-xacts", true)
                .withStatusInterval(5, TimeUnit.SECONDS)
                .start();
        log.info("GetRplStream success,slot:{}", this.slotName);
    }

    private void receiveStream() throws SQLException {

        assert !stream.isClosed();
        assert !connection.isClosed();

        //non blocking receive message
        ByteBuffer msg = stream.readPending();

        if (msg == null) {
            TimeUtils.sleepInMills(10L);
            return;
        }
        int offset = msg.arrayOffset();
        byte[] source = msg.array();
        int length = source.length - offset;
        LogSequenceNumber lsn = stream.getLastReceiveLSN();

        InvokeContext ctx = new InvokeContext();
        ctx.setMessage(new String(source, offset, length));
        ctx.setJdbcUrl(this.jdbcConfig.getUrl());
        ctx.setJdbcUser(this.jdbcConfig.getUsername());
        ctx.setJdbcPass(this.jdbcConfig.getPassword());
        ctx.setSlotName(this.slotName);
        ctx.setServerId(this.serverId);
        ctx.setLsn(lsn.asLong());

        eventParser.parse(ctx);


        //feedback
        stream.setAppliedLSN(lsn);
        stream.setFlushedLSN(lsn);

    }

    private void recover() {
        this.lock.lock();
        try {
            long s = System.currentTimeMillis();
            closeClosable(stream);
            closeClosable(connection);

            while (true) {
                try {
                    createRplConn();
                    createRplSlot();
                    createRplStream();
                    break;
                } catch (Exception e) {
                    log.warn("Recover Streaming Occurred Error", e);
                    closeClosable(stream);
                    closeClosable(connection);
                    TimeUtils.sleepInMills(5000);
                }
            }
            long e = System.currentTimeMillis();
            log.info("recover logical replication success,slot:{},cost:{}ms", slotName, e - s);
        } finally {
            this.lock.unlock();
        }
    }

    private class StartTask implements Runnable {

        @Override
        public void run() {
            if (zkLock.tryLock()) {
                try {
                    createRplConn();
                    createRplSlot();
                    createRplStream();
                    started = true;
                    receiveThread.start();
                    log.warn("Startup RplStream Success");
                } catch (Exception e) {
                    log.warn("Startup RplStream Failure", e);
                    shutdown();
                }
            }
        }

    }

    private class ReceiveTask implements Runnable {

        @Override
        public void run() {
            int index = 0;
            long start = System.currentTimeMillis();
            while (started) {
                try {
                    receiveStream();
                    index++;
                    if(index==1000){
                        log.info("deal {} records ,it cost time {}",index,(System.currentTimeMillis()-start));
                        index = 0;
                        start = System.currentTimeMillis();
                    }
                } catch (Exception e) {
                    log.warn("receive msg failure,try to recover.", e);
                    recover();
                    TimeUtils.sleepInMills(2000);
                }
            }
        }
    }

}
