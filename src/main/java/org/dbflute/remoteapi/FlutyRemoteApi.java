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
import java.lang.reflect.Type;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.http.HttpMessage;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.optional.OptionalThing;
import org.dbflute.remoteapi.exception.RemoteApiHttpClientErrorException;
import org.dbflute.remoteapi.exception.RemoteApiHttpServerErrorException;
import org.dbflute.remoteapi.exception.RemoteApiReceiverOfResponseBodyNotFoundException;
import org.dbflute.remoteapi.exception.RemoteApiRequestFailureException;
import org.dbflute.remoteapi.exception.RemoteApiSenderOfQueryParameterNotFoundException;
import org.dbflute.remoteapi.exception.RemoteApiSenderOfRequestBodyNotFoundException;
import org.dbflute.remoteapi.receiver.ResponseBodyReceiver;
import org.dbflute.remoteapi.sender.body.RequestBodySender;
import org.dbflute.remoteapi.sender.query.QueryParameterSender;
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
     * @param <RESULT> The type of request result (response).
     * @param beanType The class type of bean to convert, should have default constructor. (NotNull)
     * @param urlBase The base part of URL to remote API server. e.g. http://localhost:8090/harbor (NotNull)
     * @param actionPath The path to action without URL parameter, and trailing slash is no difference. e.g. /sea/land (NotNull)
     * @param pathVariables The array of URL path variables, e.g. ["hangar", 3]. (NotNull, EmptyAllowed)
     * @param queryForm The optional form of query (GET parameters). (NotNull, EmptyAllowed)
     * @param ruleLambda The callback for rule of remote API. (NotNull)
     * @return The JSON result of response as result, returned from the request. (NotNull)
     */
    public <RESULT> RESULT requestGet(Type beanType, String urlBase, String actionPath, Object[] pathVariables,
            OptionalThing<Object> queryForm, Consumer<FlutyRemoteApiRule> ruleLambda) {
        assertArgumentNotNull("beanType", beanType);
        assertArgumentNotNull("urlBase", urlBase);
        assertArgumentNotNull("actionPath", actionPath);
        assertArgumentNotNull("pathVariables", pathVariables);
        assertArgumentNotNull("queryForm", queryForm);
        assertArgumentNotNull("ruleLambda", ruleLambda);
        queryForm.ifPresent(form -> validateForm(beanType, urlBase, actionPath, pathVariables, form));
        final FlutyRemoteApiRule option = createRemoteApiRule(ruleLambda);
        final String url = buildUrl(urlBase, actionPath, pathVariables, queryForm, option);
        logger.debug("#flow #remote ...Requesting as GET to Remote API:\n {}\n   => {}", url, beanType);
        return doRequestGet(beanType, url, option);
    }

    protected <RESULT> RESULT doRequestGet(Type beanType, String url, FlutyRemoteApiRule rule) {
        try (CloseableHttpClient httpClient = buildHttpClient(rule)) {
            final HttpGet httpGet = prepareHttpGet(url, rule);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                return handleResponse(beanType, url, /*form*/OptionalThing.empty(), response, rule);
            }
        } catch (IOException e) {
            handleRemoteIOException(beanType, url, /*form*/OptionalThing.empty(), e);
            return null; // unreachable
        }
    }

    protected HttpGet prepareHttpGet(String uri, FlutyRemoteApiRule rule) {
        final HttpGet httpGet = new HttpGet(uri);
        setupHeader(httpGet, rule);
        return httpGet;
    }

    // ===================================================================================
    //                                                                        Request POST
    //                                                                        ============
    /**
     * @param <RESULT> The type of request result (response).
     * @param beanType The class type of bean to convert, should have default constructor. (NotNull)
     * @param urlBase The base part of URL to remote API server. e.g. http://localhost:8090/harbor (NotNull)
     * @param actionPath The path to action without URL parameter, and trailing slash is no difference. e.g. /sea/land (NotNull)
     * @param pathVariables The array of URL path variables, e.g. ["hangar", 3]. (NotNull, EmptyAllowed)
     * @param form The form of POST parameters, or request body if Body class. (NotNull)
     * @param ruleLambda The callback for rule of remote API. (NotNull)
     * @return The JSON result of response as result, returned from the request. (NotNull)
     */
    public <RESULT> RESULT requestPost(Type beanType, String urlBase, String actionPath, Object[] pathVariables, Object form,
            Consumer<FlutyRemoteApiRule> ruleLambda) {
        assertArgumentNotNull("beanType", beanType);
        assertArgumentNotNull("urlBase", urlBase);
        assertArgumentNotNull("actionPath", actionPath);
        assertArgumentNotNull("pathVariables", pathVariables);
        assertArgumentNotNull("form", form);
        assertArgumentNotNull("ruleLambda", ruleLambda);
        validateForm(beanType, urlBase, actionPath, pathVariables, form);
        final FlutyRemoteApiRule rule = createRemoteApiRule(ruleLambda);
        final String url = buildUrl(urlBase, actionPath, pathVariables, OptionalThing.empty(), rule);
        logger.debug("#flow #remote ...Requesting as POST to Remote API:\n: {}\n with form: {}\n   => {}: ", url, form, beanType);
        return doRequestPost(beanType, url, form, rule);
    }

    protected <RESULT> RESULT doRequestPost(Type beanType, String url, Object form, FlutyRemoteApiRule rule) {
        try (CloseableHttpClient httpClient = buildHttpClient(rule)) {
            final HttpPost httpPost = prepareHttpPost(beanType, url, form, rule);
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                return handleResponse(beanType, url, OptionalThing.of(form), response, rule);
            }
        } catch (IOException e) {
            handleRemoteIOException(beanType, url, OptionalThing.of(form), e);
        }
        return null;
    }

    protected HttpPost prepareHttpPost(Type beanType, String url, Object form, FlutyRemoteApiRule rule) {
        final HttpPost httpPost = new HttpPost(url);
        setupHeader(httpPost, rule);
        final RequestBodySender converter = rule.getRequestBodySender().orElseThrow(() -> {
            return createRemoteApiSenderOfRequestBodyNotFoundException(beanType, url, form, rule);
        });
        converter.prepareBodyRequest(httpPost, form);
        return httpPost;
    }

    protected RuntimeException createRemoteApiSenderOfRequestBodyNotFoundException(Type beanType, String url, Object form,
            FlutyRemoteApiRule rule) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the sender for request body.");
        br.addItem("Advice");
        br.addElement("Request body sender is required for e.g. JSON response.");
        br.addElement("Set sender to your rule like this:");
        br.addElement("  (o):");
        br.addElement("    protected void yourDefaultRule(FlutyRemoteApiRule rule) {");
        br.addElement("        rule.sendBodyBy(new LaJsonSender());");
        br.addElement("    }");
        br.addElement("  (o):");
        br.addElement("    doRequestGet(..., rule -> rule.sendBodyBy(new LaJsonSender());");
        br.addItem("Bean Type");
        br.addElement(beanType);
        br.addItem("Request URL");
        br.addElement(url);
        br.addItem("Form");
        br.addElement(form);
        br.addItem("Your Rule");
        br.addElement(rule);
        setupCallerExpression(br);
        final String msg = br.buildExceptionMessage();
        return new RemoteApiSenderOfRequestBodyNotFoundException(msg);
    }

    // ===================================================================================
    //                                                                          Validation
    //                                                                          ==========
    protected void validateForm(Type beanType, String urlBase, String actionPath, Object[] pathVariables, Object form) {
        // you can override
    }

    protected void validateResult(Type beanType, String url, OptionalThing<Object> form, int httpStatus, String body, Object result,
            FlutyRemoteApiRule ruledRemoteApiOption) {
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
    protected String buildUrl(String urlBase, String actionPath, Object[] pathVariables, OptionalThing<Object> queryForm,
            FlutyRemoteApiRule rule) {
        assertArgumentNotNull("urlBase", urlBase);
        assertArgumentNotNull("actionPath", actionPath);
        assertArgumentNotNull("pathVariables", pathVariables);
        assertArgumentNotNull("queryForm", queryForm);
        assertArgumentNotNull("rule", rule);
        final StringBuilder sb = new StringBuilder();
        sb.append(urlBase);
        sb.append(actionPath);
        if (pathVariables.length != 0) {
            sb.append("/");
            sb.append(Stream.of(pathVariables).map(el -> el.toString()).collect(Collectors.joining("/")));
        }
        queryForm.ifPresent(form -> {
            buildQueryParameter(sb, form, rule);
        });
        return sb.toString();
    }

    // ===================================================================================
    //                                                                     Query Parameter
    //                                                                     ===============
    protected void buildQueryParameter(StringBuilder sb, Object form, FlutyRemoteApiRule rule) {
        final QueryParameterSender sender = rule.getQueryParameterSender().orElseThrow(() -> {
            return createRemoteApiSenderOfQueryParameterNotFoundException(sb, form, rule);
        });
        sb.append(sender.toQueryString(form, rule.getQueryParameterCharset()));
    }

    protected RuntimeException createRemoteApiSenderOfQueryParameterNotFoundException(StringBuilder sb, Object form,
            FlutyRemoteApiRule rule) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the sender for query parameter.");
        br.addItem("Advice");
        br.addElement("Query parameter sender is required for e.g. GET request.");
        br.addElement("Set sender to your rule like this:");
        br.addElement("  (o):");
        br.addElement("    protected void yourDefaultRule(FlutyRemoteApiRule rule) {");
        br.addElement("        rule.sendQueryBy(new LaQuerySender());");
        br.addElement("    }");
        br.addElement("  (o):");
        br.addElement("    doRequestGet(..., rule -> rule.sendQueryBy(new LaQuerySender()));");
        br.addItem("Request URL");
        br.addElement(sb.toString()); // just it
        br.addItem("Query Form");
        br.addElement(form);
        br.addItem("Your Rule");
        br.addElement(rule);
        setupCallerExpression(br);
        final String msg = br.buildExceptionMessage();
        return new RemoteApiSenderOfQueryParameterNotFoundException(msg);
    }

    // ===================================================================================
    //                                                                   Response Handling
    //                                                                   =================
    protected <RESULT> RESULT handleResponse(Type beanType, String url, OptionalThing<Object> form, CloseableHttpResponse response,
            FlutyRemoteApiRule rule) throws IOException {
        final int httpStatus = response.getStatusLine().getStatusCode();
        final String body = extractResponseBody(response, rule);
        final RESULT resut = parseResponse(beanType, url, form, httpStatus, body, rule);
        validateResult(beanType, url, form, httpStatus, body, resut, rule);
        return resut;
    }

    protected <RESULT> RESULT parseResponse(Type beanType, String url, OptionalThing<Object> form, int httpStatus, String body,
            FlutyRemoteApiRule rule) {
        logger.debug("#flow #remote ...Parsing response to Remote API:\n {}\n   => {}\n{}", url, beanType, body);
        if (httpStatus >= 200 && httpStatus < 300) {
            final RESULT result = toResult(beanType, url, form, httpStatus, body, rule);
            return result;
        } else if (httpStatus >= 400 && httpStatus < 500) { // e.g. not found, bad request
            final Object failureResponse = prepareFailureResponse(url, form, httpStatus, body, rule);
            throwRemoteApiHttpClientErrorException(beanType, url, form, httpStatus, body, failureResponse);
        } else { // might be framework error
            throwRemoteApiHttpServerErrorException(beanType, url, form, httpStatus, body);
        }
        return null; // unreachable
    }

    protected Object prepareFailureResponse(String url, OptionalThing<Object> form, int httpStatus, String body, FlutyRemoteApiRule rule) {
        return rule.getFailureResponseType().map(failureResponseType -> {
            // #hope jflute parse failure should be warning? because of already failureo
            return toResult(failureResponseType, url, form, httpStatus, body, rule);
        }).orElse(null); // when no rule
    }

    protected <RESULT> RESULT toResult(Type beanType, String url, OptionalThing<Object> form, int httpStatus, String body,
            FlutyRemoteApiRule rule) {
        final ResponseBodyReceiver converter = rule.getResponseBodyReceiver().orElseThrow(() -> {
            return createRemoteApiReceiverOfResponseBodyNotFoundException(beanType, url, form, httpStatus, body, rule);
        });
        try {
            return converter.toResult(body, beanType);
        } catch (RuntimeException e) {
            throwRemoteApiResponseCannotParseException(beanType, url, form, httpStatus, body, e);
            return null; // unreachable
        }
    }

    protected RuntimeException createRemoteApiReceiverOfResponseBodyNotFoundException(Type beanType, String url, OptionalThing<Object> form,
            int httpStatus, String body, FlutyRemoteApiRule rule) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the receiver for response body.");
        br.addItem("Advice");
        br.addElement("Response body receiver is required for e.g. JSON response.");
        br.addElement("Set receiver to your rule like this:");
        br.addElement("  (o):");
        br.addElement("    protected void yourDefaultRule(FlutyRemoteApiRule rule) {");
        br.addElement("        rule.receiveBodyBy(new LaJsonReceiver());");
        br.addElement("    }");
        br.addElement("  (o):");
        br.addElement("    doRequestGet(..., rule -> rule.receiveBodyBy(new LaJsonReceiver()));");
        br.addItem("BeanType");
        br.addElement(beanType);
        br.addItem("Request URL");
        br.addElement(url);
        br.addItem("Form");
        br.addElement(form);
        br.addItem("Response HTTP Status");
        br.addElement(httpStatus);
        br.addItem("Response Body");
        br.addElement(body);
        br.addItem("Your Rule");
        br.addElement(rule);
        setupCallerExpression(br);
        final String msg = br.buildExceptionMessage();
        return new RemoteApiReceiverOfResponseBodyNotFoundException(msg);
    }

    // ===================================================================================
    //                                                                      Error Handling
    //                                                                      ==============
    // -----------------------------------------------------
    //                                           HTTP Status
    //                                           -----------
    protected void throwRemoteApiHttpClientErrorException(Type beanType, String url, OptionalThing<Object> form, int httpStatus,
            String body, Object failureResponse) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Client Error as HTTP status from the remote API.");
        setupRequestInfo(br, beanType, url, form);
        setupResponseInfo(br, httpStatus, body);
        setupCallerExpression(br);
        final String msg = br.buildExceptionMessage();
        throw new RemoteApiHttpClientErrorException(msg, httpStatus, failureResponse);
    }

    protected void throwRemoteApiHttpServerErrorException(Type beanType, String url, OptionalThing<Object> form, int httpStatus,
            String body) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Server Error as HTTP status from the remote API.");
        setupRequestInfo(br, beanType, url, form);
        setupResponseInfo(br, httpStatus, body);
        setupCallerExpression(br);
        final String msg = br.buildExceptionMessage();
        throw new RemoteApiHttpServerErrorException(msg, httpStatus);
    }

    // -----------------------------------------------------
    //                                          IO Exception
    //                                          ------------
    protected void handleRemoteIOException(Type beanType, String url, OptionalThing<Object> form, IOException cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("IO Error to the remote API.");
        setupRequestInfo(br, beanType, url, form);
        setupCallerExpression(br);
        final String msg = br.buildExceptionMessage();
        throw new RemoteApiRequestFailureException(msg, cause);
    }

    // -----------------------------------------------------
    //                                          Cannot Parse
    //                                          ------------
    protected void throwRemoteApiResponseCannotParseException(Type type, String url, OptionalThing<Object> form, int httpStatus,
            String body, RuntimeException e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to parse the JSON of remote API.");
        setupRequestInfo(br, type, url, form);
        setupResponseInfo(br, httpStatus, body);
        setupCallerExpression(br);
        final String msg = br.buildExceptionMessage();
        throw new RemoteApiRequestFailureException(msg, e);
    }

    // -----------------------------------------------------
    //                                        Message Helper
    //                                        --------------
    protected void setupRequestInfo(ExceptionMessageBuilder br, Type beanType, String url, OptionalThing<Object> optForm) {
        br.addItem("Bean Type");
        br.addElement(beanType);
        br.addItem("Remote API");
        br.addElement(url);
        optForm.ifPresent(form -> {
            br.addItem("Request Form (or Body)");
            br.addElement(convertFormToDebugString(form));
        });
    }

    protected String convertFormToDebugString(Object form) {
        return form.toString(); // as default
    }

    protected void setupResponseInfo(ExceptionMessageBuilder br, int httpStatus, String body) {
        br.addItem("Response HTTP Status");
        br.addElement(httpStatus);
        br.addItem("Response Body");
        br.addElement(body);
    }

    protected <RESULT> void setupResultInfo(ExceptionMessageBuilder br, RESULT result) {
        br.addItem("Result");
        br.addElement(result);
    }

    protected void setupCallerExpression(final ExceptionMessageBuilder br) {
        br.addItem("Caller Expression");
        br.addElement(callerExp);
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    protected void setupHeader(HttpMessage httpMessage, FlutyRemoteApiRule rule) {
        rule.getHeaders().ifPresent(map -> map.forEach((name, value) -> {
            httpMessage.addHeader(name, value);
        }));
    }

    protected String extractResponseBody(CloseableHttpResponse response, FlutyRemoteApiRule rule) throws IOException {
        return EntityUtils.toString(response.getEntity(), rule.getResponseBodyCharset());
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
