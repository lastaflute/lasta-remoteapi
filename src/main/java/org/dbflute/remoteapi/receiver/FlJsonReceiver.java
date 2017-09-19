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

/**
 * @author inoue
 * @author jflute
 */
public abstract class FlJsonReceiver implements ResponseBodyReceiver {

    protected static final Object VOID_OBJ = new Object();

    @SuppressWarnings("unchecked")
    @Override
    public <RETURN> RETURN toResponseReturn(String json, Type beanType) {
        if (isVoid(beanType)) { // e.g. doRequestPost(void.class, ...);
            return (RETURN) VOID_OBJ;
        }
        if (beanType instanceof Class<?>) {
            return (RETURN) fromJson(json, (Class<?>) beanType);
        } else {
            return (RETURN) fromJsonParameteried(json, (ParameterizedType) beanType);
        }
    }

    protected boolean isVoid(Type beanType) {
        return Void.class.equals(beanType) || void.class.equals(beanType);
    }

    protected abstract <BEAN> BEAN fromJson(String json, Class<BEAN> beanType);

    protected abstract <BEAN> BEAN fromJsonParameteried(String json, ParameterizedType parameterizedType);
}
