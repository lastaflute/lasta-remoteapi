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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpContext;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.remoteapi.mock.supporter.MockFreedomResponse;
import org.dbflute.remoteapi.mock.supporter.MockFreedomResponse.MockHttpResponseProvider;
import org.dbflute.remoteapi.mock.supporter.MockFreedomResponse.MockHttpResponseResource;
import org.dbflute.remoteapi.mock.supporter.MockFreedomResponse.MockRequestPeeking;
import org.dbflute.remoteapi.mock.supporter.MockSupposedRequest;
import org.dbflute.util.DfResourceUtil;

/**
 * @author awane
 * @author jflute
 */
public class MockHttpClient extends CloseableHttpClient {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final MockFreedomResponse freedomResponse;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    protected MockHttpClient(MockFreedomResponse freedomResponse) {
        this.freedomResponse = freedomResponse;
    }

    // e.g.
    //  MockHttpClient.create(response -> {
    //      response.asJson(path, request -> request...);
    //      response.peekRequest(request -> {});
    //  });
    public static MockHttpClient create(MockFreedomResponseSetupper responseLambda) {
        MockFreedomResponse response = new MockFreedomResponse();
        responseLambda.setup(response);
        return new MockHttpClient(response);
    }

    public static interface MockFreedomResponseSetupper {

        void setup(MockFreedomResponse response);
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    @Override
    protected CloseableHttpResponse doExecute(HttpHost httpHost, HttpRequest httpRequest, HttpContext httpContext)
            throws IOException, ClientProtocolException {
        final Charset charset = StandardCharsets.UTF_8;
        final String url = extractRequestUrl(httpRequest); // e.g. http://localhost:8090/harbor/lido/product/list/1
        final String body = extractRequestBody(httpRequest, charset); // null allowed
        final Map<String, List<String>> headerMap = extractHeaderMap(httpRequest);
        final String hostName = httpHost.getHostName();
        final Integer port = httpHost.getPort() >= 0 ? httpHost.getPort() : null;
        final MockSupposedRequest supposedRequest = new MockSupposedRequest(url, body, hostName, port, headerMap);
        final List<MockRequestPeeking> requestHandlerList = freedomResponse.getRequestPeekingList();
        for (MockRequestPeeking requestHandler : requestHandlerList) {
            requestHandler.peek(supposedRequest);
        }
        final List<MockHttpResponseResource> responseResourceList = freedomResponse.getResponseResourceList();
        for (MockHttpResponseResource responseResource : responseResourceList) {
            final MockHttpResponseProvider responseProvider = responseResource.getResponseProvider();
            final MockHttpResponse provided = responseProvider.provide(supposedRequest);
            if (provided != null) {
                responseResource.getHttpStatus().ifPresent(httpStatus -> {
                    provided.setStatusCode(httpStatus);
                });
                return provided;
            }
        }
        throwMockHttpResponseNotFoundException(supposedRequest, responseResourceList);
        return null; // unreachable
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
        if (request instanceof HttpEntityEnclosingRequest) {
            final HttpEntityEnclosingRequest httpPost = (HttpEntityEnclosingRequest) request;
            final HttpEntity entity = httpPost.getEntity();
            if (entity != null) {
                try (InputStream content = entity.getContent(); InputStreamReader reader = new InputStreamReader(content, charset)) {
                    body = DfResourceUtil.readText(reader);
                } catch (UnsupportedOperationException | IOException e) {
                    throw new IllegalStateException("Failed to read the text: request=" + request, e);
                }
            } else { // e.g. POST but noRequestBody()
                body = null;
            }
        } else { // e.g. GET
            body = null;
        }
        return body;
    }

    protected Map<String, List<String>> extractHeaderMap(HttpRequest httpRequest) {
        final Header[] allHeaders = httpRequest.getAllHeaders();
        final Map<String, List<String>> headerMap = new LinkedHashMap<String, List<String>>();
        if (allHeaders != null) { // just in case
            for (Header header : allHeaders) {
                final String name = header.getName();
                List<String> elementList = headerMap.get(name);
                if (elementList == null) {
                    elementList = new ArrayList<String>();
                    headerMap.put(name, elementList);
                }
                elementList.add(header.getValue());
            }
            for (Entry<String, List<String>> entry : headerMap.entrySet()) {
                headerMap.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
            }
        }
        return Collections.unmodifiableMap(headerMap);
    }

    protected void throwMockHttpResponseNotFoundException(MockSupposedRequest supposedRequest,
            List<MockHttpResponseResource> responseResourceList) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the mock response for the request.");
        br.addItem("Advice");
        br.addElement("This is how to mock response:");
        br.addElement("  (o):");
        br.addElement("    MockHttpClient client = MockHttpClient.create(resopnse -> {");
        br.addElement("        resopnse.asJson(json, request -> true); // *here");
        br.addElement("    });");
        br.addElement("The asJson()'s second argument is lambda");
        br.addElement("to determine the target request like this:");
        br.addElement("  (o):");
        br.addElement("    MockHttpClient client = MockHttpClient.create(resopnse -> {");
        br.addElement("        resopnse.asJson(json, request -> request.getUrl().contains(\"/harbor/\")); // *here");
        br.addElement("    });");
        br.addElement("So the condition is wrong then not found.");
        br.addElement("Confirm your condition implementation.");
        br.addItem("Requset");
        br.addElement(supposedRequest.getUrl());
        supposedRequest.getBody().ifPresent(supportedBody -> {
            br.addElement(supportedBody);
        });
        br.addItem("Response Resource");
        br.addElement(responseResourceList);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
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
