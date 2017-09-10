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

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.dbflute.remoteapi.mock.MockRemoteApiHttpClient.MockRequest;
import org.dbflute.util.DfResourceUtil;

/**
 * @author awane
 * @author jflute
 */
public class MockRemoteApiHttpClientBuilder {

    protected Consumer<MockRequest> requestLambda;
    protected MockRemoteApiHttpResponse response;

    public static MockRemoteApiHttpClientBuilder create() {
        return new MockRemoteApiHttpClientBuilder();
    }

    protected MockRemoteApiHttpClientBuilder() {
    }

    public MockRemoteApiHttpClient build() {
        MockRemoteApiHttpClient mockRemoteApiHttpClient = new MockRemoteApiHttpClient(requestLambda);
        mockRemoteApiHttpClient.response(response);
        return mockRemoteApiHttpClient;
    }

    public MockRemoteApiHttpClientBuilder assertRequest(Consumer<MockRequest> requestLambda) {
        this.requestLambda = requestLambda;
        return this;
    }

    public MockRemoteApiHttpClientBuilder responseJson(String responseFilePath) {
        InputStream inputStream = DfResourceUtil.getResourceStream(responseFilePath);
        response = new MockRemoteApiHttpResponse(new InputStreamEntity(inputStream, ContentType.APPLICATION_JSON));
        return this;
    }

    public MockRemoteApiHttpClientBuilder responseXml(String responseFilePath) {
        InputStream inputStream = DfResourceUtil.getResourceStream(responseFilePath);
        response = new MockRemoteApiHttpResponse(
                new InputStreamEntity(inputStream, ContentType.create(ContentType.APPLICATION_XML.getMimeType(), StandardCharsets.UTF_8)));
        return this;
    }

    public MockRemoteApiHttpClientBuilder responseJson(File responseFile) {
        response = new MockRemoteApiHttpResponse(new FileEntity(responseFile, ContentType.APPLICATION_JSON));
        return this;
    }

    public MockRemoteApiHttpClientBuilder responseXml(File responseFile) {
        response = new MockRemoteApiHttpResponse(
                new FileEntity(responseFile, ContentType.create(ContentType.APPLICATION_XML.getMimeType(), StandardCharsets.UTF_8)));
        return this;
    }

    public MockRemoteApiHttpClientBuilder response(File responseFile, ContentType contentType) {
        response = new MockRemoteApiHttpResponse(new FileEntity(responseFile, contentType));
        return this;
    }
}
