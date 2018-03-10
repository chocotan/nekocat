package io.loli.nekocat.interceptor;

import io.loli.nekocat.NekoCatContext;
import io.loli.nekocat.request.NekoCatRequest;
import io.loli.nekocat.response.NekoCatResponse;

public interface NekoCatInterceptor {
    public default void beforeStart(String startUrl) {
    }


    public default void beforeStop(String startUrl) {
    }


    public default boolean beforeDownload(NekoCatRequest request) {
        return true;
    }

    public default void afterDownload(NekoCatResponse response) {
    }


    public default void errorDownload(NekoCatResponse response) {
    }



    public default boolean beforePipline(NekoCatResponse response) {
        return true;
    }

    public default void afterPipline(NekoCatContext context) {

    }

    public default void errorPipline(NekoCatContext context, Throwable throwable) {
    }

}
