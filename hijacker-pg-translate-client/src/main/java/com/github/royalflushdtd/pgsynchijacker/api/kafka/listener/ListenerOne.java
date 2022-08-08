package com.github.royalflushdtd.pgsynchijacker.api.kafka.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.github.royalflushdtd.pgsynchijacker.api.kafka.service.TranslateAndSyncService;

import cn.hutool.core.util.StrUtil;

/**
 * @author zhanglijie
 * @version 1.0
 * @since 1.1.0 2022/8/6 0006 16:03
 */
@Service
public class ListenerOne {
    @Autowired
    private TranslateAndSyncService translateAndSyncService;
    @KafkaListener(topics = {"topic_three"})
    public void topicOneConsumer(ConsumerRecord<String,String> record){
        if(record != null && StrUtil.isNotEmpty(record.value())){
            translateAndSyncService.translate(record.value());
        }
    }
}
