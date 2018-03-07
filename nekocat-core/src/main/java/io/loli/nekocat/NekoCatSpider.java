package io.loli.nekocat;

import io.loli.nekocat.downloader.NekoCatDownloader;
import io.loli.nekocat.downloader.NekoCatOkhttpDownloader;
import io.loli.nekocat.interceptor.NekoCatInterceptor;
import io.loli.nekocat.request.NekoCatRequest;
import io.loli.nekocat.response.NekoCatResponse;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import javaslang.control.Try;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static io.loli.nekocat.thread.NekoCatGlobalThreadPools.getConsumeExecutor;
import static io.loli.nekocat.thread.NekoCatGlobalThreadPools.getDownloadExecutor;

@Slf4j
public class NekoCatSpider {
    private String startUrl;
    private String name;
    private PublishSubject<NekoCatRequest> source;

    private List<NekoCatProperties> consumers;
    private NekoCatDownloader downloader;
    private List<NekoCatInterceptor> interceptors;
    private Disposable subscribeResult;
    private long stopAfterEmmitMillis;
    private ScheduledExecutorService stopSchedule;
    private AtomicLong lastRequestEmmitTime;


    private volatile boolean stop;


    private NekoCatSpider(String startUrl, String name, PublishSubject<NekoCatRequest> source, List<NekoCatProperties> consumers, NekoCatDownloader downloader, List<NekoCatInterceptor> interceptors) {
        this.startUrl = startUrl;
        this.name = name;
        this.source = source;
        this.consumers = consumers;
        this.downloader = downloader;
        this.interceptors = interceptors;
    }

    private NekoCatSpider() {
    }

    public void start() {
        // merge interceptors
        consumers.forEach(
                c -> {
                    List<NekoCatInterceptor> mergedInterceptors = new ArrayList<>();
                    mergedInterceptors.addAll(interceptors);
                    mergedInterceptors.addAll(c.getInterceptorList());
                    c.setInterceptorList(mergedInterceptors);
                }
        );

        Observable<NekoCatRequest> observable = source;
        // stop schedule
        if (stopAfterEmmitMillis > 0) {
            stopSchedule = Executors.newScheduledThreadPool(1);
            stopSchedule.scheduleAtFixedRate(() -> {
                if (lastRequestEmmitTime.get() != 0) {
                    if (System.currentTimeMillis() - lastRequestEmmitTime.get() > stopAfterEmmitMillis) {
                        log.info("No request emitted from source for {} ms, spider will stop" + stopAfterEmmitMillis);
                        stop();
                    }
                }
            }, 1000, 1000, TimeUnit.MILLISECONDS);
            observable = source
                    .doOnNext(req -> lastRequestEmmitTime.set(System.currentTimeMillis()));
        }
        subscribeResult = observable
                .subscribe(request -> {
                    if (stop) {
                        log.info("Stopping spider [{}]", name);
                        return;
                    }
                    consumers.stream()
                            .filter(c -> {
                                try {
                                    return request.getUrl().matches(c.getRegex());
                                } catch (Exception e) {
                                    log.warn("[{}] Regex error while filtering url {}, regex={}, download function will be skipped  {}" + System.lineSeparator()
                                            , request.getContext().getId()
                                            , request.getUrl(), c.getRegex(),
                                            ExceptionUtils.getStackTrace(e));
                                    return false;
                                }
                            })
                            .peek(properties -> {
                                request.getContext().setProperties(properties);
                            })
                            .forEach(c -> {
                                try {
                                    CompletableFuture
                                            .completedFuture(request)
                                            .thenApplyAsync(req -> {
                                                c.getInterceptorList().forEach(i -> Try.run(() -> i.beforeDownload(req))
                                                        .onFailure(exception -> log.warn("[{}] Error occurred while execute interceptor before download, {}", request.getContext().getId(), ExceptionUtils.getStackTrace(exception))));
                                                return req;
                                            }, getDownloadExecutor(c, name))
                                            .thenApply(downloader)
                                            .exceptionally(excep -> {
                                                NekoCatResponse response = new NekoCatResponse();
                                                response.setThrowable(excep);
                                                response.setContext(request.getContext());
                                                c.getInterceptorList().forEach(i -> Try.run(() -> i.errorDownload(response, excep))
                                                        .onFailure(exception -> log.warn("[{}] Error occurred while execute interceptor error download, {}", request.getContext().getId(), ExceptionUtils.getStackTrace(exception))));
                                                return response;
                                            })
                                            .thenApply(resp -> {
                                                c.getInterceptorList().forEach(i -> Try.run(() -> i.afterDownload(resp))
                                                        .onFailure(exception -> log.info("[{}] Error occurred while execute interceptor after download, {}", request.getContext().getId(), ExceptionUtils.getStackTrace(exception))));
                                                return resp;
                                            })
                                            .thenApplyAsync(response -> {
                                                c.getInterceptorList().forEach(i -> Try.run(() -> i.beforePipline(response))
                                                        .onFailure(exception -> log.info("[{}] Error occurred while execute interceptor before pipline, {}", request.getContext().getId(), ExceptionUtils.getStackTrace(exception))));
                                                return response;
                                            }, getConsumeExecutor(c, name))
                                            .thenAccept(c.getPipline())
                                            .exceptionally(excep -> {
                                                c.getInterceptorList().forEach(i -> Try.run(() -> i.errorPipline(request.getContext(), excep))
                                                        .onFailure(exception -> log.warn("[{}] Error occurred while execute interceptor error pipline, {}", request.getContext().getId(), ExceptionUtils.getStackTrace(exception))));
                                                return null;
                                            })
                                            .thenRun(() ->
                                                    c.getInterceptorList().forEach(i -> Try.run(() -> i.afterPipline(request.getContext()))
                                                            .onFailure(exception -> log.info("[{}] Error occurred while execute interceptor after pipline, {}", request.getContext().getId(), ExceptionUtils.getStackTrace(exception)))));

                                } catch (Exception e) {
                                    log.warn("[{}] Error occurred while downloading or consuming response, {}", request.getContext().getId(), ExceptionUtils.getStackTrace(e));
                                }
                            });
                });
        NekoCatContext context = new NekoCatContext(source);
        NekoCatRequest request = new NekoCatRequest(context);
        request.setUrl(startUrl);
        interceptors.forEach(i -> i.beforeStart(request));
        source.onNext(request);
    }


