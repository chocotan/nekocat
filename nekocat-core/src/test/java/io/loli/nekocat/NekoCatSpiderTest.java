package io.loli.nekocat;

import io.loli.nekocat.app.NekoCatTestApplication;
import io.loli.nekocat.downloader.NekoCatDownloader;
import io.loli.nekocat.downloader.NekoCatOkhttpDownloader;
import io.loli.nekocat.interceptor.NekoCatInterceptor;
import io.loli.nekocat.pipline.NekoCatPipline;
import io.loli.nekocat.request.NekoCatRequest;
import io.loli.nekocat.response.NekoCatResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@SpringBootTest(classes = NekoCatTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
public class NekoCatSpiderTest {

    @LocalServerPort
    int port;

    @Test
    public void testSpiderDefaultValueBuild() {
        NekoCatSpider spider = NekoCatSpider.builder()
                .build();
        Assert.assertNotNull(spider.getDownloader());
    }


    @Test
    public void testPipline() throws Exception {
        NekoCatPipline mock = Mockito.mock(NekoCatPipline.class);
        NekoCatSpider spider = NekoCatSpider.builder()
                .startUrl("http://localhost:" + port)
                .url(NekoCatProperties.builder()
                        .regex("http://localhost:" + port)
                        .name("index")
                        .pipline(resp -> {
                            resp.getContext().next(resp.asDocument().select("a").attr("href"));
                            return null;
                        })
                        .build())
                .url(NekoCatProperties.builder()
                        .regex("http://localhost:" + port + "/get")
                        .name("get")
                        .pipline(mock)
                        .build())
                .build();
        spider.start();
        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
        }
        Mockito.verify(mock, Mockito.times(1)).apply(Mockito.any());
    }


    @Test
    public void testAutoStop() throws Exception {
        NekoCatPipline mock = Mockito.mock(NekoCatPipline.class);
        Mockito.doReturn("").when(mock).apply(Mockito.any());
        NekoCatSpider spider = NekoCatSpider.builder()
                .startUrl("http://localhost:" + port)
                .url(NekoCatProperties.builder()
                        .regex("http://localhost:" + port)
                        .name("index")
                        .pipline(mock)
                        .build())
                .stopAfterNoRequestEmmitMillis(1000)
                .build();
        spider.start();
        try {
            Thread.sleep(3500L);
        } catch (InterruptedException e) {
        }

        Assert.assertTrue(spider.getStop().get());
    }


    @Test
    public void testManualStop() throws Exception {
        NekoCatPipline mock = Mockito.mock(NekoCatPipline.class);
        Mockito.doReturn("").when(mock).apply(Mockito.any());
        NekoCatSpider spider = NekoCatSpider.builder()
                .startUrl("http://localhost:" + port)
                .url(NekoCatProperties.builder()
                        .regex("http://localhost:" + port)
                        .name("index")
                        .pipline(mock)
                        .build())
                .build();
        spider.start();
        Thread.sleep(2000L);
        spider.stop();
        Assert.assertTrue(spider.getStop().get());
    }


    @Test
    public void testManualStopAndStart() throws Exception {
        NekoCatPipline mock = Mockito.mock(NekoCatPipline.class);
        Mockito.doReturn("").when(mock).apply(Mockito.any());
        NekoCatSpider spider = NekoCatSpider.builder()
                .startUrl("http://localhost:" + port)
                .url(NekoCatProperties.builder()
                        .regex("http://localhost:" + port)
                        .name("index")
                        .pipline(mock)
                        .build())
                .build();
        spider.start();
        Thread.sleep(2000L);
        spider.stop();
        Thread.sleep(2000L);
        spider.start();
        Thread.sleep(2000L);
        Assert.assertFalse(spider.getStop().get());
        Mockito.verify(mock, Mockito.times(2)).apply(Mockito.any());
    }


    @Test
    public void testDownloadError() throws Exception {
        NekoCatPipline mock = Mockito.mock(NekoCatPipline.class);
        Mockito.doReturn("").when(mock).apply(Mockito.any());
        NekoCatDownloader downloader = Mockito.mock(NekoCatDownloader.class);
        Mockito.doThrow(new RuntimeException("error")).when(downloader).apply(Mockito.any());
        NekoCatSpider spider = NekoCatSpider.builder()
                .startUrl("http://localhost:" + port)
                .url(NekoCatProperties.builder()
                        .regex("http://localhost:" + port)
                        .name("index")
                        .pipline(mock)
                        .build())
                .downloader(downloader)
                .build();
        spider.start();
        Thread.sleep(2000L);
        ArgumentCaptor<NekoCatResponse> argument = ArgumentCaptor.forClass(NekoCatResponse.class);
        Mockito.verify(mock).apply(argument.capture());
        Assert.assertFalse(argument.getValue().isSuccess());
    }


    @Test
    public void testPiplineError() throws Exception {
        NekoCatPipline mock = Mockito.mock(NekoCatPipline.class);
        Mockito.doThrow(new RuntimeException("error")).when(mock).apply(Mockito.any());
        NekoCatDownloader downloader = Mockito.mock(NekoCatDownloader.class);
        Mockito.doReturn(new NekoCatResponse()).when(downloader).apply(Mockito.any());
        NekoCatInterceptor interceptor = Mockito.mock(NekoCatInterceptor.class);
        NekoCatSpider spider = NekoCatSpider.builder()
                .startUrl("http://localhost:" + port)
                .url(NekoCatProperties.builder()
                        .regex("http://localhost:" + port)
                        .name("index")
                        .pipline(mock)
                        .build())
                .downloader(downloader)
                .interceptor(interceptor)
                .build();
        spider.start();
        Thread.sleep(2000L);

        Mockito.verify(interceptor, Mockito.times(1)).errorPipline(Mockito.any(), Mockito.any());

    }


    @Test
    public void testInterceptor() throws Exception {
        NekoCatPipline mock = Mockito.mock(NekoCatPipline.class);
        Mockito.doReturn("").when(mock).apply(Mockito.any());
        NekoCatDownloader downloader = Mockito.mock(NekoCatDownloader.class);
        Mockito.doReturn(new NekoCatResponse()).when(downloader).apply(Mockito.any());
        NekoCatInterceptor interceptor = Mockito.mock(NekoCatInterceptor.class);
        NekoCatSpider spider = NekoCatSpider.builder()
                .startUrl("http://localhost:" + port)
                .url(NekoCatProperties.builder()
                        .regex("http://localhost:" + port)
                        .name("index")
                        .pipline(mock)
                        .interceptor(interceptor)
                        .build())
                .downloader(downloader)
                .interceptor(interceptor)
                .build();
        spider.start();
        Thread.sleep(2000L);

        spider.stop();

        Mockito.verify(interceptor, Mockito.times(2)).beforePipline(Mockito.any());
        Mockito.verify(interceptor, Mockito.times(2)).beforeDownload(Mockito.any());
        Mockito.verify(interceptor, Mockito.times(2)).afterDownload(Mockito.any());
        Mockito.verify(interceptor, Mockito.times(2)).afterPipline(Mockito.any());
        Mockito.verify(interceptor, Mockito.times(1)).beforeStart(Mockito.any());
        Mockito.verify(interceptor, Mockito.times(1)).beforeStop(Mockito.any());
    }
}
