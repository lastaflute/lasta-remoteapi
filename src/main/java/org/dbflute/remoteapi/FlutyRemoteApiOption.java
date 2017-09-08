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
import org.dbflute.remoteapi.converter.FlutyRequestConverter;
import org.dbflute.remoteapi.converter.FlutyResponseConverter;
import org.dbflute.remoteapi.rule.FlutyEmptyRemoteConversionRule;
import org.dbflute.remoteapi.rule.FlutyRemoteConversionRule;
import org.dbflute.util.DfCollectionUtil;

/**
 * The option of ruled remote API.
 * @author awane
 * @author jflute
 */
public class FlutyRemoteApiOption {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // default values are defined here
    protected boolean sslUntrusted;
    protected int connectTimeout = 3000;
    protected int connectionRequestTimeout = 3000;
    protected int socketTimeout = 3000;
    protected Charset charset = StandardCharsets.UTF_8;
    protected FlutyRequestConverter requestConverter;
    protected FlutyResponseConverter responseConverter;
    protected FlutyRemoteConversionRule remoteConversionRule = new FlutyEmptyRemoteConversionRule();
    protected Map<String, String> headers;
    protected Type failureResponseType;

    // ===================================================================================
    //                                                                         Http Client
    //                                                                         ===========
    public CloseableHttpClient prepareHttpClient() {
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
                throw new RuntimeException(e);
            }
            httpClientBuilder.setSSLContext(sslContext).setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
        }
        return httpClientBuilder.setDefaultRequestConfig(requestBuilder.build()).build();
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "option:{" + sslUntrusted + ", " + connectTimeout + ", " + connectionRequestTimeout + ", " + socketTimeout + ", " + charset
                + ", " + remoteConversionRule + ", " + requestConverter + ", " + responseConverter + ", " + headers + ", "
                + failureResponseType + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public boolean isSslUntrusted() {
        return sslUntrusted;
    }

    public void setSslUntrusted(boolean sslUntrusted) {
        this.sslUntrusted = sslUntrusted;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    public void setConnectionRequestTimeout(int connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    /**
     * @return The conversion rule for remote API values. (NotNull)
     */
    public FlutyRemoteConversionRule getConversionRule() { // you can switch if it needs
        return remoteConversionRule;
    }

    public void setConversionRule(FlutyRemoteConversionRule communicationTypeConversionRule) { // you can switch if it needs
        this.remoteConversionRule = communicationTypeConversionRule;
    }

    public FlutyRequestConverter getRequestConverter() {
        return requestConverter;
    }

    public void setRequestConverter(FlutyRequestConverter requestConverter) {
        this.requestConverter = requestConverter;
    }

    public FlutyResponseConverter getResponseConverter() {
        return responseConverter;
    }

    public void setResponseConverter(FlutyResponseConverter responseConverter) {
        this.responseConverter = responseConverter;
    }

    public OptionalThing<Map<String, String>> getHeaders() {
        return OptionalThing.ofNullable(headers, () -> {
            throw new IllegalStateException("Not found the headers.");
        });
    }

    public void setHeader(String name, String value) {
        if (headers == null) {
            headers = DfCollectionUtil.newLinkedHashMap();
        }
        this.headers.put(name, value);
    }

    public OptionalThing<Type> getFailureResponseType() {
        return OptionalThing.ofNullable(failureResponseType, () -> {
            throw new IllegalStateException("Not found the failureResponseType.");
        });
    }

    public void setFailureResponseType(Type failureResponseType) {
        this.failureResponseType = failureResponseType;
    }

    // ===================================================================================
    //                                                                         For Testing
    //                                                                         ===========
    private CloseableHttpClient __xmockHttpClient;

    public void xregisterMockHttpClient(CloseableHttpClient mockHttpClient) {
        this.__xmockHttpClient = mockHttpClient;
    }
}
