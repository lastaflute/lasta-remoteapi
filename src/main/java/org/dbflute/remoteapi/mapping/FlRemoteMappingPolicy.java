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

/**
 * @author jflute
 */
public interface FlRemoteMappingPolicy {

    // not use optional for performance
    // ===================================================================================
    //                                                                               Date
    //                                                                              ======
    /**
     * @return The formatter of date. (NullAllowed: if null, use default format that is ISO)
     */
    default DateTimeFormatter getDateFormatter() {
        return null;
    }

    /**
     * @return The formatter of date-time. (NullAllowed: if null, use default format that is ISO)
     */
    default DateTimeFormatter getDateTimeFormatter() {
        return null;
    }

    // ===================================================================================
    //                                                                             Boolean
    //                                                                             =======
    /**
     * @param boo The value of boolean converted to string. (NullAllowed: if null, returns null)
     * @return The string for the boolean. (NullAllowed: if argument is null)
     */
    default String serializeBoolean(Boolean boo) {
        return booleanToString(boo);
    }

    static String booleanToString(Boolean boo) { // for overriding method use
        return boo != null ? boo.toString() : null; // e.g. "true", "false"
    }

    /**
     * @param exp The expression converted to boolean. (NullAllowed: if null, returns null)
     * @return The boolean for the expression. (NullAllowed: if argument is null)
     */
    default Boolean deserializeBoolean(Object exp) {
        return booleanValueOf(exp);
    }

    static Boolean booleanValueOf(Object exp) { // for overriding method use
        return exp != null ? Boolean.valueOf(exp.toString()) : null; // expects "true" or not
    }

    // ===================================================================================
    //                                                                      Classification
    //                                                                      ==============
    /**
     * @return The preferred sub-item (name) for classification. (NullAllowed: if null, unused)
     */
    default String getClsPreferredItem() {
        return null; // means unused
    }
}
