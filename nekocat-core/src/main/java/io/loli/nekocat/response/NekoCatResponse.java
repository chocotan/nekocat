package io.loli.nekocat.response;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.loli.nekocat.NekoCatContext;
import io.loli.nekocat.exception.DownloadException;
import lombok.NoArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * The response class
 */
@NoArgsConstructor
public class NekoCatResponse {
    private NekoCatContext context;
    private byte[] bytes;
    private String dataStr;
    private Document document;

    private Throwable throwable;

    public NekoCatResponse(byte[] bytes) {
        this.bytes =  bytes;
    }

    public InputStream asStream() {
        return new ByteArrayInputStream(bytes);
    }

    public void setContext(NekoCatContext context) {
        if (context != null) {
            this.context = context;
            context.setResponse(this);
        }
    }

    public byte[] asBytes() {
        return bytes;

    }

    public String asString() {
        return asString("UTF-8");
    }

    public String asString(String charset) {
        if (dataStr != null) {
            return dataStr;
        }
        try {
            dataStr = IOUtils.toString(bytes, charset);
            return dataStr;
        } catch (IOException e) {
            throwable = e;
            throw new DownloadException(e);
        }
    }

    public Document asDocument(String charset) {
        if (this.document != null) {
            return this.document;
        }
        Document parse = Jsoup.parse(asString(charset));
        this.document = parse;
        return parse;
    }

    public Document asDocument() {
        return asDocument("UTF-8");
    }
    public JSONObject asJson() {
        return JSON.parseObject(asString("UTF-8"));
    }

    public NekoCatContext getContext() {
        return context;
    }

    public boolean isSuccess() {
        return throwable == null;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public Throwable getThrowable() {
        return throwable;
    }
}
