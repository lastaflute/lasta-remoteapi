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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.dbflute.optional.OptionalThing;

/**
 * @author jflute
 * @since 0.3.7 (2017/10/13 Friday at showbase)
 */
public class SendReceiveLogKeeper {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected LocalDateTime beginDateTime; // null allowed until beginning
    protected String queryParameter; // null allowed if e.g. no parameter
    protected Map<String, Object> formParameterMap; // null allowed if e.g. no parameter
    protected String requestBodyContent; // null allowed if e.g. no body
    protected String requestBodyType; // format e.g. json, xml, null allowed if e.g. no body, body
    protected String responseBodyContent; // null allowed if e.g. no body
    protected String responseBodyType; // body format e.g. json, xml, null allowed if e.g. no body
    protected RuntimeException cause; // null allowed

    // ===================================================================================
    //                                                                         Keep Facade
    //                                                                         ===========
    // -----------------------------------------------------
    //                                        Begin DateTime
    //                                        --------------
    public void keepBeginDateTime(LocalDateTime beginDateTime) {
        assertArgumentNotNull("beginDateTime", beginDateTime);
        this.beginDateTime = beginDateTime;
    }

    // -----------------------------------------------------
    //                                          Query String
    //                                          ------------
    public void keepQueryParameter(String queryParameter) {
        assertArgumentNotNull("queryParameter", queryParameter);
        this.queryParameter = queryParameter;
    }

    // -----------------------------------------------------
    //                                        Form Parameter
    //                                        --------------
    public void keepFormParameter(Map<String, ? extends Object> parameterMap) {
        assertArgumentNotNull("parameterMap", parameterMap);
        parameterMap.forEach((key, value) -> addFormParameter(key, value));
    }

    protected void addFormParameter(String key, Object value) { // value may be null!? accept it just in case
        assertArgumentNotNull("key", key);
        if (formParameterMap == null) {
            formParameterMap = new LinkedHashMap<String, Object>();
        }
        formParameterMap.put(key, value);
    }

    // -----------------------------------------------------
    //                                          Request Body
    //                                          ------------
    public void keepRequestBody(String requestBodyContent, String requestBodyType) { // accept null just in case
        assertArgumentNotNull("requestBodyType", requestBodyType);
        this.requestBodyContent = requestBodyContent;
        this.requestBodyType = requestBodyType;
    }

    // -----------------------------------------------------
    //                                         Response Body
    //                                         -------------
    public void keepResponseBody(String responseBodyContent, String responseBodyType) { // accept null just in case
        assertArgumentNotNull("responseBodyType", responseBodyType);
        this.responseBodyContent = responseBodyContent;
        this.responseBodyType = responseBodyType;
    }

    // -----------------------------------------------------
    //                                             Exception
    //                                             ---------
    public void keepCause(RuntimeException cause) {
        assertArgumentNotNull("cause", cause);
        this.cause = cause;
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
    public OptionalThing<LocalDateTime> getBeginDateTime() {
        return OptionalThing.ofNullable(beginDateTime, () -> {
            throw new IllegalStateException("Not found the begin date-time.");
        });
    }

    public OptionalThing<String> getQueryParameter() {
        return OptionalThing.ofNullable(queryParameter, () -> {
            throw new IllegalStateException("Not found the query parameter.");
        });
    }

    public Map<String, Object> getFormParameterMap() { // not null
        return formParameterMap != null ? Collections.unmodifiableMap(formParameterMap) : Collections.emptyMap();
    }

    public OptionalThing<String> getRequestBodyContent() {
        return OptionalThing.ofNullable(requestBodyContent, () -> {
            throw new IllegalStateException("Not found the request body content.");
        });
    }

    public OptionalThing<String> getRequestBodyType() {
        return OptionalThing.ofNullable(requestBodyType, () -> {
            throw new IllegalStateException("Not found the request body type.");
        });
    }

    public OptionalThing<String> getResponseBodyContent() {
        return OptionalThing.ofNullable(responseBodyContent, () -> {
            throw new IllegalStateException("Not found the response body content.");
        });
    }

    public OptionalThing<String> getResponseBodyType() {
        return OptionalThing.ofNullable(responseBodyType, () -> {
            throw new IllegalStateException("Not found the response body type.");
        });
    }

    public OptionalThing<RuntimeException> getCause() {
        return OptionalThing.ofNullable(cause, () -> {
            throw new IllegalStateException("Not found the cause.");
        });
    }
}
