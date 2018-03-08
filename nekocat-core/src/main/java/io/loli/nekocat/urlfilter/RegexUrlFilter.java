package io.loli.nekocat.urlfilter;

public class RegexUrlFilter implements UrlFilter {
    private String regex;

    public RegexUrlFilter(String regex) {
        this.regex = regex;
    }

    @Override
    public boolean test(String url) {
        return url.matches(regex);
    }
}
