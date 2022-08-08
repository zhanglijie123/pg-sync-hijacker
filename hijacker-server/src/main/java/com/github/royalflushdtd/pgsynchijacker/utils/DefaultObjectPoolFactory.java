package com.github.royalflushdtd.pgsynchijacker.utils;

/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public class DefaultObjectPoolFactory implements ObjectPoolFactory {

    @Override
    public <T> ObjectPool<T> createObjectPool(ObjectManager<T> manager) {
        return new CommonPool<>(manager);
    }

}
