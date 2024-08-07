/*
 * Copyright 2015-2024 the original author or authors.
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
package org.lastaflute.remoteapi.receiver;

import java.lang.reflect.ParameterizedType;

import org.dbflute.remoteapi.receiver.FlJsonReceiver;
import org.lastaflute.core.json.JsonEngineResource;
import org.lastaflute.core.json.JsonManager;
import org.lastaflute.core.json.JsonMappingOption;
import org.lastaflute.core.json.engine.RealJsonEngine;
import org.lastaflute.web.servlet.request.RequestManager;

/**
 * @author inoue
 * @author jflute
 */
public class LaJsonReceiver extends FlJsonReceiver {

    protected final RealJsonEngine jsonEngine; // to parse JSON response and request as JsonBody

    public LaJsonReceiver(RequestManager requestManager, JsonMappingOption mappingOption) {
        this.jsonEngine = createJsonEngine(requestManager.getJsonManager(), mappingOption);
    }

    protected RealJsonEngine createJsonEngine(JsonManager jsonManager, JsonMappingOption mappingOption) {
        return jsonManager.newRuledEngine(prepareJsonEngineResource(mappingOption));
    }

    protected JsonEngineResource prepareJsonEngineResource(JsonMappingOption mappingOption) {
        final JsonEngineResource resource = new JsonEngineResource();
        resource.acceptMappingOption(mappingOption);
        return resource;
    }

    @Override
    protected <BEAN> BEAN fromJson(String json, Class<BEAN> beanType) {
        return jsonEngine.fromJson(json, beanType);
    }

    @Override
    protected <BEAN> BEAN fromJsonParameteried(String json, ParameterizedType parameterizedType) {
        return jsonEngine.fromJsonParameteried(json, parameterizedType);
    }
}
