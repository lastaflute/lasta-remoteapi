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
import org.dbflute.remoteapi.exception.RemoteApiHttpClientErrorException;
import org.dbflute.remoteapi.http.SupportedHttpMethod;

/**
 * @author jflute
 * @since 0.3.4 (2017/09/23 Sutarday)
 */
public class ClientErrorRetryResource {

    protected final Type beanType;
    protected final String urlBase;
    protected final String actionPath;
    protected final Object[] pathVariables;
    protected final OptionalThing<? extends Object> param;
    protected final SupportedHttpMethod httpMethod;
    protected final RemoteApiHttpClientErrorException clientError;

    public ClientErrorRetryResource(Type beanType, String urlBase, String actionPath, Object[] pathVariables,
            OptionalThing<? extends Object> param, SupportedHttpMethod httpMethod, RemoteApiHttpClientErrorException clientError) {
        super();
        this.beanType = beanType;
        this.urlBase = urlBase;
        this.actionPath = actionPath;
        this.pathVariables = pathVariables;
        this.param = param;
        this.httpMethod = httpMethod;
        this.clientError = clientError;
    }

    public Type getBeanType() {
        return beanType;
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

    public SupportedHttpMethod getHttpMethod() {
        return httpMethod;
    }

    public RemoteApiHttpClientErrorException getClientError() {
        return clientError;
    }
}
