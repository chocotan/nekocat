package io.loli.nekocat.thread;

import io.loli.nekocat.NekoCatProperties;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPoolsTest {
    @Test
    public void testGetThreadPoolTheSame() {
        NekoCatProperties properties = NekoCatProperties.builder()
                .name("testurl")
                .build();
        ThreadPoolExecutor test1 = NekoCatGlobalThreadPools.getConsumeExecutor(properties, "testspider");
        ThreadPoolExecutor test2 = NekoCatGlobalThreadPools.getConsumeExecutor(properties, "testspider");
        Assert.assertEquals(test1, test2);
        ThreadPoolExecutor test3 = NekoCatGlobalThreadPools.getDownloadExecutor(properties, "testspider");
        ThreadPoolExecutor test4 = NekoCatGlobalThreadPools.getDownloadExecutor(properties, "testspider");
        Assert.assertEquals(test3, test4);
        Assert.assertNotEquals(test2, test4);
    }

    @Test
    public void testShutdown() {
        NekoCatProperties properties = NekoCatProperties.builder()
                .name("testurl")
                .build();
        ThreadPoolExecutor test1 = NekoCatGlobalThreadPools.getConsumeExecutor(properties, "testspider");
        ThreadPoolExecutor test2 = NekoCatGlobalThreadPools.getConsumeExecutor(properties, "testspider");
        NekoCatGlobalThreadPools.shutdown(properties, "testspider");
        Assert.assertTrue(test1.isShutdown());
        Assert.assertTrue(test2.isShutdown());
        NekoCatGlobalThreadPools.shutdown(properties, "testspider");
    }

}
