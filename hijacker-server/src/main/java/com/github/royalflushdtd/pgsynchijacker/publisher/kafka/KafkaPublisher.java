package com.github.royalflushdtd.pgsynchijacker.publisher.kafka;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.github.royalflushdtd.pgsynchijacker.Statics;
import com.github.royalflushdtd.pgsynchijacker.MonitorFactory;
import com.github.royalflushdtd.pgsynchijacker.config.KafkaConfig;
import com.github.royalflushdtd.pgsynchijacker.model.ColumnData;
import com.github.royalflushdtd.pgsynchijacker.model.Event;
import com.github.royalflushdtd.pgsynchijacker.publisher.BasePublisher;
import com.github.royalflushdtd.pgsynchijacker.publisher.IPublisher;

/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public class KafkaPublisher extends BasePublisher implements IPublisher {

    private static final Logger                     /**/ log = LoggerFactory.getLogger(KafkaPublisher.class);

    private final List<KafkaConfig>                 /**/ kafkaConfigs;
    private final KafkaProducer<String, String>     /**/ producer;

    public KafkaPublisher(List<KafkaConfig> kafkaConfigs) {
        this.kafkaConfigs = kafkaConfigs;
        this.producer = new KafkaProducer<>(getProperties(kafkaConfigs));
    }

    private static Properties getProperties(List<KafkaConfig> kafkaConfigs) {
        KafkaConfig kafkaConfig = kafkaConfigs.get(0);
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getServer());
        props.put(ProducerConfig.ACKS_CONFIG, kafkaConfig.getAckConfig());
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, kafkaConfig.getKeySerializer());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, kafkaConfig.getValSerializer());
        props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, "org.apache.kafka.clients.producer.internals.DefaultPartitioner");
        return props;
    }

    @Override
    public void publish(Event event, Callback callback) {
        //数据备份 防止篡改
        List<ColumnData> dataList = new ArrayList<>();
        for (ColumnData columnData : event.getDataList()) {
            dataList.add(columnData.copyColumnData());
        }
        for (KafkaConfig kafkaConfig : this.kafkaConfigs) {
            this.internalPublish(kafkaConfig,event,callback);
            //防止被修改后 被其他队列用到，所以重新数据装载次
            event.setDataList(dataList);
        }

    }

    @Override
    public void close() {
        this.producer.close();
        log.info("KafkaPublisher Closed");
    }

    private void internalPublish(KafkaConfig kafkaConfig, Event event, Callback callback) {
        //keys 增加
        List<String> keys = kafkaConfig.getKeys();
        wrapperEventByKeys(event,keys);
        if (CollectionUtils.isEmpty(kafkaConfig.getFilters()) || kafkaConfig.getFilters().stream().allMatch(filter -> filter.filter(event))) {
            sendToKafka(kafkaConfig, event, callback);
        }
    }

    /**
     * 将key放到event中
     * @param event
     * @param keys
     */
    private void wrapperEventByKeys(Event event, List<String> keys) {
        if( keys == null || keys.size() == 0){
            return;
        }
        List<ColumnData> dataList = event.getDataList();
        for (ColumnData columnData : dataList) {
            if(keys.contains(columnData.getName())){
                columnData.setKey(true);
            }
        }
    }

    private void sendToKafka(KafkaConfig kafkaConfig, Event event, Callback callback) {
        String value = JSON.toJSONString(event);
        ProducerRecord<String, String> record = new ProducerRecord<>(kafkaConfig.getTopic(), getPrimaryKey(kafkaConfig, event), value);
        String[] errors = new String[1];
        try {
            if (callback == null) {
                producer.send(record);
            } else {
                producer.send(record,
                        (metadata, exception) -> {
                            if (exception != null) {
                                errors[0] = exception.getMessage();
                                KafkaPublisher.this.onFailure(callback, exception);
                            } else {
                                KafkaPublisher.this.onSuccess(callback);
                            }
                        });
            }
        } catch (Exception e) {
            errors[0] = e.getMessage();
            throw new RuntimeException(e);
        } finally {
            Statics statics = Statics.createStatics(
                    "PgSyncAppService",
                    event.getSchema(),
                    event.getSlotName(),
                    event.getTable(),
                    1,
                    "kafka",
                    errors[0] == null ? "" : errors[0]
            );
            MonitorFactory.getMonitor().collect(statics);
        }
    }

    private String getPrimaryKey(KafkaConfig kafkaConfig, Event event) {
        List<String> pkNames = kafkaConfig.getKeys();
        if (CollectionUtils.isEmpty(pkNames)) {
            return null;
        }
        Map<String, String> data = event.getDataList().stream().collect(Collectors.toMap(ColumnData::getName, ColumnData::getValue));
        return kafkaConfig.getKeys().stream().map(data::get).reduce((s1, s2) -> s1 + "_" + s2).orElse(null);
    }

}
