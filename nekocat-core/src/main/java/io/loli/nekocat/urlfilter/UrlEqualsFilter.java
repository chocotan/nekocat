package io.loli.nekocat.urlfilter;

import io.loli.nekocat.request.NekoCatRequest;

public class UrlEqualsFilter implements UrlFilter {
    private String url;

    public UrlEqualsFilter(String url) {
        this.url = url;
    }

    @Override
    public boolean test(NekoCatRequest nekoCatRequest) throws Exception {
        return nekoCatRequest.getUrl().equals(url);
    }
}
