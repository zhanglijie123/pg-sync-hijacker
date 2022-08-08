package com.github.royalflushdtd.pgsynchijacker.utils;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;


/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public class CommonPool<T> implements ObjectPool<T> {

    private final ObjectManager<T> manager;
    private final GenericObjectPool<T> pool;

    public CommonPool(ObjectManager<T> manager) {
        this.manager = manager;
        this.pool = newObjectPool();
    }

    @Override
    public T borrowObject() {
        try {
            return pool.borrowObject();
        } catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }
    }

    @Override
    public void returnObject(T obj) {
        pool.returnObject(obj);
    }

    @Override
    public void close() {
        this.pool.close();
    }

    private GenericObjectPool<T> newObjectPool() {
        GenericObjectPool<T> pool = new GenericObjectPool<>(new CommonFactory()); // NOSONAR
        pool.setConfig(newPoolConfig());
        return pool;
    }

    private GenericObjectPoolConfig<T> newPoolConfig() {
        GenericObjectPoolConfig<T> config = new GenericObjectPoolConfig<>();
        config.setTestWhileIdle(true);
        config.setTestOnCreate(true);
        config.setTestOnBorrow(true);
        config.setTestOnReturn(false);
        config.setMaxTotal(100);
        config.setMinIdle(30);
        config.setMaxIdle(80);
        return config;
    }

    private class CommonFactory extends BasePooledObjectFactory<T> {

        @Override
        public T create() throws Exception {
            return manager.newInstance();
        }

        @Override
        public PooledObject<T> wrap(T t) {
            return new DefaultPooledObject<>(t);
        }

        @Override
        public boolean validateObject(PooledObject<T> p) {
            return manager.validateInstance(p.getObject());
        }

        @Override
        public void destroyObject(PooledObject<T> p) {
            manager.releaseInstance(p.getObject());
        }

    }

}
