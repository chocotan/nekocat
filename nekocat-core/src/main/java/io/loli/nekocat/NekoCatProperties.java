package io.loli.nekocat;

import io.loli.nekocat.interceptor.NekoCatInterceptor;
import io.loli.nekocat.pipline.NekoCatPipline;
import io.loli.nekocat.urlfilter.RegexUrlFilter;
import io.loli.nekocat.urlfilter.UrlFilter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * properties of each url regex
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NekoCatProperties {

    private String name;
    private UrlFilter urlFilter;
    private NekoCatPipline pipline;
    private Integer downloadMinPoolSize;
    private Integer downloadMaxPoolSize;
    private Integer downloadMaxQueueSize;
    private String downloadThreadName;
    private Integer consumeMinPoolSize;
    private Integer consumeMaxPoolSize;
    private Integer consumeMaxQueueSize;
    private String consumeThreadName;
    private NekoCatPipline consumer;
    private List<NekoCatInterceptor> interceptorList;


    public void setInterceptorList(List<NekoCatInterceptor> interceptorList) {
        this.interceptorList = interceptorList;
    }

    public static NekoCatProperties.NekoCatPropertiesBuilder builder() {
        return new NekoCatProperties.NekoCatPropertiesBuilder();
    }


    public static class NekoCatPropertiesBuilder {
        private final static AtomicInteger idx = new AtomicInteger(0);
        private String name = "default" + idx.addAndGet(1);
        private UrlFilter urlFilter;
        private NekoCatPipline pipline;
        private Integer downloadMinPoolSize = 1;
        private Integer downloadMaxPoolSize = 1;
        private Integer downloadMaxQueueSize = 1024;
        private String downloadThreadName = "nekocat-download";
        private Integer consumeMinPoolSize = 1;
        private Integer consumeMaxPoolSize = 1;
        private Integer consumeMaxQueueSize = 1024;
        private String consumeThreadName = "nekocat-consume";
        private NekoCatPipline consumer;
        private List<NekoCatInterceptor> interceptorList = new ArrayList<>();

        NekoCatPropertiesBuilder() {
        }

        public NekoCatProperties.NekoCatPropertiesBuilder name(String name) {
            this.name = name;
            return this;
        }

        public NekoCatProperties.NekoCatPropertiesBuilder regex(String regex) {
            this.urlFilter = new RegexUrlFilter(regex);
            return this;
        }

        public NekoCatProperties.NekoCatPropertiesBuilder pipline(NekoCatPipline pipline) {
            this.pipline = pipline;
            return this;
        }

        public NekoCatProperties.NekoCatPropertiesBuilder downloadMinPoolSize(Integer downloadMinPoolSize) {
            this.downloadMinPoolSize = downloadMinPoolSize;
            return this;
        }

        public NekoCatProperties.NekoCatPropertiesBuilder downloadMaxPoolSize(Integer downloadMaxPoolSize) {
            this.downloadMaxPoolSize = downloadMaxPoolSize;
            return this;
        }

        public NekoCatProperties.NekoCatPropertiesBuilder downloadMaxQueueSize(Integer downloadMaxQueueSize) {
            this.downloadMaxQueueSize = downloadMaxQueueSize;
            return this;
        }

        public NekoCatProperties.NekoCatPropertiesBuilder downloadThreadName(String downloadThreadName) {
            this.downloadThreadName = downloadThreadName;
            return this;
        }

        public NekoCatProperties.NekoCatPropertiesBuilder consumeMinPoolSize(Integer consumeMinPoolSize) {
            this.consumeMinPoolSize = consumeMinPoolSize;
            return this;
        }

        public NekoCatProperties.NekoCatPropertiesBuilder consumeMaxPoolSize(Integer consumeMaxPoolSize) {
            this.consumeMaxPoolSize = consumeMaxPoolSize;
            return this;
        }

        public NekoCatProperties.NekoCatPropertiesBuilder consumeMaxQueueSize(Integer consumeMaxQueueSize) {
            this.consumeMaxQueueSize = consumeMaxQueueSize;
            return this;
        }

        public NekoCatProperties.NekoCatPropertiesBuilder consumeThreadName(String consumeThreadName) {
            this.consumeThreadName = consumeThreadName;
            return this;
        }


        public NekoCatProperties.NekoCatPropertiesBuilder consumer(NekoCatPipline consumer) {
            this.consumer = consumer;
            return this;
        }

        public NekoCatProperties.NekoCatPropertiesBuilder interceptor(NekoCatInterceptor interceptor) {
            this.interceptorList.add(interceptor);
            return this;
        }

        public NekoCatProperties build() {
            return new NekoCatProperties(this.name, this.urlFilter, this.pipline, this.downloadMinPoolSize, this.downloadMaxPoolSize, this.downloadMaxQueueSize, this.downloadThreadName, this.consumeMinPoolSize, this.consumeMaxPoolSize, this.consumeMaxQueueSize, this.consumeThreadName, this.consumer, this.interceptorList);
        }


    }
}