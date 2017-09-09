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
package org.dbflute.remoteapi.converter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author inoue
 * @author jflute
 */
public abstract class FlutyJsonResponseConverter implements FlutyResponseConverter {

    @SuppressWarnings("unchecked")
    @Override
    public <CONTENT extends Object> CONTENT toResult(String json, Type type) {
        if (type instanceof Class<?>) {
            return (CONTENT) fromJson(json, (Class<?>) type);
        } else {
            return (CONTENT) fromJsonParameteried(json, (ParameterizedType) type);
        }
    }

    protected abstract <BEAN> BEAN fromJson(String json, Class<BEAN> beanType);

    protected abstract <BEAN> BEAN fromJsonParameteried(String json, ParameterizedType parameterizedType);
}
