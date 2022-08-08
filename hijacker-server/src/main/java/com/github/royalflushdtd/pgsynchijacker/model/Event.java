package com.github.royalflushdtd.pgsynchijacker.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public class Event implements Serializable {

    private static final long serialVersionUID = 3414755790085772526L;

    private long lsn;

    private transient String slotName;
    private transient String serverId;
    private String schema;
    private String table;
    private EventType eventType;
    private List<ColumnData> dataList = new ArrayList<>();

    public String getSlotName() {
        return slotName;
    }

    public void setSlotName(String slotName) {
        this.slotName = slotName;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public List<ColumnData> getDataList() {
        return dataList;
    }

    public void setDataList(List<ColumnData> dataList) {
        this.dataList = dataList;
    }

    public long getLsn() {
        return lsn;
    }

    public void setLsn(long lsn) {
        this.lsn = lsn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Event event = (Event) o;
        return Objects.equals(schema, event.schema) &&
                Objects.equals(table, event.table) &&
                eventType == event.eventType &&
                Objects.equals(dataList, event.dataList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schema, table, eventType, dataList);
    }

    @Override
    public String toString() {
        return "Event{" +
                "schema='" + schema + '\'' +
                ", table='" + table + '\'' +
                ", eventType=" + eventType +
                ", dataList=" + dataList +
                '}';
    }
}
