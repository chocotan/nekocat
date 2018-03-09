package io.loli.nekocat.downloader;

import io.loli.nekocat.request.NekoCatRequest;
import io.loli.nekocat.response.NekoCatResponse;
import io.reactivex.functions.Function;


/**
 * the url downloader interface
 * <p>
 * the default downloader uses okhttp
 *
 * @see NekoCatOkhttpDownloader
 */
public interface NekoCatDownloader extends Function<NekoCatRequest, NekoCatResponse> {

}
