Lasta RemoteApi
=======================
Remote API Call library for LastaFlute with RemoFlute

contributed by U-NEXT: http://video.unext.jp/


# Architecture
```
 +---------------------+       +----------------+       +-------------+
 | LastaRemoteBehavior |  -->  | LastaRemoteApi |  -->  | HTTP Client |
 +---------------------+       +----------------+       +-------------+
        A        |                  | use
        |        |                  V
        |        |             +--------------------+
        |        +--setup----> | LastaRemoteApiRule |<>-----+
        |        |             +--------------------+       |
     extends     |                                          |
        |        |                                          |
        |        |                                          |
        |        |                                          |
        |        |                                        +-----------------------------+
        |        |    +-----------------------new-------> | LaJsonSender/LaJsonReceiver |
        |        |    |                                   +-----------------------------+
        |        |    |
 +-------------------------+      +----------------+
 |    RemoteHarborBhv      |      | remote_api.xml |
 |    (your component)     |----<>| (your DI xml)  |
 +-------------------------+      +----------------+
                                          A
                                          | include
                                     +---------+
                                     | app.xml |
                                     +---------+
```

# Example Code
```
/**
 * @author jflute
 */
public class RemoteHarborBhv extends LastaRemoteBehavior {

    public RemoteHarborBhv(RequestManager requestManager) {
        super(requestManager);
    }

    @Override
    protected void yourDefaultRule(FlutyRemoteApiRule rule) {
        JsonMappingOption jsonMappingOption = new JsonMappingOption();
        rule.sendBodyBy(new LaJsonSender(requestManager, jsonMappingOption));
        rule.receiveBodyBy(new LaJsonReceiver(requestManager, jsonMappingOption));
    }

    @Override
    protected String getUrlBase() {
        return "http://localhost:8090/harbor";
    }

    public HbSearchPagingResult<RemoteProductRowResult> requestProductList(RemoteProductSearchBody body) {
        return doRequestPost(new ParameterizedRef<RemoteSearchPagingResult<RemoteProductRowResult>>() {
        }.getType(), "/lido/product/list", moreUrl(1), body, rule -> {});
    }
}
```

# Information
## Maven Dependency in pom.xml
```xml
<dependency>
    <groupId>org.lastaflute.remoteapi</groupId>
    <artifactId>lasta-remoteapi</artifactId>
    <version>0.4.8</version>
</dependency>
```

## License
Apache License 2.0

## Official site
comming soon...

# Thanks, Friends
MailFlute is used by:  
comming soon...
