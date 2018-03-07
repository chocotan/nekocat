package io.loli.nekocat.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.LongAdder;

public class NekoCatNamedThreadFactory implements ThreadFactory {


    private final LongAdder threadIdx = new LongAdder();
    private final String type;
    private final String suffix;

    public NekoCatNamedThreadFactory(String type, String suffix) {
        this.type = type;
        this.suffix = suffix;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        threadIdx.add(1);
        return new Thread(runnable, type + "-" + threadIdx.intValue() + "-" + suffix);
    }
}