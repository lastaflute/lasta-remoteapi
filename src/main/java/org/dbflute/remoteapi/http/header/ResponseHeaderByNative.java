/*
 * Copyright 2015-2022 the original author or authors.
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
package org.dbflute.remoteapi.http.header;

import org.apache.http.Header;
import org.dbflute.optional.OptionalThing;

/**
 * @author jflute
 * @since 0.4.3 (2019/03/13 Wednesday)
 */
public class ResponseHeaderByNative implements ResponseHeader {

    protected final Header nativeHeader; // not null

    public ResponseHeaderByNative(Header nativeHeader) {
        this.nativeHeader = nativeHeader;
    }

    public String getName() { // not null
        return nativeHeader.getName(); // not null
    }

    public OptionalThing<String> getValue() {
        return OptionalThing.ofNullable(nativeHeader.getValue(), () -> {
            throw new IllegalStateException("Not found the header value: " + nativeHeader);
        });
    }
}
