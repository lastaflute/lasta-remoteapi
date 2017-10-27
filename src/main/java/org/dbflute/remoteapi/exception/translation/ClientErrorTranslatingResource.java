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
package org.dbflute.remoteapi.exception.translation;

import java.lang.reflect.Type;
import java.util.function.BiFunction;

import org.dbflute.optional.OptionalThing;
import org.dbflute.remoteapi.exception.RemoteApiHttpClientErrorException;

/**
 * @author jflute
 * @since 0.3.3 (2017/09/21 Thursday)
 */
public class ClientErrorTranslatingResource {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Type returnType; // not null
    protected final String url; // not null
    protected final RemoteApiHttpClientErrorException clientError; // not null
    protected final BiFunction<RemoteApiHttpClientErrorException, Object, RuntimeException> validationErrorProvider; // null allowed

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ClientErrorTranslatingResource(Type returnType, String url, RemoteApiHttpClientErrorException clientError,
            BiFunction<RemoteApiHttpClientErrorException, Object, RuntimeException> validationErrorProvider) {
        this.returnType = returnType;
        this.url = url;
        this.clientError = clientError;
        this.validationErrorProvider = validationErrorProvider;
    }

    // ===================================================================================
    //                                                                              Facade
    //                                                                              ======
    /**
     * @param messages The messages from error response. (NotNull)
     * @return The exception of validation error for HTML response. (NotNull)
     */
    public RuntimeException asHtmlValidationError(Object messages) {
        if (messages == null) {
            throw new IllegalArgumentException("The argument 'messages' should not be null.");
        }
        assertValidationErrorPrepared(messages);
        return validationErrorProvider.apply(clientError, messages);
    }

    protected void assertValidationErrorPrepared(Object messages) {
        if (validationErrorProvider == null) {
            String msg = "Not found the validation error provider, unsupported?: messages=" + messages;
            throw new IllegalStateException(msg);
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public Type getReturnType() {
        return returnType;
    }

    public String getUrl() {
        return url;
    }

    @Deprecated
    public RemoteApiHttpClientErrorException getCause() { // use getClientError()
        return clientError;
    }

    public RemoteApiHttpClientErrorException getClientError() {
        return clientError;
    }

    public OptionalThing<BiFunction<RemoteApiHttpClientErrorException, Object, RuntimeException>> getValidationErrorHook() {
        return OptionalThing.ofNullable(validationErrorProvider, () -> {
            throw new IllegalStateException("Not found the validation error provider: url=" + url);
        });
    }
}
