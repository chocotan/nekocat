package io.loli.nekocat.interceptor;

import io.loli.nekocat.request.NekoCatRequest;

public abstract class AdditionalHeaderInterceptor implements NekoCatInterceptor {
    @Override
    public abstract boolean beforeDownload(NekoCatRequest request);
}
