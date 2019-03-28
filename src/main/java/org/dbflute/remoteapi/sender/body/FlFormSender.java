/*
 * Copyright 2015-2019 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.dbflute.helper.beans.DfBeanDesc;
import org.dbflute.helper.beans.DfPropertyDesc;
import org.dbflute.helper.beans.factory.DfBeanDescFactory;
import org.dbflute.remoteapi.FlutyRemoteApiRule;
import org.dbflute.remoteapi.logging.SendReceiveLogOption;
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
    public void prepareEnclosingRequest(HttpEntityEnclosingRequest enclosingRequest, Object param, FlutyRemoteApiRule rule) {
        final List<NameValuePair> parameterList = prepareTextParameterList(enclosingRequest, param, rule);
        enclosingRequest.setEntity(prepareEnclosedHttpEntity(param, parameterList, rule));
        readySendReceiveLogIfNeeds(param, parameterList, rule);
    }

    // -----------------------------------------------------
    //                                        Text Parameter
    //                                        --------------
    protected List<NameValuePair> prepareTextParameterList(HttpEntityEnclosingRequest enclosingRequest, Object param,
            FlutyRemoteApiRule rule) {
        final DfBeanDesc beanDesc = DfBeanDescFactory.getBeanDesc(param.getClass());
        final List<NameValuePair> parameters = new ArrayList<>();
        beanDesc.getProppertyNameList().stream().forEach(proppertyName -> {
            final DfPropertyDesc propertyDesc = beanDesc.getPropertyDesc(proppertyName);
            if (isExceptTextParameter(param, propertyDesc, rule)) { // e.g. multi-part
                return;
            }
            final String serializedParameterName = asSerializedParameterName(propertyDesc);
            final Object plainValue = beanDesc.getPropertyDesc(proppertyName).getValue(param);
            if (plainValue != null && Iterable.class.isAssignableFrom(plainValue.getClass())) {
                final Iterable<?> plainValueIterable = (Iterable<?>) plainValue;
                plainValueIterable.forEach(value -> {
                    parameters.add(createBasicNameValuePair(serializedParameterName, asSerializedParameterValue(value)));
                });
            } else {
                parameters.add(createBasicNameValuePair(serializedParameterName, asSerializedParameterValue(plainValue)));
            }
        });
        return parameters;
    }

    protected boolean isExceptTextParameter(Object param, DfPropertyDesc propertyDesc, FlutyRemoteApiRule rule) {
        return false; // you can override for e.g. multi-part
    }

    protected BasicNameValuePair createBasicNameValuePair(String name, String value) {
        return new BasicNameValuePair(name, value);
    }

    // -----------------------------------------------------
    //                                           HTTP Entity
    //                                           -----------
    protected HttpEntity prepareEnclosedHttpEntity(Object param, List<NameValuePair> parameterList, FlutyRemoteApiRule rule) {
        return createUrlEncodedFormEntity(parameterList, rule); // as default, may be overridden for e.g. multi-part
    }

    protected UrlEncodedFormEntity createUrlEncodedFormEntity(List<NameValuePair> parameterList, FlutyRemoteApiRule rule) {
        return new UrlEncodedFormEntity(parameterList, rule.getRequestBodyCharset());
    }

    // -----------------------------------------------------
    //                                  Send/Receive Logging
    //                                  --------------------
    protected void readySendReceiveLogIfNeeds(Object param, List<NameValuePair> textParameterList, FlutyRemoteApiRule rule) {
        final SendReceiveLogOption option = rule.getSendReceiveLogOption();
        if (option.isEnabled()) {
            final Map<String, String> keptMap = prepareLoggingParameterMap(param, textParameterList);
            option.keeper().keepFormParameter(keptMap);
        }
    }

    protected Map<String, String> prepareLoggingParameterMap(Object param, List<NameValuePair> textParameterList) {
        return textParameterList.stream().collect(Collectors.toMap(bean -> bean.getName(), bean -> bean.getValue()));
    }

    // ===================================================================================
    //                                                                  Parameter Handling
    //                                                                  ==================
    protected String asSerializedParameterName(DfPropertyDesc propertyDesc) { // may be overridden
        return parameterSerializer.asSerializedParameterName(propertyDesc, mappingPolicy);
    }

    protected String asSerializedParameterValue(Object value) {
        return parameterSerializer.asSerializedParameterValue(value, mappingPolicy);
    }
}
