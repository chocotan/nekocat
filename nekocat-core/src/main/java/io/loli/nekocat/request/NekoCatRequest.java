package io.loli.nekocat.request;

import io.loli.nekocat.NekoCatContext;
import io.loli.nekocat.NekoCatProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jsoup.helper.HttpConnection;

import java.net.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.loli.nekocat.NekoCatConstants.UA_DEFAULT;

/**
 * Class that stores url, method, request body  for each http request
 */
@Data
@NoArgsConstructor
public class NekoCatRequest {
    private String url;
    private String method;
    private Map<String, String> additionalHeaders;
    private String requestBody;
    private NekoCatContext context;
    private NekoCatProperties properties;
    private boolean forceDownload;
    private Proxy proxy;


    public NekoCatRequest(String url) {
        this(url, "GET", null, "");
    }

    public NekoCatRequest(String url, String method, Map<String, String> additionalHeaders, String requsetBody) {
        this.url = url;
        this.method = method;
        if (additionalHeaders == null) {
            additionalHeaders = new HashMap<>();
            additionalHeaders.put("User-Agent", UA_DEFAULT);
        }
        this.additionalHeaders = additionalHeaders;
        this.requestBody = requsetBody;
    }


    public void setContext(NekoCatContext context) {
        if (context != null) {
            this.context = context;
            context.setRequest(this);
        }
    }

    public void addHeader(String k, String v) {
        if (additionalHeaders == null) {
            additionalHeaders = new HashMap<>();
        }
        additionalHeaders.put(k, v);
    }
}
