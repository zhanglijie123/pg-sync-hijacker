package com.github.royalflushdtd.pgsynchijacker.config.fileloader;

import com.github.royalflushdtd.pgsynchijacker.config.ConfigListener;

/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public interface ConfigLoader {

    /**
     * 获取配置
     *
     * @param key          key
     * @param defaultValue 默认值
     * @return
     */
    String getProperty(String key, String defaultValue);

    /**
     * 添加事件监听器
     *
     * @param configListener 监听器
     */
    void addChangeListener(ConfigListener configListener);

}
