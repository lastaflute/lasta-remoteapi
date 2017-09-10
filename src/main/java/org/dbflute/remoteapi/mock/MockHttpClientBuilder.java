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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.dbflute.util.DfResourceUtil;

/**
 * @author awane
 * @author jflute
 */
public class MockHttpClientBuilder {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected Consumer<MockSupposedRequest> requestLambda; // null allowed, not required
    protected MockHttpResponse response; // not null after set, basically required

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    protected MockHttpClientBuilder() {
    }

    public static MockHttpClientBuilder create() {
        return new MockHttpClientBuilder();
    }

    public MockHttpClient build() {
        if (response == null) {
            throw new IllegalStateException("Not found the mock response.");
        }
        return new MockHttpClient(requestLambda != null ? requestLambda : req -> {}, response);
    }

    // ===================================================================================
    //                                                                             Request
    //                                                                             =======
    public MockHttpClientBuilder assertRequest(Consumer<MockSupposedRequest> requestLambda) {
        this.requestLambda = requestLambda;
        return this;
    }

    // ===================================================================================
    //                                                                            Response
    //                                                                            ========
    // -----------------------------------------------------
    //                                                 Basic
    //                                                 -----
    public MockHttpClientBuilder response(File responseFile, ContentType contentType) {
        response = new MockHttpResponse(new FileEntity(responseFile, contentType));
        return this;
    }

    // -----------------------------------------------------
    //                                                 JSON
    //                                                ------
    public MockHttpClientBuilder responseJson(File responseFile) {
        response = new MockHttpResponse(new FileEntity(responseFile, prepareContentTypeJson()));
        return this;
    }

    public MockHttpClientBuilder responseJson(InputStream responseStream) {
        response = new MockHttpResponse(new InputStreamEntity(responseStream, prepareContentTypeJson()));
        return this;
    }

    public MockHttpClientBuilder responseJson(String responseFilePath) {
        final InputStream ins = DfResourceUtil.getResourceStream(responseFilePath);
        if (ins == null) {
            throw new IllegalStateException("Not found the response file: responseFilePath=" + responseFilePath);
        }
        return responseJson(ins);
    }

    public MockHttpClientBuilder responseJsonDirectly(String json) {
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
    public MockHttpClientBuilder responseXml(File responseFile) {
        response = new MockHttpResponse(new FileEntity(responseFile, prepareContentTypeXml()));
        return this;
    }

    public MockHttpClientBuilder responseXml(String responseFilePath) {
        InputStream inputStream = DfResourceUtil.getResourceStream(responseFilePath);
        response = new MockHttpResponse(new InputStreamEntity(inputStream, prepareContentTypeXml()));
        return this;
    }

    protected ContentType prepareContentTypeXml() {
        return ContentType.create(ContentType.APPLICATION_XML.getMimeType(), StandardCharsets.UTF_8);
    }
}
