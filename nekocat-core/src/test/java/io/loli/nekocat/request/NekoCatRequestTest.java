package io.loli.nekocat.request;

import io.loli.nekocat.NekoCatContext;
import org.junit.Assert;
import org.junit.Test;

public class NekoCatRequestTest {

    @Test
    public void testSetContext() {
        NekoCatRequest request = new NekoCatRequest();
        NekoCatContext context = new NekoCatContext(null);
        request.setContext(context);
        Assert.assertEquals(request, context.getRequest());
    }
}
