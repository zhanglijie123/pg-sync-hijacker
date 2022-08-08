package com.github.royalflushdtd.pgsynchijacker.utils;


/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public interface ObjectPoolFactory {

    <T> ObjectPool<T> createObjectPool(ObjectManager<T> manager);

}

