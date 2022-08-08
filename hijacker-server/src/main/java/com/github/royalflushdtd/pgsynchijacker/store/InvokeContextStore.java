package com.github.royalflushdtd.pgsynchijacker.store;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.collections.CollectionUtils;
import com.github.royalflushdtd.pgsynchijacker.model.Event;
import com.github.royalflushdtd.pgsynchijacker.model.InvokeContext;
import com.github.royalflushdtd.pgsynchijacker.publisher.PublisherManager;
import com.github.royalflushdtd.pgsynchijacker.utils.NamedThreadFactory;
import com.github.royalflushdtd.pgsynchijacker.utils.TimeUtils;

/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public class InvokeContextStore implements IStore, AutoCloseable {

    private static final int PER_CONTEXTS = 10240;
    private static final int MAX_CONTEXTS = PER_CONTEXTS * 5;

    private static final int CPU_NUMBERS = Runtime.getRuntime().availableProcessors();
    private static final int THD_NUMBERS = CPU_NUMBERS << 1;

    private final AtomicBoolean started = new AtomicBoolean(Boolean.FALSE);
    private final Map<String, BlockingQueue<InvokeContext>> caches;
    private final ThreadPoolExecutor executor;

    public InvokeContextStore() {
        this.caches = new ConcurrentHashMap<>();
        this.executor = new ThreadPoolExecutor(
                THD_NUMBERS, THD_NUMBERS,
                60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(MAX_CONTEXTS),
                new NamedThreadFactory("")
        );

        this.started.compareAndSet(Boolean.FALSE, Boolean.TRUE);
        for (int i = 0; i < THD_NUMBERS; i++) {
            this.executor.submit(new ConsumeTask());
        }
    }

    private static boolean eventIsEmpty(Event event) {
        return event == null ||
                (event.getSchema() == null
                        && event.getTable() == null
                        && event.getEventType() == null
                        && CollectionUtils.isEmpty(event.getDataList()));
    }

    private static boolean ctxIsEmpty(InvokeContext ctx) {
        return ctx == null || eventIsEmpty(ctx.getEvent());
    }

    @Override
    public void store(InvokeContext ctx) {
        if (ctxIsEmpty(ctx)) {
            return;
        }
        PublisherManager.getInstance().publish(ctx, null);
    }

    @Override
    public void close() {
        this.started.compareAndSet(Boolean.TRUE, Boolean.FALSE);
        this.executor.shutdown();
        this.caches.values().forEach(BlockingQueue::clear);
        this.caches.values().clear();
        this.caches.clear();
    }

    public void asyncPublish(InvokeContext ctx) {
        if (ctxIsEmpty(ctx)) {
            return;
        }

        BlockingQueue<InvokeContext> queue = putToCache(ctx);
        if (queue.size() > PER_CONTEXTS) {
            forceFlushMemory(queue);
        }
    }

    private synchronized BlockingQueue<InvokeContext> putToCache(InvokeContext context) {
        BlockingQueue<InvokeContext> blockingQueue = caches.get(context.getSlotName());
        if (blockingQueue == null) {
            caches.put(context.getSlotName(), new LinkedBlockingQueue<>(MAX_CONTEXTS));
            blockingQueue = caches.get(context.getSlotName());
        }
        try {
            blockingQueue.put(context);
        } catch (Exception e) {
            //
        }
        return blockingQueue;
    }

    private void forceFlushMemory(BlockingQueue<InvokeContext> queue) {
        if (CollectionUtils.isEmpty(queue)) {
            return;
        }
        int capacity = started.get() ? Math.min(PER_CONTEXTS, queue.size()) : queue.size();
        List<InvokeContext> contexts = new LinkedList<>();
        queue.drainTo(contexts, capacity);
        PublisherManager.getInstance().publish(contexts);
    }

    private class ConsumeTask implements Runnable {

        @Override
        public void run() {
            while (started.get()) {
                long s = System.currentTimeMillis();
                try {
                    for (BlockingQueue<InvokeContext> queue : caches.values()) {
                        forceFlushMemory(queue);
                    }
                } finally {
                    long e = System.currentTimeMillis();
                    TimeUtils.sleepOneSecond(s, e);
                }
            }

            for (BlockingQueue<InvokeContext> queue : caches.values()) {
                forceFlushMemory(queue);
            }
        }

    }

}
