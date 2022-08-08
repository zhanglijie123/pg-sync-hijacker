package com.github.royalflushdtd.pgsynchijacker.demo;

import org.postgresql.PGConnection;
import org.postgresql.PGProperty;
import org.postgresql.replication.LogSequenceNumber;
import org.postgresql.replication.PGReplicationStream;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import sun.rmi.runtime.Log;

public class Demo {

    String url = "jdbc:postgresql://192.168.47.145:5432/postgres";

    String user = "postgres";
    String pass = "postgres";
    private Connection connection;
    private PGConnection rplConnection;
    private PGReplicationStream stream;

    private void createConn() throws SQLException {
        Properties props = new Properties();
        PGProperty.USER.set(props, user);
        PGProperty.PASSWORD.set(props, pass);
        PGProperty.ASSUME_MIN_SERVER_VERSION.set(props, "9.4");
        PGProperty.REPLICATION.set(props, "database");
        PGProperty.PREFER_QUERY_MODE.set(props, "simple");
        this.connection = DriverManager.getConnection(url, props);
        this.rplConnection = this.connection.unwrap(PGConnection.class);
    }

    private void createRplSlot() throws SQLException {
        this.rplConnection.getReplicationAPI()
                .createReplicationSlot()
                .logical()
                .withSlotName("test_slot8")
                .withOutputPlugin("test_decoding")
                .make();
    }

    private void createRplStream() throws SQLException {
        this.stream = this.rplConnection.getReplicationAPI()
                .replicationStream()
                .logical()
                .withSlotName("test_slot8")
                .withSlotOption("include-xids", true)
                .withSlotOption("skip-empty-xacts", true)
                .withStatusInterval(5, TimeUnit.SECONDS)
                .start();
    }

    public String processRecords() throws Exception {
        String event = null;
        final ByteBuffer record = this.stream.read();
        LogSequenceNumber lsn = stream.getLastReceiveLSN();
        if (record != null) {
            final int offset = record.arrayOffset();
            final byte[] source = record.array();
            final int length = source.length - offset;
            event = new String(source, offset, length);
        }
        //feedback offset
        stream.setAppliedLSN(lsn);
        stream.setFlushedLSN(lsn);
        return event;
    }

    private void dropSlot() throws SQLException {
        this.rplConnection.getReplicationAPI().dropReplicationSlot("test_slot8");
    }


    public static void main(String[] args) throws Exception {
        Demo demo = new Demo();
        demo.createConn();
        try{
            demo.createRplSlot();
        }catch (Exception e){
            System.out.println(e+"slot has exits");
        }
        demo.createRplStream();
        while (true) {
            String res = demo.processRecords();
            System.out.println(res);
        }
        // Demo demo = new Demo();
        // demo.createConn();
        // String sql = "INSERT INTO \"public\".\"user\" (\"id\", \"name\", \"hobby\") VALUES (4, 'dsf', 'sssss');\n";
        // PreparedStatement pstmt=demo.connection.prepareStatement(sql);
        // pstmt.execute();
    }
}
