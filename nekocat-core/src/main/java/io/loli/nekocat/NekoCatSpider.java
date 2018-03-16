package io.loli.nekocat;

import com.google.mu.util.Retryer;
import io.loli.nekocat.downloader.NekoCatDownloader;
import io.loli.nekocat.downloader.NekoCatOkhttpDownloader;
import io.loli.nekocat.interceptor.NekoCatInterceptor;
import io.loli.nekocat.request.NekoCatRequest;
import io.loli.nekocat.response.NekoCatResponse;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.UnicastSubject;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.loli.nekocat.thread.NekoCatGlobalThreadPools.getDownloadExecutor;
import static io.loli.nekocat.thread.NekoCatGlobalThreadPools.getPiplineExecutor;

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
    private long interval;
    private long loopInterval;
    private long startTime;

    private List<Disposable> disposables = new CopyOnWriteArrayList<>();
    private AtomicBoolean stop = new AtomicBoolean(false);


    private NekoCatSpider(String startUrl, String name, List<NekoCatProperties> consumers, NekoCatDownloader downloader, List<NekoCatInterceptor> interceptors, long stopAfterEmmitMillis, long interval,
                          long loopInterval) {
        this.startUrl = startUrl;
        this.name = name;
        this.consumers = consumers;
        this.downloader = downloader;
        this.interceptors = interceptors;
        this.stopAfterEmmitMillis = stopAfterEmmitMillis;
        this.interval = interval;
        this.loopInterval = loopInterval;
    }


    public void start() {
        stop.set(false);
        startTime = System.currentTimeMillis();
        source = UnicastSubject.create();
        NekoCatContext context = new NekoCatContext(source);
        NekoCatRequest request = new NekoCatRequest(startUrl);
        request.setContext(context);

        Observable<NekoCatRequest> observable = source;
        observable = observable.filter(p -> consumers.stream().anyMatch(c -> Try.of(() -> c.getFilters().stream().anyMatch(a -> Try.of(() -> a.test(p)).getOrElse(true)))
                .getOrElse(true)));
        if (stopAfterEmmitMillis > 0) {
            observable = observable.timeout(stopAfterEmmitMillis, TimeUnit.MILLISECONDS)
                    .doOnError(excep -> stop());
        }

        if (interval > 0) {
            observable = zipObservableWithInterval(interval, observable);

        }
        // start url interval
        if (loopInterval > 0) {
            Disposable subscribe = Observable.interval(interval, TimeUnit.MILLISECONDS)
                    .doOnNext(startUrlIntervalAction())
                    .subscribe();
            disposables.add(subscribe);
        }


        List<UnicastSubject<NekoCatRequest>> subjects = new ArrayList<>();
        consumers.forEach(p -> {
            UnicastSubject<NekoCatRequest> subject = UnicastSubject.create();
            Observable<NekoCatRequest> obs = subject.filter(d -> p.getFilters().stream().allMatch(f -> Try.of(() -> f.test(d)).getOrElse(true)));


            if (p.getInterval() > 0) {
                obs = zipObservableWithInterval(p.getInterval(), obs);
            }

            Disposable subscribe = obs
                    .flatMap(f -> Observable.just(f)
                            .observeOn(Schedulers.from(getDownloadExecutor(p, name)))
                            .doOnNext(fillNekoCatContextAtBeginning(p))
                            .filter(interceptorBeforeDownload(p))
                            .map(downloadWithTry())
                            .doOnNext(interceptorAfterDownload(p))
                            .retry()
                    )
                    .flatMap(f -> Observable.just(f)
                            .observeOn(Schedulers.from(getPiplineExecutor(p, name)))
                            .filter(interceptorBeforePipline(p))
                            .doOnNext(piplineWithTry(p))
                            .doOnNext(interceptorAfterPipline(p))
                            .retry()

                    )
                    .subscribe();
            subjects.add(subject);
            disposables.add(subscribe);
        });

        Disposable subscribe = observable
                .flatMap(d -> Observable.just(d).observeOn(Schedulers.newThread()))
                .doOnSubscribe(interceptorBeforeStart())
                .doOnNext(dispatchRequestToConsumer(subjects))
                .retry()
                .subscribe();
        disposables.add(subscribe);
        source.onNext(request);
    }

    private Consumer<? super Disposable> interceptorBeforeStart() {
        return url -> interceptors.forEach(i -> i.beforeStart(startUrl));
    }

    private Consumer<NekoCatRequest> dispatchRequestToConsumer(List<UnicastSubject<NekoCatRequest>> subjects) {
        return url -> subjects.forEach(o -> {
            o.onNext(url);
        });
    }

    private Observable<NekoCatRequest> zipObservableWithInterval(long p, Observable<NekoCatRequest> subject) {
        return subject.zipWith(Observable.interval(p, TimeUnit.MILLISECONDS), (item, interval) -> item);
    }

    private Consumer<NekoCatResponse> interceptorAfterPipline(NekoCatProperties p) {
        return r -> {
            List<NekoCatInterceptor> interceptorList = r.getContext().getInterceptorList();
            interceptorList.forEach(i -> i.afterPipline(r.getContext()));
        };
    }

    private Consumer<NekoCatResponse> piplineWithTry(NekoCatProperties p) {
        return r ->
                Try.of(() -> {
                    if (p.getPiplineRetry() > 0) {
                        return new Retryer()
                                .upon(Throwable.class, Retryer.Delay.ofMillis(100).exponentialBackoff(1.5, p.getPiplineRetry()))
                                .retryBlockingly(() -> p.getPipline().apply(r));
                    }
                    return p.getPipline().apply(r);
                }).onSuccess(resp -> {
                    r.getContext().setPiplineResult(resp);
                }).onFailure(excep -> {
                    r.getContext().setPiplineException(excep);
                    mergeInterceptor(p).forEach(i -> i.errorPipline(r.getContext(), excep));
                });
    }

    private Predicate<NekoCatResponse> interceptorBeforePipline(NekoCatProperties p) {
        return r -> {
            List<NekoCatInterceptor> interceptorList = r.getContext().getInterceptorList();
            return interceptorList.isEmpty() || interceptorList.stream().allMatch(i -> i.beforePipline(r));
        };
    }

    private List<NekoCatInterceptor> mergeInterceptor(NekoCatProperties p) {
        List<NekoCatInterceptor> interceptorList = new ArrayList<>();
        interceptorList.addAll(interceptors);
        interceptorList.addAll(p.getInterceptorList());
        return interceptorList;
    }

    private Consumer<NekoCatResponse> interceptorAfterDownload(NekoCatProperties p) {
        return r -> {
            r.getContext().getInterceptorList().forEach(i -> i.afterDownload(r));
        };
    }

    private Function<NekoCatRequest, NekoCatResponse> downloadWithTry() {
        return r ->
                Try.of(() -> {
                    if (r.getProperties().getDownloadRetry() > 0) {
                        return new Retryer()
                                .upon(Throwable.class, Retryer.Delay.ofMillis(100).exponentialBackoff(1.5, r.getProperties().getDownloadRetry()))
                                .retryBlockingly(() -> downloader.apply(r));
                    }
                    return downloader.apply(r);
                }).onSuccess(res -> res.setContext(r.getContext())).getOrElseGet(
                        (error) -> {
                            NekoCatResponse response = new NekoCatResponse();
                            response.setContext(r.getContext());
                            response.setThrowable(error);
                            r.getContext().getInterceptorList().forEach(i -> i.errorDownload(response));
                            return response;
                        });
    }


    private Predicate<NekoCatRequest> interceptorBeforeDownload(NekoCatProperties p) {
        return r -> {
            List<NekoCatInterceptor> interceptorList = r.getContext().getInterceptorList();
            return interceptorList.isEmpty() || interceptorList.stream().allMatch(i -> i.beforeDownload(r));
        };
    }


    private Consumer<NekoCatRequest> fillNekoCatContextAtBeginning(NekoCatProperties p) {
        return r -> {
            r.getContext().setSource(source);
            r.getContext().setProperties(p);
            r.getContext().setInterceptorList(mergeInterceptor(p));

            r.setProperties(p);
        };
    }

    private Consumer<Long> startUrlIntervalAction() {
        return l -> {
            NekoCatContext nextCtx = new NekoCatContext(source);
            NekoCatRequest nextReq = new NekoCatRequest(startUrl);
            nextReq.setContext(nextCtx);
            source.onNext(nextReq);
        };
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
        interceptors.forEach(interceptorBeforeStop());
        stop.set(true);
        disposables.forEach(disposable -> {
            if (!disposable.isDisposed()) {
                disposable.dispose();
            }
        });
        disposables.clear();
    }

    private java.util.function.Consumer<NekoCatInterceptor> interceptorBeforeStop() {
        return i -> i.beforeStop(startUrl);
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
        private long interval;
        private long loopInterval;

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

        public NekoCatSpiderBuilder interval(long interval) {
            this.interval = interval;
            return this;
        }


        public NekoCatSpiderBuilder loopInterval(long loopInterval) {
            this.loopInterval = loopInterval;
            return this;
        }

        public NekoCatSpider build() {
            if (this.downloader == null) {
                this.downloader = new NekoCatOkhttpDownloader();
            }

            return new NekoCatSpider(this.startUrl, this.name, this.consumers, this.downloader, this.interceptors, stopAfterEmmitMillis, interval, loopInterval);
        }

    }
}
