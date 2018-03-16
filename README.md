# A lightweight crawler framework


## Usage

Add maven dependency
```xml
<dependency>
    <groupId>io.loli.nekocat</groupId>
    <artifactId>nekocat-core</artifactId>
    <version>0.0.4</version>
</dependency>
```



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
     .start();
```

### Thread pool

```java
NekoCatProperties.builder()
    .regex(".*\\.jpg")
    ...
    .downloadPoolSize(1)
    .downloadMaxQueueSize(1024)
    .piplinePoolSize(1)
    .piplineMaxQueueSize(1024)
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
                resp.asDocument().select("img")
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
                resp.asDocument().select("img")
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


### Http POST

```java
// form
// value must be urlencoded
request.setMethod("POST");
request.setRequestBody("param1=value1&param2=value2");
...

// json
request.setMethod("POST");
request.addHeader("content-type", "application/json");
request.setRequestBody(your_json_str);
```



### Additional headers
```java
request.addHeader(yourAdditionalHeader);
```



### Scheduled

```java
// spider will download the startUrl every 10 mins
NekoCatSpider.builder()
    .name("spiderName")
    .startUrl("http://www.example.com")
    ...
    .loopInterval(1000 * 60 * 10)
    ...
```


```java
// interval of each download 
NekoCatProperties.builder()
    .regex(".*\\.jpg")
    .interval(1000)
    ...
```

### Filter duplicate url 
```java
NekoCatProperties.builder()
    ...
    .interceptor(new FilterDownloadedUrlInterceptor(1024))
    ...

```

### Retry
```java
NekoCatProperties.builder()
    ...
    downloadRetry(1)
    ...
    piplineRetry(1)
    ...
```

### TODO
1. proxy pool
2. json export
3. redis queue/db queue