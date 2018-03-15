package io.loli.nekocat.downloader;

import io.loli.nekocat.exception.DownloadException;
import io.loli.nekocat.request.NekoCatRequest;
import io.loli.nekocat.response.NekoCatResponse;
import okhttp3.*;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.concurrent.TimeUnit;

/**
 * the okhttp downloader implementation
 */
public class NekoCatOkhttpNotSaveCookieDownloader implements NekoCatDownloader {




    public NekoCatOkhttpNotSaveCookieDownloader() {
        // set default connection timeout
        client = new OkHttpClient().newBuilder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build();
    }

    private OkHttpClient client;

    @Override
    public NekoCatResponse apply(NekoCatRequest request) {
        Headers headers = Headers.of(request.getAdditionalHeaders());
        Response execute = null;
        try {
            Request.Builder builder = new Request.Builder();
            if ("GET".equalsIgnoreCase(request.getMethod())) {
                builder = builder.get();
            } else {
                String contentType = headers.get("content-type");
                if (contentType == null) {
                    contentType = "application/x-www-form-urlencoded";
                }
                builder = builder.post(RequestBody.create(MediaType.parse(contentType), request.getRequestBody()));
            }
            builder = builder.url(request.getUrl());
            builder = builder.headers(headers);
            execute = client.newCall(builder
                    .build()
            ).execute();
            NekoCatResponse nekoCatResponse = new NekoCatResponse(execute.body().bytes());
            return nekoCatResponse;
        } catch (Exception e) {
            throw new DownloadException(e);
        } finally {
            if (execute != null) {
                if (execute.body() != null) {
                    execute.body().close();
                }
            }
        }
    }


}
