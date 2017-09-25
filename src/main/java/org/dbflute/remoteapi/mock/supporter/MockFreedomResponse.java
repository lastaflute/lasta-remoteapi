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
package org.dbflute.remoteapi.mock.supporter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.dbflute.optional.OptionalThing;
import org.dbflute.remoteapi.mock.MockHttpResponse;
import org.dbflute.util.DfResourceUtil;

/**
 * @author awane
 * @author jflute
 * @since 0.2.2 (2017/09/11 Monday at bay maihama fine terrace)
 */
public class MockFreedomResponse {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final List<MockRequestPeeking> requestPeekingList = new ArrayList<>();
    protected final List<MockHttpResponseResource> responseResourceList = new ArrayList<>();

    public static interface MockRequestDeterminer {

        /**
         * @param request The supposed request that has e.g. URL, request Body. (NotNull)
         * @return The determination of corresponding request or not.
         */
        boolean determine(MockSupposedRequest request);
    }

    public static class MockHttpResponseResource {

        protected final MockHttpResponseProvider responseProvider;
        protected Integer httpStatus;

        public MockHttpResponseResource(MockHttpResponseProvider responseProvider) {
            this.responseProvider = responseProvider;
        }

        public MockHttpResponseResource httpStatus(Integer httpStatus) {
            if (httpStatus == null) {
                throw new IllegalArgumentException("The argument 'httpStatus' should not be null.");
            }
            this.httpStatus = httpStatus;
            return this;
        }

        public MockHttpResponseProvider getResponseProvider() {
            return responseProvider;
        }

        public OptionalThing<Integer> getHttpStatus() {
            return OptionalThing.ofNullable(httpStatus, () -> {
                throw new IllegalStateException("Not found the HTTP status: provider=" + responseProvider);
            });
        }
    }

    public static interface MockHttpResponseProvider {

        /**
         * @param request The supposed request that has URL and other information. (NotNull)
         * @return The mock response matched with the request. (NullAllowed: if unmatched)
         */
        MockHttpResponse provide(MockSupposedRequest request);
    }

    // ===================================================================================
    //                                                                             Request
    //                                                                             =======
    /**
     * Peek supposed request for the response. (Basically for unit-test assertion)
     * <pre>
     * e.g. request body should exist and contain request keywords if /harbor/product/list 
     *  response.peekRequest(request -&gt; {
     *      if (request.getUrl().contains("/harbor/product/list")) {
     *          assertContainsAll(request.getBody().get(), "productName", body.productName);
     *      }
     *  });
     * 
     * e.g. request body should exist and contain request keywords at all requests 
     *  response.peekRequest(request -&gt; {
     *      assertContainsAll(request.getBody().get(), "productName", body.productName);
     *  });
     * </pre>
     * @param requestLambda The callback for peeking request. (NotNull)
     */
    public void peekRequest(MockRequestPeeking requestLambda) {
        assertArgumentNotNull("requestLambda", requestLambda);
        requestPeekingList.add(requestLambda);
    }

    public static interface MockRequestPeeking {

        /**
         * @param request The supposed request that has e.g. URL, request Body. (NotNull)
         */
        void peek(MockSupposedRequest request);
    }

    // ===================================================================================
    //                                                                            Response
    //                                                                            ========
    // -----------------------------------------------------
    //                                                 JSON
    //                                                ------
    // #hope jflute non-determination method?
    /**
     * Return response as JSON from the input stream if the request is ...
     * <pre>
     * e.g. returns response as the json if the request is /harbor/
     *  response.asJson(ins, request -&gt; request.getUrl().contains("/harbor/"));
     * 
     * e.g. returns response as the json at all requests
     *  response.asJson(ins, request -&gt; true);
     * </pre>
     * @param responseStream The input stream to JSON resource for mock response. (NotNull)
     * @param requestLambda The callback for determination of corresponding request. (NotNull)
     * @return The resource to create mock HTTP response. (NotNull)
     */
    public MockHttpResponseResource asJson(InputStream responseStream, MockRequestDeterminer requestLambda) {
        assertArgumentNotNull("responseStream", responseStream);
        assertArgumentNotNull("requestLambda", requestLambda);
        return registerProvider(request -> {
            return requestLambda.determine(request) ? responseJson(responseStream) : null;
        });
    }

    /**
     * Return response as JSON from the resource path if the request is ...
     * <pre>
     * e.g. returns response as the json if the request is /harbor/
     *  response.asJson("/mock/harbor/product.json", request -&gt; request.getUrl().contains("/harbor/"));
     * 
     * e.g. returns response as the json at all requests
     *  response.asJson("/mock/harbor/product.json", request -&gt; true);
     * </pre>
     * @param responseFilePath The resource path to JSON resource file for mock response. (NotNull)
     * @param requestLambda The callback for determination of corresponding request. (NotNull)
     * @return The resource to create mock HTTP response. (NotNull)
     */
    public MockHttpResponseResource asJson(String responseFilePath, MockRequestDeterminer requestLambda) {
        assertArgumentNotNull("responseFilePath", responseFilePath);
        assertArgumentNotNull("requestLambda", requestLambda);
        return registerProvider(request -> {
            return requestLambda.determine(request) ? responseJson(responseFilePath) : null;
        });
    }

