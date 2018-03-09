package io.loli.nekocat.downloader;

import io.loli.nekocat.app.NekoCatTestApplication;
import io.loli.nekocat.exception.DownloadException;
import io.loli.nekocat.request.NekoCatRequest;
import io.loli.nekocat.response.NekoCatResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.AssertFalse;
import java.util.HashMap;

@SpringBootTest(classes = NekoCatTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
public class NekoCatDownloaderTest {

    @LocalServerPort
    int port;

    @Test
    public void testOkHttpGet() throws Exception {
        NekoCatDownloader downloader = new NekoCatOkhttpDownloader();
        NekoCatRequest request = new NekoCatRequest("http://localhost:" + port);
        NekoCatResponse response = downloader.apply(request);
        Assert.assertNotNull(response);
        Assert.assertNull(response.getThrowable());
        Assert.assertNotNull(response.asString());
    }


    @Test(expected = DownloadException.class)
    public void testOkHttpGetNonePath() throws Exception {
        NekoCatDownloader downloader = new NekoCatOkhttpDownloader();
        NekoCatRequest request = new NekoCatRequest("http://localhost:54312");
        NekoCatResponse response = downloader.apply(request);
    }

    @Test
    public void testOkHttpPost() throws Exception {
        NekoCatDownloader downloader = new NekoCatOkhttpDownloader();
        HashMap<String, String> headers = new HashMap<>();
        headers.put("testheader", "test-header");
        NekoCatRequest request = new NekoCatRequest("http://localhost:" + port+"/post", "POST", headers, "name=testname");
        NekoCatResponse response = downloader.apply(request);
        Assert.assertNotNull(response);
        Assert.assertNull(response.getThrowable());
        Assert.assertEquals("test-headertestname", response.asString());
    }
}
