package com.github.royalflushdtd.pgsynchijacker.api.kafka.service;

/**
 * @author zhanglijie
 * @version 1.0
 * @since 1.1.0 2022/8/7 0007 17:56
 */
public interface TranslateAndSyncService {
    /**
     * 对文本进行翻译并且同步数据库
     * @param content
     */
    public void translate(String content);
}
