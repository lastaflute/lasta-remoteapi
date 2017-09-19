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

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.dbflute.optional.OptionalThing;
import org.dbflute.remoteapi.receiver.ResponseBodyReceiver;
import org.dbflute.remoteapi.sender.body.RequestBodySender;
import org.dbflute.remoteapi.sender.query.QueryParameterSender;
import org.dbflute.util.DfCollectionUtil;

/**
 * The rule of remote API.
 * @author awane
 * @author jflute
 */
public class FlutyRemoteApiRule {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                         Required Rule
    //                                         -------------
    protected QueryParameterSender queryParameterSender; // null allowed, but required
    protected RequestBodySender requestBodySender; // null allowed, but required
    protected ResponseBodyReceiver responseBodyReceiver; // null allowed, but required

    // -----------------------------------------------------
    //                                         Optional Rule
    //                                         -------------
    // default values are defined here
    protected boolean sslUntrusted;
    protected int connectTimeout = 3000;
    protected int connectionRequestTimeout = 3000;
    protected int socketTimeout = 3000;
    protected Charset pathVariableCharset = StandardCharsets.UTF_8; // not null
    protected Charset queryParameterCharset = StandardCharsets.UTF_8; // not null
    protected Charset requestBodyCharset = StandardCharsets.UTF_8; // not null
    protected Charset responseBodyCharset = StandardCharsets.UTF_8; // not null
    protected Map<String, List<String>> headers; // null allowed, not required, lazy-loaded
    protected Type failureResponseType; // null allowed, not required

    // #hope jflute can accept response header, interface? mapping? (2017/09/13)
    // #hope jflute validation on/off/warning option (2017/09/13)
    // #hope jflute request trace ID option (2017/09/13)
    // #hope jflute improve tracebility like DBFlute (2017/09/13)

