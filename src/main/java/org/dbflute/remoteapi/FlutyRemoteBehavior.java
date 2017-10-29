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
import org.dbflute.remoteapi.FlutyRemoteApi.EmptyRequestBody;
import org.dbflute.remoteapi.mock.MockHttpClient;
import org.dbflute.util.DfCollectionUtil;

/**
 * The base class of behavior for remote API.
 * @author awane
 * @author jflute
 * @author inoue
 */
public abstract class FlutyRemoteBehavior {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final Object[] EMPTY_OBJECTS = new Object[] {};
    protected static final EmptyRequestBody EMPTY_REQUEST_BODY = new EmptyRequestBody();

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
        return newRemoteApi(createRemoteApiOptionSetupper(), getFacadeExp());
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
    //                                     Facade Expression
    //                                     -----------------
    protected Object getFacadeExp() { // not null, for various purpose (basically debug)
        return getClass(); // as default
    }

    @Deprecated // use getFacadeExp()
    protected Object getCallerExp() { // not null, for various purpose (basically debug)
        return getFacadeExp(); // as default
    }

    // -----------------------------------------------------
    //                                    RemoteApi Instance
    //                                    ------------------
    protected FlutyRemoteApi newRemoteApi(Consumer<FlutyRemoteApiRule> ruleSetupper, Object facadeExp) {
        return new FlutyRemoteApi(ruleSetupper, facadeExp);
    }

    // ===================================================================================
    //                                                                         Basic Parts
    //                                                                         ===========
    /**
     * Get the base part of URL for remote API server. <br>
     * The string is from first to context path.
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
     * Request as GET, receiving as simple bean type.
     * <pre>
     * e.g. if sender, receiver are already set as default: /lido/product/list/7?sea=mystic&amp;land=oneman
     *  return doRequestGet(RemoteProductListReturn.class, "/lido/product/list", moreUrl(7), OptionalThing.of(form), rule -&gt; {});
     *  
     * e.g. if sender, receiver are not set yet, so set them here: /lido/product/list/7?sea=mystic&amp;land=oneman
     *  return doRequestGet(RemoteProductListReturn.class, "/lido/product/list", moreUrl(7), OptionalThing.of(form), rule -&gt; {
     *      rule.sendQueryBy(new LaQuerySender(...));
     *      rule.receiveBodyBy(new LaJsonReceiver(...));
     *  });
     * </pre>
     * @param <RETURN> The type of response return.
     * @param returnType The class type of bean as return (response body), should have default constructor. (NotNull)
     * @param actionPath The path to action without URL parameter. e.g. /sea/land (NotNull)
     * @param pathVariables The array of URL path variables, e.g. ["hangar", 3]. (NotNull, EmptyAllowed)
     * @param param The optional parameter object of query (GET parameters). (NotNull, EmptyAllowed)
     * @param ruleLambda The callback for rule of remote API. (NotNull)
     * @return The analyzed return of response from the request. (NotNull)
     */
    protected <RETURN> RETURN doRequestGet(Class<? extends RETURN> returnType //
            , String actionPath, Object[] pathVariables, OptionalThing<? extends Object> param, Consumer<FlutyRemoteApiRule> ruleLambda) {
        return remoteApi.requestGet(returnType, getUrlBase(), actionPath, pathVariables, param, ruleLambda);
    }

    /**
     * Request as GET, receiving as parameterized type (has nested generics).
     * <pre>
     * e.g. if sender, receiver are already set as default: /lido/product/list/7?sea=mystic&amp;land=oneman
     *  return doRequestGet(new ParameterizedRef&lt;RemoteSearchPagingReturn&lt;RemoteProductRowReturn&gt;&gt;() {
     *  }.getType(), "/lido/product/list", moreUrl(7), OptionalThing.of(form), rule -&gt; {});
     *  
     * e.g. if sender, receiver are not set yet, so set them here: /lido/product/list/7?sea=mystic&amp;land=oneman
     *  return doRequestGet(new ParameterizedRef&lt;RemoteSearchPagingReturn&lt;RemoteProductRowReturn&gt;&gt;() {
     *  }.getType(), "/lido/product/list", moreUrl(7), OptionalThing.of(form), rule -&gt; {
     *      rule.sendQueryBy(new LaQuerySender(...));
     *      rule.receiveBodyBy(new LaJsonReceiver(...));
     *  });
     * </pre>
     * @param <RETURN> The type of response return.
     * @param returnType The parameterized type of bean as return (response body), should have default constructor. (NotNull)
     * @param actionPath The path to action without URL parameter. e.g. /sea/land (NotNull)
     * @param pathVariables The array of URL path variables, e.g. ["hangar", 3]. (NotNull, EmptyAllowed)
     * @param param The optional parameter object of query (GET parameters). (NotNull, EmptyAllowed)
     * @param ruleLambda The callback for rule of remote API. (NotNull)
     * @return The analyzed return of response from the request. (NotNull)
     */
    protected <RETURN> RETURN doRequestGet(ParameterizedType returnType //
            , String actionPath, Object[] pathVariables, OptionalThing<? extends Object> param, Consumer<FlutyRemoteApiRule> ruleLambda) {
        return remoteApi.requestGet(returnType, getUrlBase(), actionPath, pathVariables, param, ruleLambda);
    }

