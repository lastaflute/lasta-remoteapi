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
    protected static final Object[] EMPTY_OBJECTS = new Object[] {};

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
        return newRemoteApi(createRemoteApiOptionSetupper(), getCallerExp());
    }

    // -----------------------------------------------------
    //                                       Option Setupper
    //                                       ---------------
    protected Consumer<FlutyRemoteApiRule> createRemoteApiOptionSetupper() {
        return op -> setupDefaultRemoteApiRule(op);
    }

    protected void setupDefaultRemoteApiRule(FlutyRemoteApiRule rule) {
        reflectMockHttpClientIfNeeds(rule);
        if (isUseApplicationalUserAgent()) {
            rule.setHeader("User-Agent", buildApplicationalUserAgent());
        }
        yourDefaultRule(rule); // you can override default rules
    }

    protected void reflectMockHttpClientIfNeeds(FlutyRemoteApiRule rule) {
        if (__xmockHttpClient != null) {
            rule.xregisterMockHttpClient(__xmockHttpClient);
        }
    }

    // -----------------------------------------------------
    //                               Applicational UserAgent
    //                               -----------------------
    protected boolean isUseApplicationalUserAgent() {
        return false; // as default, for security
    }

    protected String buildApplicationalUserAgent() { // basically for internal remote AIP
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
    protected String getUserAgentServiceName() {
        return null; // as default
    }

    /**
     * @return The application name for user-agent. (NullAllowed: then no use)
     */
    protected String getUserAgentAppName() {
        return null; // as default
    }

    // -----------------------------------------------------
    //                                             Your Rule
    //                                             ---------
    /**
     * Set up your default rule of remote API.
     * <pre>
     * rule.sendQueryBy(new FlQuerySender(...));
     * rule.sendBodyBy(new FlJsonSender(...));
     * rule.receiveBodyBy(new FlJsonReceiver(...));
     * </pre>
     * @param rule The rule of remote API. (NotNull)
     */
    protected abstract void yourDefaultRule(FlutyRemoteApiRule rule);

    // -----------------------------------------------------
    //                                     Caller Expression
    //                                     -----------------
    protected Object getCallerExp() { // not null, for various purpose (basically debug)
        return getClass(); // as default
    }

    // -----------------------------------------------------
    //                                    RemoteApi Instance
    //                                    ------------------
    protected FlutyRemoteApi newRemoteApi(Consumer<FlutyRemoteApiRule> ruleSetupper, Object callerExp) {
        return new FlutyRemoteApi(ruleSetupper, callerExp);
    }

    // ===================================================================================
    //                                                                         Basic Parts
    //                                                                         ===========
    /**
     * Get the base part of URL for remote API server. <br>
     * The string is until context path.
     * @return The base part of URL. e.g. http://localhost:8090/harbor (NotNull)
     */
    protected abstract String getUrlBase();

    // ===================================================================================
    //                                                                       Remote Facade
    //                                                                       =============
    // -----------------------------------------------------
    //                                                  GET
    //                                                 -----
    /**
     * Request as POST, receiving as simple bean type.
     * <pre>
     * e.g. if sender, receiver are already set as default: /lido/product/list/7
     *  return doRequestGet(HbProductListResult.class, "/lido/product/list", moreUrl(7), OptionalThing.of(form), rule -&gt; {});
     *  
     * e.g. if sender, receiver are not set yet, so set them here: /lido/product/list/7
     *  return doRequestGet(HbProductListResult.class, "/lido/product/list", moreUrl(7), OptionalThing.of(form), rule -&gt; {
     *      rule.sendQueryBy(new LaQuerySender(...));
     *      rule.receiveBodyBy(new LaJsonReceiver(...));
     *  });
     * </pre>
     * @param <RESULT> The type of request result (response).
     * @param beanType The class type of bean to convert, should have default constructor. (NotNull)
     * @param actionPath The path to action without URL parameter. e.g. /sea/land (NotNull)
     * @param pathVariables The array of URL path variables, e.g. ["hangar", 3]. (NotNull, EmptyAllowed)
     * @param queryForm The optional form of query (GET parameters). (NotNull, EmptyAllowed)
     * @param ruleLambda The callback for rule of remote API. (NotNull)
     * @return The JSON result of response as result, returned from the request. (NotNull)
     */
    protected <RESULT extends Object> RESULT doRequestGet(Class<? extends Object> beanType //
            , String actionPath, Object[] pathVariables, OptionalThing<Object> queryForm, Consumer<FlutyRemoteApiRule> ruleLambda) {
        return remoteApi.requestGet(beanType, getUrlBase(), actionPath, pathVariables, queryForm, ruleLambda);
    }

    /**
     * Request as POST, receiving as parameterized type (has nested generics).
     * <pre>
     * e.g. if sender, receiver are already set as default: /lido/product/list/7
     *  return doRequestGet(new ParameterizedRef&lt;HbSearchPagingResult&lt;HbProductRowResult&gt;&gt;() {
     *  }.getType(), "/lido/product/list", moreUrl(7), OptionalThing.of(form), rule -&gt; {});
     *  
     * e.g. if sender, receiver are not set yet, so set them here: /lido/product/list/7
     *  return doRequestGet(new ParameterizedRef&lt;HbSearchPagingResult&lt;HbProductRowResult&gt;&gt;() {
     *  }.getType(), "/lido/product/list", moreUrl(7), OptionalThing.of(form), rule -&gt; {
     *      rule.sendQueryBy(new LaQuerySender(...));
     *      rule.receiveBodyBy(new LaJsonReceiver(...));
     *  });
     * </pre>
     * @param <RESULT> The type of request result (response).
     * @param beanType The parameterized type of bean to convert, should have default constructor. (NotNull)
     * @param actionPath The path to action without URL parameter. e.g. /sea/land (NotNull)
     * @param pathVariables The array of URL path variables, e.g. ["hangar", 3]. (NotNull, EmptyAllowed)
     * @param queryForm The optional form of query (GET parameters). (NotNull, EmptyAllowed)
     * @param ruleLambda The callback for rule of remote API. (NotNull)
     * @return The JSON result of response as result, returned from the request. (NotNull)
     */
    protected <RESULT extends Object> RESULT doRequestGet(ParameterizedType beanType //
            , String actionPath, Object[] pathVariables, OptionalThing<Object> queryForm, Consumer<FlutyRemoteApiRule> ruleLambda) {
        return remoteApi.requestGet(beanType, getUrlBase(), actionPath, pathVariables, queryForm, ruleLambda);
    }

    // -----------------------------------------------------
    //                                                 Post
    //                                                ------
    /**
     * Request as POST, receiving as simple bean type.
     * <pre>
     * e.g. if sender, receiver are already set as default: /lido/product/list/7
     *  return doRequestPost(HbProductListResult.class, "/lido/product/list", moreUrl(7), body, rule -&gt; {});
     *  
     * e.g. if sender, receiver are not set yet, so set them here: /lido/product/list/7
     *  return doRequestPost(HbProductListResult.class, "/lido/product/list", moreUrl(7), body, rule -&gt; {
     *      rule.sendBodyBy(new LaJsonSender(...));
     *      rule.receiveBodyBy(new LaJsonReceiver(...));
     *  });
     * </pre>
     * @param <RESULT> The type of request result (response).
     * @param beanType The class type of bean for response body, should have default constructor. (NotNull)
     * @param actionPath The path to action without URL parameter. e.g. /sea/land (NotNull)
     * @param pathVariables The array of URL path variables, e.g. ["hangar", 3]. (NotNull, EmptyAllowed)
     * @param form The form of POST parameters. (NotNull)
     * @param ruleLambda The callback for rule of remote API. (NotNull)
     * @return The JSON result of response as result, returned from the request. (NotNull)
     */
    protected <RESULT extends Object> RESULT doRequestPost(Class<? extends Object> beanType //
            , String actionPath, Object[] pathVariables, Object form, Consumer<FlutyRemoteApiRule> ruleLambda) {
        return remoteApi.requestPost(beanType, getUrlBase(), actionPath, pathVariables, form, ruleLambda);
    }

    /**
     * Request as POST, receiving as parameterized type (has nested generics).
     * <pre>
     * e.g. if sender, receiver are already set as default: /lido/product/list/7
     *  return doRequestPost(new ParameterizedRef&lt;HbSearchPagingResult&lt;HbProductRowResult&gt;&gt;() {
     *  }.getType(), "/lido/product/list", moreUrl(7), body, rule -&gt; {});
     *  
     * e.g. if sender, receiver are not set yet, so set them here: /lido/product/list/7
     *  return doRequestPost(new ParameterizedRef&lt;HbSearchPagingResult&lt;HbProductRowResult&gt;&gt;() {
     *  }.getType(), "/lido/product/list", moreUrl(7), body, rule -&gt; {
     *      rule.sendBodyBy(new LaJsonSender(...));
     *      rule.receiveBodyBy(new LaJsonReceiver(...));
     *  });
     * </pre>
     * @param <RESULT> The type of request result (response).
     * @param beanType The parameterized type of bean for response body, should have default constructor. (NotNull)
     * @param actionPath The path to action without URL parameter. e.g. /sea/land (NotNull)
     * @param pathVariables The array of URL path variables, e.g. ["hangar", 3]. (NotNull, EmptyAllowed)
     * @param form The form of POST parameters. (NotNull)
     * @param ruleLambda The callback for rule of remote API. (NotNull)
     * @return The JSON result of response as result, returned from the request. (NotNull)
     */
    protected <RESULT extends Object> RESULT doRequestPost(ParameterizedType beanType //
            , String actionPath, Object[] pathVariables, Object form, Consumer<FlutyRemoteApiRule> ruleLambda) {
        return remoteApi.requestPost(beanType, getUrlBase(), actionPath, pathVariables, form, ruleLambda);
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    /**
     * Make array for path variables. (easy utility)
     * <pre>
     * return doRequestPost(..., "/lido/product/list", moreUrl(7), body, rule -&gt; {});
     * </pre>
     * @param pathVariables The varying arguments for path variables. (NotNull)
     * @return The array of object. (NotNull)
     */
    protected Object[] moreUrl(Object... pathVariables) {
        if (pathVariables == null) {
            throw new IllegalArgumentException("The argument 'pathVariables' should not be null.");
        }
        return pathVariables;
    }

    // ===================================================================================
    //                                                                         For Testing
    //                                                                         ===========
    protected CloseableHttpClient __xmockHttpClient;

    // #hope jflute too easy, so want to switch other way...
    public void xregisterMockHttpClient(CloseableHttpClient mockHttpClient) {
        this.__xmockHttpClient = mockHttpClient;
    }
}
