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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.dbflute.helper.function.IndependentProcessor;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.jdbc.Classification;
import org.dbflute.optional.OptionalThing;
import org.dbflute.remoteapi.exception.RemoteApiErrorTranslationFailureException;
import org.dbflute.remoteapi.exception.RemoteApiFailureResponseTypeNotFoundException;
import org.dbflute.remoteapi.exception.RemoteApiHttpBasisErrorException;
import org.dbflute.remoteapi.exception.RemoteApiHttpBasisErrorException.RemoteApiFailureResponseHolder;
import org.dbflute.remoteapi.exception.RemoteApiHttpClientErrorException;
import org.dbflute.remoteapi.exception.RemoteApiHttpServerErrorException;
import org.dbflute.remoteapi.exception.RemoteApiIOException;
import org.dbflute.remoteapi.exception.RemoteApiPathVariableNullElementException;
import org.dbflute.remoteapi.exception.RemoteApiPathVariableShortElementException;
import org.dbflute.remoteapi.exception.RemoteApiReceiverOfResponseBodyNotFoundException;
import org.dbflute.remoteapi.exception.RemoteApiResponseParseFailureException;
import org.dbflute.remoteapi.exception.RemoteApiRetryReadyFailureException;
import org.dbflute.remoteapi.exception.RemoteApiSenderOfQueryParameterNotFoundException;
import org.dbflute.remoteapi.exception.RemoteApiSenderOfRequestBodyNotFoundException;
import org.dbflute.remoteapi.exception.retry.ClientErrorRetryDeterminer;
import org.dbflute.remoteapi.exception.retry.ClientErrorRetryResource;
import org.dbflute.remoteapi.exception.translation.ClientErrorTranslatingResource;
import org.dbflute.remoteapi.http.SupportedHttpMethod;
import org.dbflute.remoteapi.logging.SendReceiveLogOption;
import org.dbflute.remoteapi.logging.SendReceiveLogger;
import org.dbflute.remoteapi.receiver.ResponseBodyReceiver;
import org.dbflute.remoteapi.sender.body.RequestBodySender;
import org.dbflute.remoteapi.sender.query.QueryParameterSender;
import org.dbflute.system.DBFluteSystem;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @author awaawa
 * @author inoue
 */
public class FlutyRemoteApi {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger logger = LoggerFactory.getLogger(FlutyRemoteApi.class);
    protected static final Object VOID_OBJ = new Object();

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Consumer<FlutyRemoteApiRule> defaultRuleLambda; // not null
    protected final Object facadeExp; // for various purpose, basically debug, not null

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public FlutyRemoteApi(Consumer<FlutyRemoteApiRule> defaultRuleLambda, Object facadeExp) {
        assertArgumentNotNull("defaultRuleLambda", defaultRuleLambda);
        assertArgumentNotNull("facadeExp", facadeExp);
        this.defaultRuleLambda = defaultRuleLambda;
        this.facadeExp = facadeExp;
    }

    // ===================================================================================
    //                                                                         Request GET
    //                                                                         ===========
    /**
     * @param <RETURN> The type of response return.
     * @param returnType The class type of bean as return (response body), should have default constructor. (NotNull)
     * @param urlBase The base part of URL to remote API server. e.g. http://localhost:8090/harbor (NotNull)
     * @param actionPath The path to action without URL parameter, and trailing slash is no difference. e.g. /sea/land (NotNull)
     * @param pathVariables The array of URL path variables, e.g. ["hangar", 3]. (NotNull, EmptyAllowed)
     * @param param The optional parameter object of query (GET parameters). (NotNull, EmptyAllowed)
     * @param ruleLambda The callback for rule of remote API. (NotNull)
     * @return The analyzed return of response from the request. (NotNull)
     */
    public <RETURN> RETURN requestGet(Type returnType, String urlBase, String actionPath, Object[] pathVariables,
            OptionalThing<? extends Object> param, Consumer<FlutyRemoteApiRule> ruleLambda) {
        return doRequestEmptyBody(returnType, urlBase, actionPath, pathVariables, param, ruleLambda, SupportedHttpMethod.GET, url -> {
            return new HttpGet(url);
        });
    }

    // ===================================================================================
    //                                                                        Request POST
    //                                                                        ============
    /**
     * @param <RETURN> The type of response return.(response).
     * @param returnType The class type of bean as return (response body), should have default constructor. (NotNull)
     * @param urlBase The base part of URL to remote API server. e.g. http://localhost:8090/harbor (NotNull)
     * @param actionPath The path to action without URL parameter, and trailing slash is no difference. e.g. /sea/land (NotNull)
     * @param pathVariables The array of URL path variables, e.g. ["hangar", 3]. (NotNull, EmptyAllowed)
     * @param param The parameter objet of POST parameters, may be JSON body. (NotNull)
     * @param ruleLambda The callback for rule of remote API. (NotNull)
     * @return The analyzed return of response from the request. (NotNull)
     */
    public <RETURN> RETURN requestPost(Type returnType, String urlBase, String actionPath, Object[] pathVariables, Object param,
            Consumer<FlutyRemoteApiRule> ruleLambda) {
        return doRequestEnclosing(returnType, urlBase, actionPath, pathVariables, param, ruleLambda, SupportedHttpMethod.POST, url -> {
            return new HttpPost(url);
        });
    }

    // ===================================================================================
    //                                                                         Request PUT
    //                                                                         ===========
    /**
     * @param <RETURN> The type of response return.
     * @param returnType The class type of bean as return (response body), should have default constructor. (NotNull)
     * @param urlBase The base part of URL to remote API server. e.g. http://localhost:8090/harbor (NotNull)
     * @param actionPath The path to action without URL parameter, and trailing slash is no difference. e.g. /sea/land (NotNull)
     * @param pathVariables The array of URL path variables, e.g. ["hangar", 3]. (NotNull, EmptyAllowed)
     * @param param The parameter object of PUT parameters, may be JSON body. (NotNull)
     * @param ruleLambda The callback for rule of remote API. (NotNull)
     * @return The analyzed return of response from the request. (NotNull)
     */
    public <RETURN> RETURN requestPut(Type returnType, String urlBase, String actionPath, Object[] pathVariables, Object param,
            Consumer<FlutyRemoteApiRule> ruleLambda) {
        return doRequestEnclosing(returnType, urlBase, actionPath, pathVariables, param, ruleLambda, SupportedHttpMethod.PUT, url -> {
            return new HttpPut(url);
        });
    }

