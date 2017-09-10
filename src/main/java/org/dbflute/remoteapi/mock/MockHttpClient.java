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
package org.dbflute.remoteapi.mock;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpContext;
import org.dbflute.util.DfResourceUtil;

/**
 * @author awane
 * @author jflute
 */
public class MockHttpClient extends CloseableHttpClient {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Consumer<MockSupposedRequest> requestLambda; // not null
    protected final MockHttpResponse response; // not null

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public MockHttpClient(Consumer<MockSupposedRequest> requestLambda, MockHttpResponse response) {
        this.requestLambda = requestLambda;
        this.response = response;
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    @Override
    protected CloseableHttpResponse doExecute(HttpHost target, HttpRequest request, HttpContext context)
            throws IOException, ClientProtocolException {
        final Charset charset = StandardCharsets.UTF_8;
        final String url = extractRequestUrl(request);
        final String body = extractRequestBody(request, charset);
        final MockSupposedRequest supposedRequest = new MockSupposedRequest(url, body);
        requestLambda.accept(supposedRequest);
        return response;
    }

    protected String extractRequestUrl(HttpRequest request) {
        final Charset charset = StandardCharsets.UTF_8;
        final String url;
        try {
            url = URLDecoder.decode(request.getRequestLine().getUri(), charset.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Not found the encoding: " + charset, e);
        }
        return url;
    }

    protected String extractRequestBody(HttpRequest request, Charset charset) {
        final String body;
        if (request instanceof HttpPost) {
            HttpPost httpPost = (HttpPost) request;
            HttpEntity entity = httpPost.getEntity();
            try (InputStream content = entity.getContent(); InputStreamReader reader = new InputStreamReader(content, charset)) {
                body = DfResourceUtil.readText(reader);
            } catch (UnsupportedOperationException | IOException e) {
                throw new IllegalStateException("Failed to read the text: request=" + request, e);
            }
        } else {
            body = null;
        }
        return body;
    }

    // ===================================================================================
    //                                                              Empty Request Override
    //                                                              ======================
    @SuppressWarnings("deprecation")
    @Override
    public org.apache.http.params.HttpParams getParams() {
        return null;
    }

    @SuppressWarnings("deprecation")
    @Override
    public org.apache.http.conn.ClientConnectionManager getConnectionManager() {
        return null;
    }

    @Override
    public void close() throws IOException {
    }
}