    // ===================================================================================
    //                                                                         Http Client
    //                                                                         ===========
    public CloseableHttpClient prepareHttpClient() { // not null
        if (__xmockHttpClient != null) {
            return __xmockHttpClient;
        }
        final RequestConfig.Builder requestBuilder = RequestConfig.custom();
        requestBuilder.setConnectTimeout(getConnectTimeout());
        requestBuilder.setConnectionRequestTimeout(getConnectionRequestTimeout());
        requestBuilder.setSocketTimeout(getSocketTimeout());
        final HttpClientBuilder httpClientBuilder = HttpClients.custom();
        if (isSslUntrusted()) {
            final TrustStrategy trustStrategy = new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            };
            final SSLContext sslContext;
            try {
                sslContext = SSLContexts.custom().loadTrustMaterial(trustStrategy).build();
            } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
                throw new IllegalStateException("Failed to build SSL context: trustStrategy=" + trustStrategy, e);
            }
            httpClientBuilder.setSSLContext(sslContext).setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
        }
        return httpClientBuilder.setDefaultRequestConfig(requestBuilder.build()).build();
    }

    // ===================================================================================
    //                                                                      Setting Facade
    //                                                                      ==============
    // -----------------------------------------------------
    //                                         Required Rule
    //                                         -------------
    /**
     * @param queryParameterSender The sender of (request) query parameter. (NotNull)
     */
    public void sendQueryBy(QueryParameterSender queryParameterSender) {
        assertArgumentNotNull("queryParameterSender", queryParameterSender);
        this.queryParameterSender = queryParameterSender;
    }

    /**
     * @param requestBodySender The sender of request body. (NotNull)
     */
    public void sendBodyBy(RequestBodySender requestBodySender) {
        assertArgumentNotNull("requestBodySender", requestBodySender);
        this.requestBodySender = requestBodySender;
    }

    /**
     * @param responseBodyReceiver The receiver of response body. (NotNull)
     */
    public void receiveBodyBy(ResponseBodyReceiver responseBodyReceiver) {
        assertArgumentNotNull("responseBodyReceiver", responseBodyReceiver);
        this.responseBodyReceiver = responseBodyReceiver;
    }

    // -----------------------------------------------------
    //                                         Optional Rule
    //                                         -------------
    public void setSslUntrusted(boolean sslUntrusted) {
        this.sslUntrusted = sslUntrusted;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public void setConnectionRequestTimeout(int connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    /**
     * @param pathVariableCharset The charset of request path variable. (NotNull)
     */
    public void encodeRequestPathVariableAs(Charset pathVariableCharset) {
        assertArgumentNotNull("pathVariableCharset", pathVariableCharset);
        this.pathVariableCharset = pathVariableCharset;
    }

    /**
     * @param requestQueryCharset The charset of request query parameter. (NotNull)
     */
    public void encodeRequestQueryAs(Charset requestQueryCharset) {
        assertArgumentNotNull("requestQueryCharset", requestQueryCharset);
        this.queryParameterCharset = requestQueryCharset;
    }

    /**
     * @param requestBodyCharset The charset of request body. (NotNull)
     */
    public void encodeRequestBodyAs(Charset requestBodyCharset) {
        assertArgumentNotNull("requestBodyCharset", requestBodyCharset);
        this.requestBodyCharset = requestBodyCharset;
    }

    /**
     * @param responseBodyCharset The charset of response body. (NotNull)
     */
    public void encodeResponseBodyAs(Charset responseBodyCharset) {
        assertArgumentNotNull("responseBodyCharset", responseBodyCharset);
        this.responseBodyCharset = responseBodyCharset;
    }

    /**
     * Set header value by the name. <br>
     * It overwrites the same-name header if it already exists.
     * @param name The name of the header. (NotNull)
     * @param value The value of the header. (NotNull)
     */
    public void setHeader(String name, String value) {
        assertArgumentNotNull("name", name);
        assertArgumentNotNull("value", value);
        if (headers == null) {
            headers = DfCollectionUtil.newLinkedHashMap();
        }
        headers.put(name, DfCollectionUtil.newArrayList(value));
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
        if (headers == null) {
            headers = DfCollectionUtil.newLinkedHashMap();
        }
        List<String> valueList = headers.get(name);
        if (valueList == null) {
            valueList = DfCollectionUtil.newArrayList();
            headers.put(name, valueList);
        }
        valueList.add(value);
    }

    /**
     * Handle failure response as specified type. <br>
     * You can get the failure response from exception.
     * <pre>
     * try {
     *     ... = remoteHarborBhv.request...();
     * } catch (RemoteApiHttpClientErrorException e) {
     *     [your-specified-type] failureResponse = e.getFailureResponse().get();
     *     ...
     * }
     * </pre>
     * @param failureResponseType The type of failure response. (NotNull)
     */
    public void handleFailureResponseAs(Type failureResponseType) {
        assertArgumentNotNull("failureResponseType", failureResponseType);
        this.failureResponseType = failureResponseType;
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
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("option:{");
        sb.append("sender:{").append(queryParameterSender);
        sb.append(", ").append(requestBodySender);
        sb.append(", receiver:{").append(responseBodyReceiver);
        sb.append("}, ").append(sslUntrusted);
        sb.append(", timeout:{").append(connectTimeout);
        sb.append(", ").append(connectionRequestTimeout);
        sb.append(", ").append(socketTimeout);
        sb.append("}, ").append(headers);
        sb.append(", ").append(failureResponseType);
        sb.append(", charset:{").append(queryParameterCharset);
        sb.append(", ").append(requestBodyCharset);
        sb.append(", ").append(responseBodyCharset);
        sb.append("}}");
        return sb.toString();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    // -----------------------------------------------------
    //                                         Required Rule
    //                                         -------------
    public OptionalThing<QueryParameterSender> getQueryParameterSender() {
        return OptionalThing.ofNullable(queryParameterSender, () -> {
            throw new IllegalStateException("Not found the queryParameterSender in the option: " + toString());
        });
    }

    public OptionalThing<RequestBodySender> getRequestBodySender() {
        return OptionalThing.ofNullable(requestBodySender, () -> {
            throw new IllegalStateException("Not found the requestConverter in the option: " + toString());
        });
    }

    public OptionalThing<ResponseBodyReceiver> getResponseBodyReceiver() {
        return OptionalThing.ofNullable(responseBodyReceiver, () -> {
            throw new IllegalStateException("Not found the responseConverter in the option: " + toString());
        });
    }

    // -----------------------------------------------------
    //                                         Optional Rule
    //                                         -------------
    public boolean isSslUntrusted() {
        return sslUntrusted;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public int getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    /**
     * @return The charset of request path variable. (NotNull)
     */
    public Charset getPathVariableCharset() {
        return pathVariableCharset;
    }

    /**
     * @return The charset of request query parameter. (NotNull)
     */
    public Charset getQueryParameterCharset() {
        return queryParameterCharset;
    }

    /**
     * @return The charset of request body. (NotNull)
     */
    public Charset getRequestBodyCharset() {
        return requestBodyCharset;
    }

    /**
     * @return The charset of response body. (NotNull)
     */
    public Charset getResponseBodyCharset() {
        return responseBodyCharset;
    }

    public OptionalThing<Map<String, List<String>>> getHeaders() {
        return OptionalThing.ofNullable(headers, () -> {
            throw new IllegalStateException("Not found the headers in the option: " + toString());
        });
    }

    public OptionalThing<Type> getFailureResponseType() {
        return OptionalThing.ofNullable(failureResponseType, () -> {
            throw new IllegalStateException("Not found the failureResponseType in the option: " + toString());
        });
    }

    // ===================================================================================
    //                                                                         For Testing
    //                                                                         ===========
    protected CloseableHttpClient __xmockHttpClient;

    public void xregisterMockHttpClient(CloseableHttpClient mockHttpClient) {
        this.__xmockHttpClient = mockHttpClient;
    }
}