    // ===================================================================================
    //                                                                      Request DELETE
    //                                                                      ==============
    /**
     * @param <RETURN> The type of response return.
     * @param returnType The class type of bean as return (response body), should have default constructor. (NotNull)
     * @param urlBase The base part of URL to remote API server. e.g. http://localhost:8090/harbor (NotNull)
     * @param actionPath The path to action without URL parameter, and trailing slash is no difference. e.g. /sea/land (NotNull)
     * @param pathVariables The array of URL path variables, e.g. ["hangar", 3]. (NotNull, EmptyAllowed)
     * @param param The optional parameter object of query (GET parameters). (NotNull, EmptyAllowed)
     * @param ruleLambda The callback for rule of remote API. (NotNull)
     * @return The analyzed return of response from the request. (NotNull)
     */
    public <RETURN> RETURN requestDelete(Type returnType, String urlBase, String actionPath, Object[] pathVariables,
            OptionalThing<? extends Object> param, Consumer<FlutyRemoteApiRule> ruleLambda) {
        return doRequestEmptyBody(returnType, urlBase, actionPath, pathVariables, param, ruleLambda, SupportedHttpMethod.DELETE, url -> {
            return new HttpDelete(url);
        });
    }

    // ===================================================================================
    //                                                                       Request PATCH
    //                                                                       =============
    /**
     * @param <RETURN> The type of response return.
     * @param returnType The class type of bean as return (response body), should have default constructor. (NotNull)
     * @param urlBase The base part of URL to remote API server. e.g. http://localhost:8090/harbor (NotNull)
     * @param actionPath The path to action without URL parameter, and trailing slash is no difference. e.g. /sea/land (NotNull)
     * @param pathVariables The array of URL path variables, e.g. ["hangar", 3]. (NotNull, EmptyAllowed)
     * @param param The parameter object of PATCH parameters, may be JSON body. (NotNull)
     * @param ruleLambda The callback for rule of remote API. (NotNull)
     * @return The analyzed return of response from the request. (NotNull)
     */
    public <RETURN> RETURN requestPatch(Type returnType, String urlBase, String actionPath, Object[] pathVariables, Object param,
            Consumer<FlutyRemoteApiRule> ruleLambda) {
        return doRequestEnclosing(returnType, urlBase, actionPath, pathVariables, param, ruleLambda, SupportedHttpMethod.PATCH, url -> {
            return new HttpPatch(url);
        });
    }

    // ===================================================================================
    //                                                                   Request EmptyBody
    //                                                                   =================
    protected <RETURN> RETURN doRequestEmptyBody(Type returnType, String urlBase, String actionPath, Object[] pathVariables,
            OptionalThing<? extends Object> optParam, Consumer<FlutyRemoteApiRule> ruleLambda, SupportedHttpMethod httpMethod,
            Function<String, HttpUriRequest> emptyBodyFactory) {
        assertArgumentNotNull("returnType", returnType);
        assertArgumentNotNull("urlBase", urlBase);
        assertArgumentNotNull("actionPath", actionPath);
        assertArgumentNotNull("pathVariables", pathVariables);
        assertArgumentNotNull("param", optParam); // variable name is for facade method
        assertArgumentNotNull("ruleLambda", ruleLambda);
        assertArgumentNotNull("httpMethod", httpMethod);
        assertArgumentNotNull("emptyBodyFactory", emptyBodyFactory);
        final FlutyRemoteApiRule rule = createRemoteApiRule(ruleLambda);
        keepBeginDateTimeIfNeeds(rule);
        keepFacadeExpIfNeeds(rule);
        return retryableRequest(returnType, urlBase, actionPath, pathVariables, optParam, rule, () -> {
            return actuallyRequestEmptyBody(returnType, urlBase, actionPath, pathVariables, optParam, rule, httpMethod, emptyBodyFactory);
        }, clientError -> {
            return createClientErrorRetryResource(returnType, urlBase, actionPath, pathVariables, optParam, rule, httpMethod, clientError);
        });
    }

    protected <RETURN> RETURN actuallyRequestEmptyBody(Type returnType, String urlBase, String actionPath, Object[] pathVariables,
            OptionalThing<? extends Object> optParam, FlutyRemoteApiRule rule, SupportedHttpMethod httpMethod,
            Function<String, HttpUriRequest> emptyBodyFactory) {
        optParam.ifPresent(param -> validateParam(returnType, urlBase, actionPath, pathVariables, param, rule));
        final String requestPath = buildRequestPath(returnType, urlBase, actionPath, pathVariables, optParam, rule);
        final String url = buildUrl(returnType, urlBase, requestPath, optParam, rule);
        showBeginEmptyBody(rule, httpMethod, url);
        return delegateExecute(httpMethod, requestPath, rule, () -> {
            return executeEmptyBody(returnType, url, rule, httpMethod, emptyBodyFactory);
        });
    }

    protected void showBeginEmptyBody(FlutyRemoteApiRule rule, SupportedHttpMethod httpMethod, final String url) {
        if (!logger.isDebugEnabled()) {
            return;
        }
        final Map<String, List<String>> headerMap = rule.getHeaders().orElseGet(() -> Collections.emptyMap());
        logger.debug("#flow #remote ...Sending request as {} to Remote API:\n{}\n with headers: {}", httpMethod, url, headerMap);
    }

    protected <RETURN> RETURN executeEmptyBody(Type returnType, String url, FlutyRemoteApiRule rule, SupportedHttpMethod httpMethod,
            Function<String, HttpUriRequest> emptyBodyFactory) {
        try (CloseableHttpClient httpClient = buildHttpClient(rule)) {
            final HttpUriRequest httpEmptyBody = prepareHttpEmptyBody(url, rule, httpMethod, emptyBodyFactory);
            try (CloseableHttpResponse response = httpClient.execute(httpEmptyBody)) {
                return handleResponse(returnType, url, /*param*/OptionalThing.empty(), response, rule);
            }
        } catch (IOException e) {
            handleRemoteApiIOException(returnType, url, /*param*/OptionalThing.empty(), e);
            return null; // unreachable
        }
    }

