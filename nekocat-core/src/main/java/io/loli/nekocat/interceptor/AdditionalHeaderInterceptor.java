package io.loli.nekocat.interceptor;

import io.loli.nekocat.request.NekoCatRequest;

public abstract class AdditionalHeaderInterceptor implements NekoCatInterceptor {
    @Override
    public abstract void beforeDownload(NekoCatRequest request);
}
