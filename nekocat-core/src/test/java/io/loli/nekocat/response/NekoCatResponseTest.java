package io.loli.nekocat.response;

import io.loli.nekocat.NekoCatContext;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.Charset;

public class NekoCatResponseTest {
    @Test
    public void testSetContext() {
        NekoCatResponse response = new NekoCatResponse();
        NekoCatContext context = new NekoCatContext(null);
        response.setContext(context);
        Assert.assertEquals(response, context.getResponse());
    }

    @Test
    public void testResponse() {

        NekoCatResponse response = new NekoCatResponse("data".getBytes(Charset.forName("UTF-8")));
        Assert.assertEquals(response.asString(), response.asString());
        Assert.assertEquals("data", new String(response.asBytes(), Charset.forName("UTF-8")));
        Assert.assertNotNull(response.asBytes());
        Assert.assertNotNull(response.asStream());
        Assert.assertNotNull(response.asDocument());
    }


}
