package io.loli.nekocat.pipline;

import io.loli.nekocat.response.NekoCatResponse;
import io.reactivex.functions.Function;


/**
 * The response consumer
 * <p>
 * You can store the response to local filesystem and other actions
 */
public interface NekoCatPipline extends Function<NekoCatResponse, Object> {

}
