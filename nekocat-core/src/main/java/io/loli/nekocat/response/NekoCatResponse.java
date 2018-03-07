package io.loli.nekocat.response;

import io.loli.nekocat.NekoCatContext;
import io.loli.nekocat.exception.DownloadException;
import lombok.NoArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * The response class
 */
@NoArgsConstructor
public class NekoCatResponse {
    private NekoCatContext context;
    private byte[] bytes;
    private Document document;

    private Throwable throwable;

    public NekoCatResponse(byte[] bytes) {
        this.bytes = bytes;
    }

    public InputStream asStream() {
        return new ByteArrayInputStream(bytes);
    }

    public void setContext(NekoCatContext context) {
        this.context = context;
        context.setResponse(this);
    }

    public byte[] asBytes() {
        return bytes;

    }

    public String asString() {
        try {
            return IOUtils.toString(bytes, "UTF-8");
        } catch (IOException e) {
            throwable = e;
            throw new DownloadException(e);
        }
    }

    public Document asDocument() {
        if (this.document != null) {
            return this.document;
        }
        Document parse = Jsoup.parse(asString());
        this.document = parse;
        return parse;
    }


    public NekoCatContext getContext() {
        return context;
    }

    public boolean isSuccess() {
        return throwable != null;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public Throwable getThrowable() {
        return throwable;
    }
}