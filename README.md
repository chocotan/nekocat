# A lightweight clawer framework


## Ussage

```java
NekoCatSpider.builder()
    .name("spiderName")
    .startUrl("http://www.example.com/")
    .url(NekoCatProperties.builder()
            // deal with the start-url
            .regex("http://www.example.com/")
            .pipline((resp)->{
                response.asDocument()
                    .select("css-select")
                    .forEach(a ->
                        // url that should be downloaded
                        resp.getContext().next(a.attr("href"));
                    );
            })
            .build())
    .url(NekoCatProperties.builder().regex("http://www.example.com/.+")
            .pipline(resp -> {
                // select all images
                resp.adDocument().select("img")
                .forEach(img->{
                    resp.getContext().next(img.attr("src"));
                });
            })
            .build())
     .build()
```

### Thread pool

```java
NekoCatProperties.builder()
    .regex(".*\\.jpg")
    ...
    .downloadMinPoolSize(1)
    .downloadMaxPoolSize(1)
    .downloadMaxQueueSize(1024)
    .consumeMinPoolSize(1)
    .consumeMaxPoolSize(10)
    .consumeMaxQueueSize(1024)
```

### Exit while no urls emitted

```java
NekoCatSpider.builder()
    .name("spiderName")
    ...
    .stopAfterNoRequestEmmitMillis(3600 * 1000L)
```

### Get next pipline result

```java
NekoCatSpider.builder()
    .name("spiderName")
    .startUrl("http://www.example.com/")
    .url(NekoCatProperties.builder().regex("http://www.example.com/")
            .pipline(resp -> {
                // select all images
                resp.adDocument().select("img")
                .forEach(img->{
                    CompletableFuture<Object> result = resp.getContext().next(img.attr("src")).getPiplineResult();
                    // get the file returned by the next pipline
                    File imgFile = (File)result.get();
                    
                });
            })
            .build())
    .url(NekoCatProperties.builder().regex(".*\\.jpg")
            .pipline(resp -> {
                // select all images
                byte[] bytes = resp.asBytes();
                // write img to filesystem and return this file
                writeBytesToFile(bytes);
                return yourFile;
            })
            .build())
    .build()
```

### Pass object to next request

```java
NekoCatSpider.builder()
    .name("spiderName")
    .startUrl("http://www.example.com/")
    .url(NekoCatProperties.builder().regex("http://www.example.com/")
            .pipline(resp -> {
                // select all images
                resp.adDocument().select("img")
                .forEach(img->{
                    resp.getContext().addNextAttribute("storeFolder", "/tmp");
                    resp.getContext().next(img.attr("src"));
                });
            })
            .build())
    .url(NekoCatProperties.builder().regex(".*\\.jpg")
            .pipline(resp -> {
                String storeFolder = resp.getContext().getAttribute("storeFolder");
                // select all images
                byte[] bytes = resp.asBytes();
                // write img to filesystem and return this file
                writeBytesToFile(storeFolder, bytes);
                return null;
            })
            .build())
    .build()
```

### Add additional headers




## TODO
1. spider scoped thread pool
2. scheduled download
3. unit test