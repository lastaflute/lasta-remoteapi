/*
 * Copyright 2015-2020 the original author or authors.
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
import java.util.Optional;
import java.util.stream.Collectors;

import org.dbflute.optional.OptionalThing;

/**
 * @author jflute
 * @since 0.4.3 (2019/03/13 Wednesday)
 */
public class ResponseHeaderProvider {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final List<ResponseHeader> responseHeaderList; // not null

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ResponseHeaderProvider(List<ResponseHeader> headerList) {
        this.responseHeaderList = headerList;
    }

    // ===================================================================================
    //                                                                              Finder
    //                                                                              ======
    /**
     * Find the list of header value, which is existing-value only.
     * @param headerName The header name to search header value as case-insensitive. (NotNull)
     * @return The optional value of found header. (NotNull, EmptyAllowed)
     */
    public OptionalThing<String> findPresentFirstValue(String headerName) {
        if (headerName == null) {
            throw new IllegalArgumentException("The argument 'headerName' should not be null.");
        }
        final Optional<String> first = responseHeaderList.stream()
                .filter(header -> matchesByName(header, headerName))
                .filter(header -> isPresentValue(header)) // value-existing only
                .map(header -> extractValue(header))
                .findFirst();
        return OptionalThing.migratedFrom(first, () -> {
            throw new IllegalStateException("Not found the header value: headerName=" + headerName);
        });
    }

    /**
     * Find the list of header value, which is existing-value only.
     * @param headerName The header name to search header value as case-insensitive. (NotNull)
     * @return The read-only list of found header value. (NotNull, EmptyAllowed)
     */
    public List<String> findPresentValueList(String headerName) {
        if (headerName == null) {
            throw new IllegalArgumentException("The argument 'headerName' should not be null.");
        }
        return responseHeaderList.stream()
                .filter(header -> matchesByName(header, headerName))
                .filter(header -> isPresentValue(header)) // value-existing only
                .map(header -> extractValue(header))
                .collect(Collectors.toList());
    }

    protected boolean matchesByName(ResponseHeader header, String headerName) {
        return header.getName().equalsIgnoreCase(headerName); // header should be case-insensitive
    }

    protected boolean isPresentValue(ResponseHeader header) {
        return header.getValue().isPresent();
    }

    protected String extractValue(ResponseHeader header) {
        return header.getValue().get();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public List<ResponseHeader> getResponseHeaderList() { // read-only
        return Collections.unmodifiableList(responseHeaderList);
    }
}