    protected HttpUriRequest prepareHttpEmptyBody(String url, FlutyRemoteApiRule rule, SupportedHttpMethod httpMethod,
            Function<String, HttpUriRequest> emptyBodyFactory) {
        final HttpUriRequest httpEmptyBody = emptyBodyFactory.apply(url);
        setupHeader(httpEmptyBody, rule);
        return httpEmptyBody;
    }

    // ===================================================================================
    //                                                                   Request Enclosing
    //                                                                   =================
    protected <RETURN> RETURN doRequestEnclosing(Type returnType, String urlBase, String actionPath, Object[] pathVariables, Object param,
            Consumer<FlutyRemoteApiRule> ruleLambda, SupportedHttpMethod httpMethod,
            Function<String, HttpEntityEnclosingRequestBase> enclosingFactory) {
        assertArgumentNotNull("returnType", returnType);
        assertArgumentNotNull("urlBase", urlBase);
        assertArgumentNotNull("actionPath", actionPath);
        assertArgumentNotNull("pathVariables", pathVariables);
        assertArgumentNotNull("param", param);
        assertArgumentNotNull("ruleLambda", ruleLambda);
        assertArgumentNotNull("httpMethod", httpMethod);
        assertArgumentNotNull("enclosingFactory", enclosingFactory);
        final FlutyRemoteApiRule rule = createRemoteApiRule(ruleLambda);
        keepBeginDateTimeIfNeeds(rule);
        keepFacadeExpIfNeeds(rule);
        return retryableRequest(returnType, urlBase, actionPath, pathVariables, param, rule, () -> {
            return actuallyRequestEnclosing(returnType, urlBase, actionPath, pathVariables, param, rule, httpMethod, enclosingFactory);
        }, clientError -> {
            final OptionalThing<Object> optParam = OptionalThing.of(param);
            return createClientErrorRetryResource(returnType, urlBase, actionPath, pathVariables, optParam, rule, httpMethod, clientError);
        });
    }

    protected <RETURN> RETURN actuallyRequestEnclosing(Type returnType, String urlBase, String actionPath, Object[] pathVariables,
            Object param, FlutyRemoteApiRule rule, SupportedHttpMethod httpMethod,
            Function<String, HttpEntityEnclosingRequestBase> enclosingFactory) {
        validateParam(returnType, urlBase, actionPath, pathVariables, param, rule);
        final OptionalThing<? extends Object> queryParam = OptionalThing.empty();
        final String requestPath = buildRequestPath(returnType, urlBase, actionPath, pathVariables, queryParam, rule);
        final String url = buildUrl(returnType, urlBase, requestPath, queryParam, rule);
        showBeginRequestEnclosing(param, rule, httpMethod, url);
        return delegateExecute(httpMethod, requestPath, rule, () -> {
            return executeEnclosing(returnType, url, param, rule, httpMethod, enclosingFactory);
        });
    }

    protected void showBeginRequestEnclosing(Object param, FlutyRemoteApiRule rule, SupportedHttpMethod httpMethod, final String url) {
        if (!logger.isDebugEnabled()) {
            return;
        }
        final String paramDisp = param.getClass().getSimpleName() + ":" + convertBeanToDebugString(param); // because toString() might not be overridden
        final Map<String, List<String>> headerMap = rule.getHeaders().orElseGet(() -> Collections.emptyMap());
        logger.debug("#flow #remote ...Sending request as {} to Remote API:\n{}\n with param: {}\n with headers: {}", httpMethod, url,
                paramDisp, headerMap);
    }

    protected <RETURN> RETURN executeEnclosing(Type returnType, String url, Object param, FlutyRemoteApiRule rule,
            SupportedHttpMethod httpMethod, Function<String, HttpEntityEnclosingRequestBase> enclosingFactory) {
        try (CloseableHttpClient httpClient = buildHttpClient(rule)) {
            final HttpUriRequest httpEnclosing = prepareHttpEnclosing(returnType, url, param, rule, httpMethod, enclosingFactory);
            try (CloseableHttpResponse response = httpClient.execute(httpEnclosing)) {
                return handleResponse(returnType, url, OptionalThing.of(param), response, rule);
            }
        } catch (IOException e) {
            handleRemoteApiIOException(returnType, url, OptionalThing.of(param), e);
        }
        return null;
    }

    protected HttpEntityEnclosingRequestBase prepareHttpEnclosing(Type returnType, String url, Object param, FlutyRemoteApiRule rule,
            SupportedHttpMethod httpMethod, Function<String, HttpEntityEnclosingRequestBase> enclosingFactory) {
        final HttpEntityEnclosingRequestBase enclosingRequest = enclosingFactory.apply(url);
        setupHeader(enclosingRequest, rule);
        if (param instanceof EmptyRequestBody) { // e.g. POST but noRequestBody()
            return enclosingRequest;
        }
        final RequestBodySender converter = rule.getRequestBodySender().orElseThrow(() -> {
            return createRemoteApiSenderOfRequestBodyNotFoundException(returnType, url, param, rule, httpMethod);
        });
        converter.prepareEnclosingRequest(enclosingRequest, param, rule);
        return enclosingRequest;
    }

    public static class EmptyRequestBody { // special type to control noRequestBody()
    }

    // ===================================================================================
    //                                                                  Unified Controller
    //                                                                  ==================
    protected <RETURN> RETURN retryableRequest(Type returnType, String urlBase, String actionPath, Object[] pathVariables,
            Object optOrParam, FlutyRemoteApiRule rule, Supplier<RETURN> actuallyRequester,
            Function<RemoteApiHttpClientErrorException, ClientErrorRetryResource> retryResourceProvider) {
        try {
            return actuallyRequester.get();
        } catch (RemoteApiHttpClientErrorException clientError) {
            final OptionalThing<ClientErrorRetryDeterminer> determiner = rule.getClientErrorRetryDeterminer();
            if (determiner.isPresent()) {
                final ClientErrorRetryDeterminer retryDeterminer = determiner.get();
                final ClientErrorRetryResource retryResource = retryResourceProvider.apply(clientError);
                final boolean ready;
                try {
                    ready = retryDeterminer.ready(retryResource);
                } catch (RuntimeException e) {
                    throwRemoteApiRetryReadyFailureException(clientError, e);
                    return null; // unreachable
                }
                if (ready) {
                    if (logger.isDebugEnabled()) { // debug log of receiving exists so simple here
                        logger.debug("#flow #remote ...Retrying request by client error: HTTP status={}", clientError.getHttpStatus());
                    }
                    return actuallyRequester.get();
                }
            }
            throw clientError;
        }
    }