    // -----------------------------------------------------
    //                                                 Post
    //                                                ------
    /**
     * Request as POST, receiving as simple bean type.
     * <pre>
     * e.g. if sender, receiver are already set as default: /lido/product/list/7
     *  return doRequestPost(RemoteProductListReturn.class, "/lido/product/list", moreUrl(7), body, rule -&gt; {});
     *  
     * e.g. if sender, receiver are not set yet, so set them here: /lido/product/list/7
     *  return doRequestPost(RemoteProductListReturn.class, "/lido/product/list", moreUrl(7), body, rule -&gt; {
     *      rule.sendBodyBy(new LaJsonSender(...));
     *      rule.receiveBodyBy(new LaJsonReceiver(...));
     *  });
     * </pre>
     * @param <RETURN> The type of response return.
     * @param returnType The class type of bean as return (response body), should have default constructor. (NotNull)
     * @param actionPath The path to action without URL parameter. e.g. /sea/land (NotNull)
     * @param pathVariables The array of URL path variables, e.g. ["hangar", 3]. (NotNull, EmptyAllowed)
     * @param param The parameter object of POST parameters, may be JSON body. (NotNull)
     * @param ruleLambda The callback for rule of remote API. (NotNull)
     * @return The analyzed return of response from the request. (NotNull)
     */
    protected <RETURN> RETURN doRequestPost(Class<? extends RETURN> returnType //
            , String actionPath, Object[] pathVariables, Object param, Consumer<FlutyRemoteApiRule> ruleLambda) {
        return remoteApi.requestPost(returnType, getUrlBase(), actionPath, pathVariables, param, ruleLambda);
    }

    /**
     * Request as POST, receiving as parameterized type (has nested generics).
     * <pre>
     * e.g. if sender, receiver are already set as default: /lido/product/list/7
     *  return doRequestPost(new ParameterizedRef&lt;RemoteSearchPagingReturn&lt;RemoteProductRowReturn&gt;&gt;() {
     *  }.getType(), "/lido/product/list", moreUrl(7), body, rule -&gt; {});
     *  
     * e.g. if sender, receiver are not set yet, so set them here: /lido/product/list/7
     *  return doRequestPost(new ParameterizedRef&lt;RemoteSearchPagingReturn&lt;RemoteProductRowReturn&gt;&gt;() {
     *  }.getType(), "/lido/product/list", moreUrl(7), body, rule -&gt; {
     *      rule.sendBodyBy(new LaJsonSender(...));
     *      rule.receiveBodyBy(new LaJsonReceiver(...));
     *  });
     * </pre>
     * @param <RETURN> The type of response return.
     * @param returnType The parameterized type of bean as return (response body), should have default constructor. (NotNull)
     * @param actionPath The path to action without URL parameter. e.g. /sea/land (NotNull)
     * @param pathVariables The array of URL path variables, e.g. ["hangar", 3]. (NotNull, EmptyAllowed)
     * @param param The parameter object of POST parameters, may be JSON body. (NotNull)
     * @param ruleLambda The callback for rule of remote API. (NotNull)
     * @return The analyzed return of response from the request. (NotNull)
     */
    protected <RETURN> RETURN doRequestPost(ParameterizedType returnType //
            , String actionPath, Object[] pathVariables, Object param, Consumer<FlutyRemoteApiRule> ruleLambda) {
        return remoteApi.requestPost(returnType, getUrlBase(), actionPath, pathVariables, param, ruleLambda);
    }

