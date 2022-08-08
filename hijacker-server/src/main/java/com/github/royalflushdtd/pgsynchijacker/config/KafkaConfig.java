package com.github.royalflushdtd.pgsynchijacker.config;

import java.util.List;

import com.github.royalflushdtd.pgsynchijacker.filter.IEventFilter;

/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public class KafkaConfig {

    private String topic;
    private String server;
    private String ackConfig = "all";
    private int retryTimes;
    private String keySerializer = "org.apache.kafka.common.serialization.StringSerializer";
    private String valSerializer = "org.apache.kafka.common.serialization.StringSerializer";
    private int partition = 0;
    private List<IEventFilter> filters;

    private List<String> keys;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getAckConfig() {
        return ackConfig;
    }

    public void setAckConfig(String ackConfig) {
        this.ackConfig = ackConfig;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public String getKeySerializer() {
        return keySerializer;
    }

    public void setKeySerializer(String keySerializer) {
        this.keySerializer = keySerializer;
    }

    public String getValSerializer() {
        return valSerializer;
    }

    public void setValSerializer(String valSerializer) {
        this.valSerializer = valSerializer;
    }

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public List<IEventFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<IEventFilter> filters) {
        this.filters = filters;
    }

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }
}
