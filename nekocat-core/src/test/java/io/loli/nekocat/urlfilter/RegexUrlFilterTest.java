package io.loli.nekocat.urlfilter;

import io.loli.nekocat.request.NekoCatRequest;
import org.junit.Assert;
import org.junit.Test;

public class RegexUrlFilterTest {
    @Test
    public void testRegexMatch() throws Exception {
        Assert.assertTrue(new RegexUrlFilter("http://www\\.baidu\\.com/.*").test(new NekoCatRequest("http://www.baidu.com/")));
        Assert.assertFalse(new RegexUrlFilter("http://www\\.baidu\\.com/.*").test( new NekoCatRequest("http://www.google.com/")));
    }
}
