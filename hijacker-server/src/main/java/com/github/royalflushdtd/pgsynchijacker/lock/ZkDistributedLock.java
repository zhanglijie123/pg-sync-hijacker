package com.github.royalflushdtd.pgsynchijacker.lock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public class ZkDistributedLock implements DistributedLock {

    private static final Logger LOG = LoggerFactory.getLogger(ZkDistributedLock.class.getName());

    private final CuratorFramework zkClient;
    private final String lockPath;

    private final AtomicBoolean aborted = new AtomicBoolean(false);
    private CountDownLatch syncPoint;
    private boolean holdsLock = false;
    private String currentId;
    private String currentNode;
    private String watchedNode;

    /**
     * Creates CONFIG_NAME distributed lock using the given {@code zkClient} to coordinate locking.
     *
     * @param zkClient The ZooKeeper client to use.
     * @param lockPath The path used to manage the lock under.
     * @param acl      The acl to apply to newly created lock nodes.
     */
    public ZkDistributedLock(CuratorFramework zkClient, String lockPath, Iterable<ACL> acl) {
        this.zkClient = zkClient;
        this.lockPath = lockPath;
        this.syncPoint = new CountDownLatch(1);
    }

    @Override
    public synchronized void lock() throws LockingException {
        if (holdsLock) {
            throw new LockingException("Error, already holding CONFIG_NAME lock. Call unlock first!");
        }
        try {
            prepare();
            syncPoint.await();
            if (!holdsLock) {
                throw new LockingException("Error, couldn't acquire the lock!");
            }
        } catch (InterruptedException e) {
            cancelAttempt();
            throw new LockingException("InterruptedException while trying to acquire lock!", e);
        } catch (Exception e) {
            // No need to clean up since the node wasn't created yet.
            throw new LockingException("ZkException while trying to acquire lock!", e);
        }
    }

    @Override
    public synchronized boolean tryLock(long timeout, TimeUnit unit) {
        if (holdsLock) {
            throw new LockingException("Error, already holding CONFIG_NAME lock. Call unlock first!");
        }
        try {
            prepare();
            boolean success = syncPoint.await(timeout, unit);
            if (!success) {
                return false;
            }
            if (!holdsLock) {
                throw new LockingException("Error, couldn't acquire the lock!");
            }
        } catch (InterruptedException e) {
            cancelAttempt();
            return false;
        } catch (Exception e) {
            // No need to clean up since the node wasn't created yet.
            throw new LockingException("ZkException while trying to acquire lock!", e);
        }
        return true;
    }

    @Override
    public synchronized void unlock() throws LockingException {
        if (currentId == null) {
            throw new LockingException("Error, neither attempting to lock nor holding CONFIG_NAME lock!");
        }
        Preconditions.checkNotNull(currentId);
        // Try aborting!
        if (!holdsLock) {
            aborted.set(true);
        } else {
            cleanup();
        }
    }

    private synchronized void prepare() throws Exception {
        this.zkClient.checkExists().forPath(lockPath);

        // Create an EPHEMERAL_SEQUENTIAL node.
        this.currentNode = this.zkClient.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(lockPath + "/member_");

        // We only care about our actual id since we want to compare ourselves to siblings.
        if (currentNode.contains("/")) {
            currentId = currentNode.substring(currentNode.lastIndexOf("/") + 1);
        }
    }

    private synchronized void cancelAttempt() {
        cleanup();
        // Bubble up failure...
        holdsLock = false;
        syncPoint.countDown();
    }

    private void cleanup() {
        LOG.info("Cleaning up!");
        Preconditions.checkNotNull(currentId);
        try {
            Stat stat = zkClient.checkExists().forPath(currentNode);
            if (stat != null) {
                zkClient.delete().forPath(currentNode);
            } else {
                LOG.info("Called cleanup but nothing to cleanup!");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        holdsLock = false;
        aborted.set(false);
        currentId = null;
        currentNode = null;
        syncPoint = new CountDownLatch(1);
    }

}
