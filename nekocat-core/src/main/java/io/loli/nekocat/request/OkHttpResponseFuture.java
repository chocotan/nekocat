package io.loli.nekocat.request;

import io.loli.nekocat.response.NekoCatResponse;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class OkHttpResponseFuture implements Callback {
    public final CompletableFuture<NekoCatResponse> future = new CompletableFuture<>();
    private final NekoCatRequest request;

    public OkHttpResponseFuture(NekoCatRequest request) {
        this.request = request;
    }

    @Override
    public void onFailure(Call call, IOException e) {
        future.completeExceptionally(e);
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        NekoCatResponse nekoCatResponse = null;
        try {
            nekoCatResponse = new NekoCatResponse(response.body().bytes());
            nekoCatResponse.setContext(request.getContext());
        } catch (Exception e) {
            nekoCatResponse = new NekoCatResponse();
            nekoCatResponse.setThrowable(e);
            nekoCatResponse.setContext(request.getContext());
        } finally {
            future.complete(nekoCatResponse);

        }
    }
}