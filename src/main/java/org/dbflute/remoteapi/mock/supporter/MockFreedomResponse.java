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
    protected final List<MockRequestPeeking> requestHandlerList = new ArrayList<>();
    protected final List<MockHttpResponseProvider> responseProviderList = new ArrayList<>();

    public static interface MockHttpResponseProvider {

        /**
         * @param request The supposed request that has URL and other information. (NotNull)
         * @return The mock response matched with the request. (NullAllowed: if unmatched)
         */
        MockHttpResponse provide(MockSupposedRequest request);
    }

    public static interface MockRequestDeterminer {

        /**
         * @param request The supposed request that has e.g. URL, request Body. (NotNull)
         * @return The determination of corresponding request or not.
         */
        boolean determine(MockSupposedRequest request);
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
        requestHandlerList.add(requestLambda);
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
     */
    public void asJson(InputStream responseStream, MockRequestDeterminer requestLambda) {
        responseProviderList.add(request -> {
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
     */
    public void asJson(String responseFilePath, MockRequestDeterminer requestLambda) {
        responseProviderList.add(request -> {
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
     * @param json The string of JSON for mock response. (NotNull)
     * @param requestLambda The callback for determination of corresponding request. (NotNull)
     */
    public void asJsonDirectly(String json, MockRequestDeterminer requestLambda) {
        responseProviderList.add(request -> {
            return requestLambda.determine(request) ? responseJsonDirectly(json) : null;
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

    protected ContentType prepareContentTypeJson() {
        return ContentType.APPLICATION_JSON;
    }

    // -----------------------------------------------------
    //                                                  XML
    //                                                 -----
    public void asXml(InputStream responseStream, MockRequestDeterminer requestLambda) {
        responseProviderList.add(request -> {
            return requestLambda.determine(request) ? responseXml(responseStream) : null;
        });
    }

    public void asXml(String responseFilePath, MockRequestDeterminer requestLambda) {
        responseProviderList.add(request -> {
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
    //                                                                            Accessor
    //                                                                            ========
    public List<MockRequestPeeking> getRequestHandlerList() {
        return Collections.unmodifiableList(requestHandlerList);
    }

    public List<MockHttpResponseProvider> getResponseProviderList() {
        return Collections.unmodifiableList(responseProviderList);
    }
}
