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
import java.util.Locale;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.params.HttpParams;

/**
 * @author awane
 * @author jflute
 */
@SuppressWarnings("all")
public class MockHttpResponse implements CloseableHttpResponse {

    protected final HttpEntity httpEntity; // null allowed if no content

    protected Integer httpStatus;

    public MockHttpResponse(HttpEntity httpEntity) {
        this.httpEntity = httpEntity;
    }

    @Override
    public HeaderIterator headerIterator(String name) {
        return null;
    }

    @Override
    public HeaderIterator headerIterator() {
        return null;
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return HttpVersion.HTTP_1_1;
    }

    @Override
    public HttpParams getParams() {
        return null;
    }

    @Override
    public Header getLastHeader(String name) {
        return null;
    }

    @Override
    public Header[] getHeaders(String name) {
        return null;
    }

    @Override
    public Header getFirstHeader(String name) {
        return null;
    }

    @Override
    public Header[] getAllHeaders() {
        return null;
    }

    @Override
    public boolean containsHeader(String name) {
        return false;
    }

    @Override
    public StatusLine getStatusLine() {
        final int statusCode = httpStatus != null ? httpStatus : 200;
        final String reasonPhrase = httpStatus != null ? "OK" : "No Good"; // simple
        return new BasicStatusLine(getProtocolVersion(), statusCode, reasonPhrase);
    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public HttpEntity getEntity() { // null allowed
        return this.httpEntity;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public void addHeader(String name, String value) {
        throw new UnsupportedOperationException("not supported by mock.");
    }

    @Override
    public void addHeader(Header header) {
        throw new UnsupportedOperationException("not supported by mock.");
    }

    @Override
    public void setStatusLine(ProtocolVersion ver, int code, String reason) {
        throw new UnsupportedOperationException("not supported by mock.");
    }

    @Override
    public void setStatusLine(ProtocolVersion ver, int code) {
        throw new UnsupportedOperationException("not supported by mock.");
    }

    @Override
    public void setStatusLine(StatusLine statusline) {
        throw new UnsupportedOperationException("not supported by mock.");
    }

    @Override
    public void setStatusCode(int code) throws IllegalStateException {
        this.httpStatus = code;
    }

    @Override
    public void setReasonPhrase(String reason) throws IllegalStateException {
        throw new UnsupportedOperationException("not supported by mock.");
    }

    @Override
    public void setLocale(Locale loc) {
        throw new UnsupportedOperationException("not supported by mock.");
    }

    @Override
    public void setEntity(HttpEntity entity) {
        throw new UnsupportedOperationException("not supported by mock.");
    }

    @Override
    public void setParams(HttpParams params) {
        throw new UnsupportedOperationException("not supported by mock.");
    }

    @Override
    public void setHeaders(Header[] headers) {
        throw new UnsupportedOperationException("not supported by mock.");
    }

    @Override
    public void setHeader(String name, String value) {
        throw new UnsupportedOperationException("not supported by mock.");
    }

    @Override
    public void setHeader(Header header) {
        throw new UnsupportedOperationException("not supported by mock.");
    }

    @Override
    public void removeHeaders(String name) {
        throw new UnsupportedOperationException("not supported by mock.");
    }

    @Override
    public void removeHeader(Header header) {
        throw new UnsupportedOperationException("not supported by mock.");
    }
}
