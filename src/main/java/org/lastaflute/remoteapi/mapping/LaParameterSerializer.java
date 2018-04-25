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
package org.lastaflute.remoteapi.mapping;

import java.lang.reflect.Field;

import org.dbflute.helper.beans.DfPropertyDesc;
import org.dbflute.remoteapi.mapping.FlParameterSerializer;
import org.dbflute.remoteapi.mapping.FlRemoteMappingPolicy;
import org.dbflute.util.Srl;
import org.lastaflute.remoteapi.mapping.LaRemoteMappingPolicy.FormFieldNaming;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.annotations.SerializedName;

/**
 * @author jflute
 * @since 0.3.7 (2017/10/06 Friday at showbase)
 */
public class LaParameterSerializer extends FlParameterSerializer {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final FieldNamingPolicy LOWER_CASE_WITH_UNDERSCORES = FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES;

    // ===================================================================================
    //                                                                      Parameter Name
    //                                                                      ==============
    @Override
    public String asSerializedParameterName(DfPropertyDesc propertyDesc, FlRemoteMappingPolicy mappingPolicy) {
        // #hope migrate to standard annotation, Gson's annotation for now (for compatible) by jflute
        final SerializedName serializedName = propertyDesc.getField().getAnnotation(SerializedName.class);
        if (serializedName != null) {
            return serializedName.value();
        }
        if (mappingPolicy instanceof LaSelectedMappingPolicy) {
            final FormFieldNaming fieldNaming = ((LaSelectedMappingPolicy) mappingPolicy).getFieldNaming();
            if (FormFieldNaming.IDENTITY.equals(fieldNaming)) {
                return super.asSerializedParameterName(propertyDesc, mappingPolicy);
            } else if (FormFieldNaming.CAMEL_TO_LOWER_SNAKE.equals(fieldNaming)) {
                return convertCamelToLowerSnake(propertyDesc);
            } else {
                throw new IllegalStateException("Unknown form field naming: " + fieldNaming);
            }
        } else { // uses lasta but non-lasta mapping policy
            return super.asSerializedParameterName(propertyDesc, mappingPolicy);
        }
    }

    // -----------------------------------------------------
    //                                        to Lower Snake
    //                                        --------------
    protected String convertCamelToLowerSnake(DfPropertyDesc propertyDesc) {
        final Field field = propertyDesc.getField(); // null allowed if non-field property
        final String propertyName = propertyDesc.getPropertyName();
        if (canTreatAsFieldNaming(field, propertyName)) { // e.g. public field
            return decamelizeLowerByGson(field);
        } else { // e.g. method property
            return decamelizeLowerByDefaultEngine(propertyName);
        }
    }

    protected boolean canTreatAsFieldNaming(Field field, String propertyName) {
        return field != null && field.getName().equals(propertyName);
    }

    protected String decamelizeLowerByGson(Field field) {
        return LOWER_CASE_WITH_UNDERSCORES.translateName(field); // same engine as json's conversion for coherence
    }

    protected String decamelizeLowerByDefaultEngine(String propertyName) {
        return Srl.decamelize(propertyName).toLowerCase(); // as default engine
    }
}
