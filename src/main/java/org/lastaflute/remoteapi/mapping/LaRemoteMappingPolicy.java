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

import org.dbflute.remoteapi.mapping.FlRemoteMappingPolicy;

/**
 * @author jflute
 */
public interface LaRemoteMappingPolicy extends FlRemoteMappingPolicy {

    // ===================================================================================
    //                                                                        Field Naming
    //                                                                        ============
    /**
     * @return The field naming type of form. (contains query form) (NotNull)
     */
    default FormFieldNaming getFieldNaming() {
        return defaultFieldNaming();
    }

    static FormFieldNaming defaultFieldNaming() { // for overriding method use
        return FormFieldNaming.IDENTITY;
    }

    enum FormFieldNaming {

        /** Gson's FieldNamingPolicy.IDENTITY */
        IDENTITY,
        /** Gson's FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES */
        CAMEL_TO_LOWER_SNAKE
    }
}