    protected ClientErrorRetryResource createClientErrorRetryResource(Type returnType, String urlBase, String actionPath,
            Object[] pathVariables, OptionalThing<? extends Object> optParam, FlutyRemoteApiRule rule, SupportedHttpMethod httpMethod,
            RemoteApiHttpClientErrorException clientError) {
        return new ClientErrorRetryResource(returnType, urlBase, actionPath, pathVariables, optParam, rule, httpMethod, clientError);
    }

    protected <RETURN> RETURN delegateExecute(SupportedHttpMethod httpMethod, String requestPath, FlutyRemoteApiRule rule,
            Supplier<RETURN> execution) {
        try {
            saveMemories();
            return execution.get();
        } catch (RuntimeException e) {
            keepCauseIfNeeds(rule, e);
            throw e;
        } finally {
            keepEndDateTimeIfNeeds(rule);
            showSendReceiveLogIfNeeds(httpMethod, requestPath, rule);
        }
    }

    // ===================================================================================
    //                                                                          Validation
    //                                                                          ==========
    protected void validateParam(Type returnType, String urlBase, String actionPath, Object[] pathVariables, Object param,
            FlutyRemoteApiRule rule) {
        // you can override
    }

    protected void validateReturn(Type returnType, String url, OptionalThing<Object> param, int httpStatus, OptionalThing<String> body,
            Object ret, FlutyRemoteApiRule rule) {
        // you can override
    }

    // ===================================================================================
    //                                                                      RemoteApi Rule
    //                                                                      ==============
    protected FlutyRemoteApiRule createRemoteApiRule(Consumer<FlutyRemoteApiRule> ruleLambda) {
        final FlutyRemoteApiRule rule = newRemoteApiRule();
        defaultRuleLambda.accept(rule);
        ruleLambda.accept(rule);
        return rule;
    }

    protected FlutyRemoteApiRule newRemoteApiRule() {
        return new FlutyRemoteApiRule();
    }

    // ===================================================================================
    //                                                                 HttpClient Building
    //                                                                 ===================
    protected CloseableHttpClient buildHttpClient(FlutyRemoteApiRule rule) {
        return rule.prepareHttpClient();
    }

    // ===================================================================================
    //                                                                        URL Building
    //                                                                        ============
    protected String buildRequestPath(Type returnType, String urlBase, String actionPath, Object[] pathVariables,
            OptionalThing<? extends Object> queryParam, FlutyRemoteApiRule rule) {
        final StringBuilder sb = new StringBuilder();
        final ActionPathNew pathNew = prepareActionPathVariableNew(returnType, urlBase, actionPath, pathVariables, queryParam, rule);
        sb.append(pathNew.getActionPath());
        if (pathNew.hasPathVariables()) {
            sb.append("/");
            sb.append(buildPathVariablePart(returnType, urlBase, actionPath, pathNew.getPathVariables(), queryParam, rule));
        }
        return sb.toString();
    }

    protected String buildUrl(Type returnType, String urlBase, String requestPath, OptionalThing<? extends Object> queryParam,
            FlutyRemoteApiRule rule) {
        assertArgumentNotNull("returnType", returnType);
        assertArgumentNotNull("urlBase", urlBase);
        assertArgumentNotNull("requestPath", requestPath);
        assertArgumentNotNull("queryParam", queryParam);
        assertArgumentNotNull("rule", rule);
        final StringBuilder sb = new StringBuilder();
        sb.append(urlBase);
        sb.append(requestPath);
        queryParam.ifPresent(form -> {
            buildQueryParameter(sb, returnType, form, rule);
        });
        return sb.toString();
    }

    protected ActionPathNew prepareActionPathVariableNew(Type returnType, String urlBase, String actionPath, Object[] pathVariables,
            OptionalThing<? extends Object> queryParam, FlutyRemoteApiRule rule) {
        final String newActionPath;
        final Object[] newPathVariables;
        if (Srl.containsAll(actionPath, "{", "}")) { // e.g. /sea/{hangar}/land/{showbase}, {"mystic", "onamna"}
            final List<String> pathElementList = Srl.splitList(actionPath, "/");
            final List<Object> resolvedElementList = new ArrayList<Object>();
            int pathVariableUsedIndex = 0;
            for (String token : pathElementList) {
                final Object newToken;
                if (Srl.isQuotedAnything(token, "{", "}")) {
                    if (pathVariables.length <= pathVariableUsedIndex) {
                        throwRemoteApiPathVariableShortElementException(returnType, urlBase, actionPath, pathVariables, queryParam, rule);
                    }
                    newToken = pathVariables[pathVariableUsedIndex];
                    if (newToken == null) {
                        throwRemoteApiPathVariableNullElementException(returnType, urlBase, actionPath, pathVariables, queryParam, rule);
                    }
                    ++pathVariableUsedIndex;
                } else {
                    newToken = token;
                }
                resolvedElementList.add(newToken);
            }
            newActionPath = resolvedElementList.stream().map(token -> token.toString()).collect(Collectors.joining("/"));
            if (pathVariables.length > 0) { // basically here
                newPathVariables = Arrays.asList(pathVariables).subList(pathVariableUsedIndex, pathVariables.length).toArray();
            } else { // no way, already checked but just in case (or may be broken variable expression...!?)
                newPathVariables = pathVariables;
            }
        } else { // e.g. sea/land
            newActionPath = actionPath;
            newPathVariables = pathVariables;
        }
        return new ActionPathNew(newActionPath, newPathVariables);
    }

    protected static class ActionPathNew {

        protected final String actionPath;
        protected final Object[] pathVariables;

        public ActionPathNew(String actionPath, Object[] pathVariables) {
            this.actionPath = actionPath;
            this.pathVariables = pathVariables;
        }

        public boolean hasPathVariables() {
            return pathVariables.length > 0;
        }

        public String getActionPath() {
            return actionPath;
        }

        public Object[] getPathVariables() {
            return pathVariables;
        }
    }

