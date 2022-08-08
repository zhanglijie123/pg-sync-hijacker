package com.github.royalflushdtd.pgsynchijacker.config;

/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public interface ConfigListener {


    /**
     * 监听值变化
     *
     * @param key      键值
     * @param oldValue 旧值
     * @param newValue 新值
     */
    void onChange(String key, String oldValue, String newValue);

}