    // -----------------------------------------------------
    //                                                  Put
    //                                                 -----
    /**
     * Request as PUT, receiving as simple bean type.
     * <pre>
     * e.g. if sender, receiver are already set as default: /lido/product/list/7
     *  return doRequestPut(RemoteProductListReturn.class, "/lido/product/list", moreUrl(7), body, rule -&gt; {});
     *  
     * e.g. if sender, receiver are not set yet, so set them here: /lido/product/list/7
     *  return doRequestPut(RemoteProductListReturn.class, "/lido/product/list", moreUrl(7), body, rule -&gt; {
     *      rule.sendBodyBy(new LaJsonSender(...));
     *      rule.receiveBodyBy(new LaJsonReceiver(...));
     *  });
     * </pre>
     * @param <RETURN> The type of response return.
     * @param returnType The class type of bean as return (response body), should have default constructor. (NotNull)
     * @param actionPath The path to action without URL parameter. e.g. /sea/land (NotNull)
     * @param pathVariables The array of URL path variables, e.g. ["hangar", 3]. (NotNull, EmptyAllowed)
     * @param param The parameter object of PUT parameters, may be JSON body. (NotNull)
     * @param ruleLambda The callback for rule of remote API. (NotNull)
     * @return The analyzed return of response from the request. (NotNull)
     */
    protected <RETURN> RETURN doRequestPut(Class<? extends RETURN> returnType //
            , String actionPath, Object[] pathVariables, Object param, Consumer<FlutyRemoteApiRule> ruleLambda) {
        return remoteApi.requestPut(returnType, getUrlBase(), actionPath, pathVariables, param, ruleLambda);
    }

    /**
     * Request as PUT, receiving as parameterized type (has nested generics).
     * <pre>
     * e.g. if sender, receiver are already set as default: /lido/product/list/7
     *  return doRequestPut(new ParameterizedRef&lt;RemoteSearchPagingReturn&lt;RemoteProductRowReturn&gt;&gt;() {
     *  }.getType(), "/lido/product/list", moreUrl(7), body, rule -&gt; {});
     *  
     * e.g. if sender, receiver are not set yet, so set them here: /lido/product/list/7
     *  return doRequestPut(new ParameterizedRef&lt;RemoteSearchPagingReturn&lt;RemoteProductRowReturn&gt;&gt;() {
     *  }.getType(), "/lido/product/list", moreUrl(7), body, rule -&gt; {
     *      rule.sendBodyBy(new LaJsonSender(...));
     *      rule.receiveBodyBy(new LaJsonReceiver(...));
     *  });
     * </pre>
     * @param <RETURN> The type of response return.
     * @param returnType The parameterized type of bean as return (response body), should have default constructor. (NotNull)
     * @param actionPath The path to action without URL parameter. e.g. /sea/land (NotNull)
     * @param pathVariables The array of URL path variables, e.g. ["hangar", 3]. (NotNull, EmptyAllowed)
     * @param param The parameter object of PUT parameters, may be JSON body. (NotNull)
     * @param ruleLambda The callback for rule of remote API. (NotNull)
     * @return The analyzed return of response from the request. (NotNull)
     */
    protected <RETURN> RETURN doRequestPut(ParameterizedType returnType //
            , String actionPath, Object[] pathVariables, Object param, Consumer<FlutyRemoteApiRule> ruleLambda) {
        return remoteApi.requestPut(returnType, getUrlBase(), actionPath, pathVariables, param, ruleLambda);
    }

    // -----------------------------------------------------
    //                                                DELETE
    //                                                ------
    /**
     * Request as DELETE, receiving as simple bean type.
     * <pre>
     * e.g. if sender, receiver are already set as default: /lido/product/list/7
     *  return doRequestDelete(RemoteProductListReturn.class, "/lido/product/list", moreUrl(7), rule -&gt; {});
     *  
     * e.g. if sender, receiver are not set yet, so set them here: /lido/product/list/7
     *  return doRequestDelete(RemoteProductListReturn.class, "/lido/product/list", moreUrl(7), rule -&gt; {
     *      rule.sendQueryBy(new LaQuerySender(...));
     *      rule.receiveBodyBy(new LaJsonReceiver(...));
     *  });
     * </pre>
     * @param <RETURN> The type of response return.
     * @param returnType The class type of bean as return (response body), should have default constructor. (NotNull)
     * @param actionPath The path to action without URL parameter. e.g. /sea/land (NotNull)
     * @param pathVariables The array of URL path variables, e.g. ["hangar", 3]. (NotNull, EmptyAllowed)
     * @param param The optional paramter object of query (GET parameters). (NotNull, EmptyAllowed)
     * @param ruleLambda The callback for rule of remote API. (NotNull)
     * @return The analyzed return of response from the request. (NotNull)
     */
    protected <RETURN> RETURN doRequestDelete(Class<? extends RETURN> returnType //
            , String actionPath, Object[] pathVariables, OptionalThing<? extends Object> param, Consumer<FlutyRemoteApiRule> ruleLambda) {
        return remoteApi.requestDelete(returnType, getUrlBase(), actionPath, pathVariables, param, ruleLambda);
    }

