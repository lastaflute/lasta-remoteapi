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
package org.dbflute.remoteapi.sender.body;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.dbflute.helper.beans.DfBeanDesc;
import org.dbflute.helper.beans.DfPropertyDesc;
import org.dbflute.helper.beans.factory.DfBeanDescFactory;
import org.dbflute.remoteapi.mapping.FlParameterSerializer;
import org.dbflute.remoteapi.mapping.FlRemoteMappingPolicy;

/**
 * @author awane
 * @author jflute
 */
public class FlFormSender implements RequestBodySender {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final FlRemoteMappingPolicy mappingPolicy;
    protected final FlParameterSerializer parameterSerializer;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public FlFormSender(FlRemoteMappingPolicy mappingPolicy) {
        this.mappingPolicy = mappingPolicy;
        this.parameterSerializer = createParameterSerializer();
    }

    protected FlParameterSerializer createParameterSerializer() {
        return new FlParameterSerializer();
    }

    // ===================================================================================
    //                                                                             Prepare
    //                                                                             =======
    @Override
    public void prepareBodyRequest(HttpEntityEnclosingRequest enclosingRequest, Object form) {
        final DfBeanDesc beanDesc = DfBeanDescFactory.getBeanDesc(form.getClass());
        final List<NameValuePair> parameters = new ArrayList<>();
        beanDesc.getProppertyNameList().stream().forEach(proppertyName -> {
            final DfPropertyDesc propertyDesc = beanDesc.getPropertyDesc(proppertyName);
            final String serializedParameterName = asSerializedParameterName(propertyDesc);
            final Object plainValue = beanDesc.getPropertyDesc(proppertyName).getValue(form);
            if (plainValue != null && Iterable.class.isAssignableFrom(plainValue.getClass())) {
                final Iterable<?> plainValueIterable = (Iterable<?>) plainValue;
                plainValueIterable.forEach(value -> {
                    parameters.add(new BasicNameValuePair(serializedParameterName, asSerializedParameterValue(value)));
                });
            } else {
                parameters.add(new BasicNameValuePair(serializedParameterName, asSerializedParameterValue(plainValue)));
            }
        });
        enclosingRequest.setEntity(new UrlEncodedFormEntity(parameters, StandardCharsets.UTF_8));
    }

    // ===================================================================================
    //                                                                  Parameter Handling
    //                                                                  ==================
    protected String asSerializedParameterName(DfPropertyDesc propertyDesc) { // may be overridden
        return propertyDesc.getPropertyName();
    }

    protected String asSerializedParameterValue(Object value) {
        return parameterSerializer.asSerializedParameterValue(value, mappingPolicy);
    }
}
