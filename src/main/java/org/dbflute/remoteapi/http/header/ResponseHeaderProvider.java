/*
 * Copyright 2015-2019 the original author or authors.
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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jflute
 * @since 0.4.3 (2019/03/13 Wednesday)
 */
public class ResponseHeaderProvider {

    protected final List<ResponseHeader> responseHeaderList; // not null

    public ResponseHeaderProvider(List<ResponseHeader> headerList) {
        this.responseHeaderList = headerList;
    }

    public List<String> findPresentValueList(String headerName) { // not null, empty allowed, read-only
        if (headerName == null) {
            throw new IllegalArgumentException("The argument 'headerName' should not be null.");
        }
        return responseHeaderList.stream()
                .filter(header -> header.getName().equals(headerName))
                .filter(header -> header.getValue().isPresent()) // value-existing only
                .map(header -> header.getValue().get())
                .collect(Collectors.toList());
    }

    public List<ResponseHeader> getResponseHeaderList() { // read-only
        return Collections.unmodifiableList(responseHeaderList);
    }
}
