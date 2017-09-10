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
package org.lastaflute.remoteapi.receiver;

import org.dbflute.helper.beans.DfPropertyDesc;
import org.dbflute.remoteapi.receiver.FlSplitReceiver;

import com.google.gson.annotations.SerializedName;

/**
 * @author awane
 * @author jflute
 */
public class LaSplitReceiver extends FlSplitReceiver {

    public LaSplitReceiver(String delimiter, String delimiterByKeyValue) {
        super(delimiter, delimiterByKeyValue);
    }

    @Override
    protected String asDeserializedParameterName(DfPropertyDesc propertyDesc) {
        final SerializedName serializedName = propertyDesc.getField().getAnnotation(SerializedName.class);
        if (serializedName != null) {
            return serializedName.value();
        }
        return super.asDeserializedParameterName(propertyDesc);
    }
}
