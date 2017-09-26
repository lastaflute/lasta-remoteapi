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

import java.util.function.Supplier;

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
    protected final RemoteApiFailureResponseHolder failureResponseHolder; // not null

    public static class RemoteApiFailureResponseHolder {

        protected final Object failureResponse; // null allowed
        protected final Supplier<RuntimeException> emptyResponseCause; // null allowed

        public RemoteApiFailureResponseHolder(Object failureResponse, Supplier<RuntimeException> emptyResponseCause) {
            this.failureResponse = failureResponse;
            this.emptyResponseCause = emptyResponseCause;
        }

        public Object getFailureResponse() {
            return failureResponse;
        }

        public Supplier<RuntimeException> getEmptyResponseCause() {
            return emptyResponseCause;
        }
    }

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public RemoteApiHttpBasisErrorException(String msg, int httpStatus, RemoteApiFailureResponseHolder failureResponseHolder) {
        super(msg);
        this.httpStatus = httpStatus;
        if (failureResponseHolder != null) {
            this.failureResponseHolder = failureResponseHolder;
        } else { // for outer framework
            this.failureResponseHolder = new RemoteApiFailureResponseHolder(null, null);
        }
    }

    // botsu: to keep simple by jflute (2017/09/13)
    //// ===================================================================================
    ////                                                                         Translating
    ////                                                                         ===========
    ///**
    // * Throw your translating exception if the specified status matches.
    // * <pre>
    // * try {
    // *     ...(calling remote API)
    // * } catch (RemoteApiHttpClientErrorException e) {
    // *     e.throwIfStatus(403, () -&gt; new SeaLandException("your message", e);
    // *     throw e;
    // * }
    // * </pre>
    // * @param <EXP> The type of exception.
    // * @param httpStatus The HTTP status of remote API response.
    // * @param noArgInLambda The callback for creating exception as translating. (NotNull)
    // * @throws EXP When the status matches.
    // */
    //public <EXP extends Exception> void throwIfStatus(int httpStatus, TranslatedExceptionProvider<EXP> noArgInLambda) throws EXP {
    //    if (this.httpStatus == httpStatus) {
    //        throw noArgInLambda.provide();
    //    }
    //}
    //
    //// now making by jflute
    ////public <EXP extends Exception> void throwIfStatusAnd(int httpStatus, Predicate<OptionalThing<Object>> oneArgLambda,
    ////        TranslatedExceptionProvider<EXP> noArgInLambda) throws EXP {
    ////    if (this.httpStatus == httpStatus) {
    ////        if (oneArgLambda.test(getFailureResponse())) {
    ////            throw noArgInLambda.provide();
    ////        }
    ////    }
    ////}

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    /**
     * Get the HTTP status of remote API response.
     * @return The status code as integer.
     */
    public int getHttpStatus() {
        return httpStatus;
    }

    /**
     * Get failure response data as specified type in your rule. <br>
     * It returns empty optional if the following case:
     * <pre>
     * o no failure response type in your rule
     *  =&gt; RemoteApiFailureResponseTypeNotFoundException
     * 
     * o parse failure of failure response:
     *  =&gt; RemoteApiResponseParseFailureException
     * 
     * </pre>
     * @return The optional object for failure response. (NotNull, EmptyAllowed: when no type or parse failure) 
     */
    public OptionalThing<Object> getFailureResponse() {
        final Object failureResponse = failureResponseHolder.getFailureResponse();
        final Supplier<RuntimeException> emptyResponseCause = failureResponseHolder.getEmptyResponseCause();
        return OptionalThing.ofNullable(failureResponse, () -> {
            throw emptyResponseCause != null ? emptyResponseCause.get() : new IllegalStateException("Not found the failure response.");
        });
    }
}