    public void stop() {
        stop = true;
        if (!subscribeResult.isDisposed()) {
            subscribeResult.dispose();
        }
    }

    public static NekoCatSpiderBuilder builder() {
        return new NekoCatSpiderBuilder();
    }


    public static class NekoCatSpiderBuilder {
        private String startUrl;
        private String name;
        private PublishSubject<NekoCatRequest> source;
        private List<NekoCatProperties> consumers = new ArrayList<>();
        private NekoCatDownloader downloader;
        private List<NekoCatInterceptor> interceptors = new ArrayList<>();

        public NekoCatSpiderBuilder() {
        }

        public NekoCatSpider.NekoCatSpiderBuilder startUrl(String startUrl) {
            this.startUrl = startUrl;
            return this;
        }

        public NekoCatSpider.NekoCatSpiderBuilder name(String name) {
            this.name = name;
            return this;
        }

        public NekoCatSpider.NekoCatSpiderBuilder source(PublishSubject<NekoCatRequest> source) {
            this.source = source;
            return this;
        }

        public NekoCatSpiderBuilder url(NekoCatProperties properties) {
            consumers.add(properties);
            return this;
        }

        public NekoCatSpiderBuilder downloader(NekoCatDownloader downloader) {
            this.downloader = downloader;
            return this;
        }

        public NekoCatSpiderBuilder interceptor(NekoCatInterceptor interceptor) {
            this.interceptors.add(interceptor);
            return this;
        }

        public NekoCatSpider build() {
            if (this.source == null) {
                this.source = PublishSubject.create();
            }
            if (this.downloader == null) {
                this.downloader = new NekoCatOkhttpDownloader();
            }
            return new NekoCatSpider(this.startUrl, this.name, this.source, this.consumers, this.downloader, this.interceptors);
        }

    }
}
