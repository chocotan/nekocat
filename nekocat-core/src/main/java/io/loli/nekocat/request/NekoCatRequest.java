package io.loli.nekocat.request;

import io.loli.nekocat.NekoCatContext;
import io.loli.nekocat.NekoCatProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class that stores url, method, request body  for each http request
 */
@Data
@NoArgsConstructor
public class NekoCatRequest {
    private String url;
    private NekoCatContext context;
    private NekoCatProperties properties;

    public NekoCatRequest(NekoCatContext context) {
        this.context = context;
        context.setRequest(this);
    }
}
