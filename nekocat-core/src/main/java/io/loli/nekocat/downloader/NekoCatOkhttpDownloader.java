package io.loli.nekocat.downloader;

import io.loli.nekocat.exception.DownloadException;
import io.loli.nekocat.request.NekoCatRequest;
import io.loli.nekocat.response.NekoCatResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import okhttp3.*;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.ProxySelector;
import java.util.concurrent.TimeUnit;

/**
 * the okhttp downloader implementation
 */
@Builder
public class NekoCatOkhttpDownloader implements NekoCatDownloader {

    private boolean saveCookie = false;
    private ProxySelector proxySelector = null;
    private OkHttpClient client;

    public NekoCatOkhttpDownloader(boolean saveCookie, ProxySelector proxySelector, OkHttpClient okHttpClient) {
        this.saveCookie = saveCookie;
        this.proxySelector = proxySelector;
        this.client = okHttpClient;
        if (client == null) {
            OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
            if (saveCookie) {
                CookieManager cookieManager = new CookieManager();
                cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
                CookieJar cookieJar = new JavaNetCookieJar(cookieManager);
                builder = builder.cookieJar(cookieJar);
            }
            if (proxySelector != null) {
                builder.proxySelector(proxySelector);
            }
            // set default connection timeout
            client = builder
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(5, TimeUnit.SECONDS)
                    .writeTimeout(5, TimeUnit.SECONDS)
                    .build();
        }
    }

    public NekoCatOkhttpDownloader() {
        if (client == null) {
            OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
            if (saveCookie) {
                CookieManager cookieManager = new CookieManager();
                cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
                CookieJar cookieJar = new JavaNetCookieJar(cookieManager);
                builder = builder.cookieJar(cookieJar);
            }
            if (proxySelector != null) {
                builder.proxySelector(proxySelector);
            }
            // set default connection timeout
            client = builder
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .build();
        }
    }


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
