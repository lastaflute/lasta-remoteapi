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
package org.dbflute.remoteapi.rule;

import java.time.format.DateTimeFormatter;

/**
 * @author jflute
 * @author mito
 */
public class FlutyEmptyRemoteConversionRule implements FlutyRemoteConversionRule { // state-less

    // ===================================================================================
    //                                                                               Date
    //                                                                              ======
    public DateTimeFormatter getDateFormatter() {
        return null; // uses default
    }

    public DateTimeFormatter getDateTimeFormatter() {
        return null; // uses default
    }

    // ===================================================================================
    //                                                                             Boolean
    //                                                                             =======
    // simple conversion, for e.g. form parameter
    public String serializeBoolean(Boolean boo) {
        return boo != null ? boo.toString() : null; // e.g. "true", "false"
    }

    public Boolean deserializeBoolean(Object exp) {
        return exp != null ? Boolean.valueOf(exp.toString()) : null; // expects "true" or not
    }

    // ===================================================================================
    //                                                                      Classification
    //                                                                      ==============
    public String getClsPreferredItem() {
        return null; // means unused
    }
}
