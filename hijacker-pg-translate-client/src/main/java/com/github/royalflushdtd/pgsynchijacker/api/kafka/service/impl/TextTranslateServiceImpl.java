package com.github.royalflushdtd.pgsynchijacker.api.kafka.service.impl;

import java.sql.Struct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.royalflushdtd.pgsynchijacker.api.kafka.service.Translate;

import cn.hutool.core.util.StrUtil;

/**
 * @author zhanglijie
 * @version 1.0
 * @since 1.1.0 2022/8/7 0007 19:55
 */
@Service
public class TextTranslateServiceImpl implements Translate {
    @Value("${translate.uri:http://xxxxx.com/translate}")
    private String translateUri;

    @Override
    public String textTranslate(String content, String appId, String appKey) {
        //这里做简单处理 都翻译成helloworld. 实际场景需要调用机器翻译接口
        return "HelloWorld";
    }
}
