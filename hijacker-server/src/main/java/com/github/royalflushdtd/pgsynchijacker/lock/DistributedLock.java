package com.github.royalflushdtd.pgsynchijacker.lock;

import java.util.concurrent.TimeUnit;

/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public interface DistributedLock {

    /**
     * 获取锁
     *
     * @throws LockingException
     */
    void lock() throws LockingException;

    /**
     * 尝试获取锁
     *
     * @param timeout 超时时间
     * @param unit    时间刻度
     * @return lock success:true else:false
     */
    boolean tryLock(long timeout, TimeUnit unit);

    /**
     * 释放锁
     *
     * @throws LockingException
     */
    void unlock() throws LockingException;

}
