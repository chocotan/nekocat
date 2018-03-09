package io.loli.nekocat;

import io.loli.nekocat.request.NekoCatRequest;
import io.loli.nekocat.response.NekoCatResponse;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.UnicastSubject;
import org.junit.Assert;
import org.junit.Test;

public class NekoCatContextTest {
    @Test
    public void testContextCheckFixUrl() {
        UnicastSubject subject = UnicastSubject.create();
        NekoCatContext context = new NekoCatContext(subject);
        context.setRequest(new NekoCatRequest("https://www.baidu.com"));
        Assert.assertEquals("https://www.baidu.com/path", context.next("path").getRequest().getUrl());
        Assert.assertEquals("http://www.baidu.com/path", context.next("http://www.baidu.com/path").getRequest().getUrl());
        Assert.assertEquals("https://www.baidu.com/path", context.next("//www.baidu.com/path").getRequest().getUrl());
        context.setRequest(new NekoCatRequest("https://www.baidu.com/path"));
        Assert.assertEquals("https://www.baidu.com/path", context.next("/path").getRequest().getUrl());
    }


    @Test
    public void testClearRef(){
        UnicastSubject subject = UnicastSubject.create();
        NekoCatContext context = new NekoCatContext(subject);
        context.setRequest(new NekoCatRequest("https://www.baidu.com"));
        context.setResponse(new NekoCatResponse());
        context.clearRef();

        Assert.assertNull(context.getResponse());
        Assert.assertNull(context.getRequest());
    }
}
