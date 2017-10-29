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
package org.dbflute.remoteapi.logging;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.dbflute.optional.OptionalThing;

/**
 * @author jflute
 * @since 0.3.7 (2017/10/13 Friday at showbase)
 */
public class SendReceiveLogOption {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String categoryName; // [top-keyword].remoteapi.sendreceive.[here], null allowed
    protected boolean suppressResponseBody; // may be too big
    protected Function<String, String> requestParameterFilter; // null allowed, not required
    protected Function<String, String> requestBodyFilter; // null allowed, not required
    protected Set<String> responseHeaderTargetSet; // null allowed, not required
    protected Function<String, String> responseBodyFilter; // null allowed, not required
    protected SendReceiveLogKeeper sendReceiveLogKeeper; // null allowed, not required, lazy-loaded
    protected boolean enabled;

    // ===================================================================================
    //                                                                         Easy-to-Use
    //                                                                         ===========
    public SendReceiveLogOption categorize(String categoryName) {
        assertArgumentNotNull("categoryName", categoryName);
        this.categoryName = categoryName;
        return this;
    }

    /**
     * Suppress response body. (remove response body from send-receive log)
     * @return this. (NotNull)
     */
    public SendReceiveLogOption suppressResponseBody() {
        suppressResponseBody = true;
        return this;
    }

    /**
     * @param requestParameterFilter The filter of request parameter, no filter if returns null. (NotNull)
     * @return this. (NotNull)
     */
    public SendReceiveLogOption filterRequestParameter(Function<String, String> requestParameterFilter) {
        assertArgumentNotNull("requestParameterFilter", requestParameterFilter);
        this.requestParameterFilter = requestParameterFilter;
        return this;
    }

    /**
     * @param requestBodyFilter The filter of request body, no filter if returns null. (NotNull)
     * @return this. (NotNull)
     */
    public SendReceiveLogOption filterRequestBody(Function<String, String> requestBodyFilter) {
        assertArgumentNotNull("requestBodyFilter", requestBodyFilter);
        this.requestBodyFilter = requestBodyFilter;
        return this;
    }

    /**
     * @param headerNames The array of header name, as logging target. (NotNull)
     * @return this. (NotNull)
     */
    public SendReceiveLogOption targetResponseHeader(String... headerNames) {
        assertArgumentNotNull("headerNames", headerNames);
        if (responseHeaderTargetSet == null) {
            responseHeaderTargetSet = new HashSet<String>();
        }
        for (String headerName : headerNames) {
            responseHeaderTargetSet.add(headerName);
        }
        return this;
    }

    /**
     * @param responseBodyFilter The filter of response body, no filter if returns null. (NotNull)
     * @return this. (NotNull)
     */
    public SendReceiveLogOption filterResponseBody(Function<String, String> responseBodyFilter) {
        assertArgumentNotNull("responseBodyFilter", responseBodyFilter);
        this.responseBodyFilter = responseBodyFilter;
        return this;
    }

    // ===================================================================================
    //                                                                           Keep Data
    //                                                                           =========
    public SendReceiveLogKeeper keeper() {
        if (sendReceiveLogKeeper == null) {
            sendReceiveLogKeeper = new SendReceiveLogKeeper();
        }
        return sendReceiveLogKeeper;
    }

    // ===================================================================================
    //                                                                           Framework
    //                                                                           =========
    public void xframeworkEnable() { // for framework
        enabled = true;
    }

    // ===================================================================================
    //                                                                        Small Helper
    //                                                                        ============
    protected void assertArgumentNotNull(String variableName, Object value) {
        if (variableName == null) {
            throw new IllegalArgumentException("The variableName should not be null.");
        }
        if (value == null) {
            throw new IllegalArgumentException("The argument '" + variableName + "' should not be null.");
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public boolean isEnabled() {
        return enabled;
    }

    public OptionalThing<String> getCategoryName() {
        return OptionalThing.ofNullable(categoryName, () -> {
            throw new IllegalStateException("Not found the categoryName.");
        });
    }

    public boolean isSuppressResponseBody() {
        return suppressResponseBody;
    }

    public OptionalThing<Function<String, String>> getRequestParameterFilter() {
        return OptionalThing.ofNullable(requestParameterFilter, () -> {
            throw new IllegalStateException("Not found the requestParameterFilter.");
        });
    }

    public OptionalThing<Function<String, String>> getRequestBodyFilter() {
        return OptionalThing.ofNullable(requestBodyFilter, () -> {
            throw new IllegalStateException("Not found the requestBodyFilter.");
        });
    }

    public Set<String> getResponseHeaderTargetSet() {
        return responseHeaderTargetSet != null ? Collections.unmodifiableSet(responseHeaderTargetSet) : Collections.emptySet();
    }

    public OptionalThing<Function<String, String>> getResponseBodyFilter() {
        return OptionalThing.ofNullable(responseBodyFilter, () -> {
            throw new IllegalStateException("Not found the responseBodyFilter.");
        });
    }
}
