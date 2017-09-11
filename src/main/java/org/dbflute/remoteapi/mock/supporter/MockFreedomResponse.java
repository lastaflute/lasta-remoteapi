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
    protected final List<MockRequestHandler> requestHandlerList = new ArrayList<>();
    protected final List<MockHttpResponseProvider> responseProviderList = new ArrayList<>();

    public static interface MockHttpResponseProvider {

        /**
         * @param request The supposed request that has URL and other information. (NotNull)
         * @return The mock response matched with the request. (NullAllowed: if unmatched)
         */
        MockHttpResponse provide(MockSupposedRequest request);
    }

    public static interface MockRequestDeterminer {

        boolean determine(MockSupposedRequest request);
    }

    // ===================================================================================
    //                                                                             Request
    //                                                                             =======
    public void peekRequest(MockRequestHandler requestLambda) {
        requestHandlerList.add(requestLambda);
    }

    public static interface MockRequestHandler {

        void handle(MockSupposedRequest request);
    }

    // ===================================================================================
    //                                                                            Response
    //                                                                            ========
    // -----------------------------------------------------
    //                                                 JSON
    //                                                ------
    public void asJson(InputStream responseStream, MockRequestDeterminer requestLambda) {
        responseProviderList.add(request -> {
            return requestLambda.determine(request) ? responseJson(responseStream) : null;
        });
    }

    public void asJson(String responseFilePath, MockRequestDeterminer requestLambda) {
        responseProviderList.add(request -> {
            return requestLambda.determine(request) ? responseJson(responseFilePath) : null;
        });
    }

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
    public List<MockRequestHandler> getRequestHandlerList() {
        return Collections.unmodifiableList(requestHandlerList);
    }

    public List<MockHttpResponseProvider> getResponseProviderList() {
        return Collections.unmodifiableList(responseProviderList);
    }
}
