package io.loli.nekocat;

import io.loli.nekocat.request.NekoCatRequest;
import io.loli.nekocat.response.NekoCatResponse;
import io.reactivex.subjects.PublishSubject;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Stores info of a request
 */
@Data
public class NekoCatContext {
    private final static AtomicLong idx = new AtomicLong(0);

    private Long id = idx.incrementAndGet();

    /**
     * properties for url of this request
     */
    private NekoCatProperties properties;

    /**
     * processing subject
     */
    private PublishSubject<NekoCatRequest> source;
    private NekoCatRequest request;
    private NekoCatResponse response;
    private CompletableFuture<Object> piplineResult = new CompletableFuture<>();

    private Map<String, Object> nextAttributes = new HashMap<>();
    private Map<String, Object> attributes = new HashMap<>();


    /**
     * @param source the subject for add urls what will be downloaded
     */
    public NekoCatContext(PublishSubject<NekoCatRequest> source) {
        this.source = source;
    }


    /**
     * Add url that will be downloaded
     *
     * @param url the url that will be downloaded
     */
    public NekoCatContext next(String url) {
        NekoCatContext context = new NekoCatContext(source);
        context.setAttributes(this.getNextAttributes());
        NekoCatRequest request = new NekoCatRequest(url);
        request.setContext(context);
        source.onNext(request);
        return context;
    }


    public void addAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public void addNextAttribute(String key, Object value) {
        nextAttributes.put(key, value);
    }

    public <T> T fetchAttribute(String key) {
        return (T) attributes.get(key);
    }

    public void setResponse(NekoCatResponse response) {
        this.response = response;
    }

    public <T> void setPiplineResult(T object) {
        piplineResult.complete(object);
    }

    public void setPiplineException(Throwable exception) {
        piplineResult.completeExceptionally(exception);
    }


    public synchronized void clearRef() {
        if (this.response != null) {
            this.response.setContext(null);
            this.response = null;
        }
        if (this.request != null) {
            this.request.setContext(null);
            this.request = null;
        }
    }
}
