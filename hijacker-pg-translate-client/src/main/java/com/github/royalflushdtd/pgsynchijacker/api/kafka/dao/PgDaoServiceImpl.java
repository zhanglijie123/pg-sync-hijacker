package com.github.royalflushdtd.pgsynchijacker.api.kafka.dao;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.postgresql.PGConnection;
import org.postgresql.PGProperty;
import org.postgresql.replication.LogSequenceNumber;
import org.postgresql.replication.PGReplicationStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhanglijie
 * @version 1.0
 * @since 1.1.0 2022/8/7 0007 20:34
 */
@Service
@Slf4j
public class PgDaoServiceImpl {
    @Value("${jdbc.pg.uri:}")
    private String url ;
    @Value("${jdbc.pg.user}")
    private String user ;
    @Value("${jdbc.pg.password}")
    private String pass  ;
    private Connection connection;
    @PostConstruct
    public void createConn() throws SQLException {
        Properties props = new Properties();
        PGProperty.USER.set(props, user);
        PGProperty.PASSWORD.set(props, pass);
        PGProperty.ASSUME_MIN_SERVER_VERSION.set(props, "9.4");
        PGProperty.REPLICATION.set(props, "database");
        PGProperty.PREFER_QUERY_MODE.set(props, "simple");
        this.connection = DriverManager.getConnection(url, props);
    }
    public void exeSql(String sql){
        try {
            //简单实现 数据库操作
            PreparedStatement pstmt = this.connection.prepareStatement(sql);
            pstmt.execute();
        }catch (Exception e){
            log.error("{}",e);
        }
    }

}