    /**
     * Return response as JSON directly if the request is ...
     * <pre>
     * e.g. returns response as the json if the request is /harbor/
     *  response.asJsonDirectly("{sea = mystic, land = oneman}", request -&gt; request.getUrl().contains("/harbor/"));
     * 
     * e.g. returns response as the json at all requests
     *  response.asJsonDirectly("{sea = mystic, land = oneman}", request -&gt; true);
     * </pre>
     * @param json The string of JSON for mock response. (NotNull, EmptyAllowed)
     * @param requestLambda The callback for determination of corresponding request. (NotNull)
     * @return The resource to create mock HTTP response. (NotNull)
     */
    public MockHttpResponseResource asJsonDirectly(String json, MockRequestDeterminer requestLambda) {
        assertArgumentNotNull("json", json);
        assertArgumentNotNull("requestLambda", requestLambda);
        return registerProvider(request -> {
            return requestLambda.determine(request) ? responseJsonDirectly(json) : null;
        });
    }

    /**
     * Return response as JSON no content if the request is ...
     * <pre>
     * e.g. returns response as the json if the request is /harbor/
     *  response.asJsonNoContent(request -&gt; request.getUrl().contains("/harbor/"));
     * 
     * e.g. returns response as the json at all requests
     *  response.asJsonNoContent(request -&gt; true);
     * </pre>
     * @param requestLambda The callback for determination of corresponding request. (NotNull)
     * @return The resource to create mock HTTP response. (NotNull)
     */
    public MockHttpResponseResource asJsonNoContent(MockRequestDeterminer requestLambda) {
        assertArgumentNotNull("requestLambda", requestLambda);
        return registerProvider(request -> {
            return requestLambda.determine(request) ? responseJsonNoContent() : null;
        });
    }

    protected MockHttpResponse responseJson(InputStream responseStream) {
        return new MockHttpResponse(new InputStreamEntity(responseStream, prepareContentTypeJson()));
    }

    protected MockHttpResponse responseJson(String responseFilePath) {
        final InputStream ins = DfResourceUtil.getResourceStream(responseFilePath);
        if (ins == null) {
            throw new IllegalStateException("Not found the response file: responseFilePath=" + responseFilePath);
        }
        return responseJson(ins);
    }

    protected MockHttpResponse responseJsonDirectly(String json) {
        final String encoding = "UTF-8";
        try {
            return responseJson(new ByteArrayInputStream(json.getBytes(encoding)));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Not found the encoding: " + encoding, e);
        }
    }

    protected MockHttpResponse responseJsonNoContent() {
        return new MockHttpResponse(null);
    }

    protected ContentType prepareContentTypeJson() {
        return ContentType.APPLICATION_JSON;
    }

    // -----------------------------------------------------
    //                                                  XML
    //                                                 -----
    public MockHttpResponseResource asXml(InputStream responseStream, MockRequestDeterminer requestLambda) {
        return registerProvider(request -> {
            return requestLambda.determine(request) ? responseXml(responseStream) : null;
        });
    }

    public MockHttpResponseResource asXml(String responseFilePath, MockRequestDeterminer requestLambda) {
        return registerProvider(request -> {
            return requestLambda.determine(request) ? responseXml(responseFilePath) : null;
        });
    }

    protected MockHttpResponse responseXml(InputStream responseStream) {
        return new MockHttpResponse(new InputStreamEntity(responseStream, prepareContentTypeXml()));
    }

    protected MockHttpResponse responseXml(String responseFilePath) {
        final InputStream ins = DfResourceUtil.getResourceStream(responseFilePath);
        if (ins == null) {
            throw new IllegalStateException("Not found the response file: responseFilePath=" + responseFilePath);
        }
        return responseXml(ins);
    }

    protected ContentType prepareContentTypeXml() {
        return ContentType.create(ContentType.APPLICATION_XML.getMimeType(), StandardCharsets.UTF_8);
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    protected MockHttpResponseResource registerProvider(MockHttpResponseProvider provider) {
        final MockHttpResponseResource resource = new MockHttpResponseResource(provider);
        responseResourceList.add(resource);
        return resource;
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
    //                                                                            Accessor
    //                                                                            ========
    public List<MockRequestPeeking> getRequestPeekingList() {
        return Collections.unmodifiableList(requestPeekingList);
    }

    public List<MockHttpResponseResource> getResponseResourceList() {
        return Collections.unmodifiableList(responseResourceList);
    }
}