    protected String buildPathVariablePart(Type returnType, String urlBase, String actionPath, Object[] pathVariables,
            OptionalThing<? extends Object> queryParam, FlutyRemoteApiRule rule) {
        final String encoding = rule.getPathVariableCharset().name();
        return Stream.of(pathVariables).map(el -> {
            if (el == null) {
                throwRemoteApiPathVariableNullElementException(returnType, urlBase, actionPath, pathVariables, queryParam, rule);
            }
            try {
                return URLEncoder.encode(convertPathVariableToString(el, rule), encoding);
            } catch (UnsupportedEncodingException e) { // basically no way
                throw new IllegalStateException("Unknown encoding: " + encoding, e);
            }
        }).collect(Collectors.joining("/"));
    }

    protected String convertPathVariableToString(Object el, FlutyRemoteApiRule rule) {
        // #hope jflute needs PathVariableFilter?
        if (el instanceof String) {
            return (String) el;
        } else if (el instanceof Classification) {
            return ((Classification) el).code();
        } else {
            return el.toString();
        }
    }

    // ===================================================================================
    //                                                                     Query Parameter
    //                                                                     ===============
    protected void buildQueryParameter(StringBuilder sb, Type returnType, Object form, FlutyRemoteApiRule rule) {
        final QueryParameterSender sender = rule.getQueryParameterSender().orElseThrow(() -> {
            return createRemoteApiSenderOfQueryParameterNotFoundException(sb, returnType, form, rule);
        });
        final String queryString = sender.toQueryString(form, rule.getQueryParameterCharset(), rule);
        sb.append(queryString);
    }

    // ===================================================================================
    //                                                                   Response Handling
    //                                                                   =================
    protected <RETURN> RETURN handleResponse(Type returnType, String url, OptionalThing<Object> param, CloseableHttpResponse response,
            FlutyRemoteApiRule rule) throws IOException {
        final int httpStatus = response.getStatusLine().getStatusCode();
        keepResponseHeaderIfNeeds(rule, response.getAllHeaders());
        keepResponseStatusIfNeeds(rule, httpStatus);
        final OptionalThing<String> body = extractResponseBody(response, rule);
        try {
            final RETURN ret = parseResponse(returnType, url, param, httpStatus, body, rule);
            validateReturn(returnType, url, param, httpStatus, body, ret, rule);
            return ret;
        } catch (RemoteApiHttpBasisErrorException cause) {
            cause.getFailureResponse().ifPresent(failureResponse -> { // don't forget it
                validateReturn(returnType, url, param, httpStatus, body, failureResponse, rule);
            });
            if (cause instanceof RemoteApiHttpClientErrorException) {
                throwTranslatedClientErrorIfNeeds(returnType, url, param, rule, httpStatus, body,
                        (RemoteApiHttpClientErrorException) cause);
            }
            throw cause;
        }
    }

    // -----------------------------------------------------
    //                                        Parse Response
    //                                        --------------
    protected <RETURN> RETURN parseResponse(Type returnType, String url, OptionalThing<Object> form, int httpStatus,
            OptionalThing<String> body, FlutyRemoteApiRule rule) {
        logger.debug("#flow #remote ...Receiving response as {} from Remote API:\n{}\n as {}\n{}", httpStatus, url, returnType,
                body.orElse("(no body)"));
        if (httpStatus >= 200 && httpStatus < 300) {
            final RETURN ret = toResponseReturn(returnType, url, form, httpStatus, body, rule);
            return ret;
        } else if (httpStatus >= 400 && httpStatus < 500) { // e.g. not found, bad request
            final RemoteApiFailureResponseHolder failureResponseHolder = holdFailureResponse(returnType, url, form, httpStatus, body, rule);
            throwRemoteApiHttpClientErrorException(returnType, url, form, httpStatus, body, failureResponseHolder);
        } else { // e.g. 500, unknown error
            final RemoteApiFailureResponseHolder failureResponseHolder = holdFailureResponse(returnType, url, form, httpStatus, body, rule);
            throwRemoteApiHttpServerErrorException(returnType, url, form, httpStatus, body, failureResponseHolder);
        }
        return null; // unreachable
    }

    // -----------------------------------------------------
    //                                      Failure Response
    //                                      ----------------
    protected RemoteApiFailureResponseHolder holdFailureResponse(Type returnType, String url, OptionalThing<Object> form, int httpStatus,
            OptionalThing<String> body, FlutyRemoteApiRule rule) {
        Object failureResponse = null;
        Supplier<RuntimeException> emptyResponseCause = null; // null allowed
        try {
            failureResponse = parseFailureResponse(url, form, httpStatus, body, rule);
            if (failureResponse == null) { // when no rule
                emptyResponseCause =
                        () -> createRemoteApiFailureResponseTypeNotFoundException(returnType, url, form, httpStatus, body, rule);
            }
        } catch (RemoteApiResponseParseFailureException kept) { // failure response might be broken
            emptyResponseCause = () -> kept;
        }
        return new RemoteApiFailureResponseHolder(failureResponse, emptyResponseCause);
    }

    protected Object parseFailureResponse(String url, OptionalThing<Object> form, int httpStatus, OptionalThing<String> body,
            FlutyRemoteApiRule rule) {
        return rule.getFailureResponseType().map(failureResponseType -> {
            return toResponseReturn(failureResponseType, url, form, httpStatus, body, rule);
        }).orElse(null); // when no rule
    }

    // -----------------------------------------------------
    //                                     Convert to Return
    //                                     -----------------
    protected <RETURN> RETURN toResponseReturn(Type returnType, String url, OptionalThing<Object> form, int httpStatus,
            OptionalThing<String> body, FlutyRemoteApiRule rule) {
        if (isVoid(returnType)) { // e.g. doRequestPost(void.class, ...);
            @SuppressWarnings("unchecked")
            final RETURN ret = (RETURN) VOID_OBJ;
            return ret; // no look body here
        }
        final ResponseBodyReceiver receiver = rule.getResponseBodyReceiver().orElseThrow(() -> {
            return createRemoteApiReceiverOfResponseBodyNotFoundException(returnType, url, form, httpStatus, body, rule);
        });
        try {
            return receiver.toResponseReturn(body, returnType, rule);
        } catch (RuntimeException e) {
            throwRemoteApiResponseParseFailureException(returnType, url, form, httpStatus, body, receiver, e);
            return null; // unreachable
        }
    }

    protected boolean isVoid(Type returnType) {
        return Void.class.equals(returnType) || void.class.equals(returnType);
    }

