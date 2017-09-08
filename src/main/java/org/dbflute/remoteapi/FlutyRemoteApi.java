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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.http.HttpMessage;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.dbflute.helper.beans.DfBeanDesc;
import org.dbflute.helper.beans.DfPropertyDesc;
import org.dbflute.helper.beans.factory.DfBeanDescFactory;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.jdbc.Classification;
import org.dbflute.optional.OptionalThing;
import org.dbflute.remoteapi.exception.RemoteApiHttpClientErrorException;
import org.dbflute.remoteapi.exception.RemoteApiHttpServerErrorException;
import org.dbflute.remoteapi.exception.RemoteApiRequestFailureException;
import org.dbflute.remoteapi.rule.FlutyRemoteConversionRule;
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
    protected final Consumer<FlutyRemoteApiOption> defaultOpLambda;
    protected final Object callerExp;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public FlutyRemoteApi(Consumer<FlutyRemoteApiOption> defaultOpLambda, Object callerExp) {
        this.defaultOpLambda = defaultOpLambda;
        this.callerExp = callerExp;
    }

    // ===================================================================================
    //                                                                         Request GET
    //                                                                         ===========
    /**
     * @param <RESULT> The type of result.
     * @param beanType The class type of bean to convert, should have default constructor. (NotNull)
     * @param urlBase The base part of URL to remote API server. e.g. http://localhost:8090/harbor (NotNull)
     * @param actionPath The path to action without URL parameter, and trailing slash is no difference. e.g. /sea/land (NotNull)
     * @param pathVariables The array of URL path variables, e.g. ["hangar", 3]. (NotNull, EmptyAllowed)
     * @param queryForm The optional form of query (GET parameters). (NotNull, EmptyAllowed)
     * @param opLambda The callback for option of remote API. (NotNull)
     * @return The JSON result of response as result, returned from the request. (NotNull)
     */
    public <RESULT> RESULT requestGet(Type beanType, String urlBase, String actionPath, Object[] pathVariables,
            OptionalThing<Object> queryForm, Consumer<FlutyRemoteApiOption> opLambda) {
        assertArgumentNotNull("beanType", beanType);
        assertArgumentNotNull("urlBase", urlBase);
        assertArgumentNotNull("actionPath", actionPath);
        assertArgumentNotNull("pathVariables", pathVariables);
        assertArgumentNotNull("queryForm", queryForm);
        assertArgumentNotNull("opLambda", opLambda);
        queryForm.ifPresent(form -> validateForm(beanType, urlBase, actionPath, pathVariables, form));
        final FlutyRemoteApiOption option = createRemoteApiOption(opLambda);
        final String url = buildUrl(urlBase, actionPath, pathVariables, queryForm, option);
        logger.debug("#flow #remote ...Requesting as GET to Remote API:\n {}\n   => {}", url, beanType);
        return doRequestGet(beanType, url, option);
    }

    protected <RESULT> RESULT doRequestGet(Type beanType, String url, FlutyRemoteApiOption option) {
        try (CloseableHttpClient httpClient = buildHttpClient(option)) {
            final HttpGet httpGet = prepareHttpGet(url, option);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                return handleResponse(beanType, url, /*form*/OptionalThing.empty(), response, option);
            }
        } catch (IOException e) {
            handleRemoteIOException(beanType, url, /*form*/OptionalThing.empty(), e);
            return null; // unreachable
        }
    }

    protected HttpGet prepareHttpGet(String uri, FlutyRemoteApiOption option) {
        final HttpGet httpGet = new HttpGet(uri);
        setupHeader(httpGet, option);
        return httpGet;
    }

    // ===================================================================================
    //                                                                        Request POST
    //                                                                        ============
    /**
     * @param <RESULT> The type of result.
     * @param beanType The class type of bean to convert, should have default constructor. (NotNull)
     * @param urlBase The base part of URL to remote API server. e.g. http://localhost:8090/harbor (NotNull)
     * @param actionPath The path to action without URL parameter, and trailing slash is no difference. e.g. /sea/land (NotNull)
     * @param pathVariables The array of URL path variables, e.g. ["hangar", 3]. (NotNull, EmptyAllowed)
     * @param form The form of POST parameters, or request body if Body class. (NotNull)
     * @param opLambda The callback for option of remote API. (NotNull)
     * @return The JSON result of response as result, returned from the request. (NotNull)
     */
    public <RESULT> RESULT requestPost(Type beanType, String urlBase, String actionPath, Object[] pathVariables, Object form,
            Consumer<FlutyRemoteApiOption> opLambda) {
        assertArgumentNotNull("beanType", beanType);
        assertArgumentNotNull("urlBase", urlBase);
        assertArgumentNotNull("actionPath", actionPath);
        assertArgumentNotNull("pathVariables", pathVariables);
        assertArgumentNotNull("form", form);
        assertArgumentNotNull("opLambda", opLambda);
        validateForm(beanType, urlBase, actionPath, pathVariables, form);
        FlutyRemoteApiOption option = createRemoteApiOption(opLambda);
        final String url = buildUrl(urlBase, actionPath, pathVariables, OptionalThing.empty(), option);
        logger.debug("#flow #remote ...Requesting as POST to Remote API:\n: {}\n with form: {}\n   => {}: ", url, form, beanType);
        return doRequestPost(beanType, url, form, option);
    }

    protected <RESULT> RESULT doRequestPost(Type beanType, String url, Object form, FlutyRemoteApiOption option) {
        try (CloseableHttpClient httpClient = buildHttpClient(option)) {
            final HttpPost httpPost = prepareHttpPost(url, form, option);
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                return handleResponse(beanType, url, OptionalThing.of(form), response, option);
            }
        } catch (IOException e) {
            handleRemoteIOException(beanType, url, OptionalThing.of(form), e);
        }
        return null;
    }

    protected HttpPost prepareHttpPost(String uri, Object form, FlutyRemoteApiOption option) throws UnsupportedEncodingException {
        final HttpPost httpPost = new HttpPost(uri);
        setupHeader(httpPost, option);
        option.getRequestConverter().prepareHttpPost(httpPost, form);
        return httpPost;
    }

    // ===================================================================================
    //                                                                 HttpClient Building
    //                                                                 ===================
    protected CloseableHttpClient buildHttpClient(FlutyRemoteApiOption option) {
        return option.prepareHttpClient();
    }

    // ===================================================================================
    //                                                                        URL Building
    //                                                                        ============
    protected String buildUrl(String urlBase, String actionPath, Object[] pathVariables, OptionalThing<Object> queryForm,
            FlutyRemoteApiOption ruledRemoteApiOption) {
        assertArgumentNotNull("urlBase", urlBase);
        assertArgumentNotNull("actionPath", actionPath);
        assertArgumentNotNull("pathVariables", pathVariables);
        assertArgumentNotNull("queryForm", queryForm);
        final StringBuilder sb = new StringBuilder();
        sb.append(urlBase);
        sb.append(actionPath);
        if (pathVariables.length != 0) {
            sb.append("/");
            sb.append(Stream.of(pathVariables).map(el -> el.toString()).collect(Collectors.joining("/")));
        }
        queryForm.ifPresent(form -> {
            final String encoding = ruledRemoteApiOption.getCharset().name();
            final DfBeanDesc beanDesc = DfBeanDescFactory.getBeanDesc(form.getClass());
            final MyValueHolder<Integer> paramIndex = new MyValueHolder<>(0);
            for (String propertyName : beanDesc.getProppertyNameList()) {
                final DfPropertyDesc propertyDesc = beanDesc.getPropertyDesc(propertyName);
                final Object plainValue = propertyDesc.getValue(form);
                if (plainValue != null) {
                    if (Iterable.class.isAssignableFrom(plainValue.getClass())) {
                        Iterable<?> plainValueIterable = (Iterable<?>) plainValue;
                        plainValueIterable.forEach(value -> {
                            sb.append(paramIndex.getValue() == 0 ? "?" : "&");
                            sb.append(asSerializedParameterName(propertyDesc)).append("=");
                            try {
                                sb.append(URLEncoder.encode(asSerializedParameterValue(value, ruledRemoteApiOption), encoding));
                            } catch (UnsupportedEncodingException e) {
                                throw new IllegalStateException("Unknown encoding: " + encoding);
                            }
                        });
                    } else {
                        sb.append(paramIndex.getValue() == 0 ? "?" : "&");
                        sb.append(asSerializedParameterName(propertyDesc)).append("=");
                        try {
                            sb.append(URLEncoder.encode(asSerializedParameterValue(plainValue, ruledRemoteApiOption), encoding));
                        } catch (UnsupportedEncodingException e) {
                            throw new IllegalStateException("Unknown encoding: " + encoding);
                        }
                    }
                    paramIndex.setValue(paramIndex.getValue() + 1);
                }
            }
        });
        return sb.toString();
    }

    // ===================================================================================
    //                                                                  Parameter Handling
    //                                                                  ==================
    protected String asSerializedParameterName(DfPropertyDesc propertyDesc) { // may be overridden
        return propertyDesc.getPropertyName();
    }

    protected String asSerializedParameterValue(Object value, FlutyRemoteApiOption option) { // with standard rule filter
        if (value == null) {
            return null;
        }
        final String realValue;
        final FlutyRemoteConversionRule conversionRule = option.getConversionRule();
        if (value instanceof LocalDate) {
            realValue = ((LocalDate) value).format(conversionRule.getDateFormatter());
        } else if (value instanceof LocalDateTime) {
            realValue = ((LocalDateTime) value).format(conversionRule.getDateTimeFormatter());
        } else if (value instanceof Boolean || boolean.class.equals(value.getClass())) {
            realValue = conversionRule.serializeBoolean((boolean) value);
        } else if (value instanceof Classification) {
            final Classification cls = (Classification) value;
            final Map<String, Object> map = cls.subItemMap();
            final String preferredValue = (String) map.get(conversionRule.getClsPreferredItem());
            if (preferredValue != null) { // means Flg
                realValue = preferredValue;
            } else {
                realValue = cls.code();
            }
        } else {
            realValue = value.toString();
        }
        return realValue;
    }

    // ===================================================================================
    //                                                                   Response Handling
    //                                                                   =================
    protected <RESULT> RESULT handleResponse(Type beanType, String url, OptionalThing<Object> form, CloseableHttpResponse response,
            FlutyRemoteApiOption option) throws IOException {
        final int statusCode = response.getStatusLine().getStatusCode();
        final String body = extractResponseBody(response, option);
        final RESULT resut = parseResponse(beanType, url, form, statusCode, body, option);
        validateResult(beanType, url, form, statusCode, body, resut, option);
        return resut;
    }

    protected <RESULT> RESULT parseResponse(Type beanType, String url, OptionalThing<Object> form, int statusCode, String body,
            FlutyRemoteApiOption option) {
        logger.debug("#flow #remote ...Parsing response to Remote API:\n {}\n   => {}\n{}", url, beanType, body);
        if (statusCode >= 200 && statusCode < 300) {
            final RESULT result = toResult(beanType, url, form, statusCode, body, option);
            return result;
        } else if (statusCode >= 400 && statusCode < 500) { // e.g. not found, bad request
            final Object failureResponse = prepareFailureResponse(url, form, statusCode, body, option);
            throwRemoteApiHttpClientErrorException(beanType, url, form, statusCode, body, failureResponse);
        } else { // might be framework error
            throwRemoteApiHttpServerErrorException(beanType, url, form, statusCode, body);
        }
        return null; // unreachable
    }

    protected Object prepareFailureResponse(String url, OptionalThing<Object> form, int statusCode, String body,
            FlutyRemoteApiOption option) {
        return option.getFailureResponseType().map(failureResponseType -> {
            // #hope jflute parse failure should be warning? because of already failureo
            return toResult(failureResponseType, url, form, statusCode, body, option);
        }).orElse(null); // when no option
    }

    protected <RESULT> RESULT toResult(Type type, String url, OptionalThing<Object> form, int statusCode, String body,
            FlutyRemoteApiOption option) {
        try {
            return option.getResponseConverter().toResult(body, type);
        } catch (RuntimeException e) {
            throwRemoteApiResponseCannotParseException(type, url, form, statusCode, body, e);
            return null; // unreachable
        }
    }

    // ===================================================================================
    //                                                                      Error Handling
    //                                                                      ==============
    // -----------------------------------------------------
    //                                           HTTP Status
    //                                           -----------
    protected void throwRemoteApiHttpClientErrorException(Type beanType, String url, OptionalThing<Object> form, int statusCode,
            String body, Object failureResponse) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Client Error as HTTP status from the remote API.");
        setupRequestInfo(br, beanType, url, form);
        setupResponseInfo(br, statusCode, body);
        final String msg = br.buildExceptionMessage();
        throw new RemoteApiHttpClientErrorException(msg, failureResponse);
    }

    protected void throwRemoteApiHttpServerErrorException(Type beanType, String url, OptionalThing<Object> form, int statusCode,
            String body) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Server Error as HTTP status from the remote API.");
        setupRequestInfo(br, beanType, url, form);
        setupResponseInfo(br, statusCode, body);
        final String msg = br.buildExceptionMessage();
        throw new RemoteApiHttpServerErrorException(msg);
    }

    // -----------------------------------------------------
    //                                          IO Exception
    //                                          ------------
    protected void handleRemoteIOException(Type beanType, String url, OptionalThing<Object> form, IOException cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("IO Error to the remote API.");
        setupRequestInfo(br, beanType, url, form);
        final String msg = br.buildExceptionMessage();
        throw new RemoteApiRequestFailureException(msg, cause);
    }

    // -----------------------------------------------------
    //                                          Cannot Parse
    //                                          ------------
    protected void throwRemoteApiResponseCannotParseException(Type type, String url, OptionalThing<Object> form, int statusCode,
            String body, RuntimeException e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to parse the JSON of remote API.");
        setupRequestInfo(br, type, url, form);
        setupResponseInfo(br, statusCode, body);
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

    protected void setupResponseInfo(ExceptionMessageBuilder br, int statusCode, String body) {
        br.addItem("Response Status Code");
        br.addElement(statusCode);
        br.addItem("Response Body");
        br.addElement(body);
    }

    protected <RESULT> void setupResultInfo(ExceptionMessageBuilder br, RESULT result) {
        br.addItem("Result");
        br.addElement(result);
    }

    // ===================================================================================
    //                                                                          Validation
    //                                                                          ==========
    protected void validateForm(Type beanType, String urlBase, String actionPath, Object[] pathVariables, Object form) {
        // you can override
    }

    protected void validateResult(Type beanType, String url, OptionalThing<Object> form, int statusCode, String body, Object result,
            FlutyRemoteApiOption ruledRemoteApiOption) {
        // you can override
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    protected FlutyRemoteApiOption createRemoteApiOption(Consumer<FlutyRemoteApiOption> opLambda) {
        final FlutyRemoteApiOption option = newFlutyRemoteApiOption();
        defaultOpLambda.accept(option);
        opLambda.accept(option);
        return option;
    }

    protected FlutyRemoteApiOption newFlutyRemoteApiOption() {
        return new FlutyRemoteApiOption();
    }

    protected void setupHeader(HttpMessage httpMessage, FlutyRemoteApiOption option) {
        option.getHeaders().ifPresent(map -> map.forEach((name, value) -> {
            httpMessage.addHeader(name, value);
        }));
    }

    protected String extractResponseBody(CloseableHttpResponse response, FlutyRemoteApiOption option) throws IOException {
        return EntityUtils.toString(response.getEntity(), option.getCharset());
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

    protected static class MyValueHolder<T> { // copied from Lasta Di

        protected T value;

        public MyValueHolder() {
        }

        public MyValueHolder(T value) {
            this.value = value;
        }

        public T getValue() {
            return value;
        }

        public void setValue(final T value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value.toString();
        }
    }
}
