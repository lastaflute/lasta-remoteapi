/*
 * Copyright 2015-2018 the original author or authors.
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
package org.dbflute.remoteapi.http;

import java.net.URI;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

/**
 * The HTTP entity for DELETE with entity-enclosing. <br>
 * HTTP specification does not suppress entity-enclosing of DELETE <br>
 * but Because Apache HTTP Client does not support it and it's needed. <br>
 * (However, is this rare case? So it's secondary function)
 * @author jflute
 * @since 0.4.2 (2019/01/12 Saturday)
 */
public class HttpDeleteEnclosing extends HttpEntityEnclosingRequestBase {

    public final static String METHOD_NAME = "DELETE";

    public HttpDeleteEnclosing() {
    }

    public HttpDeleteEnclosing(URI uri) {
        setURI(uri);
    }

    public HttpDeleteEnclosing(String uri) {
        setURI(URI.create(uri));
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }
}