    // -----------------------------------------------------
    //                                 Translate ClientError
    //                                 ---------------------
    protected void throwTranslatedClientErrorIfNeeds(Type returnType, String url, OptionalThing<Object> param, FlutyRemoteApiRule rule,
            int httpStatus, OptionalThing<String> body, RemoteApiHttpClientErrorException cause) {
        rule.getClientErrorTranslator().ifPresent(translator -> {
            final ClientErrorTranslatingResource resource = createRemoteApiClientErrorResource(returnType, url, cause);
            RuntimeException translated = null;
            try {
                translated = translator.translate(resource);
            } catch (RuntimeException e) {
                throwRemoteApiErrorTranslationFailureException(returnType, url, param, rule, httpStatus, body, cause, e);
            }
            if (translated != null) {
                throw translated;
            }
        });
    }

    protected ClientErrorTranslatingResource createRemoteApiClientErrorResource(Type returnType, String url,
            RemoteApiHttpClientErrorException cause) {
        return new ClientErrorTranslatingResource(returnType, url, cause, prepareValidatorErrorProvider(returnType, url, cause));
    }

    protected BiFunction<RemoteApiHttpClientErrorException, Object, RuntimeException> prepareValidatorErrorProvider(Type returnType,
            String url, RemoteApiHttpClientErrorException cause) { // may be overridden
        return null; // as default
    }

    // ===================================================================================
    //                                                                            Memories
    //                                                                            ========
    protected void saveMemories() {
        if (!hasMemoriesContext()) {
            return;
        }
        final Consumer<String> counter = counterComesHere();
        if (counter == null) { // e.g. before LastaFlute-1.0.1
            return;
        }
        final String facadeName;
        if (facadeExp instanceof Class<?>) {
            facadeName = ((Class<?>) facadeExp).getSimpleName();
        } else {
            facadeName = facadeExp.toString();
        }
        counter.accept(facadeName);
    }

    protected boolean hasMemoriesContext() { // may be overridden
        return false; // as default
    }

    protected Consumer<String> counterComesHere() {
        Consumer<String> counter = findlRemoteApiCounter();
        if (counter == null) {
            final IndependentProcessor initializer = findRemoteApiCounterInitializer();
            if (initializer != null) {
                initializer.process();
                counter = findlRemoteApiCounter();
            }
        }
        return counter;
    }

    // expectes LastaFlute-1.0.1
    protected Consumer<String> findlRemoteApiCounter() { // may be overridden
        return null; // as default
    }

    protected IndependentProcessor findRemoteApiCounterInitializer() { // may be overridden
        return null; // as default
    }

    // ===================================================================================
    //                                                                          Basic Keep
    //                                                                          ==========
    // basically for send-receive logging
    // -----------------------------------------------------
    //                                           Basic Begin
    //                                           -----------
    protected void keepBeginDateTimeIfNeeds(FlutyRemoteApiRule rule) {
        final SendReceiveLogOption option = rule.getSendReceiveLogOption();
        if (option.isEnabled()) {
            option.keeper().keepBeginDateTime(flashDateTime());
        }
    }

    protected void keepFacadeExpIfNeeds(FlutyRemoteApiRule rule) {
        final SendReceiveLogOption option = rule.getSendReceiveLogOption();
        if (option.isEnabled()) {
            option.keeper().keepFacadeExp(facadeExp);
        }
    }

    // -----------------------------------------------------
    //                                               Request
    //                                               -------
    protected void keepRequestHeaderIfNeeds(FlutyRemoteApiRule rule, Map<String, ? extends Object> headerMap) {
        final SendReceiveLogOption option = rule.getSendReceiveLogOption();
        if (option.isEnabled()) {
            option.keeper().keepRequestHeader(headerMap);
        }
    }

    // -----------------------------------------------------
    //                                              Response
    //                                              --------
    protected void keepResponseHeaderIfNeeds(FlutyRemoteApiRule rule, Header[] headers) {
        final SendReceiveLogOption option = rule.getSendReceiveLogOption();
        if (option.isEnabled() && headers != null) {
            for (Header header : headers) {
                option.keeper().keepResponseHeader(header.getName(), header.getValue());
            }
        }
    }

    protected void keepResponseStatusIfNeeds(FlutyRemoteApiRule rule, int httpStatus) {
        final SendReceiveLogOption option = rule.getSendReceiveLogOption();
        if (option.isEnabled()) {
            option.keeper().keepHttpStatus(httpStatus);
        }
    }

    // -----------------------------------------------------
    //                                             Basic End
    //                                             ---------
    protected void keepCauseIfNeeds(FlutyRemoteApiRule rule, RuntimeException cause) {
        final SendReceiveLogOption option = rule.getSendReceiveLogOption();
        if (option.isEnabled()) {
            option.keeper().keepCause(cause);
        }
    }

    protected void keepEndDateTimeIfNeeds(FlutyRemoteApiRule rule) {
        final SendReceiveLogOption option = rule.getSendReceiveLogOption();
        if (option.isEnabled()) {
            option.keeper().keepEndDateTime(flashDateTime());
        }
    }

    // -----------------------------------------------------
    //                                            Flash Date
    //                                            ----------
    protected LocalDateTime flashDateTime() { // may be overriden
        return DBFluteSystem.currentLocalDateTime();
    }

    // ===================================================================================
    //                                                                Send/Receive Logging
    //                                                                ====================
    protected void showSendReceiveLogIfNeeds(SupportedHttpMethod httpMethod, String requestPath, FlutyRemoteApiRule rule) {
        final SendReceiveLogOption option = rule.getSendReceiveLogOption();
        if (option.isEnabled()) {
            final SendReceiveLogger sendReceiveLogger = createSendReceiveLogger();
            sendReceiveLogger.show(httpMethod, requestPath, option, prepareSendReceiveLogAsync());
        }
    }

    protected SendReceiveLogger createSendReceiveLogger() { // may be overridden
        return new SendReceiveLogger();
    }

    protected Consumer<Runnable> prepareSendReceiveLogAsync() { // may be overridden
        return runner -> runner.run(); // non-async as default
    }

