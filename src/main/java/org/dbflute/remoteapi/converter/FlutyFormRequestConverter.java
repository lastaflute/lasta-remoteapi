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

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.dbflute.helper.beans.DfBeanDesc;
import org.dbflute.helper.beans.DfPropertyDesc;
import org.dbflute.helper.beans.factory.DfBeanDescFactory;
import org.dbflute.jdbc.Classification;
import org.dbflute.remoteapi.rule.FlutyRemoteMappingPolicy;

/**
 * @author awane
 * @author jflute
 */
public class FlutyFormRequestConverter implements FlutyRequestConverter {

    protected final FlutyRemoteMappingPolicy mappingPolicy;

    public FlutyFormRequestConverter(FlutyRemoteMappingPolicy mappingPolicy) {
        this.mappingPolicy = mappingPolicy;
    }

    public void prepareHttpPost(HttpPost httpPost, Object form) {
        final DfBeanDesc beanDesc = DfBeanDescFactory.getBeanDesc(form.getClass());
        final List<NameValuePair> parameters = new ArrayList<>();
        beanDesc.getProppertyNameList().stream().forEach(proppertyName -> {
            DfPropertyDesc propertyDesc = beanDesc.getPropertyDesc(proppertyName);
            String serializedParameterName = asSerializedParameterName(propertyDesc);
            final Object plainValue = beanDesc.getPropertyDesc(proppertyName).getValue(form);
            if (plainValue != null && Iterable.class.isAssignableFrom(plainValue.getClass())) {
                Iterable<?> plainValueIterable = (Iterable<?>) plainValue;
                plainValueIterable.forEach(value -> {
                    parameters.add(new BasicNameValuePair(serializedParameterName, asSerializedParameterValue(value)));
                });
            } else {
                parameters.add(new BasicNameValuePair(serializedParameterName, asSerializedParameterValue(plainValue)));
            }
        });
        httpPost.setEntity(new UrlEncodedFormEntity(parameters, StandardCharsets.UTF_8));
    }

    // #hope awane copied from FlutyRemoteApi, needs to refactor (2017/06/27)
    // ===================================================================================
    //                                                                  Parameter Handling
    //                                                                  ==================
    protected String asSerializedParameterName(DfPropertyDesc propertyDesc) { // may be overridden
        return propertyDesc.getPropertyName();
    }

    protected String asSerializedParameterValue(Object value) { // with standard rule filter
        if (value == null) {
            return null;
        }
        final String realValue;
        if (value instanceof LocalDate) {
            realValue = ((LocalDate) value).format(mappingPolicy.getDateFormatter());
        } else if (value instanceof LocalDateTime) {
            realValue = ((LocalDateTime) value).format(mappingPolicy.getDateTimeFormatter());
        } else if (value instanceof Boolean || boolean.class.equals(value.getClass())) {
            realValue = mappingPolicy.serializeBoolean((boolean) value);
        } else if (value instanceof Classification) {
            final Classification cls = (Classification) value;
            final Map<String, Object> map = cls.subItemMap();
            final String preferredValue = (String) map.get(mappingPolicy.getClsPreferredItem());
            if (preferredValue != null) { // means Flg
                realValue = preferredValue;
            } else {
                realValue = cls.code();
            }
        } else {
            realValue = value.toString();
        }
        return realValue;
    }
}
