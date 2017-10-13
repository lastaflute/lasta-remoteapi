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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
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
    // -----------------------------------------------------
    //                                           Basic Begin
    //                                           -----------
    protected LocalDateTime beginDateTime; // null allowed until beginning
    protected Object facadeExp; // null allowed until beginning

    // -----------------------------------------------------
    //                                               Request
    //                                               -------
    protected Map<String, Object> requestHeaderMap; // null allowed if e.g. no request header
    protected Map<String, Object> queryParameterMap; // null allowed if e.g. no query parameter
    protected Map<String, Object> formParameterMap; // null allowed if e.g. no form parameter
    protected String requestBodyContent; // null allowed if e.g. no body or null body
    protected String requestBodyType; // body format e.g. json, xml, null allowed if e.g. no body or null body

    // -----------------------------------------------------
    //                                              Response
    //                                              --------
    protected Map<String, Object> responseHeaderMap; // null allowed until response
    protected String responseBodyContent; // null allowed until response or if e.g. no body or null body
    protected String responseBodyType; // body format e.g. json, xml, null allowed until response or if e.g. no body
    protected Integer httpStatus; // null allowed

    // -----------------------------------------------------
    //                                             Basic End
    //                                             ---------
    protected RuntimeException cause; // null allowed
    protected LocalDateTime endDateTime; // null allowed until ending

    // ===================================================================================
    //                                                                         Keep Facade
    //                                                                         ===========
    // -----------------------------------------------------
    //                                           Basic Begin
    //                                           -----------
    public void keepBeginDateTime(LocalDateTime beginDateTime) {
        assertArgumentNotNull("beginDateTime", beginDateTime);
        this.beginDateTime = beginDateTime;
    }

    public void keepFacadeExp(Object facadeExp) {
        assertArgumentNotNull("facadeExp", facadeExp);
        this.facadeExp = facadeExp;
    }

    // -----------------------------------------------------
    //                                               Request
    //                                               -------
    public void keepRequestHeader(Map<String, ? extends Object> requestHeaderMap) {
        assertArgumentNotNull("requestHeaderMap", requestHeaderMap);
        requestHeaderMap.forEach((name, value) -> addRequestHeader(name, value)); // copy for independency
    }

    protected void addRequestHeader(String name, Object value) { // value may be null!? accept it just in case
        assertArgumentNotNull("name", name);
        if (requestHeaderMap == null) {
            requestHeaderMap = new LinkedHashMap<String, Object>();
        }
        requestHeaderMap.put(name, value);
    }

    public void keepQueryParameter(String parameterName, String parameterValue) { // value may be null!? accept it just in case
        assertArgumentNotNull("parameterName", parameterName);
        if (queryParameterMap == null) {
            queryParameterMap = new LinkedHashMap<String, Object>();
        }
        addHierarchalMapElement(queryParameterMap, parameterName, parameterValue);
    }

    public void keepFormParameter(Map<String, ? extends Object> parameterMap) {
        assertArgumentNotNull("parameterMap", parameterMap);
        parameterMap.forEach((key, value) -> addFormParameter(key, value)); // copy for independency
    }

    protected void addFormParameter(String key, Object value) { // value may be null!? accept it just in case
        assertArgumentNotNull("key", key);
        if (formParameterMap == null) {
            formParameterMap = new LinkedHashMap<String, Object>();
        }
        formParameterMap.put(key, value);
    }

    public void keepRequestBody(String requestBodyContent, String requestBodyType) { // accept null just in case
        assertArgumentNotNull("requestBodyType", requestBodyType);
        this.requestBodyContent = requestBodyContent;
        this.requestBodyType = requestBodyType;
    }

    // -----------------------------------------------------
    //                                              Response
    //                                              --------
    public void keepResponseHeader(String name, String value) {
        assertArgumentNotNull("name", name);
        if (responseHeaderMap == null) {
            responseHeaderMap = new LinkedHashMap<String, Object>();
        }
        addHierarchalMapElement(responseHeaderMap, name, value);
    }

    public void keepResponseBody(String responseBodyContent, String responseBodyType) { // accept null just in case
        assertArgumentNotNull("responseBodyType", responseBodyType);
        this.responseBodyContent = responseBodyContent;
        this.responseBodyType = responseBodyType;
    }

    public void keepHttpStatus(Integer httpStatus) {
        assertArgumentNotNull("httpStatus", httpStatus);
        this.httpStatus = httpStatus;
    }

    // -----------------------------------------------------
    //                                             Basic End
    //                                             ---------
    public void keepCause(RuntimeException cause) {
        assertArgumentNotNull("cause", cause);
        this.cause = cause;
    }

    public void keepEndDateTime(LocalDateTime endDateTime) {
        assertArgumentNotNull("endDateTime", endDateTime);
        this.endDateTime = endDateTime;
    }

    // ===================================================================================
    //                                                                        Small Helper
    //                                                                        ============
    protected void addHierarchalMapElement(Map<String, Object> map, String name, String value) {
        if (map.containsKey(name)) {
            final Object existing = map.get(name); // may be null value
            if (existing instanceof List<?>) {
                @SuppressWarnings("unchecked")
                final List<String> valueList = (List<String>) existing;
                valueList.add(value); // may be null value
            } else {
                final List<Object> valueList = new ArrayList<Object>();
                valueList.add(existing); // may be null value
                valueList.add(value); // may be null value
                map.put(name, valueList); // override as list
            }
        } else {
            map.put(name, value); // may be null value
        }
    }

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
    // -----------------------------------------------------
    //                                           Basic Begin
    //                                           -----------
    public OptionalThing<LocalDateTime> getBeginDateTime() {
        return OptionalThing.ofNullable(beginDateTime, () -> {
            throw new IllegalStateException("Not found the begin date-time.");
        });
    }

    public OptionalThing<Object> getFacadeExp() {
        return OptionalThing.ofNullable(facadeExp, () -> {
            throw new IllegalStateException("Not found the facade expression.");
        });
    }

    // -----------------------------------------------------
    //                                               Requset
    //                                               -------
    public Map<String, Object> getRequestHeaderMap() {
        return requestHeaderMap != null ? Collections.unmodifiableMap(requestHeaderMap) : Collections.emptyMap();
    }

    public Map<String, Object> getQueryParameterMap() {
        return queryParameterMap != null ? Collections.unmodifiableMap(queryParameterMap) : Collections.emptyMap();
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

    // -----------------------------------------------------
    //                                              Response
    //                                              --------
    public Map<String, Object> getResponseHeaderMap() {
        return responseHeaderMap != null ? Collections.unmodifiableMap(responseHeaderMap) : Collections.emptyMap();
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

    public OptionalThing<Integer> getHttpStatus() {
        return OptionalThing.ofNullable(httpStatus, () -> {
            throw new IllegalStateException("Not found the HTTP Status.");
        });
    }

    // -----------------------------------------------------
    //                                             Basic End
    //                                             ---------
    public OptionalThing<RuntimeException> getCause() {
        return OptionalThing.ofNullable(cause, () -> {
            throw new IllegalStateException("Not found the cause.");
        });
    }

    public OptionalThing<LocalDateTime> getEndDateTime() {
        return OptionalThing.ofNullable(endDateTime, () -> {
            throw new IllegalStateException("Not found the end date-time.");
        });
    }
}