    // ===================================================================================
    //                                                                       Request Error
    //                                                                       =============
    // -----------------------------------------------------
    //                                 Path Variable Failure
    //                                 ---------------------
    protected void throwRemoteApiPathVariableShortElementException(Type returnType, String urlBase, String actionPath,
            Object[] pathVariables, OptionalThing<? extends Object> queryParam, FlutyRemoteApiRule rule) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Short element of embedded path variable in action path.");
        br.addItem("Advice");
        br.addElement("Make sure your path variable values.");
        br.addElement("  (x):");
        br.addElement("    \"/sea/{hangar}/land/{showbase}\", moreUrl(\"mystic\")");
        br.addElement("  (o):");
        br.addElement("    \"/sea/{hangar}/land/{showbase}\", moreUrl(\"mystic\", \"oneman\")");
        br.addItem("Path Variables");
        br.addElement(Arrays.asList(pathVariables));
        setupRequestInfo(br, returnType, urlBase + actionPath, queryParam);
        setupYourRule(br, rule);
        setupFacadeExpression(br);
        final String msg = br.buildExceptionMessage();
        throw new RemoteApiPathVariableShortElementException(msg);
    }

    protected void throwRemoteApiPathVariableNullElementException(Type returnType, String urlBase, String actionPath,
            Object[] pathVariables, OptionalThing<? extends Object> queryParam, FlutyRemoteApiRule rule) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Cannot set null element in your rule.");
        br.addItem("Advice");
        br.addElement("Make sure your path variable values.");
        br.addElement("  (x):");
        br.addElement("    moreUrl(1, null, 3)");
        br.addElement("  (o):");
        br.addElement("    moreUrl(1, 2, 3)");
        br.addItem("Path Variables");
        br.addElement(Arrays.asList(pathVariables));
        setupRequestInfo(br, returnType, urlBase + actionPath, queryParam);
        setupYourRule(br, rule);
        setupFacadeExpression(br);
        final String msg = br.buildExceptionMessage();
        throw new RemoteApiPathVariableNullElementException(msg);
    }

    // -----------------------------------------------------
    //                                         Retry Failure
    //                                         -------------
    protected void throwRemoteApiRetryReadyFailureException(RemoteApiHttpClientErrorException clientError, RuntimeException cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to ready the retry of request for client error.");
        br.addItem("Advice");
        br.addElement("Confirm your rule.retryIfClientError() callback.");
        // clientError has rich message of requset and response information
        //setupRequestInfo(br, returnType, url, optOrParam);
        br.addItem("Client Error");
        br.addElement(clientError.getMessage());
        setupFacadeExpression(br);
        final String msg = br.buildExceptionMessage();
        throw new RemoteApiRetryReadyFailureException(msg, cause);
    }

    // ===================================================================================
    //                                                                      Response Error
    //                                                                      ==============
    // -----------------------------------------------------
    //                                           HTTP Status
    //                                           -----------
    protected void throwRemoteApiHttpClientErrorException(Type returnType, String url, OptionalThing<Object> form, int httpStatus,
            OptionalThing<String> body, RemoteApiFailureResponseHolder failureResponseHolder) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Client Error as HTTP status from the remote API.");
        setupRequestInfo(br, returnType, url, form);
        setupResponseInfo(br, httpStatus, body);
        setupFacadeExpression(br);
        final String msg = br.buildExceptionMessage();
        throw new RemoteApiHttpClientErrorException(msg, httpStatus, failureResponseHolder);
    }

    protected void throwRemoteApiHttpServerErrorException(Type returnType, String url, OptionalThing<Object> form, int httpStatus,
            OptionalThing<String> body, RemoteApiFailureResponseHolder failureResponseHolder) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Server Error as HTTP status from the remote API.");
        setupRequestInfo(br, returnType, url, form);
        setupResponseInfo(br, httpStatus, body);
        setupFacadeExpression(br);
        final String msg = br.buildExceptionMessage();
        throw new RemoteApiHttpServerErrorException(msg, httpStatus, failureResponseHolder);
    }

    // -----------------------------------------------------
    //                                          IO Exception
    //                                          ------------
    protected void handleRemoteApiIOException(Type returnType, String url, OptionalThing<Object> form, IOException cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("IO Error to the remote API.");
        setupRequestInfo(br, returnType, url, form);
        setupFacadeExpression(br);
        final String msg = br.buildExceptionMessage();
        throw new RemoteApiIOException(msg, cause);
    }

    // -----------------------------------------------------
    //                                          Cannot Parse
    //                                          ------------
    protected void throwRemoteApiResponseParseFailureException(Type type, String url, OptionalThing<Object> form, int httpStatus,
            OptionalThing<String> body, ResponseBodyReceiver receiver, RuntimeException e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to parse the response body from remote API.");
        setupRequestInfo(br, type, url, form);
        setupResponseInfo(br, httpStatus, body);
        br.addItem("Receiver");
        br.addElement(receiver);
        setupFacadeExpression(br);
        final String msg = br.buildExceptionMessage();
        throw new RemoteApiResponseParseFailureException(msg, e);
    }

    // -----------------------------------------------------
    //                                   Translation Failure
    //                                   -------------------
    protected void throwRemoteApiErrorTranslationFailureException(Type returnType, String url, OptionalThing<Object> param,
            FlutyRemoteApiRule rule, int httpStatus, OptionalThing<String> body, RemoteApiHttpClientErrorException clientError,
            RuntimeException translationEx) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to translate client error.");
        br.addItem("Advice");
        br.addElement("Confirm your logic of rule.translateClientError().");
        setupRequestInfo(br, returnType, url, param);
        setupResponseInfo(br, httpStatus, body);
        setupYourRule(br, rule);
        setupFacadeExpression(br);
        clientError.getFailureResponse().ifPresent(failureResponse -> {
            br.addItem("Failure Response");
            br.addElement(convertBeanToDebugString(failureResponse));
        }); // client error's data are already set up
        final String msg = br.buildExceptionMessage();
        throw new RemoteApiErrorTranslationFailureException(msg, translationEx);
    }

    // ===================================================================================
    //                                                                          Rule Error
    //                                                                          ==========
    protected RuntimeException createRemoteApiSenderOfQueryParameterNotFoundException(StringBuilder sb, Type returnType, Object form,
            FlutyRemoteApiRule rule) {
        final String url = sb.toString(); // just it
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the sender for query parameter in your rule.");
        br.addItem("Advice");
        br.addElement("Query parameter sender is required for e.g. GET request.");
        br.addElement("Set sender to your rule like this:");
        br.addElement("  (o):");
        br.addElement("    protected void yourDefaultRule(FlutyRemoteApiRule rule) {");
        br.addElement("        rule.sendQueryBy(new LaQuerySender());");
        br.addElement("    }");
        br.addElement("  (o):");
        br.addElement("    doRequestGet(..., rule -> rule.sendQueryBy(new LaQuerySender()));");
        setupRequestInfo(br, returnType, url, form);
        setupYourRule(br, rule);
        setupFacadeExpression(br);
        final String msg = br.buildExceptionMessage();
        return new RemoteApiSenderOfQueryParameterNotFoundException(msg);
    }

    protected RuntimeException createRemoteApiSenderOfRequestBodyNotFoundException(Type returnType, String url, Object param,
            FlutyRemoteApiRule rule, SupportedHttpMethod httpMethod) {
        final String camelMethod = Srl.camelize(httpMethod.name().toLowerCase());
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the sender for request body in your rule.");
        br.addItem("Advice");
        br.addElement("Request body sender is required for e.g. JSON response.");
        br.addElement("Set sender to your rule like this:");
        br.addElement("  (o):");
        br.addElement("    protected void yourDefaultRule(FlutyRemoteApiRule rule) {");
        br.addElement("        rule.sendBodyBy(new LaJsonSender());");
        br.addElement("    }");
        br.addElement("  (o):");
        br.addElement("    doRequest" + camelMethod + "(..., rule -> rule.sendBodyBy(new LaJsonSender());");
        setupRequestInfo(br, returnType, url, param);
        setupYourRule(br, rule);
        br.addItem("HTTP Method");
        br.addElement(httpMethod);
        setupFacadeExpression(br);
        final String msg = br.buildExceptionMessage();
        return new RemoteApiSenderOfRequestBodyNotFoundException(msg);
    }

    protected RuntimeException createRemoteApiReceiverOfResponseBodyNotFoundException(Type returnType, String url,
            OptionalThing<Object> form, int httpStatus, OptionalThing<String> body, FlutyRemoteApiRule rule) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the receiver for response body in your rule.");
        br.addItem("Advice");
        br.addElement("Response body receiver is required for e.g. JSON response.");
        br.addElement("Set receiver to your rule like this:");
        br.addElement("  (o):");
        br.addElement("    protected void yourDefaultRule(FlutyRemoteApiRule rule) {");
        br.addElement("        rule.receiveBodyBy(new LaJsonReceiver());");
        br.addElement("    }");
        br.addElement("  (o):");
        br.addElement("    doRequestGet(..., rule -> rule.receiveBodyBy(new LaJsonReceiver()));");
        setupRequestInfo(br, returnType, url, form);
        setupResponseInfo(br, httpStatus, body);
        setupYourRule(br, rule);
        setupFacadeExpression(br);
        final String msg = br.buildExceptionMessage();
        return new RemoteApiReceiverOfResponseBodyNotFoundException(msg);
    }

    protected RuntimeException createRemoteApiFailureResponseTypeNotFoundException(Type returnType, String url, OptionalThing<Object> form,
            int httpStatus, OptionalThing<String> body, FlutyRemoteApiRule rule) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the failure response type in your rule.");
        br.addItem("Adivce");
        br.addElement("Set failure response type to your rule");
        br.addElement("if you get failure response.");
        br.addElement("For example:");
        br.addElement("  (o):");
        br.addElement("    protected void yourDefaultRule(FlutyRemoteApiRule rule) {");
        br.addElement("        rule.handleFailureResponseAs(FaicliUnifiedFailureResult.class);");
        br.addElement("    }");
        setupRequestInfo(br, returnType, url, form);
        setupResponseInfo(br, httpStatus, body);
        setupYourRule(br, rule);
        final String msg = br.buildExceptionMessage();
        return new RemoteApiFailureResponseTypeNotFoundException(msg);
    }

    // ===================================================================================
    //                                                                      Message Helper
    //                                                                      ==============
    protected void setupRequestInfo(ExceptionMessageBuilder br, Type returnType, String url, Object optOrParam) {
        setupReturnTypeAndRemoteApi(br, returnType, url);
        if (optOrParam instanceof OptionalThing<?>) {
            ((OptionalThing<?>) optOrParam).ifPresent(param -> {
                br.addItem("Request Parameter");
                br.addElement(convertBeanToDebugString(param));
            });
        } else {
            br.addItem("Request Parameter");
            br.addElement(convertBeanToDebugString(optOrParam));
        }
    }

    protected void setupReturnTypeAndRemoteApi(ExceptionMessageBuilder br, Type returnType, String url) {
        br.addItem("Return Type");
        br.addElement(returnType);
        br.addItem("Remote API");
        br.addElement(url);
    }

    protected String convertBeanToDebugString(Object param) {
        return param.toString(); // as default
    }

    protected void setupResponseInfo(ExceptionMessageBuilder br, int httpStatus, OptionalThing<String> body) {
        br.addItem("Response HTTP Status");
        br.addElement(httpStatus);
        br.addItem("Response Body");
        br.addElement(body.orElse("(no body)"));
    }

    protected <RET> void setupReturnInfo(ExceptionMessageBuilder br, RET ret) {
        br.addItem("Return Object");
        br.addElement(ret);
    }

    protected void setupYourRule(ExceptionMessageBuilder br, FlutyRemoteApiRule rule) {
        br.addItem("Your Rule");
        br.addElement(rule);
    }

    protected void setupFacadeExpression(ExceptionMessageBuilder br) {
        br.addItem("Facade Expression");
        br.addElement(facadeExp);
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    protected void setupHeader(HttpMessage httpMessage, FlutyRemoteApiRule rule) {
        rule.getHeaders().ifPresent(headerMap -> {
            headerMap.forEach((name, valueList) -> {
                valueList.forEach(value -> {
                    httpMessage.addHeader(name, value);
                });
            });
            keepRequestHeaderIfNeeds(rule, headerMap);
        });
    }

    protected OptionalThing<String> extractResponseBody(CloseableHttpResponse response, FlutyRemoteApiRule rule) throws IOException {
        final HttpEntity entity = response.getEntity(); // null allowed
        final String body = entity != null ? EntityUtils.toString(entity, rule.getResponseBodyCharset()) : null;
        return OptionalThing.ofNullable(body, () -> {
            throw new IllegalStateException("Not found the response body.");
        });
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
}
