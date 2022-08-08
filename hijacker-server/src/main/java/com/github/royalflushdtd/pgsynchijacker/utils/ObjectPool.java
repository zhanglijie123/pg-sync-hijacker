package com.github.royalflushdtd.pgsynchijacker.utils;


/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public interface ObjectPool<T> extends AutoCloseable {

    /**
     * get CONFIG_NAME object from pool
     *
     * @return instance
     */
    T borrowObject();

    /**
     * return obj to pool
     *
     * @param obj object
     * @return
     */
    void returnObject(T obj);

    /**
     * 释放资源
     */
    @Override
    void close();

}
