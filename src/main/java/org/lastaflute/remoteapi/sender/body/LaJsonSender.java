/*
 * Copyright 2015-2020 the original author or authors.
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
package org.lastaflute.remoteapi.sender.body;

import org.dbflute.remoteapi.sender.body.FlJsonSender;
import org.lastaflute.core.json.JsonEngineResource;
import org.lastaflute.core.json.JsonManager;
import org.lastaflute.core.json.JsonMappingOption;
import org.lastaflute.core.json.engine.RealJsonEngine;
import org.lastaflute.web.servlet.request.RequestManager;

/**
 * @author inoue
 * @author jflute
 */
public class LaJsonSender extends FlJsonSender {

    protected final RealJsonEngine jsonEngine; // to parse JSON response and request as JsonBody

    public LaJsonSender(RequestManager requestManager, JsonMappingOption mappingOption) {
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
    protected String toJson(Object param) {
        return jsonEngine.toJson(param);
    }
}
