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
package org.dbflute.remoteapi.mapping;

import java.time.format.DateTimeFormatter;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author jflute
 * @since 0.3.7 (2017/10/06 Friday)
 */
public class FlSelectedMappingPolicy implements FlRemoteMappingPolicy { // state-ful

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected DateTimeFormatter dateFormatter;
    protected DateTimeFormatter dateTimeFormatter;
    protected Function<Boolean, String> booleanSerializer;
    protected Predicate<Object> booleanDeserializer;
    protected String clsPreferredItem;

    // ===================================================================================
    //                                                                     Selected Option
    //                                                                     ===============
    // -----------------------------------------------------
    //                                                 Date
    //                                                ------
    public FlSelectedMappingPolicy dateFormatter(DateTimeFormatter dateFormatter) {
        assertArgumentNotNull("dateFormatter", dateFormatter);
        this.dateFormatter = dateFormatter;
        return this;
    }

    public FlSelectedMappingPolicy dateTimeFormatter(DateTimeFormatter dateTimeFormatter) {
        assertArgumentNotNull("dateTimeFormatter", dateTimeFormatter);
        this.dateTimeFormatter = dateTimeFormatter;
        return this;
    }

    // -----------------------------------------------------
    //                                               Boolean
    //                                               -------
    public FlSelectedMappingPolicy booleanSerializer(Function<Boolean, String> booleanSerializer) {
        assertArgumentNotNull("booleanSerializer", booleanSerializer);
        this.booleanSerializer = booleanSerializer;
        return this;
    }

    public FlSelectedMappingPolicy booleanDeserializer(Predicate<Object> booleanDeserializer) {
        assertArgumentNotNull("booleanDeserializer", booleanDeserializer);
        this.booleanDeserializer = booleanDeserializer;
        return this;
    }

    // -----------------------------------------------------
    //                                        Classification
    //                                        --------------
    public FlSelectedMappingPolicy clsPreferredItem(String clsPreferredItem) {
        assertArgumentNotNull("clsPreferredItem", clsPreferredItem);
        this.clsPreferredItem = clsPreferredItem;
        return this;
    }

    // ===================================================================================
    //                                                                      Implementation
    //                                                                      ==============
    @Override
    public DateTimeFormatter getDateFormatter() {
        return dateFormatter;
    }

    @Override
    public DateTimeFormatter getDateTimeFormatter() {
        return dateTimeFormatter;
    }

    @Override
    public String serializeBoolean(Boolean boo) {
        return booleanSerializer != null ? booleanSerializer.apply(boo) : FlRemoteMappingPolicy.booleanToString(boo);
    }

    @Override
    public Boolean deserializeBoolean(Object exp) {
        return booleanDeserializer != null ? booleanDeserializer.test(exp) : FlRemoteMappingPolicy.booleanValueOf(exp);
    }

    @Override
    public String getClsPreferredItem() {
        return clsPreferredItem;
    }

    // ===================================================================================
    //                                                                        Small Helper
    //                                                                        ============
    protected void assertArgumentNotNull(String variableName, Object value) {
        if (variableName == null) {
            throw new IllegalArgumentException("The variableName should not be null.");
        }
        if (value == null) {
            throw new IllegalArgumentException("The argument '" + variableName + "' should not be null.");
        }
    }
}
