package io.loli.nekocat.downloader;

import io.loli.nekocat.request.NekoCatRequest;

import java.net.Proxy;

public interface ProxySelector {
    public Proxy select(NekoCatRequest request);
}
