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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.dbflute.optional.OptionalThing;

/**
 * @author inoue
 * @author jflute
 */
public abstract class FlJsonReceiver implements ResponseBodyReceiver {

    @SuppressWarnings("unchecked")
    @Override
    public <RETURN> RETURN toResponseReturn(OptionalThing<String> body, Type beanType) {
        final String json = body.orElseThrow(() -> { // translated with rich message so simple here
            throw new IllegalStateException("Not found the response body as JSON.");
        });
        if (beanType instanceof Class<?>) {
            return (RETURN) fromJson(json, (Class<?>) beanType);
        } else {
            return (RETURN) fromJsonParameteried(json, (ParameterizedType) beanType);
        }
    }

    protected abstract <BEAN> BEAN fromJson(String json, Class<BEAN> beanType);

    protected abstract <BEAN> BEAN fromJsonParameteried(String json, ParameterizedType parameterizedType);
}
