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
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.dbflute.optional.OptionalThing;
import org.dbflute.util.DfResourceUtil;

/**
 * @author awane
 * @author jflute
 */
@SuppressWarnings("all")
public class MockRemoteApiHttpClient extends CloseableHttpClient {

    protected final Consumer<MockRequest> requestLambda;
    protected MockRemoteApiHttpResponse response;

    public MockRemoteApiHttpClient(Consumer<MockRequest> requestLambda) {
        this.requestLambda = requestLambda;
    }

    @Override
    public HttpParams getParams() {
        return null;
    }

    @Override
    public ClientConnectionManager getConnectionManager() {
        return null;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    protected CloseableHttpResponse doExecute(HttpHost target, HttpRequest request, HttpContext context)
            throws IOException, ClientProtocolException {
        Charset charset = StandardCharsets.UTF_8;
        MockRequest req = new MockRequest();
        try {
            String url = URLDecoder.decode(request.getRequestLine().getUri(), charset.name());
            req.url = url;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        if (request instanceof HttpPost) {
            HttpPost httpPost = (HttpPost) request;
            HttpEntity entity = httpPost.getEntity();
            try (InputStream content = entity.getContent(); InputStreamReader reader = new InputStreamReader(content, charset)) {
                String body = DfResourceUtil.readText(reader);
                req.body = OptionalThing.of(body);
            } catch (UnsupportedOperationException | IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            req.body = OptionalThing.empty();
        }
        requestLambda.accept(req);
        return response;
    }

    public MockRemoteApiHttpClient response(MockRemoteApiHttpResponse response) {
        this.response = response;
        return this;
    }

    public static class MockRequest {
        public String url;
        public OptionalThing<String> body;
    }
}