    /**
     * Request as POST, receiving as parameterized type (has nested generics).
     * <pre>
     * e.g. if sender, receiver are already set as default: /lido/product/list/7
     *  return doRequestDelete(new ParameterizedRef&lt;RemoteSearchPagingReturn&lt;RemoteProductRowReturn&gt;&gt;() {
     *  }.getType(), "/lido/product/list", moreUrl(7), rule -&gt; {});
     *  
     * e.g. if sender, receiver are not set yet, so set them here: /lido/product/list/7
     *  return doRequestDelete(new ParameterizedRef&lt;RemoteSearchPagingReturn&lt;RemoteProductRowReturn&gt;&gt;() {
     *  }.getType(), "/lido/product/list", moreUrl(7), rule -&gt; {
     *      rule.sendQueryBy(new LaQuerySender(...));
     *      rule.receiveBodyBy(new LaJsonReceiver(...));
     *  });
     * </pre>
     * @param <RETURN> The type of response return.
     * @param returnType The parameterized type of bean as return (response body), should have default constructor. (NotNull)
     * @param actionPath The path to action without URL parameter. e.g. /sea/land (NotNull)
     * @param pathVariables The array of URL path variables, e.g. ["hangar", 3]. (NotNull, EmptyAllowed)
     * @param param The optional parameter object of query (GET parameters). (NotNull, EmptyAllowed)
     * @param ruleLambda The callback for rule of remote API. (NotNull)
     * @return The analyzed return of response from the request. (NotNull)
     */
    protected <RETURN> RETURN doRequestDelete(ParameterizedType returnType //
            , String actionPath, Object[] pathVariables, OptionalThing<? extends Object> param, Consumer<FlutyRemoteApiRule> ruleLambda) {
        return remoteApi.requestDelete(returnType, getUrlBase(), actionPath, pathVariables, param, ruleLambda);
    }

    // -----------------------------------------------------
    //                                                 Patch
    //                                                 -----
    /**
     * Request as PATCH, receiving as simple bean type.
     * <pre>
     * e.g. if sender, receiver are already set as default: /lido/product/list/7
     *  return doRequestPatch(RemoteProductListReturn.class, "/lido/product/list", moreUrl(7), body, rule -&gt; {});
     *  
     * e.g. if sender, receiver are not set yet, so set them here: /lido/product/list/7
     *  return doRequestPatch(RemoteProductListReturn.class, "/lido/product/list", moreUrl(7), body, rule -&gt; {
     *      rule.sendBodyBy(new LaJsonSender(...));
     *      rule.receiveBodyBy(new LaJsonReceiver(...));
     *  });
     * </pre>
     * @param <RETURN> The type of response return.
     * @param returnType The class type of bean as return (response body), should have default constructor. (NotNull)
     * @param actionPath The path to action without URL parameter. e.g. /sea/land (NotNull)
     * @param pathVariables The array of URL path variables, e.g. ["hangar", 3]. (NotNull, EmptyAllowed)
     * @param param The parameter object of PATCH parameters, may be JSON body. (NotNull)
     * @param ruleLambda The callback for rule of remote API. (NotNull)
     * @return The analyzed return of response from the request. (NotNull)
     */
    protected <RETURN> RETURN doRequestPatch(Class<? extends RETURN> returnType //
            , String actionPath, Object[] pathVariables, Object param, Consumer<FlutyRemoteApiRule> ruleLambda) {
        return remoteApi.requestPatch(returnType, getUrlBase(), actionPath, pathVariables, param, ruleLambda);
    }

