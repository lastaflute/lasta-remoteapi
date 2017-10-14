/*
 * Copyright 2015-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.dbflute.remoteapi.mock.supporter;

import java.util.List;
import java.util.Map;

import org.dbflute.optional.OptionalThing;

/**
 * @author awane
 * @author jflute
 */
public class MockSupposedRequest {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String url; // e.g. http://localhost:8090/harbor/lido/product/list/1
    protected final String body; // null allowed, not required
    protected final String hostName; // null allowed if unknown (basically known)
    protected final Integer port; // null allowed if unknown (basically known)
    protected final Map<String, List<String>> headerMap; // not null, empty allowed, read-only

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public MockSupposedRequest(String url, String body, String hostName, Integer port, Map<String, List<String>> headerMap) {
        this.url = url;
        this.body = body;
        this.hostName = hostName;
        this.port = port;
        this.headerMap = headerMap; // should be read-only
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "request:{" + url + ", " + body + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getUrl() {
        return url;
    }

    public OptionalThing<String> getBody() {
        return OptionalThing.ofNullable(body, () -> {
            throw new IllegalStateException("Not found the request body: url=" + url);
        });
    }

    public OptionalThing<String> getHostName() {
        return OptionalThing.ofNullable(hostName, () -> {
            throw new IllegalStateException("Not found the host name: url=" + url);
        });
    }

    public OptionalThing<Integer> getPort() {
        return OptionalThing.ofNullable(port, () -> {
            throw new IllegalStateException("Not found the port: url=" + url);
        });
    }

    public Map<String, List<String>> getHeaderMap() {
        return headerMap;
    }
}
