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
    private Integer downloadPoolSize;
    private Integer downloadMaxQueueSize;
    private Integer piplinePoolSize;
    private Integer piplineMaxQueueSize;
    private List<NekoCatInterceptor> interceptorList;
    private long interval;


    public void setInterceptorList(List<NekoCatInterceptor> interceptorList) {
        this.interceptorList = interceptorList;
    }

    public static NekoCatProperties.NekoCatPropertiesBuilder builder() {
        return new NekoCatProperties.NekoCatPropertiesBuilder();
    }


    public static class NekoCatPropertiesBuilder {
        private final static AtomicInteger idx = new AtomicInteger(0);
        private String name = "default-" + idx.addAndGet(1);
        private UrlFilter urlFilter;
        private NekoCatPipline pipline;
        private Integer downloadPoolSize = 1;
        private Integer downloadMaxQueueSize = 1024;
        private Integer piplinePoolSize = 1;
        private Integer piplineMaxQueueSize = 1024;

        private long interval;
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


        public NekoCatProperties.NekoCatPropertiesBuilder downloadPoolSize(Integer downloadPoolSize) {
            this.downloadPoolSize = downloadPoolSize;
            return this;
        }


        public NekoCatProperties.NekoCatPropertiesBuilder downloadMaxQueueSize(Integer downloadMaxQueueSize) {
            this.downloadMaxQueueSize = downloadMaxQueueSize;
            return this;
        }


        public NekoCatProperties.NekoCatPropertiesBuilder piplinePoolSize(Integer piplinePoolSize) {
            this.piplinePoolSize = piplinePoolSize;
            return this;
        }

        public NekoCatProperties.NekoCatPropertiesBuilder piplineMaxQueueSize(Integer piplineMaxQueueSize) {
            this.piplineMaxQueueSize = piplineMaxQueueSize;
            return this;
        }


        public NekoCatProperties.NekoCatPropertiesBuilder interval(Long interval) {
            this.interval = interval;
            return this;
        }


        public NekoCatProperties.NekoCatPropertiesBuilder pipline(NekoCatPipline pipline) {
            this.pipline = pipline;
            return this;
        }

        public NekoCatProperties.NekoCatPropertiesBuilder interceptor(NekoCatInterceptor interceptor) {
            this.interceptorList.add(interceptor);
            return this;
        }

        public NekoCatProperties build() {
            return new NekoCatProperties(this.name, this.urlFilter, this.pipline, this.downloadPoolSize,
                    this.downloadMaxQueueSize, this.piplinePoolSize, this.piplineMaxQueueSize, this.interceptorList, this.interval);
        }


    }
}