    /**
     * Request as PATCH, receiving as parameterized type (has nested generics).
     * <pre>
     * e.g. if sender, receiver are already set as default: /lido/product/list/7
     *  return doRequestPatch(new ParameterizedRef&lt;RemoteSearchPagingReturn&lt;RemoteProductRowReturn&gt;&gt;() {
     *  }.getType(), "/lido/product/list", moreUrl(7), body, rule -&gt; {});
     *  
     * e.g. if sender, receiver are not set yet, so set them here: /lido/product/list/7
     *  return doRequestPatch(new ParameterizedRef&lt;RemoteSearchPagingReturn&lt;RemoteProductRowReturn&gt;&gt;() {
     *  }.getType(), "/lido/product/list", moreUrl(7), body, rule -&gt; {
     *      rule.sendBodyBy(new LaJsonSender(...));
     *      rule.receiveBodyBy(new LaJsonReceiver(...));
     *  });
     * </pre>
     * @param <RETURN> The type of response return.
     * @param returnType The parameterized type of bean as return (response body), should have default constructor. (NotNull)
     * @param actionPath The path to action without URL parameter. e.g. /sea/land (NotNull)
     * @param pathVariables The array of URL path variables, e.g. ["hangar", 3]. (NotNull, EmptyAllowed)
     * @param param The parameter object of PATCH parameters, may be JSON body. (NotNull)
     * @param ruleLambda The callback for rule of remote API. (NotNull)
     * @return The analyzed return of response from the request. (NotNull)
     */
    protected <RETURN> RETURN doRequestPatch(ParameterizedType returnType //
            , String actionPath, Object[] pathVariables, Object param, Consumer<FlutyRemoteApiRule> ruleLambda) {
        return remoteApi.requestPatch(returnType, getUrlBase(), actionPath, pathVariables, param, ruleLambda);
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    /**
     * Make array for path variables. (easy utility)
     * <pre>
     * return doRequestPost(..., "/lido/product/list", <span style="color: #CC4747">moreUrl(7)</span>, body, rule -&gt; {});
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

    /**
     * Get empty object array for path variables.
     * <pre>
     * return doRequestGet(..., "/lido/mypage", <span style="color: #CC4747">noMoreUrl()</span>, noQuery(), rule -&gt; {});
     * </pre>
     * @return The array of object as empty. (NotNull)
     */
    protected Object[] noMoreUrl() {
        return EMPTY_OBJECTS;
    }

    /**
     * Get present optional for query form.
     * <pre>
     * return doRequestGet(..., "/lido/mypage", noMoreUrl(), <span style="color: #CC4747">query(param)</span>, rule -&gt; {});
     * </pre>
     * @param <PARAM> The type of query parrameter.
     * @param param The parameter object for query parameter. (NotNull)
     * @return The optional object as present. (NotNull)
     */
    protected <PARAM> OptionalThing<PARAM> query(PARAM param) {
        if (param == null) {
            throw new IllegalArgumentException("The argument 'param' should not be null.");
        }
        return OptionalThing.of(param);
    }

    /**
     * Get empty optional for query form.
     * <pre>
     * return doRequestGet(..., "/lido/mypage", noMoreUrl(), <span style="color: #CC4747">noQuery()</span>, rule -&gt; {});
     * </pre>
     * @return The optional object as empty. (NotNull)
     */
    protected OptionalThing<Object> noQuery() {
        return OptionalThing.empty();
    }

    /**
     * Get empty object for POST/PUT form.
     * <pre>
     * return doRequestPost(..., "/lido/mypage", noMoreUrl(), <span style="color: #CC4747">noRequestBody()</span>, rule -&gt; {});
     * </pre>
     * @return The object as empty. (NotNull)
     */
    protected EmptyRequestBody noRequestBody() {
        return EMPTY_REQUEST_BODY;
    }

    // ===================================================================================
    //                                                                        For UnitTest
    //                                                                        ============
    /** The mock of HTTP client. This is only for unit test. Don't use in main code. (NullAllowed) */
    protected CloseableHttpClient __xmockHttpClient;

    /**
     * This is only for unit test. Don't use in main code. <br>
     * The mock can be injected in inject().
     * @param mockHttpClient The mock of HTTP client. (NullAllowed) 
     */
    public void setMockHttpClient(MockHttpClient mockHttpClient) {
        this.__xmockHttpClient = mockHttpClient;
    }
}
