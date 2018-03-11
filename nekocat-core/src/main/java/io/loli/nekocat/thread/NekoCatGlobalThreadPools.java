package io.loli.nekocat.thread;

import io.loli.nekocat.NekoCatProperties;

import java.util.concurrent.*;

public class NekoCatGlobalThreadPools {
    private static ConcurrentHashMap<String, ThreadPoolExecutor> downloadThreadPoolMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, ThreadPoolExecutor> piplineThreadPoolMap = new ConcurrentHashMap<>();


    public static ThreadPoolExecutor getDownloadExecutor(NekoCatProperties properties, String spiderName) {
        String key = spiderName + "-" + properties.getName() + "-download";
        return downloadThreadPoolMap.computeIfAbsent(key, name -> new ThreadPoolExecutor(properties.getDownloadPoolSize(), properties.getDownloadPoolSize(),
                0, TimeUnit.MILLISECONDS,
                properties.getDownloadMaxQueueSize() == 0 ? new SynchronousQueue<>() :
                        new LinkedBlockingQueue<>(properties.getDownloadMaxQueueSize()), new NekoCatNamedThreadFactory(key)));
    }

    public static ThreadPoolExecutor getPiplineExecutor(NekoCatProperties properties, String spiderName) {
        String key = spiderName + "-" + properties.getName() + "-pipline";
        return piplineThreadPoolMap.computeIfAbsent(key, name -> new ThreadPoolExecutor(properties.getPiplinePoolSize(), properties.getPiplinePoolSize(),
                0, TimeUnit.MILLISECONDS,
                properties.getPiplineMaxQueueSize() == 0 ? new SynchronousQueue<>() :
                        new LinkedBlockingQueue<>(properties.getPiplineMaxQueueSize()), new NekoCatNamedThreadFactory(key)));
    }


    public static void shutdown(NekoCatProperties properties, String spiderName) {
        String key = spiderName + "-" + properties.getName() + "-pipline";
        ThreadPoolExecutor threadPoolExecutor = piplineThreadPoolMap.get(key);
        if (threadPoolExecutor != null && !threadPoolExecutor.isShutdown()) {
            threadPoolExecutor.shutdownNow();
        }
        piplineThreadPoolMap.remove(key);
        key = spiderName + "-" + properties.getName() + "-download";
        ThreadPoolExecutor downloadPool = downloadThreadPoolMap.get(key);
        if (downloadPool != null && !downloadPool.isShutdown()) {
            downloadPool.shutdownNow();
        }
        downloadThreadPoolMap.remove(key);
    }


}
