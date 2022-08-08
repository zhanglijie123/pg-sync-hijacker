package com.github.royalflushdtd.pgsynchijacker.api.kafka.service;

/**
 * @author zhanglijie
 * @version 1.0
 * @since 1.1.0 2022/8/7 0007 19:52
 */
public interface Translate {
    /**
     * 翻译
     * @param content
     * @param appId  调机器翻译用的appId
     * @param appKey 调机器翻译用的appKey
     * @return 翻译后的文本
     */
    public String textTranslate(String content,String appId,String appKey);
}
