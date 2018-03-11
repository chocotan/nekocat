package io.loli.nekocat.thread;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public class NekoCatNamedThreadFactory implements ThreadFactory {


    private static final ConcurrentHashMap<String, AtomicLong> threadIdx = new ConcurrentHashMap<>();

    private final String suffix;

    public NekoCatNamedThreadFactory(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        long idx = threadIdx.computeIfAbsent(suffix, k -> new AtomicLong(0)).incrementAndGet();
        return new Thread(runnable, "nekocat" + "-" + suffix + "-" + idx);
    }
}