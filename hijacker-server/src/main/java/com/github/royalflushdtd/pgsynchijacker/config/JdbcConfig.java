package com.github.royalflushdtd.pgsynchijacker.config;


/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public class JdbcConfig {
    private String url;
    private String username;
    private String password;
    private String slotName;
    private String lastLsn = "";
    private String minVersion = "9.4";
    private String rplLevel = "database";
    private String host;
    private int port;
    private String schema;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSlotName() {
        return slotName;
    }

    public void setSlotName(String slotName) {
        this.slotName = slotName;
    }

    public String getLastLsn() {
        return lastLsn;
    }

    public void setLastLsn(String lastLsn) {
        this.lastLsn = lastLsn;
    }

    public String getMinVersion() {
        return minVersion;
    }

    public void setMinVersion(String minVersion) {
        this.minVersion = minVersion;
    }

    public String getRplLevel() {
        return rplLevel;
    }

    public void setRplLevel(String rplLevel) {
        this.rplLevel = rplLevel;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }
}
