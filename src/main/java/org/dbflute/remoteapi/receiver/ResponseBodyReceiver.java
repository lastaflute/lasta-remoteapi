/*
 * Copyright 2015-2018 the original author or authors.
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

import java.lang.reflect.Type;

import org.dbflute.optional.OptionalThing;
import org.dbflute.remoteapi.FlutyRemoteApiRule;

/**
 * The receiver of response body.
 * @author inoue
 * @author jflute
 */
public interface ResponseBodyReceiver {

    /**
     * Convert response body to return object.
     * @param <RETURN> the type of response return.
     * @param body The optional body string of response. (NotNull, EmptyAllowed: when no body)
     * @param beanType The specified bean type as return object. (NotNull)
     * @param rule The rule of remote API. (NotNull)
     * @return The converted return object from the response body. (NotNull)
     */
    <RETURN> RETURN toResponseReturn(OptionalThing<String> body, Type beanType, FlutyRemoteApiRule rule);
}
