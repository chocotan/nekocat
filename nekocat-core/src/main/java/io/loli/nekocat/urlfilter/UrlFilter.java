package io.loli.nekocat.urlfilter;

import io.loli.nekocat.request.NekoCatRequest;
import io.reactivex.functions.Predicate;


public interface UrlFilter extends Predicate<NekoCatRequest> {
}
