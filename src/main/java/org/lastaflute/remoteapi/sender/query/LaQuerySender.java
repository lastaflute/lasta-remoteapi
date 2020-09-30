/*
 * Copyright 2015-2020 the original author or authors.
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
package org.lastaflute.remoteapi.sender.query;

import org.dbflute.remoteapi.mapping.FlParameterSerializer;
import org.dbflute.remoteapi.mapping.FlRemoteMappingPolicy;
import org.dbflute.remoteapi.sender.query.FlQuerySender;
import org.lastaflute.remoteapi.mapping.LaParameterSerializer;

/**
 * @author jflute
 */
public class LaQuerySender extends FlQuerySender {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public LaQuerySender(FlRemoteMappingPolicy mappingPolicy) {
        super(mappingPolicy);
    }

    @Override
    protected FlParameterSerializer createParameterSerializer() {
        return new LaParameterSerializer(); // for e.g. field naming
    }
}
