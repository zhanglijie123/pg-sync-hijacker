package com.github.royalflushdtd.pgsynchijacker.utils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public class NamedThreadFactory implements ThreadFactory {
    private final AtomicInteger index = new AtomicInteger();

    private String name;

    public NamedThreadFactory(String name) {
        this.name = name;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, this.name + "-" + index.incrementAndGet());
    }
}
