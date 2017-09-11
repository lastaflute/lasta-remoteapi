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
    protected final String hostName; // null allowed if unknown
    protected final Integer port; // null allowed if unknown

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public MockSupposedRequest(String url, String body, String hostName, Integer port) {
        this.url = url;
        this.body = body;
        this.hostName = hostName;
        this.port = port;
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
}
