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

# Information
## Maven Dependency in pom.xml
```xml
<dependency>
    <groupId>org.lastaflute.remoteapi</groupId>
    <artifactId>lasta-remoteapi</artifactId>
    <version>0.1.0</version>
</dependency>
```

## License
Apache License 2.0

## Official site
comming soon...

# Thanks, Friends
MailFlute is used by:  
comming soon...
