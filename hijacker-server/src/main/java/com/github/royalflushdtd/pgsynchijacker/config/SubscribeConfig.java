package com.github.royalflushdtd.pgsynchijacker.config;


/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public class SubscribeConfig {

    private String serverId;
    private JdbcConfig jdbcConfig;
    private ZkConfig zkConfig;
    private String dumpFile;

    public String getDumpFile() {
        return dumpFile;
    }

    public void setDumpFile(String dumpFile) {
        this.dumpFile = dumpFile;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public JdbcConfig getJdbcConfig() {
        return jdbcConfig;
    }

    public void setJdbcConfig(JdbcConfig jdbcConfig) {
        this.jdbcConfig = jdbcConfig;
    }

    public ZkConfig getZkConfig() {
        return zkConfig;
    }

    public void setZkConfig(ZkConfig zkConfig) {
        this.zkConfig = zkConfig;
    }

}

