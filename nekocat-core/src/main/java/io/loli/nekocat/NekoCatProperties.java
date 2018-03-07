package io.loli.nekocat;

import io.loli.nekocat.downloader.NekoCatDownloader;
import io.loli.nekocat.downloader.NekoCatOkhttpDownloader;
import io.loli.nekocat.interceptor.NekoCatInterceptor;
import io.loli.nekocat.pipline.NekoCatPipline;
import lombok.*;
import org.omg.PortableInterceptor.Interceptor;

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
    private String regex;
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
        private String regex;
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
            this.regex = regex;
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
            return new NekoCatProperties(this.name, this.regex, this.pipline, this.downloadMinPoolSize, this.downloadMaxPoolSize, this.downloadMaxQueueSize, this.downloadThreadName, this.consumeMinPoolSize, this.consumeMaxPoolSize, this.consumeMaxQueueSize, this.consumeThreadName, this.consumer, this.interceptorList);
        }


    }
}