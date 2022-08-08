package com.github.royalflushdtd.pgsynchijacker.lock;

import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.RetryNTimes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public class ZkLock implements DistributedLock, AutoCloseable {

    private static final Logger         /**/ log = LoggerFactory.getLogger(ZkLock.class);
    private final CuratorFramework      /**/ zkClient;
    private final InterProcessMutex     /**/ lock;
    private final String                /**/ path;

    public ZkLock(String address, String path) {
        this.zkClient = startZkClient(address);
        this.path = path;
        this.lock = new InterProcessMutex(this.zkClient, path);
    }

    private static CuratorFramework startZkClient(String address) {
        CuratorFramework zkClient = CuratorFrameworkFactory.builder()
                .connectString(address)
                .connectionTimeoutMs(Integer.MAX_VALUE)
                .sessionTimeoutMs(Integer.MAX_VALUE)
                .retryPolicy(new RetryNTimes(5, 1000))
                .build();
        zkClient.start();
        log.info("ZkLog ---- ZkClient Started");
        return zkClient;
    }

    public boolean tryLock() {
        return tryLock(1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void lock() throws LockingException {
        try {
            lock.acquire();
        } catch (Exception e) {
            throw new LockingException("lock", e);
        }
    }

    @Override
    public boolean tryLock(long timeout, TimeUnit unit) {
        try {
            log.info("ZkLog ---- Acquiring Lock On:{}", path);
            while (true) {
                if (lock.acquire(timeout, unit)) {
                    break;
                }
            }
            log.info("ZkLog ---- Acquired Lock On:{}", path);
            return true;
        } catch (Exception e) {
            //
        }
        return false;
    }

    @Override
    public void unlock() {
        try {
            lock.release();
            log.info("ZkLog ---- Release Lock Done");
        } catch (Exception e) {
            //
        }
    }

    @Override
    public void close() {
        try {
            unlock();
            this.zkClient.close();
            log.info("ZkLog ---- ZkClient Closed");
        } catch (Exception e) {
            //
        }
    }

}
