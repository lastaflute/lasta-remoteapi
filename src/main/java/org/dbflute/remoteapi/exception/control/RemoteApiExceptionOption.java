/*
 * Copyright 2015-2025 the original author or authors.
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
package org.dbflute.remoteapi.exception.control;

import java.util.function.Function;

import org.dbflute.optional.OptionalThing;
import org.dbflute.util.DfAssertUtil;

/**
 * @author jflute
 * @since 0.5.2 (2025/09/26 Friday at ichihara)
 */
public class RemoteApiExceptionOption {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected Function<String, String> messageRemoteApiUrlFilter; // null allowed, not required
    protected Function<Object, String> messageRequestParameterFilter; // null allowed, not required

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
    public RemoteApiExceptionOption filterMessageRemoteApiUrl(Function<String, String> oneArgLambda) {
        DfAssertUtil.assertObjectNotNull("oneArgLambda (messageRemoteApiUrlFilter)", oneArgLambda);
        messageRemoteApiUrlFilter = oneArgLambda;
        return this;
    }

    public RemoteApiExceptionOption filterMessageRequestParameter(Function<Object, String> oneArgLambda) {
        DfAssertUtil.assertObjectNotNull("oneArgLambda (messageRequestParameterFilter)", oneArgLambda);
        messageRequestParameterFilter = oneArgLambda;
        return this;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("exception:{");
        sb.append(messageRemoteApiUrlFilter);
        sb.append(", ").append(messageRequestParameterFilter);
        sb.append("}");
        return sb.toString();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public OptionalThing<Function<String, String>> getMessageRemoteApiUrlFilter() {
        return OptionalThing.ofNullable(messageRemoteApiUrlFilter, () -> {
            throw new IllegalStateException("Not found the messageRemoteApiUrlFilter.");
        });
    }

    public OptionalThing<Function<Object, String>> getMessageRequestParameterFilter() {
        return OptionalThing.ofNullable(messageRequestParameterFilter, () -> {
            throw new IllegalStateException("Not found the messageRequestParameterFilter.");
        });
    }
}
