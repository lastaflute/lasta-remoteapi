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
package org.dbflute.remoteapi.sender.body;

import java.nio.charset.StandardCharsets;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

/**
 * @author inoue
 * @author jflute
 */
public abstract class FlJsonSender implements RequestBodySender {

    @Override
    public void prepareBodyRequest(HttpEntityEnclosingRequest enclosingRequest, Object form) {
        final String json = toJson(form);
        final StringEntity entity = new StringEntity(json, StandardCharsets.UTF_8.name());
        entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, buildContentType()));
        enclosingRequest.setEntity(entity);
    }

    protected abstract String toJson(Object form);

    protected String buildContentType() {
        return "application/json; charset=" + StandardCharsets.UTF_8.name();
    }
}
