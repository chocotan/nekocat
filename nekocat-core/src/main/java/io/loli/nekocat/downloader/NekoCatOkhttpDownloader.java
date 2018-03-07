package io.loli.nekocat.downloader;

import io.loli.nekocat.exception.DownloadException;
import io.loli.nekocat.request.NekoCatRequest;
import io.loli.nekocat.request.OkHttpResponseFuture;
import io.loli.nekocat.response.NekoCatResponse;
import javaslang.collection.HashMap;
import javaslang.collection.Map;
import okhttp3.*;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.concurrent.TimeUnit;

/**
 * the okhttp downloader implementation
 */
public class NekoCatOkhttpDownloader implements NekoCatDownloader {

    private CookieManager cookieManager = new CookieManager();

    {
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
    }

    public CookieJar cookieJar = new JavaNetCookieJar(cookieManager);

    public NekoCatOkhttpDownloader() {
        // set default connection timeout
        client = new OkHttpClient().newBuilder().cookieJar(cookieJar)
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build();
    }

    private OkHttpClient client;

    private Map<String, String> header = HashMap.of("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.81 Safari/537.36");

    @Override
    public NekoCatResponse apply(NekoCatRequest request) {
        Headers headers = Headers.of(header.toJavaMap());
        OkHttpResponseFuture result = new OkHttpResponseFuture(request);

        Response execute = null;
        try {
            execute = client.newCall(new Request.Builder()
                    .get()
                    .url(request.getUrl()).headers(headers)
                    .build()
            ).execute();
            NekoCatResponse nekoCatResponse = new NekoCatResponse(execute.body().bytes());
            nekoCatResponse.setContext(request.getContext());
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
