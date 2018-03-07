package io.loli.nekocat;

import io.loli.nekocat.request.NekoCatRequest;
import io.loli.nekocat.response.NekoCatResponse;
import io.reactivex.subjects.PublishSubject;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
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
    private NekoCatContext lastContext;


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
    public void next(String url) {
        NekoCatContext context = new NekoCatContext(source);
        NekoCatRequest request = new NekoCatRequest(context);
        request.setUrl(url);
        context.setLastContext(this);
        source.onNext(request);
    }

    private void setLastContext(NekoCatContext lastContext) {
        this.lastContext = lastContext;
        // clear reference for gc
        lastContext.lastContext = null;
    }


    public void addAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public <T> T fetchAttribute(String key) {
        return (T) attributes.get(key);
    }

}
