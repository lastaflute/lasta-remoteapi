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
package org.dbflute.remoteapi.sender.query;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import org.dbflute.helper.beans.DfBeanDesc;
import org.dbflute.helper.beans.DfPropertyDesc;
import org.dbflute.helper.beans.factory.DfBeanDescFactory;
import org.dbflute.remoteapi.FlutyRemoteApiRule;
import org.dbflute.remoteapi.logging.SendReceiveLogOption;
import org.dbflute.remoteapi.mapping.FlParameterSerializer;
import org.dbflute.remoteapi.mapping.FlRemoteMappingPolicy;

/**
 * @author jflute
 */
public class FlQuerySender implements QueryParameterSender {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final FlRemoteMappingPolicy mappingPolicy;
    protected final FlParameterSerializer parameterSerializer;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public FlQuerySender(FlRemoteMappingPolicy mappingPolicy) {
        this.mappingPolicy = mappingPolicy;
        this.parameterSerializer = createParameterSerializer();
    }

    protected FlParameterSerializer createParameterSerializer() { // may be overridden
        return new FlParameterSerializer();
    }

    // ===================================================================================
    //                                                                             Convert
    //                                                                             =======
    @Override
    public String toQueryString(Object param, Charset charset, FlutyRemoteApiRule rule) {
        final String queryString = buildQueryString(param, charset);
        readySendReceiveLogIfNeeds(rule, param, queryString);
        return queryString;
    }

    protected String buildQueryString(Object form, Charset charset) {
        final StringBuilder sb = new StringBuilder();
        final String charsetName = charset.name();
        final DfBeanDesc beanDesc = DfBeanDescFactory.getBeanDesc(form.getClass());
        final MyValueHolder<Integer> paramIndex = new MyValueHolder<>(0);
        for (String propertyName : beanDesc.getProppertyNameList()) {
            final DfPropertyDesc propertyDesc = beanDesc.getPropertyDesc(propertyName);
            final Object plainValue = propertyDesc.getValue(form);
            if (plainValue != null) {
                if (Iterable.class.isAssignableFrom(plainValue.getClass())) {
                    Iterable<?> plainValueIterable = (Iterable<?>) plainValue;
                    plainValueIterable.forEach(value -> {
                        sb.append(paramIndex.getValue() == 0 ? "?" : "&");
                        sb.append(asSerializedParameterName(propertyDesc)).append("=");
                        try {
                            sb.append(URLEncoder.encode(asSerializedParameterValue(value), charsetName));
                        } catch (UnsupportedEncodingException e) {
                            throw new IllegalStateException("Unknown encoding: " + charsetName, e);
                        }
                    });
                } else {
                    sb.append(paramIndex.getValue() == 0 ? "?" : "&");
                    sb.append(asSerializedParameterName(propertyDesc)).append("=");
                    try {
                        sb.append(URLEncoder.encode(asSerializedParameterValue(plainValue), charsetName));
                    } catch (UnsupportedEncodingException e) {
                        throw new IllegalStateException("Unknown encoding: " + charsetName, e);
                    }
                }
                paramIndex.setValue(paramIndex.getValue() + 1);
            }
        }
        return sb.toString();
    }

    protected String asSerializedParameterName(DfPropertyDesc propertyDesc) {
        return parameterSerializer.asSerializedParameterName(propertyDesc, mappingPolicy);
    }

    protected String asSerializedParameterValue(Object value) {
        return parameterSerializer.asSerializedParameterValue(value, mappingPolicy);
    }

    // -----------------------------------------------------
    //                                  Send/Receive Logging
    //                                  --------------------
    protected void readySendReceiveLogIfNeeds(FlutyRemoteApiRule rule, Object param, String queryString) {
        final SendReceiveLogOption option = rule.getSendReceiveLogOption();
        if (option.isEnabled()) {
            option.keeper().keepQueryParameter(queryString);
        }
    }

    // ===================================================================================
    //                                                                        Small Helper
    //                                                                        ============
    protected static class MyValueHolder<T> { // copied from Lasta Di

        protected T value;

        public MyValueHolder() {
        }

        public MyValueHolder(T value) {
            this.value = value;
        }

        public T getValue() {
            return value;
        }

        public void setValue(final T value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value.toString();
        }
    }
}
