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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
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
import org.dbflute.remoteapi.exception.RemoteApiReceiverOfResponseBodyNotFoundException;
import org.dbflute.remoteapi.exception.RemoteApiResponseParseFailureException;
import org.dbflute.remoteapi.exception.RemoteApiRetryReadyFailureException;
import org.dbflute.remoteapi.exception.RemoteApiSenderOfQueryParameterNotFoundException;
import org.dbflute.remoteapi.exception.RemoteApiSenderOfRequestBodyNotFoundException;
import org.dbflute.remoteapi.exception.retry.ClientErrorRetryDeterminer;
import org.dbflute.remoteapi.exception.retry.ClientErrorRetryResource;
import org.dbflute.remoteapi.exception.translation.ClientErrorTranslatingResource;
import org.dbflute.remoteapi.http.SupportedHttpMethod;
import org.dbflute.remoteapi.receiver.ResponseBodyReceiver;
import org.dbflute.remoteapi.sender.body.RequestBodySender;
import org.dbflute.remoteapi.sender.query.QueryParameterSender;
import org.dbflute.util.Srl;
import org.lastaflute.core.magic.ThreadCacheContext;
import org.lastaflute.core.util.Lato;
import org.lastaflute.web.validation.VaErrorHook;
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
    protected final Consumer<FlutyRemoteApiRule> defaultRuleLambda;
    protected final Object callerExp; // for various purpose, basically debug

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public FlutyRemoteApi(Consumer<FlutyRemoteApiRule> defaultRuleLambda, Object callerExp) {
        this.defaultRuleLambda = defaultRuleLambda;
        this.callerExp = callerExp;
    }

    // ===================================================================================
    //                                                                         Request GET
    //                                                                         ===========
    /**
     * @param <RETURN> The type of response return.
     * @param beanType The class type of bean to convert, should have default constructor. (NotNull)
     * @param urlBase The base part of URL to remote API server. e.g. http://localhost:8090/harbor (NotNull)
     * @param actionPath The path to action without URL parameter, and trailing slash is no difference. e.g. /sea/land (NotNull)
     * @param pathVariables The array of URL path variables, e.g. ["hangar", 3]. (NotNull, EmptyAllowed)
     * @param param The optional parameter object of query (GET parameters). (NotNull, EmptyAllowed)
     * @param ruleLambda The callback for rule of remote API. (NotNull)
     * @return The analyzed return of response from the request. (NotNull)
     */
    public <RETURN> RETURN requestGet(Type beanType, String urlBase, String actionPath, Object[] pathVariables,
            OptionalThing<? extends Object> param, Consumer<FlutyRemoteApiRule> ruleLambda) {
        return doRequestEmptyBody(beanType, urlBase, actionPath, pathVariables, param, ruleLambda, SupportedHttpMethod.GET, url -> {
            return new HttpGet(url);
        });
    }

    // ===================================================================================
    //                                                                        Request POST
    //                                                                        ============
    /**
     * @param <RETURN> The type of response return.(response).
     * @param beanType The class type of bean to convert, should have default constructor. (NotNull)
     * @param urlBase The base part of URL to remote API server. e.g. http://localhost:8090/harbor (NotNull)
     * @param actionPath The path to action without URL parameter, and trailing slash is no difference. e.g. /sea/land (NotNull)
     * @param pathVariables The array of URL path variables, e.g. ["hangar", 3]. (NotNull, EmptyAllowed)
     * @param param The parameter objet of POST parameters, may be JSON body. (NotNull)
     * @param ruleLambda The callback for rule of remote API. (NotNull)
     * @return The analyzed return of response from the request. (NotNull)
     */
    public <RETURN> RETURN requestPost(Type beanType, String urlBase, String actionPath, Object[] pathVariables, Object param,
            Consumer<FlutyRemoteApiRule> ruleLambda) {
        return doRequestEnclosing(beanType, urlBase, actionPath, pathVariables, param, ruleLambda, SupportedHttpMethod.POST, url -> {
            return new HttpPost(url);
        });
    }

    // ===================================================================================
    //                                                                         Request PUT
    //                                                                         ===========
    /**
     * @param <RETURN> The type of response return.
     * @param beanType The class type of bean to convert, should have default constructor. (NotNull)
     * @param urlBase The base part of URL to remote API server. e.g. http://localhost:8090/harbor (NotNull)
     * @param actionPath The path to action without URL parameter, and trailing slash is no difference. e.g. /sea/land (NotNull)
     * @param pathVariables The array of URL path variables, e.g. ["hangar", 3]. (NotNull, EmptyAllowed)
     * @param param The parameter object of PUT parameters, may be JSON body. (NotNull)
     * @param ruleLambda The callback for rule of remote API. (NotNull)
     * @return The analyzed return of response from the request. (NotNull)
     */
    public <RETURN> RETURN requestPut(Type beanType, String urlBase, String actionPath, Object[] pathVariables, Object param,
            Consumer<FlutyRemoteApiRule> ruleLambda) {
        return doRequestEnclosing(beanType, urlBase, actionPath, pathVariables, param, ruleLambda, SupportedHttpMethod.PUT, url -> {
            return new HttpPut(url);
        });
    }

    // ===================================================================================
    //                                                                      Request DELETE
    //                                                                      ==============
    /**
     * @param <RETURN> The type of response return.
     * @param beanType The class type of bean to convert, should have default constructor. (NotNull)
     * @param urlBase The base part of URL to remote API server. e.g. http://localhost:8090/harbor (NotNull)
     * @param actionPath The path to action without URL parameter, and trailing slash is no difference. e.g. /sea/land (NotNull)
     * @param pathVariables The array of URL path variables, e.g. ["hangar", 3]. (NotNull, EmptyAllowed)
     * @param param The optional parameter object of query (GET parameters). (NotNull, EmptyAllowed)
     * @param ruleLambda The callback for rule of remote API. (NotNull)
     * @return The analyzed return of response from the request. (NotNull)
     */
    public <RETURN> RETURN requestDelete(Type beanType, String urlBase, String actionPath, Object[] pathVariables,
            OptionalThing<? extends Object> param, Consumer<FlutyRemoteApiRule> ruleLambda) {
        return doRequestEmptyBody(beanType, urlBase, actionPath, pathVariables, param, ruleLambda, SupportedHttpMethod.DELETE, url -> {
            return new HttpDelete(url);
        });
    }

    // ===================================================================================
    //                                                                   Request EmptyBody
    //                                                                   =================
    protected <RETURN> RETURN doRequestEmptyBody(Type beanType, String urlBase, String actionPath, Object[] pathVariables,
            OptionalThing<? extends Object> optParam, Consumer<FlutyRemoteApiRule> ruleLambda, SupportedHttpMethod httpMethod,
            Function<String, HttpUriRequest> emptyBodyFactory) {
        assertArgumentNotNull("beanType", beanType);
        assertArgumentNotNull("urlBase", urlBase);
        assertArgumentNotNull("actionPath", actionPath);
        assertArgumentNotNull("pathVariables", pathVariables);
        assertArgumentNotNull("param", optParam); // variable name is for facade method
        assertArgumentNotNull("ruleLambda", ruleLambda);
        assertArgumentNotNull("httpMethod", httpMethod);
        assertArgumentNotNull("emptyBodyFactory", emptyBodyFactory);
        final FlutyRemoteApiRule rule = createRemoteApiRule(ruleLambda);
        return retryableRequest(beanType, urlBase, actionPath, pathVariables, optParam, rule, () -> {
            return actuallyRequestEmptyBody(beanType, urlBase, actionPath, pathVariables, optParam, rule, httpMethod, emptyBodyFactory);
        }, clientError -> {
            return createClientErrorRetryResource(beanType, urlBase, actionPath, pathVariables, optParam, httpMethod, clientError);
        });
    }

    protected <RETURN> RETURN actuallyRequestEmptyBody(Type beanType, String urlBase, String actionPath, Object[] pathVariables,
            OptionalThing<? extends Object> optParam, FlutyRemoteApiRule rule, SupportedHttpMethod httpMethod,
            Function<String, HttpUriRequest> emptyBodyFactory) {
        optParam.ifPresent(param -> validateParam(beanType, urlBase, actionPath, pathVariables, param, rule));
        final String url = buildUrl(beanType, urlBase, actionPath, pathVariables, optParam, rule);
        if (logger.isDebugEnabled()) {
            final Map<String, List<String>> headerMap = rule.getHeaders().orElseGet(() -> Collections.emptyMap());
            logger.debug("#flow #remote ...Sending request as {} to Remote API:\n{}\n with headers: {}", httpMethod, url, headerMap);
        }
        return executeEmptyBody(beanType, url, rule, httpMethod, emptyBodyFactory);
    }

    protected <RETURN> RETURN executeEmptyBody(Type beanType, String url, FlutyRemoteApiRule rule, SupportedHttpMethod httpMethod,
            Function<String, HttpUriRequest> emptyBodyFactory) {
        try (CloseableHttpClient httpClient = buildHttpClient(rule)) {
            final HttpUriRequest httpEmptyBody = prepareHttpEmptyBody(url, rule, httpMethod, emptyBodyFactory);
            try (CloseableHttpResponse response = httpClient.execute(httpEmptyBody)) {
                return handleResponse(beanType, url, /*param*/OptionalThing.empty(), response, rule);
            }
        } catch (IOException e) {
            handleRemoteApiIOException(beanType, url, /*param*/OptionalThing.empty(), e);
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
    protected <RETURN> RETURN doRequestEnclosing(Type beanType, String urlBase, String actionPath, Object[] pathVariables, Object param,
            Consumer<FlutyRemoteApiRule> ruleLambda, SupportedHttpMethod httpMethod,
            Function<String, HttpEntityEnclosingRequestBase> enclosingFactory) {
        assertArgumentNotNull("beanType", beanType);
        assertArgumentNotNull("urlBase", urlBase);
        assertArgumentNotNull("actionPath", actionPath);
        assertArgumentNotNull("pathVariables", pathVariables);
        assertArgumentNotNull("param", param);
        assertArgumentNotNull("ruleLambda", ruleLambda);
        assertArgumentNotNull("httpMethod", httpMethod);
        assertArgumentNotNull("enclosingFactory", enclosingFactory);
        final FlutyRemoteApiRule rule = createRemoteApiRule(ruleLambda);
        return retryableRequest(beanType, urlBase, actionPath, pathVariables, param, rule, () -> {
            return actuallyRequestEnclosing(beanType, urlBase, actionPath, pathVariables, param, rule, httpMethod, enclosingFactory);
        }, clientError -> {
            final OptionalThing<Object> optParam = OptionalThing.of(param);
            return createClientErrorRetryResource(beanType, urlBase, actionPath, pathVariables, optParam, httpMethod, clientError);
        });
    }

    protected <RETURN> RETURN actuallyRequestEnclosing(Type beanType, String urlBase, String actionPath, Object[] pathVariables,
            Object param, FlutyRemoteApiRule rule, SupportedHttpMethod httpMethod,
            Function<String, HttpEntityEnclosingRequestBase> enclosingFactory) {
        validateParam(beanType, urlBase, actionPath, pathVariables, param, rule);
        final String url = buildUrl(beanType, urlBase, actionPath, pathVariables, /*queryForm*/OptionalThing.empty(), rule);
        if (logger.isDebugEnabled()) {
            final String paramDisp = param.getClass().getSimpleName() + ":" + Lato.string(param); // because toString() might not be overridden
            final Map<String, List<String>> headerMap = rule.getHeaders().orElseGet(() -> Collections.emptyMap());
            logger.debug("#flow #remote ...Sending request as {} to Remote API:\n{}\n with param: {}\n with headers: {}", httpMethod, url,
                    paramDisp, headerMap);
        }
        return executeEnclosing(beanType, url, param, rule, httpMethod, enclosingFactory);
    }

    protected <RETURN> RETURN executeEnclosing(Type beanType, String url, Object param, FlutyRemoteApiRule rule,
            SupportedHttpMethod httpMethod, Function<String, HttpEntityEnclosingRequestBase> enclosingFactory) {
        try (CloseableHttpClient httpClient = buildHttpClient(rule)) {
            final HttpUriRequest httpEnclosing = prepareHttpEnclosing(beanType, url, param, rule, httpMethod, enclosingFactory);
            try (CloseableHttpResponse response = httpClient.execute(httpEnclosing)) {
                return handleResponse(beanType, url, OptionalThing.of(param), response, rule);
            }
        } catch (IOException e) {
            handleRemoteApiIOException(beanType, url, OptionalThing.of(param), e);
        }
        return null;
    }

    protected HttpEntityEnclosingRequestBase prepareHttpEnclosing(Type beanType, String url, Object param, FlutyRemoteApiRule rule,
            SupportedHttpMethod httpMethod, Function<String, HttpEntityEnclosingRequestBase> enclosingFactory) {
        final HttpEntityEnclosingRequestBase httpPut = enclosingFactory.apply(url);
        setupHeader(httpPut, rule);
        final RequestBodySender converter = rule.getRequestBodySender().orElseThrow(() -> {
            return createRemoteApiSenderOfRequestBodyNotFoundException(beanType, url, param, rule, httpMethod);
        });
        converter.prepareBodyRequest(httpPut, param, rule);
        return httpPut;
    }

    // ===================================================================================
    //                                                                           Retryable
    //                                                                           =========
    protected <RETURN> RETURN retryableRequest(Type beanType, String urlBase, String actionPath, Object[] pathVariables, Object optOrParam,
            FlutyRemoteApiRule rule, Supplier<RETURN> actuallyRequester,
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

    protected ClientErrorRetryResource createClientErrorRetryResource(Type beanType, String urlBase, String actionPath,
            Object[] pathVariables, OptionalThing<? extends Object> optParam, SupportedHttpMethod httpMethod,
            RemoteApiHttpClientErrorException clientError) {
        return new ClientErrorRetryResource(beanType, urlBase, actionPath, pathVariables, optParam, httpMethod, clientError);
    }

    // ===================================================================================
    //                                                                          Validation
    //                                                                          ==========
    protected void validateParam(Type beanType, String urlBase, String actionPath, Object[] pathVariables, Object param,
            FlutyRemoteApiRule rule) {
        // you can override
    }

    protected void validateReturn(Type beanType, String url, OptionalThing<Object> form, int httpStatus, OptionalThing<String> body,
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
    protected String buildUrl(Type beanType, String urlBase, String actionPath, Object[] pathVariables,
            OptionalThing<? extends Object> queryForm, FlutyRemoteApiRule rule) {
        assertArgumentNotNull("beanType", beanType);
        assertArgumentNotNull("urlBase", urlBase);
        assertArgumentNotNull("actionPath", actionPath);
        assertArgumentNotNull("pathVariables", pathVariables);
        assertArgumentNotNull("queryForm", queryForm);
        assertArgumentNotNull("rule", rule);
        final StringBuilder sb = new StringBuilder();
        sb.append(urlBase);
        sb.append(actionPath);
        if (pathVariables.length > 0) {
            sb.append("/");
            sb.append(buildPathVariablePart(beanType, urlBase, actionPath, pathVariables, queryForm, rule));
        }
        queryForm.ifPresent(form -> {
            buildQueryParameter(sb, beanType, form, rule);
        });
        return sb.toString();
    }

    protected String buildPathVariablePart(Type beanType, String urlBase, String actionPath, Object[] pathVariables,
            OptionalThing<? extends Object> queryForm, FlutyRemoteApiRule rule) {
        final String encoding = rule.getPathVariableCharset().name();
        return Stream.of(pathVariables).map(el -> {
            if (el == null) {
                throwRemoteApiPathVariableNullElementException(beanType, urlBase, actionPath, pathVariables, queryForm, rule);
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
    protected void buildQueryParameter(StringBuilder sb, Type beanType, Object form, FlutyRemoteApiRule rule) {
        final QueryParameterSender sender = rule.getQueryParameterSender().orElseThrow(() -> {
            return createRemoteApiSenderOfQueryParameterNotFoundException(sb, beanType, form, rule);
        });
        sb.append(sender.toQueryString(form, rule.getQueryParameterCharset()));
    }

    // ===================================================================================
    //                                                                   Response Handling
    //                                                                   =================
    protected <RETURN> RETURN handleResponse(Type beanType, String url, OptionalThing<Object> param, CloseableHttpResponse response,
            FlutyRemoteApiRule rule) throws IOException {
        final int httpStatus = response.getStatusLine().getStatusCode();
        final OptionalThing<String> body = extractResponseBody(response, rule);
        try {
            final RETURN ret = parseResponse(beanType, url, param, httpStatus, body, rule);
            validateReturn(beanType, url, param, httpStatus, body, ret, rule);
            return ret;
        } catch (RemoteApiHttpBasisErrorException cause) {
            cause.getFailureResponse().ifPresent(failureResponse -> { // don't forget it
                validateReturn(beanType, url, param, httpStatus, body, failureResponse, rule);
            });
            if (cause instanceof RemoteApiHttpClientErrorException) {
                throwTranslatedClientErrorIfNeeds(beanType, url, param, rule, httpStatus, body, (RemoteApiHttpClientErrorException) cause);
            }
            throw cause;
        }
    }

    protected void throwTranslatedClientErrorIfNeeds(Type beanType, String url, OptionalThing<Object> param, FlutyRemoteApiRule rule,
            int httpStatus, OptionalThing<String> body, RemoteApiHttpClientErrorException cause) {
        rule.getClientErrorTranslator().ifPresent(translator -> {
            final ClientErrorTranslatingResource resource = createRemoteApiClientErrorResource(beanType, url, cause);
            RuntimeException translated = null;
            try {
                translated = translator.translate(resource);
            } catch (RuntimeException e) {
                throwRemoteApiErrorTranslationFailureException(beanType, url, param, rule, httpStatus, body, cause, e);
            }
            if (translated != null) {
                throw translated;
            }
        });
    }

    protected ClientErrorTranslatingResource createRemoteApiClientErrorResource(Type beanType, String url,
            RemoteApiHttpClientErrorException cause) {
        final VaErrorHook errorHook = ThreadCacheContext.findValidatorErrorHook(); // null allowed
        return new ClientErrorTranslatingResource(beanType, url, errorHook, cause);
    }

    protected <RETURN> RETURN parseResponse(Type beanType, String url, OptionalThing<Object> form, int httpStatus,
            OptionalThing<String> body, FlutyRemoteApiRule rule) {
        logger.debug("#flow #remote ...Receiving response as {} from Remote API:\n{}\n as {}\n{}", httpStatus, url, beanType,
                body.orElse("(no body)"));
        if (httpStatus >= 200 && httpStatus < 300) {
            final RETURN ret = toResponseReturn(beanType, url, form, httpStatus, body, rule);
            return ret;
        } else if (httpStatus >= 400 && httpStatus < 500) { // e.g. not found, bad request
            final RemoteApiFailureResponseHolder failureResponseHolder = holdFailureResponse(beanType, url, form, httpStatus, body, rule);
            throwRemoteApiHttpClientErrorException(beanType, url, form, httpStatus, body, failureResponseHolder);
        } else { // e.g. 500, unknown error
            final RemoteApiFailureResponseHolder failureResponseHolder = holdFailureResponse(beanType, url, form, httpStatus, body, rule);
            throwRemoteApiHttpServerErrorException(beanType, url, form, httpStatus, body, failureResponseHolder);
        }
        return null; // unreachable
    }

    // -----------------------------------------------------
    //                                      Failure Response
    //                                      ----------------
    protected RemoteApiFailureResponseHolder holdFailureResponse(Type beanType, String url, OptionalThing<Object> form, int httpStatus,
            OptionalThing<String> body, FlutyRemoteApiRule rule) {
        Object failureResponse = null;
        Supplier<RuntimeException> emptyResponseCause = null; // null allowed
        try {
            failureResponse = parseFailureResponse(url, form, httpStatus, body, rule);
            if (failureResponse == null) { // when no rule
                emptyResponseCause = () -> createRemoteApiFailureResponseTypeNotFoundException(beanType, url, form, httpStatus, body, rule);
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
    protected <RETURN> RETURN toResponseReturn(Type beanType, String url, OptionalThing<Object> form, int httpStatus,
            OptionalThing<String> body, FlutyRemoteApiRule rule) {
        if (isVoid(beanType)) { // e.g. doRequestPost(void.class, ...);
            @SuppressWarnings("unchecked")
            final RETURN ret = (RETURN) VOID_OBJ;
            return ret; // no look body here
        }
        final ResponseBodyReceiver receiver = rule.getResponseBodyReceiver().orElseThrow(() -> {
            return createRemoteApiReceiverOfResponseBodyNotFoundException(beanType, url, form, httpStatus, body, rule);
        });
        try {
            return receiver.toResponseReturn(body, beanType);
        } catch (RuntimeException e) {
            throwRemoteApiResponseParseFailureException(beanType, url, form, httpStatus, body, receiver, e);
            return null; // unreachable
        }
    }

    protected boolean isVoid(Type beanType) {
        return Void.class.equals(beanType) || void.class.equals(beanType);
    }

    // ===================================================================================
    //                                                                       Request Error
    //                                                                       =============
    // -----------------------------------------------------
    //                                 Path Variable Failure
    //                                 ---------------------
    protected void throwRemoteApiPathVariableNullElementException(Type beanType, String urlBase, String actionPath, Object[] pathVariables,
            OptionalThing<? extends Object> queryForm, FlutyRemoteApiRule rule) {
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
        setupRequestInfo(br, beanType, urlBase + actionPath, queryForm);
        setupYourRule(br, rule);
        setupCallerExpression(br);
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
        //setupRequestInfo(br, beanType, url, optOrParam);
        br.addItem("Client Error");
        br.addElement(clientError.getMessage());
        setupCallerExpression(br);
        final String msg = br.buildExceptionMessage();
        throw new RemoteApiRetryReadyFailureException(msg, cause);
    }

    // ===================================================================================
    //                                                                      Response Error
    //                                                                      ==============
    // -----------------------------------------------------
    //                                           HTTP Status
    //                                           -----------
    protected void throwRemoteApiHttpClientErrorException(Type beanType, String url, OptionalThing<Object> form, int httpStatus,
            OptionalThing<String> body, RemoteApiFailureResponseHolder failureResponseHolder) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Client Error as HTTP status from the remote API.");
        setupRequestInfo(br, beanType, url, form);
        setupResponseInfo(br, httpStatus, body);
        setupCallerExpression(br);
        final String msg = br.buildExceptionMessage();
        throw new RemoteApiHttpClientErrorException(msg, httpStatus, failureResponseHolder);
    }

    protected void throwRemoteApiHttpServerErrorException(Type beanType, String url, OptionalThing<Object> form, int httpStatus,
            OptionalThing<String> body, RemoteApiFailureResponseHolder failureResponseHolder) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Server Error as HTTP status from the remote API.");
        setupRequestInfo(br, beanType, url, form);
        setupResponseInfo(br, httpStatus, body);
        setupCallerExpression(br);
        final String msg = br.buildExceptionMessage();
        throw new RemoteApiHttpServerErrorException(msg, httpStatus, failureResponseHolder);
    }

    // -----------------------------------------------------
    //                                          IO Exception
    //                                          ------------
    protected void handleRemoteApiIOException(Type beanType, String url, OptionalThing<Object> form, IOException cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("IO Error to the remote API.");
        setupRequestInfo(br, beanType, url, form);
        setupCallerExpression(br);
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
        setupCallerExpression(br);
        final String msg = br.buildExceptionMessage();
        throw new RemoteApiResponseParseFailureException(msg, e);
    }

    // -----------------------------------------------------
    //                                   Translation Failure
    //                                   -------------------
    protected void throwRemoteApiErrorTranslationFailureException(Type beanType, String url, OptionalThing<Object> param,
            FlutyRemoteApiRule rule, int httpStatus, OptionalThing<String> body, RemoteApiHttpClientErrorException clientError,
            RuntimeException translationEx) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to translate client error.");
        br.addItem("Advice");
        br.addElement("Confirm your logic of rule.translateClientError().");
        setupRequestInfo(br, beanType, url, param);
        setupResponseInfo(br, httpStatus, body);
        setupYourRule(br, rule);
        setupCallerExpression(br);
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
    protected RuntimeException createRemoteApiSenderOfQueryParameterNotFoundException(StringBuilder sb, Type beanType, Object form,
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
        setupRequestInfo(br, beanType, url, form);
        setupYourRule(br, rule);
        setupCallerExpression(br);
        final String msg = br.buildExceptionMessage();
        return new RemoteApiSenderOfQueryParameterNotFoundException(msg);
    }

    protected RuntimeException createRemoteApiSenderOfRequestBodyNotFoundException(Type beanType, String url, Object param,
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
        setupRequestInfo(br, beanType, url, param);
        setupYourRule(br, rule);
        br.addItem("HTTP Method");
        br.addElement(httpMethod);
        setupCallerExpression(br);
        final String msg = br.buildExceptionMessage();
        return new RemoteApiSenderOfRequestBodyNotFoundException(msg);
    }

    protected RuntimeException createRemoteApiReceiverOfResponseBodyNotFoundException(Type beanType, String url, OptionalThing<Object> form,
            int httpStatus, OptionalThing<String> body, FlutyRemoteApiRule rule) {
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
        setupRequestInfo(br, beanType, url, form);
        setupResponseInfo(br, httpStatus, body);
        setupYourRule(br, rule);
        setupCallerExpression(br);
        final String msg = br.buildExceptionMessage();
        return new RemoteApiReceiverOfResponseBodyNotFoundException(msg);
    }

    protected RuntimeException createRemoteApiFailureResponseTypeNotFoundException(Type beanType, String url, OptionalThing<Object> form,
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
        setupRequestInfo(br, beanType, url, form);
        setupResponseInfo(br, httpStatus, body);
        setupYourRule(br, rule);
        final String msg = br.buildExceptionMessage();
        return new RemoteApiFailureResponseTypeNotFoundException(msg);
    }

    // ===================================================================================
    //                                                                      Message Helper
    //                                                                      ==============
    protected void setupRequestInfo(ExceptionMessageBuilder br, Type beanType, String url, Object optOrParam) {
        setupBeanAndRemoteApi(br, beanType, url);
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

    protected void setupBeanAndRemoteApi(ExceptionMessageBuilder br, Type beanType, String url) {
        br.addItem("Bean Type");
        br.addElement(beanType);
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

    protected void setupCallerExpression(final ExceptionMessageBuilder br) {
        br.addItem("Caller Expression");
        br.addElement(callerExp);
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    protected void setupHeader(HttpMessage httpMessage, FlutyRemoteApiRule rule) {
        rule.getHeaders().ifPresent(map -> map.forEach((name, valueList) -> {
            valueList.forEach(value -> {
                httpMessage.addHeader(name, value);
            });
        }));
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
