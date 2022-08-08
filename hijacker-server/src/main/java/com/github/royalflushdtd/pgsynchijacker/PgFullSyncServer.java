package com.github.royalflushdtd.pgsynchijacker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.postgresql.PGConnection;
import org.postgresql.PGProperty;
import org.postgresql.replication.LogSequenceNumber;
import org.postgresql.replication.PGReplicationStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.royalflushdtd.pgsynchijacker.config.JdbcConfig;
import com.github.royalflushdtd.pgsynchijacker.config.SubscribeConfig;
import com.github.royalflushdtd.pgsynchijacker.config.YmlSubscribeConfig;
import com.github.royalflushdtd.pgsynchijacker.lock.ZkLock;
import com.github.royalflushdtd.pgsynchijacker.model.Event;
import com.github.royalflushdtd.pgsynchijacker.model.InvokeContext;
import com.github.royalflushdtd.pgsynchijacker.parse.EventParser;
import com.github.royalflushdtd.pgsynchijacker.parse.IEventParser;
import com.github.royalflushdtd.pgsynchijacker.publisher.IPublisher;
import com.github.royalflushdtd.pgsynchijacker.publisher.PublisherManager;
import com.github.royalflushdtd.pgsynchijacker.utils.TimeUtils;

/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public class PgFullSyncServer {

    private static final Logger         /**/ log = LoggerFactory.getLogger(PgFullSyncServer.class);

    private final String                /**/ serverId;
    private final String                /**/ dumpFile;
    private final IEventParser          /**/ eventParser = new EventParser();
    private final Thread                /**/ startThread;



    public PgFullSyncServer(YmlSubscribeConfig apolloConfig) {
        this.dumpFile = apolloConfig.getPgDumpPath();
        this.serverId = "xxxxx" ;
        this.startThread = new Thread(new FullyThread(), "PgFullSyncStartThread-" );
    }

    public void start() {
        this.startThread.start();
    }

    private class FullyThread implements Runnable{

       @Override
       public void run(){

           File file = new File(dumpFile);
           try{
               if(!file.exists()){
                   log.info("full dump file not exist!!");;
               }
               if(file.exists()){
                   FileInputStream fis = new FileInputStream(file);
                   BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis));
                   String str = null;
                   while((str = bufferedReader.readLine()) != null)
                   {
                        if((str.indexOf("INSERT") == 0 || str.indexOf("insert")==0) && str.lastIndexOf(";")==(str.length()-1)){

                            Event event = parseEvent(str);

                            Map<String, IPublisher> publishers = new PublisherManager().getPublishers();
                            for (Map.Entry<String, IPublisher> entry : publishers.entrySet()) {
                                IPublisher value = entry.getValue();
                                value.publish(event,null);
                            }
                        }
                   }
                  log.info("fully sync complete!!!");;
               }
           }catch (Exception e){
               log.info("current thread is {},{}",Thread.currentThread().getName(),e);
           }finally {
               file.delete();
           }
       }
   }

    private Event parseEvent(String str) {
        return new Event();
    }

    public static void main(String[] args) throws  Exception {
        FileInputStream fis = new FileInputStream("D:\\lagouStudy\\pg-sync-hijacker\\hijacker-server\\src\\main\\resources\\public_dump.sql");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis));
        String str = null;
        while((str = bufferedReader.readLine()) != null)
        {
            if((str.indexOf("INSERT") == 0 || str.indexOf("insert")==0) && str.lastIndexOf(";")==(str.length()-1)){

            }
        }
    }
}
