package io.loli.nekocat.urlfilter;

import io.loli.nekocat.request.NekoCatRequest;

public class RegexUrlFilter implements UrlFilter {
    private String regex;

    public RegexUrlFilter(String regex) {
        this.regex = regex;
    }


    @Override
    public boolean test(NekoCatRequest request) throws Exception {
        return request.getUrl().matches(regex);
    }
}
