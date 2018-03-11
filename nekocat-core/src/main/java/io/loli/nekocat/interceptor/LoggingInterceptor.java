package io.loli.nekocat.interceptor;

import io.loli.nekocat.NekoCatContext;
import io.loli.nekocat.request.NekoCatRequest;
import io.loli.nekocat.response.NekoCatResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

@Slf4j
public class LoggingInterceptor implements NekoCatInterceptor {


    public boolean beforeDownload(NekoCatRequest request) {
        request.getContext().addAttribute("downloadStart", System.currentTimeMillis());
        log.info("[{}] Start: download {}",request.getContext().getId(), request.getUrl());
        return true;
    }

    public void afterDownload(NekoCatResponse response) {
        log.info("[{}] Finished: download {}, success={}, cost={}", response.getContext().getId(), response.getContext().getRequest().getUrl(), response.isSuccess(),
                (System.currentTimeMillis() - (Long) response.getContext().fetchAttribute("downloadStart")), response.getThrowable() != null ? ExceptionUtils.getStackTrace(response.getThrowable()) : "");
    }


    @Override
    public void errorDownload(NekoCatResponse response) {
        log.info("[{}] Error: download {}, cost={}, error={}", response.getContext().getId(), response.getContext().getRequest().getUrl(),
                (System.currentTimeMillis() - (Long) response.getContext().fetchAttribute("downloadStart")), response.getThrowable() != null ? ExceptionUtils.getStackTrace(response.getThrowable()) : "");
    }

    public boolean beforePipline(NekoCatResponse response) {
        response.getContext().addAttribute("consumeStart", System.currentTimeMillis());
        log.info("[{}] Start: consume url {}",
                response.getContext().getId(),
                response.getContext().getRequest().getUrl());
        return true;
    }

    public void afterPipline(NekoCatContext context) {
        log.info("[{}] Finished: consume url {}, cost={}",
                context.getId(),
                context.getRequest().getUrl(),
                (System.currentTimeMillis() - (Long) context.fetchAttribute("downloadStart")));
    }

    @Override
    public void errorPipline(NekoCatContext context, Throwable exception) {
        log.info("[{}] Finished: consume url {}, cost={}, exception={}",
                context.getId(),
                context.getRequest().getUrl(),
                (System.currentTimeMillis() - (Long) context.fetchAttribute("downloadStart")), ExceptionUtils.getStackTrace(exception));
    }

}
