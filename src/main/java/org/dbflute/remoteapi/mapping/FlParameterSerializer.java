/*
 * Copyright 2015-2018 the original author or authors.
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
package org.dbflute.remoteapi.mapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.dbflute.helper.beans.DfPropertyDesc;
import org.dbflute.jdbc.Classification;

/**
 * @author jflute
 */
public class FlParameterSerializer {

    // ===================================================================================
    //                                                                      Parameter Name
    //                                                                      ==============
    public String asSerializedParameterName(DfPropertyDesc propertyDesc, FlRemoteMappingPolicy mappingPolicy) {
        return propertyDesc.getPropertyName();
    }

    // ===================================================================================
    //                                                                     Parameter Value
    //                                                                     ===============
    public String asSerializedParameterValue(Object value, FlRemoteMappingPolicy mappingPolicy) { // with standard rule filter
        if (value == null) {
            return null;
        }
        final String realValue;
        if (value instanceof LocalDate) {
            final DateTimeFormatter formatter = mappingPolicy.getDateFormatter(); // null allowed
            realValue = ((LocalDate) value).format(formatter != null ? formatter : DateTimeFormatter.ISO_LOCAL_DATE);
        } else if (value instanceof LocalDateTime) {
            final DateTimeFormatter formatter = mappingPolicy.getDateTimeFormatter(); // null allowed
            realValue = ((LocalDateTime) value).format(formatter != null ? formatter : DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } else if (value instanceof Boolean || boolean.class.equals(value.getClass())) {
            realValue = mappingPolicy.serializeBoolean((boolean) value);
        } else if (value instanceof Classification) {
            final Classification cls = (Classification) value;
            final Map<String, Object> map = cls.subItemMap();
            final String clsPreferredItem = mappingPolicy.getClsPreferredItem(); // null allowed
            final String preferredValue = clsPreferredItem != null ? (String) map.get(clsPreferredItem) : null;
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
