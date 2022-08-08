package com.github.royalflushdtd.pgsynchijacker.utils;

/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public interface ObjectManager<T> {

    /**
     * 产生一个实例
     *
     * @return 实例
     * @throws Exception 错误信息
     */
    T newInstance() throws Exception;

    /**
     * 释放资源
     *
     * @param instance 实例
     */
    default void releaseInstance(T instance) {
    }


    default boolean validateInstance(T instance) {
        return true;
    }

}
