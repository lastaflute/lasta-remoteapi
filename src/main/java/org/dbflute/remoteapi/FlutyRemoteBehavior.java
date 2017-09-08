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
package org.dbflute.remoteapi;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.http.impl.client.CloseableHttpClient;
import org.dbflute.optional.OptionalThing;
import org.dbflute.util.DfCollectionUtil;

/**
 * The base class of behavior for remote API. <br>
 * Reference: <a href="http://dbflute.seasar.org/ja/lastaflute/howto/impldesign/jsondesign.html">JSON Design of JSON API</a>
 * @author awane
 * @author jflute
 * @author inoue
 */
public abstract class FlutyRemoteBehavior {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final Object[] EMPTY_PARAMS = new Object[] {};

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final FlutyRemoteApi remoteApi;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public FlutyRemoteBehavior() {
        this.remoteApi = createRemoteApi();
    }

    protected FlutyRemoteApi createRemoteApi() {
        return new FlutyRemoteApi(op -> prepareDefaultRuledRemoteApiOption(op), getClass());
    }

    protected void prepareDefaultRuledRemoteApiOption(FlutyRemoteApiOption option) {
        if (__xmockHttpClient != null) {
            option.xregisterMockHttpClient(__xmockHttpClient);
        }
        option.setHeader("User-Agent", buildUserAgent());
    }

    protected String buildUserAgent() {
        final List<String> wordList = DfCollectionUtil.newArrayList();
        final String serviceName = getUserAgentServiceName();
        if (serviceName != null) {
            wordList.add(serviceName);
        }
        final String appName = getUserAgentAppName();
        if (appName != null) {
            wordList.add(appName);
        }
        wordList.add(getClass().getSimpleName());
        return wordList.stream().collect(Collectors.joining("-"));
    }

    /**
     * @return The service name for user-agent. (NullAllowed: then no use)
     */
    protected abstract String getUserAgentServiceName();

    /**
     * @return The application name for user-agent. (NullAllowed: then no use)
     */
    protected abstract String getUserAgentAppName();

    // ===================================================================================
    //                                                                         Basic Parts
    //                                                                         ===========
    /**
     * Get the base string of URL for remote API server.
     * @return The base part of URL to remote API server. e.g. http://localhost:8090/harbor (NotNull)
     */
    protected abstract String getUrlBase();

    // ===================================================================================
    //                                                                       Remote Facade
    //                                                                       =============
    // -----------------------------------------------------
    //                                                  GET
    //                                                 -----
    /**
     * @param clazz The class of bean to convert, should have default constructor. (NotNull)
     * @param actionPath The path to action without URL parameter. e.g. /sea/land (NotNull)
     * @param pathVariables The array of URL path variables, e.g. ["hangar", 3]. (NotNull, EmptyAllowed)
     * @param queryForm The optional form of query (GET parameters). (NotNull, EmptyAllowed)
     * @param header The optional map of HTTP header. (NotNull, EmptyAllowed)
     * @return The JSON result of response as result, returned from the request. (NotNull)
     */
    protected <CONTENT extends Object> CONTENT doRequestGet(Class<? extends Object> clazz //
            , String actionPath, Object[] pathVariables, OptionalThing<Object> queryForm, Consumer<FlutyRemoteApiOption> opLambda) {
        return remoteApi.requestGet(clazz, getUrlBase(), actionPath, pathVariables, queryForm, opLambda);
    }

    /**
     * @param pt The parameterized type of bean to convert, should have default constructor. (NotNull)
     * @param actionPath The path to action without URL parameter. e.g. /sea/land (NotNull)
     * @param pathVariables The array of URL path variables, e.g. ["hangar", 3]. (NotNull, EmptyAllowed)
     * @param queryForm The optional form of query (GET parameters). (NotNull, EmptyAllowed)
     * @param header The optional map of HTTP header. (NotNull, EmptyAllowed)
     * @return The JSON result of response as result, returned from the request. (NotNull)
     */
    protected <CONTENT extends Object> CONTENT doRequestGet(ParameterizedType pt //
            , String actionPath, Object[] pathVariables, OptionalThing<Object> queryForm, Consumer<FlutyRemoteApiOption> opLambda) {
        return remoteApi.requestGet(pt, getUrlBase(), actionPath, pathVariables, queryForm, opLambda);
    }

    // -----------------------------------------------------
    //                                                 Post
    //                                                ------
    /**
     * @param clazz The class of bean to convert, should have default constructor. (NotNull)
     * @param actionPath The path to action without URL parameter. e.g. /sea/land (NotNull)
     * @param pathVariables The array of URL path variables, e.g. ["hangar", 3]. (NotNull, EmptyAllowed)
     * @param form The form of POST parameters. (NotNull)
     * @param header The optional map of HTTP header. (NotNull, EmptyAllowed)
     * @return The JSON result of response as result, returned from the request. (NotNull)
     */
    protected <CONTENT extends Object> CONTENT doRequestPost(Class<? extends Object> clazz //
            , String actionPath, Object[] pathVariables, Object form, Consumer<FlutyRemoteApiOption> opLambda) {
        return remoteApi.requestPost(clazz, getUrlBase(), actionPath, pathVariables, form, opLambda);
    }

    /**
     * @param pt The parameterized type of bean to convert, should have default constructor. (NotNull)
     * @param actionPath The path to action without URL parameter. e.g. /sea/land (NotNull)
     * @param pathVariables The array of URL path variables, e.g. ["hangar", 3]. (NotNull, EmptyAllowed)
     * @param form The form of POST parameters. (NotNull)
     * @param header The optional map of HTTP header. (NotNull, EmptyAllowed)
     * @return The JSON result of response as result, returned from the request. (NotNull)
     */
    protected <CONTENT extends Object> CONTENT doRequestPost(ParameterizedType pt //
            , String actionPath, Object[] pathVariables, Object form, Consumer<FlutyRemoteApiOption> opLambda) {
        return remoteApi.requestPost(pt, getUrlBase(), actionPath, pathVariables, form, opLambda);
    }

    // ===================================================================================
    //                                                                         For Testing
    //                                                                         ===========
    protected CloseableHttpClient __xmockHttpClient;

    public void xregisterMockHttpClient(CloseableHttpClient mockHttpClient) {
        this.__xmockHttpClient = mockHttpClient;
    }
}
