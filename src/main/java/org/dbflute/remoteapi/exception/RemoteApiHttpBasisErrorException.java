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
package org.dbflute.remoteapi.exception;

import org.dbflute.optional.OptionalThing;

/**
 * @author jflute
 * @author awane
 * @since 0.2.1 (2017/09/10 Sunday at bay maihama)
 */
public class RemoteApiHttpBasisErrorException extends RemoteApiBaseException {

    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final int httpStatus;
    protected final Object failureResponse; // null allowed

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public RemoteApiHttpBasisErrorException(String msg, int httpStatus, Object failureResponse) {
        super(msg);
        this.httpStatus = httpStatus;
        this.failureResponse = failureResponse;
    }

    // ===================================================================================
    //                                                                         Translating
    //                                                                         ===========
    /**
     * Throw your translating exception if the specified status matches.
     * <pre>
     * try {
     *     ...(calling remote API)
     * } catch (RemoteApiHttpClientErrorException e) {
     *     e.throwIfStatus(403, () -&gt; new SeaLandException("your message", e);
     *     throw e;
     * }
     * </pre>
     * @param <EXP> The type of exception.
     * @param httpStatus The HTTP status of remote API response.
     * @param noArgInLambda The callback for creating exception as translating. (NotNull)
     * @throws EXP When the status matches.
     */
    public <EXP extends Exception> void throwIfStatus(int httpStatus, TranslatedExceptionProvider<EXP> noArgInLambda) throws EXP {
        if (this.httpStatus == httpStatus) {
            throw noArgInLambda.provide();
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public int getHttpStatus() {
        return httpStatus;
    }

    public OptionalThing<Object> getFailureResponse() {
        return OptionalThing.ofNullable(failureResponse, () -> {
            throw new IllegalStateException("Not found the failureResponse.");
        });
    }
}
