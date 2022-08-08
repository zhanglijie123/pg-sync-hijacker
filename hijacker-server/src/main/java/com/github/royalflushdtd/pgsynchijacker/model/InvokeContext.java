package com.github.royalflushdtd.pgsynchijacker.model;

import com.github.royalflushdtd.pgsynchijacker.publisher.IPublisher;


/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public class InvokeContext {

    private String serverId;
    private String slotName;

    private String jdbcUrl;
    private String jdbcUser;
    private String jdbcPass;

    private long lsn;
    private String message;


    private String xid;
    private Event event;

    private IPublisher.Callback callback;

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getSlotName() {
        return slotName;
    }

    public void setSlotName(String slotName) {
        this.slotName = slotName;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getJdbcUser() {
        return jdbcUser;
    }

    public void setJdbcUser(String jdbcUser) {
        this.jdbcUser = jdbcUser;
    }

    public String getJdbcPass() {
        return jdbcPass;
    }

    public void setJdbcPass(String jdbcPass) {
        this.jdbcPass = jdbcPass;
    }

    public long getLsn() {
        return lsn;
    }

    public void setLsn(long lsn) {
        this.lsn = lsn;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getXid() {
        return xid;
    }

    public void setXid(String xid) {
        this.xid = xid;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public IPublisher.Callback getCallback() {
        return callback;
    }

    public void setCallback(IPublisher.Callback callback) {
        this.callback = callback;
    }

}
