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
package org.dbflute.remoteapi.exception.retry;

import java.lang.reflect.Type;

import org.dbflute.optional.OptionalThing;
import org.dbflute.remoteapi.FlutyRemoteApiRule;
import org.dbflute.remoteapi.exception.RemoteApiHttpClientErrorException;
import org.dbflute.remoteapi.http.SupportedHttpMethod;

/**
 * @author jflute
 * @since 0.3.4 (2017/09/23 Sutarday)
 */
public class ClientErrorRetryResource {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Type returnType;
    protected final String urlBase;
    protected final String actionPath;
    protected final Object[] pathVariables;
    protected final OptionalThing<? extends Object> param;
    protected final FlutyRemoteApiRule rule; // only for changing status
    protected final SupportedHttpMethod httpMethod;
    protected final RemoteApiHttpClientErrorException clientError;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ClientErrorRetryResource(Type returnType, String urlBase, String actionPath, Object[] pathVariables,
            OptionalThing<? extends Object> param, FlutyRemoteApiRule rule, SupportedHttpMethod httpMethod,
            RemoteApiHttpClientErrorException clientError) {
        super();
        this.returnType = returnType;
        this.urlBase = urlBase;
        this.actionPath = actionPath;
        this.pathVariables = pathVariables;
        this.param = param;
        this.rule = rule;
        this.httpMethod = httpMethod;
        this.clientError = clientError;
    }

    // ===================================================================================
    //                                                                        Change State
    //                                                                        ============
    /**
     * Set header value by the name. <br>
     * It overwrites the same-name header if it already exists.
     * @param name The name of the header. (NotNull)
     * @param value The value of the header. (NotNull)
     */
    public void setHeader(String name, String value) {
        assertArgumentNotNull("name", name);
        assertArgumentNotNull("value", value);
        rule.setHeader(name, value);
    }

    /**
     * Add header value by the name. <br>
     * It is added as the second-or-more value if the name already exists.
     * @param name The name of the header. (NotNull)
     * @param value The value of the header, which may be as the second-or-more value. (NotNull)
     */
    public void addHeader(String name, String value) {
        assertArgumentNotNull("name", name);
        assertArgumentNotNull("value", value);
        rule.addHeader(name, value);
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
    public Type getReturnType() {
        return returnType;
    }

    public String getUrlBase() {
        return urlBase;
    }

    public String getActionPath() {
        return actionPath;
    }

    public Object[] getPathVariables() {
        return pathVariables;
    }

    public OptionalThing<? extends Object> getParam() {
        return param;
    }

    protected FlutyRemoteApiRule getRule() { // not public, has state so touch via facade methods
        return rule;
    }

    public SupportedHttpMethod getHttpMethod() {
        return httpMethod;
    }

    public RemoteApiHttpClientErrorException getClientError() {
        return clientError;
    }
}
