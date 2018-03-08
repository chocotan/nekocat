package io.loli.nekocat.pipline;

import io.loli.nekocat.NekoCatContext;
import io.loli.nekocat.response.NekoCatResponse;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The response consumer
 * <p>
 * You can store the response to local filesystem and other actions
 */
public interface NekoCatPipline extends Function<NekoCatResponse, Object> {

}
