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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static io.loli.nekocat.thread.NekoCatGlobalThreadPools.getConsumeExecutor;
import static io.loli.nekocat.thread.NekoCatGlobalThreadPools.getDownloadExecutor;
import static io.loli.nekocat.thread.NekoCatGlobalThreadPools.shutdown;

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
    private AtomicLong lastRequestEmmitTime;
    private List<Future> futures = new ArrayList<>();


    private AtomicBoolean stop = new AtomicBoolean(false);


    private NekoCatSpider(String startUrl, String name, PublishSubject<NekoCatRequest> source, List<NekoCatProperties> consumers, NekoCatDownloader downloader, List<NekoCatInterceptor> interceptors, long stopAfterEmmitMillis) {
        this.startUrl = startUrl;
        this.name = name;
        this.source = source;
        this.consumers = consumers;
        this.downloader = downloader;
        this.interceptors = interceptors;
        this.stopAfterEmmitMillis = stopAfterEmmitMillis;
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
            lastRequestEmmitTime = new AtomicLong(0);
            ScheduledExecutorService stopSchedule = Executors.newScheduledThreadPool(1);
            stopSchedule.scheduleAtFixedRate(() -> {
                if (lastRequestEmmitTime.get() != 0) {
                    if (System.currentTimeMillis() - lastRequestEmmitTime.get() > stopAfterEmmitMillis) {
                        log.info("[{}] No request emitted from source for {} ms, spider will stop and all executors will be shutdown", name, stopAfterEmmitMillis);
                        stop();
                        stopSchedule.shutdown();
                    }
                }
            }, 1000, 1000, TimeUnit.MILLISECONDS);
            observable = source
                    .doOnNext(req -> lastRequestEmmitTime.set(System.currentTimeMillis()));
        }
        subscribeResult = observable
                .subscribe(request -> {
                    if (stop.get()) {
                        log.info("Spider [{}] is marked stopped", name);
                        return;
                    }
                    consumers.stream()
                            .filter(c -> c.getUrlFilter().test(request.getUrl()))
                            .peek(properties -> request.getContext().setProperties(properties))
                            .forEach(c -> {
                                try {
                                    CompletableFuture<Void> runFuture = CompletableFuture
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
                                            .thenApplyAsync(c.getPipline())
                                            .thenAccept(r -> request.getContext().setPiplineResult(r))
                                            .exceptionally(excep -> {
                                                c.getInterceptorList().forEach(i -> Try.run(() -> i.errorPipline(request.getContext(), excep))
                                                        .onFailure(exception -> log.warn("[{}] Error occurred while execute interceptor error pipline, {}", request.getContext().getId(), ExceptionUtils.getStackTrace(exception))));
                                                return null;
                                            })
                                            .thenRun(() -> {
                                                c.getInterceptorList().forEach(i -> Try.run(() -> i.afterPipline(request.getContext()))
                                                        .onFailure(exception -> log.info("[{}] Error occurred while execute interceptor after pipline, {}", request.getContext().getId(), ExceptionUtils.getStackTrace(exception))));
                                            });
                                    futures.add(runFuture);

                                } catch (Exception e) {
                                    log.warn("[{}] Error occurred while downloading or consuming response, {}", request.getContext().getId(), ExceptionUtils.getStackTrace(e));
                                }
                            });
                });
        NekoCatContext context = new NekoCatContext(source);
        NekoCatRequest request = new NekoCatRequest(startUrl);
        request.setContext(context);
        interceptors.forEach(i -> i.beforeStart(request));
        source.onNext(request);
    }


    public void stop() {
        stop.set(true);
        source.onComplete();
//        futures.forEach(f -> f.cancel(true));
        consumers.forEach(c -> shutdown(c, name));
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
        private long stopAfterEmmitMillis;

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


        public NekoCatSpiderBuilder stopAfterNoRequestEmmitMillis(long stopAfterEmmitMillis) {
            this.stopAfterEmmitMillis = stopAfterEmmitMillis;
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
            return new NekoCatSpider(this.startUrl, this.name, this.source, this.consumers, this.downloader, this.interceptors, stopAfterEmmitMillis);
        }

    }
}
