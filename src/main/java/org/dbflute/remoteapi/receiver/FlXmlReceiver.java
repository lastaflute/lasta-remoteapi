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
package org.dbflute.remoteapi.receiver;

import java.io.StringReader;
import java.lang.reflect.Type;

import javax.xml.bind.JAXB;

import org.dbflute.optional.OptionalThing;
import org.dbflute.remoteapi.FlutyRemoteApiRule;

/**
 * @author inoue
 * @author jflute
 */
public class FlXmlReceiver extends FlBaseReceiver {

    // ===================================================================================
    //                                                                          Convert to
    //                                                                          ==========
    @SuppressWarnings("unchecked")
    @Override
    public <RETURN> RETURN toResponseReturn(OptionalThing<String> body, Type type, FlutyRemoteApiRule rule) {
        if (!(type instanceof Class<?>)) {
            throw new IllegalArgumentException("The specified type is not Class: type=" + type);
        }
        final String target = body.orElseThrow(() -> { // translated with rich message so simple here
            return new IllegalStateException("Not found the response body as XML.");
        });
        readySendReceiveLogIfNeeds(rule, body, target);
        return (RETURN) JAXB.unmarshal(new StringReader(target), (Class<?>) type);
    }

    // -----------------------------------------------------
    //                                  Send/Receive Logging
    //                                  --------------------
    @Override
    protected String getSendReceiveLogResponseBodyType() {
        return "xml";
    }
}
