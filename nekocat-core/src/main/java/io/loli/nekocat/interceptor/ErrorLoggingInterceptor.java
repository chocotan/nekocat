package io.loli.nekocat.interceptor;

import io.loli.nekocat.NekoCatContext;
import io.loli.nekocat.request.NekoCatRequest;
import io.loli.nekocat.response.NekoCatResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

@Slf4j
public class ErrorLoggingInterceptor implements NekoCatInterceptor {


    public boolean beforeDownload(NekoCatRequest request) {
        request.getContext().addAttribute("downloadStart", System.currentTimeMillis());
        return true;
    }

    public void afterDownload(NekoCatResponse response) {
    }


    @Override
    public void errorDownload(NekoCatResponse response) {
        log.info("[{}] Error: download {}, cost={}, error={}", response.getContext().getId(), response.getContext().getRequest().getUrl(),
                (System.currentTimeMillis() - (Long) response.getContext().fetchAttribute("downloadStart")), response.getThrowable() != null ? ExceptionUtils.getStackTrace(response.getThrowable()) : "");
    }

    public boolean beforePipline(NekoCatResponse response) {
        response.getContext().addAttribute("consumeStart", System.currentTimeMillis());
       return true;
    }

    public void afterPipline(NekoCatContext context) {
   }

    @Override
    public void errorPipline(NekoCatContext context, Throwable exception) {
        log.info("[{}] Finished: consume url {}, cost={}, exception={}",
                context.getId(),
                context.getRequest().getUrl(),
                (System.currentTimeMillis() - (Long) context.fetchAttribute("downloadStart")), ExceptionUtils.getStackTrace(exception));
    }

}
