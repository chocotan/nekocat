package io.loli.nekocat;

import io.loli.nekocat.downloader.NekoCatDownloader;
import io.loli.nekocat.downloader.NekoCatOkhttpDownloader;
import io.loli.nekocat.interceptor.NekoCatInterceptor;
import io.loli.nekocat.request.NekoCatRequest;
import io.loli.nekocat.response.NekoCatResponse;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;
import io.reactivex.subjects.UnicastSubject;
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
    private UnicastSubject<NekoCatRequest> source;

    private List<NekoCatProperties> consumers;
    private NekoCatDownloader downloader;
    private List<NekoCatInterceptor> interceptors;
    private long stopAfterEmmitMillis;
    private List<Future> futures = new ArrayList<>();


    private List<Disposable> disposables = new CopyOnWriteArrayList<>();
    private AtomicBoolean stop = new AtomicBoolean(false);


    private NekoCatSpider(String startUrl, String name, List<NekoCatProperties> consumers, NekoCatDownloader downloader, List<NekoCatInterceptor> interceptors, long stopAfterEmmitMillis) {
        this.startUrl = startUrl;
        this.name = name;
        this.consumers = consumers;
        this.downloader = downloader;
        this.interceptors = interceptors;
        this.stopAfterEmmitMillis = stopAfterEmmitMillis;
    }


    public void start() {
        stop.set(false);
        source = UnicastSubject.create();
        Observable<NekoCatRequest> observable = source;
        if (stopAfterEmmitMillis > 0) {
            observable = source.timeout(stopAfterEmmitMillis, TimeUnit.MILLISECONDS)
                    .doOnError(excep -> {
                        stop();
                    });
        }

        List<Observer<NekoCatRequest>> subjects = new ArrayList<>();

        consumers.forEach(p -> {
            List<NekoCatInterceptor> mergedInterceptors = new ArrayList<>();
            mergedInterceptors.addAll(interceptors);
            mergedInterceptors.addAll(p.getInterceptorList());
            p.setInterceptorList(mergedInterceptors);
            UnicastSubject<NekoCatRequest> subject = UnicastSubject.create();
            Disposable subscribe = subject
                    .filter(p.getUrlFilter())
                    .doOnNext(r -> {
                        r.getContext().setSource(source);
                        r.getContext().setProperties(p);
                        p.getInterceptorList().forEach(i -> i.beforeStart(r));
                    })
                    .subscribeOn(Schedulers.from(getDownloadExecutor(p, name)))
                    .doOnNext(r -> p.getInterceptorList().forEach(i -> i.beforeDownload(r)))
                    .map(r -> Try.of(() -> downloader.apply(r))
                            .onSuccess(res-> res.setContext(r.getContext()))
                            .getOrElseGet(
                                    (error) -> {
                                        NekoCatResponse response = new NekoCatResponse();
                                        response.setContext(r.getContext());
                                        response.setThrowable(error);
                                        return response;
                                    }
                            ))
                    .doOnNext(r -> p.getInterceptorList().forEach(i -> i.afterDownload(r)))
                    .observeOn(Schedulers.from(getConsumeExecutor(p, name)))
                    .doOnNext(r -> p.getInterceptorList().forEach(i -> i.beforePipline(r)))
                    .doOnNext(r -> Try.of(() -> p.getPipline().apply(r))
                            .onSuccess(resp -> {
                                r.getContext().setPiplineResult(resp);
                            }).onFailure(excep -> {
                                r.getContext().setPiplineException(excep);
                            }))
                    .doOnNext(r -> p.getInterceptorList().forEach(i -> i.afterPipline(r.getContext())))
                    .retry()
                    .subscribe();

            subjects.add(subject);
            disposables.add(subscribe);
        });

        Disposable subscribe = observable
                .doOnNext(url -> {
                    subjects.forEach(o -> {
                        o.onNext(url);
                    });
                })
                .retry()
                .subscribe();
        disposables.add(subscribe);
        NekoCatContext context = new NekoCatContext(source);
        NekoCatRequest request = new NekoCatRequest(startUrl);
        request.setContext(context);
        source.onNext(request);
    }

    public String getStartUrl() {
        return startUrl;
    }

    public String getName() {
        return name;
    }

    public UnicastSubject<NekoCatRequest> getSource() {
        return source;
    }

    public List<NekoCatProperties> getConsumers() {
        return consumers;
    }

    public NekoCatDownloader getDownloader() {
        return downloader;
    }

    public List<NekoCatInterceptor> getInterceptors() {
        return interceptors;
    }

    public long getStopAfterEmmitMillis() {
        return stopAfterEmmitMillis;
    }


    public List<Future> getFutures() {
        return futures;
    }

    public AtomicBoolean getStop() {
        return stop;
    }

    public void stop() {
        stop.set(true);
        disposables.forEach(disposable -> {
            if (!disposable.isDisposed()) {
                disposable.dispose();
            }
        });
        disposables.clear();
    }

    public static NekoCatSpiderBuilder builder() {
        return new NekoCatSpiderBuilder();
    }


    public static class NekoCatSpiderBuilder {
        private String startUrl;
        private String name = "spider";
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
            if (this.downloader == null) {
                this.downloader = new NekoCatOkhttpDownloader();
            }

            return new NekoCatSpider(this.startUrl, this.name, this.consumers, this.downloader, this.interceptors, stopAfterEmmitMillis);
        }

    }
}
