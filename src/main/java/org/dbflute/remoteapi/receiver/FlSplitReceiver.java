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

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;

import org.dbflute.helper.beans.DfBeanDesc;
import org.dbflute.helper.beans.DfPropertyDesc;
import org.dbflute.helper.beans.factory.DfBeanDescFactory;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfReflectionUtil;

/**
 * @author awane
 * @author jflute
 */
public class FlSplitReceiver implements ResponseBodyReceiver {

    protected final String delimiter;
    protected final String delimiterByKeyValue;

    public FlSplitReceiver(String delimiter, String delimiterByKeyValue) {
        this.delimiter = delimiter;
        this.delimiterByKeyValue = delimiterByKeyValue;
    }

    @Override
    public <RESULT extends Object> RESULT toResult(String target, Type type) {
        if (!(type instanceof Class<?>)) {
            throw new IllegalArgumentException("type is not Class." + type);
        }
        final Map<String, String> resultMap = DfCollectionUtil.newLinkedHashMap();
        Arrays.stream(target.split(delimiter)).forEach(keyValue -> {
            String[] keyValueArray = keyValue.split(delimiterByKeyValue);
            if (keyValueArray.length != 2) {
                return;
            }
            final String key = keyValueArray[0];
            final String value = keyValueArray[1];
            resultMap.put(key, value);
        });
        final DfBeanDesc beanDesc = DfBeanDescFactory.getBeanDesc((Class<?>) type);
        @SuppressWarnings("unchecked")
        final RESULT result = (RESULT) DfReflectionUtil.newInstance((Class<?>) type);
        beanDesc.getProppertyNameList().stream().forEach(proppertyName -> {
            final DfPropertyDesc propertyDesc = beanDesc.getPropertyDesc(proppertyName);
            final String deserializeDParameterName = asDeserializedParameterName(propertyDesc);
            if (resultMap.containsKey(deserializeDParameterName)) {
                final String value = resultMap.get(deserializeDParameterName);
                propertyDesc.setValue(result, value);
            }
        });
        return result;
    }

    protected String asDeserializedParameterName(DfPropertyDesc propertyDesc) {
        return propertyDesc.getPropertyName();
    }

    protected Object asDeserializedParameterValue(String value) {
        if (value == null) {
            return null;
        }
        return value;
    }
}
