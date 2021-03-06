package io.loli.nekocat.interceptor;

import io.loli.nekocat.request.NekoCatRequest;
import org.apache.commons.collections4.queue.CircularFifoQueue;

public class FilterDownloadedUrlInterceptor implements NekoCatInterceptor {
    private CircularFifoQueue<String> urls;

    public FilterDownloadedUrlInterceptor(int queueSize) {
        urls = new CircularFifoQueue<>(queueSize);
    }

    @Override
    public boolean beforeDownload(NekoCatRequest request) {
        if (request.isForceDownload()) {
            return true;
        }
        if (urls.contains(request.getUrl())) {
            return false;
        }
        urls.add(request.getUrl());
        return true;
    }
}
