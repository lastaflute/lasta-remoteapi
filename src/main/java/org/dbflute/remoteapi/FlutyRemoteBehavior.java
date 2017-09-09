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

    // -----------------------------------------------------
    //                                      Create RemoteApi
    //                                      ----------------
    protected FlutyRemoteApi createRemoteApi() {
        return newFlutyRemoteApi(prepareRemoteApiOptionSetupper(), getCallerExp());
    }

    // -----------------------------------------------------
    //                                       Option Setupper
    //                                       ---------------
    protected Consumer<FlutyRemoteApiOption> prepareRemoteApiOptionSetupper() {
        return op -> prepareDefaultRemoteApiOption(op);
    }

    protected void prepareDefaultRemoteApiOption(FlutyRemoteApiOption option) {
        reflectMockHttpClientIfNeeds(option);
        option.setHeader("User-Agent", buildUserAgent());
    }

    protected void reflectMockHttpClientIfNeeds(FlutyRemoteApiOption option) {
        if (__xmockHttpClient != null) {
            option.xregisterMockHttpClient(__xmockHttpClient);
        }
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

    // -----------------------------------------------------
    //                                     Caller Expression
    //                                     -----------------
    protected Object getCallerExp() {
        return getClass(); // as default
    }

    // -----------------------------------------------------
    //                                    RemoteApi Instance
    //                                    ------------------
    protected FlutyRemoteApi newFlutyRemoteApi(Consumer<FlutyRemoteApiOption> optionSetupper, Object callerExp) {
        return new FlutyRemoteApi(optionSetupper, callerExp);
    }

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
     * @param <RESULT> The type of request result.
     * @param beanType The class type of bean to convert, should have default constructor. (NotNull)
     * @param actionPath The path to action without URL parameter. e.g. /sea/land (NotNull)
     * @param pathVariables The array of URL path variables, e.g. ["hangar", 3]. (NotNull, EmptyAllowed)
     * @param queryForm The optional form of query (GET parameters). (NotNull, EmptyAllowed)
     * @param opLambda The callback for option of remote API. (NotNull)
     * @return The JSON result of response as result, returned from the request. (NotNull)
     */
    protected <RESULT extends Object> RESULT doRequestGet(Class<? extends Object> beanType //
            , String actionPath, Object[] pathVariables, OptionalThing<Object> queryForm, Consumer<FlutyRemoteApiOption> opLambda) {
        return remoteApi.requestGet(beanType, getUrlBase(), actionPath, pathVariables, queryForm, opLambda);
    }

    /**
     * @param <RESULT> The type of request result.
     * @param beanType The parameterized type of bean to convert, should have default constructor. (NotNull)
     * @param actionPath The path to action without URL parameter. e.g. /sea/land (NotNull)
     * @param pathVariables The array of URL path variables, e.g. ["hangar", 3]. (NotNull, EmptyAllowed)
     * @param queryForm The optional form of query (GET parameters). (NotNull, EmptyAllowed)
     * @param opLambda The callback for option of remote API. (NotNull)
     * @return The JSON result of response as result, returned from the request. (NotNull)
     */
    protected <RESULT extends Object> RESULT doRequestGet(ParameterizedType beanType //
            , String actionPath, Object[] pathVariables, OptionalThing<Object> queryForm, Consumer<FlutyRemoteApiOption> opLambda) {
        return remoteApi.requestGet(beanType, getUrlBase(), actionPath, pathVariables, queryForm, opLambda);
    }

    // -----------------------------------------------------
    //                                                 Post
    //                                                ------
    /**
     * @param <RESULT> The type of request result.
     * @param beanType The class type of bean to convert, should have default constructor. (NotNull)
     * @param actionPath The path to action without URL parameter. e.g. /sea/land (NotNull)
     * @param pathVariables The array of URL path variables, e.g. ["hangar", 3]. (NotNull, EmptyAllowed)
     * @param form The form of POST parameters. (NotNull)
     * @param opLambda The callback for option of remote API. (NotNull)
     * @return The JSON result of response as result, returned from the request. (NotNull)
     */
    protected <RESULT extends Object> RESULT doRequestPost(Class<? extends Object> beanType //
            , String actionPath, Object[] pathVariables, Object form, Consumer<FlutyRemoteApiOption> opLambda) {
        return remoteApi.requestPost(beanType, getUrlBase(), actionPath, pathVariables, form, opLambda);
    }

    /**
     * @param <RESULT> The type of request result.
     * @param beanType The parameterized type of bean to convert, should have default constructor. (NotNull)
     * @param actionPath The path to action without URL parameter. e.g. /sea/land (NotNull)
     * @param pathVariables The array of URL path variables, e.g. ["hangar", 3]. (NotNull, EmptyAllowed)
     * @param form The form of POST parameters. (NotNull)
     * @param opLambda The callback for option of remote API. (NotNull)
     * @return The JSON result of response as result, returned from the request. (NotNull)
     */
    protected <RESULT extends Object> RESULT doRequestPost(ParameterizedType beanType //
            , String actionPath, Object[] pathVariables, Object form, Consumer<FlutyRemoteApiOption> opLambda) {
        return remoteApi.requestPost(beanType, getUrlBase(), actionPath, pathVariables, form, opLambda);
    }

    // ===================================================================================
    //                                                                         For Testing
    //                                                                         ===========
    protected CloseableHttpClient __xmockHttpClient;

    public void xregisterMockHttpClient(CloseableHttpClient mockHttpClient) {
        this.__xmockHttpClient = mockHttpClient;
    }
}
