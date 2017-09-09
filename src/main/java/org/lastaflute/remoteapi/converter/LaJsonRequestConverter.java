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
package org.lastaflute.remoteapi.converter;

import java.nio.charset.StandardCharsets;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.dbflute.optional.OptionalThing;
import org.dbflute.remoteapi.converter.FlutyRequestConverter;
import org.lastaflute.core.json.JsonManager;
import org.lastaflute.core.json.JsonMappingOption;
import org.lastaflute.core.json.engine.RealJsonEngine;
import org.lastaflute.web.servlet.request.RequestManager;

/**
 * @author inoue
 * @author jflute
 */
public class LaJsonRequestConverter implements FlutyRequestConverter {

    protected final RealJsonEngine jsonEngine; // to parse JSON response and request as JsonBody

    public LaJsonRequestConverter(RequestManager requestManager, JsonMappingOption jsonMappingOption) {
        this.jsonEngine = createJsonEngine(requestManager.getJsonManager(), jsonMappingOption);
    }

    protected RealJsonEngine createJsonEngine(JsonManager jsonManager, JsonMappingOption jsonMappingOption) {
        return jsonManager.newAnotherEngine(OptionalThing.ofNullable(jsonMappingOption, () -> {
            throw new IllegalStateException("Not found the json mapping option: " + jsonMappingOption);
        }));
    }

    @Override
    public void prepareHttpPost(HttpPost httpPost, Object form) {
        final String json = jsonEngine.toJson(form);
        final StringEntity entity = new StringEntity(json, StandardCharsets.UTF_8.name());
        entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json; charset=" + StandardCharsets.UTF_8.name()));
        httpPost.setEntity(entity);
    }
}
