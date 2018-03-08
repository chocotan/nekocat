package io.loli.nekocat.request;

import io.loli.nekocat.NekoCatContext;
import io.loli.nekocat.NekoCatProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

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


    public NekoCatRequest(String url) {
        this(url, "GET", new HashMap<>(), "");
    }

    public NekoCatRequest(String url, String method, Map<String, String> additionalHeaders, String requsetBody) {
        this.url = url;
        this.method = method;
        this.additionalHeaders = additionalHeaders;
        this.requestBody = requsetBody;
    }


    public void setContext(NekoCatContext context) {
        if (context != null) {
            this.context = context;
            context.setRequest(this);
        }
    }
}
