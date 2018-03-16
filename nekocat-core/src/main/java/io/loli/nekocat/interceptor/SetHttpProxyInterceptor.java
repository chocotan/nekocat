package io.loli.nekocat.interceptor;

import io.loli.nekocat.downloader.ProxySelector;
import io.loli.nekocat.request.NekoCatRequest;

public class SetHttpProxyInterceptor implements NekoCatInterceptor {

    private final ProxySelector proxySelector;

    public SetHttpProxyInterceptor(ProxySelector proxySelector) {
        this.proxySelector = proxySelector;
    }

    public boolean beforeDownload(NekoCatRequest request) {
        request.setProxy(proxySelector.select(request));
        return true;
    }
}
