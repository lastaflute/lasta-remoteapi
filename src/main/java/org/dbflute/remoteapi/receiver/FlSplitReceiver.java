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
import org.dbflute.optional.OptionalThing;
import org.dbflute.remoteapi.FlutyRemoteApiRule;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfReflectionUtil;

/**
 * @author awane
 * @author jflute
 */
public class FlSplitReceiver extends FlBaseReceiver {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String delimiter;
    protected final String delimiterByKeyValue;
    protected final String responseBodyType;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public FlSplitReceiver(String delimiter, String delimiterByKeyValue) {
        this.delimiter = delimiter;
        this.delimiterByKeyValue = delimiterByKeyValue;
        this.responseBodyType = buildResponseBodyType(delimiter);
    }

    protected String buildResponseBodyType(String delimiter) {
        return "split(" + delimiter + ", " + delimiterByKeyValue + ")";
    }

    // ===================================================================================
    //                                                                          Convert to
    //                                                                          ==========
    @Override
    public <RETURN> RETURN toResponseReturn(OptionalThing<String> body, Type type, FlutyRemoteApiRule rule) {
        if (!(type instanceof Class<?>)) {
            throw new IllegalArgumentException("The specified type is not Class: type=" + type);
        }
        final String target = body.orElseThrow(() -> { // translated with rich message so simple here
            return new IllegalStateException("Not found the response body as SPLIT.");
        });
        final Map<String, String> returnMap = DfCollectionUtil.newLinkedHashMap();
        Arrays.stream(target.split(delimiter)).forEach(keyValue -> {
            String[] keyValueArray = keyValue.split(delimiterByKeyValue);
            if (keyValueArray.length != 2) {
                return;
            }
            final String key = keyValueArray[0];
            final String value = keyValueArray[1];
            returnMap.put(key, value);
        });
        @SuppressWarnings("unchecked")
        final RETURN ret = (RETURN) DfReflectionUtil.newInstance((Class<?>) type);
        final DfBeanDesc beanDesc = DfBeanDescFactory.getBeanDesc((Class<?>) type);
        beanDesc.getProppertyNameList().stream().forEach(proppertyName -> {
            final DfPropertyDesc propertyDesc = beanDesc.getPropertyDesc(proppertyName);
            final String deserializeDParameterName = asDeserializedParameterName(propertyDesc);
            if (returnMap.containsKey(deserializeDParameterName)) {
                final String value = returnMap.get(deserializeDParameterName);
                propertyDesc.setValue(ret, value);
            }
        });
        readySendReceiveLogIfNeeds(rule, body, target);
        return ret;
    }

    protected String asDeserializedParameterName(DfPropertyDesc propertyDesc) {
        return propertyDesc.getPropertyName();
    }

    protected Object asDeserializedParameterValue(String value) {
        return value;
    }

    // -----------------------------------------------------
    //                                  Send/Receive Logging
    //                                  --------------------
    @Override
    protected String getSendReceiveLogResponseBodyType() {
        return responseBodyType;
    }
}
