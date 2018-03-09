package io.loli.nekocat.interceptor;

import io.loli.nekocat.NekoCatContext;
import io.loli.nekocat.request.NekoCatRequest;
import io.loli.nekocat.response.NekoCatResponse;

public interface NekoCatInterceptor {
    public default void beforeStart(NekoCatRequest startRequest) {
    }

    public default void beforeDownload(NekoCatRequest request) {
    }

    public default void afterDownload(NekoCatResponse response) {
    }


    void errorDownload(NekoCatResponse response, Throwable exception);

    public default void beforePipline(NekoCatResponse response) {

    }

    public default void afterPipline(NekoCatContext context){

    }

    void errorPipline(NekoCatContext context, Throwable exception);
}
