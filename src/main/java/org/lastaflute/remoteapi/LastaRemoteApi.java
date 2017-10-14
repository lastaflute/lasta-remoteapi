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
package org.lastaflute.remoteapi;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.optional.OptionalThing;
import org.dbflute.remoteapi.FlutyRemoteApi;
import org.dbflute.remoteapi.FlutyRemoteApiRule;
import org.dbflute.remoteapi.exception.RemoteApiRequestValidationErrorException;
import org.dbflute.remoteapi.exception.RemoteApiResponseValidationErrorException;
import org.dbflute.remoteapi.logging.SendReceiveLogOption;
import org.dbflute.remoteapi.logging.SendReceiveLogger;
import org.dbflute.util.DfTypeUtil;
import org.dbflute.util.Srl;
import org.lastaflute.core.magic.ThreadCacheContext;
import org.lastaflute.core.magic.async.AsyncManager;
import org.lastaflute.core.magic.async.ConcurrentAsyncCall;
import org.lastaflute.core.message.UserMessages;
import org.lastaflute.core.message.supplier.UserMessagesCreator;
import org.lastaflute.core.time.TimeManager;
import org.lastaflute.core.util.Lato;
import org.lastaflute.web.response.ApiResponse;
import org.lastaflute.web.response.JsonResponse;
import org.lastaflute.web.ruts.process.exception.ResponseBeanValidationErrorException;
import org.lastaflute.web.ruts.process.validatebean.ResponseSimpleBeanValidator;
import org.lastaflute.web.servlet.request.RequestManager;
import org.lastaflute.web.validation.ActionValidator;
import org.lastaflute.web.validation.exception.ValidationStoppedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @author awaawa
 * @author inoue
 */
public class LastaRemoteApi extends FlutyRemoteApi {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger logger = LoggerFactory.getLogger(LastaRemoteApi.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected RequestManager requestManager; // not null after set, for validation and various purpose

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public LastaRemoteApi(Consumer<FlutyRemoteApiRule> defaultOpLambda, Object callerExp) {
        super(defaultOpLambda, callerExp);
    }

    public void acceptRequestManager(RequestManager requestManager) {
        this.requestManager = requestManager;
    }

    // ===================================================================================
    //                                                                          Validation
    //                                                                          ==========
    @Override
    protected void validateParam(Type returnType, String urlBase, String actionPath, Object[] pathVariables, Object param,
            FlutyRemoteApiRule rule) {
        if (rule.getValidatorOption().isSuppressParam()) {
            return;
        }
        try {
            createTransferredBeanValidator().validate(param);
        } catch (ResponseBeanValidationErrorException e) {
            handleRemoteApiRequestValidationError(returnType, urlBase, actionPath, pathVariables, param, rule, e);
        }
    }

    @Override
    protected void validateReturn(Type returnType, String url, OptionalThing<Object> form, int httpStatus, OptionalThing<String> body,
            Object ret, FlutyRemoteApiRule rule) {
        if (rule.getValidatorOption().isSuppressReturn()) {
            return;
        }
        try {
            createTransferredBeanValidator().validate(ret);
        } catch (ResponseBeanValidationErrorException | ValidationStoppedException e) {
            handleRemoteApiResponseValidationError(returnType, url, form, httpStatus, body, ret, rule, e);
        }
    }

    protected ResponseSimpleBeanValidator createTransferredBeanValidator() {
        // use ActionValidator #for_now (with suppressing request process) by jflute
        return new ResponseSimpleBeanValidator(requestManager, facadeExp, isTransferredBeanValidationAsWarning()) {
            @Override
            protected ActionValidator<UserMessages> createActionValidator() {
                final Class<?>[] groups = getValidatorGroups().orElse(ActionValidator.DEFAULT_GROUPS);
                return newActionValidator(() -> new UserMessages(), groups);
            }
        };
    }

    protected boolean isTransferredBeanValidationAsWarning() {
        return false;
    }

    protected ActionValidator<UserMessages> newActionValidator(UserMessagesCreator<UserMessages> messagesCreator, Class<?>[] groups) {
        return new ActionValidator<UserMessages>(requestManager, messagesCreator, groups) {
            @Override
            protected Locale provideUserLocale() { // not to use request
                return Locale.ENGLISH; // fixedly English because of non-user validation
            }

            @Override
            protected ApiResponse processApiValidationError() {
                return JsonResponse.asEmptyBody(); // first, may not be called
            }
        };
    }

    protected void handleRemoteApiRequestValidationError(Type returnType, String urlBase, String actionPath, Object[] pathVariables,
            Object param, FlutyRemoteApiRule rule, ResponseBeanValidationErrorException e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Validation Error as Param object for the remote API.");
        final StringBuilder sb = new StringBuilder();
        sb.append(urlBase).append(actionPath).append(actionPath.endsWith("/") ? "" : "/");
        if (pathVariables != null && pathVariables.length > 0) {
            sb.append(Stream.of(pathVariables).map(el -> el.toString()).collect(Collectors.joining("/"))); // simple for debug message
        }
        final String url = sb.toString();
        setupRequestInfo(br, returnType, url, OptionalThing.of(param));
        setupYourRule(br, rule);
        final String msg = br.buildExceptionMessage();
        if (rule.getValidatorOption().isHandleAsWarnParam()) {
            logger.warn(msg, e);
        } else {
            throw new RemoteApiRequestValidationErrorException(msg, e);
        }
    }

    protected void handleRemoteApiResponseValidationError(Type returnType, String url, OptionalThing<Object> param, int httpStatus,
            OptionalThing<String> body, Object ret, FlutyRemoteApiRule rule, RuntimeException e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Validation Error as Return object for the remote API.");
        setupRequestInfo(br, returnType, url, param);
        setupResponseInfo(br, httpStatus, body);
        setupReturnInfo(br, ret);
        setupYourRule(br, rule);
        final String msg = br.buildExceptionMessage();
        if (rule.getValidatorOption().isHandleAsWarnReturn()) {
            logger.warn(msg, e);
        } else {
            throw new RemoteApiResponseValidationErrorException(msg, e);
        }
    }

    // ===================================================================================
    //                                                                      RemoteApi Rule
    //                                                                      ==============
    @Override
    protected FlutyRemoteApiRule newRemoteApiRule() {
        return new LastaRemoteApiRule();
    }

    // ===================================================================================
    //                                                                          Basic Keep
    //                                                                          ==========
    @Override
    protected LocalDateTime flashDateTime() {
        final TimeManager timeManager = requestManager.getTimeManager();
        final Date flashDate = timeManager.flashDate(); // not depends on transaction so use flash date
        return DfTypeUtil.toLocalDateTime(flashDate, timeManager.getBusinessTimeZone());
    }

    // ===================================================================================
    //                                                                Send/Receive Logging
    //                                                                ====================
    @Override
    protected SendReceiveLogger createSendReceiveLogger() {
        return new LastaSendReceiveLogger();
    }

    public static class LastaSendReceiveLogger extends SendReceiveLogger {

        @Override
        protected String findCallerExp(SendReceiveLogOption option) {
            return buildLastaFluteExp();
        }

        protected String buildLastaFluteExp() {
            final String requestPath = ThreadCacheContext.findRequestPath(); // may contain query
            if (requestPath == null) { // no way, just in case
                return null; // no caller info
            }
            // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
            // e.g. web
            //  /sea/land/1/2/ (2017-10-14 00:31:54.773) #f7ese3f
            //    => caller:{/sea/land/1/2/ (2017-10-14 00:31:54.773) #f7ese3f}
            //
            // e.g. job
            //  SeaLandJob (2017-10-14 00:31:54.773) #hm9fk12
            //    => caller:{SeaLandJob (2017-10-14 00:31:54.773) #hm9fk12}
            // _/_/_/_/_/_/_/_/_/_/
            final StringBuilder sb = new StringBuilder();
            buildCallerRequestPath(sb, requestPath);
            buildCallerBeginTime(sb);
            buildCallerProcessHash(sb);
            return sb.toString();
        }

        protected void buildCallerRequestPath(StringBuilder sb, String requestPath) {
            final String pure = removeQueryParameter(requestPath); // see query at in-out logging instead
            sb.append(pure);
        }

        protected String removeQueryParameter(String requestPath) {
            return Srl.substringFirstFront(requestPath, "?");
        }

        protected void buildCallerBeginTime(StringBuilder sb) {
            final Object beginTime = findCallerBeginTime();
            if (beginTime != null) {
                sb.append(" (");
                final String beginExp;
                if (beginTime instanceof LocalDateTime) { // basically here
                    beginExp = beginTimeFormatter.format((LocalDateTime) beginTime);
                } else { // no way, just in case
                    beginExp = beginTime.toString();
                }
                sb.append(beginExp);
                sb.append(")");
            }
        }

        protected Object findCallerBeginTime() {
            return ThreadCacheContext.getObject("fw:beginTime"); // expects LastaFlute-1.0.1, LastaJob-0.5.2
        }

        protected void buildCallerProcessHash(StringBuilder sb) {
            final Object processHash = findCallerProcessHash();
            if (processHash != null) {
                sb.append(" #").append(processHash);
            }
        }

        protected Object findCallerProcessHash() {
            return ThreadCacheContext.getObject("fw:processHash"); // expects LastaFlute-1.0.1, LastaJob-0.5.2
        }
    }

    @Override
    protected Consumer<Runnable> prepareSendReceiveLogAsync() {
        final AsyncManager asyncManager = requestManager.getAsyncManager();
        return runner -> {
            asyncManager.async(new ConcurrentAsyncCall() {

                @Override
                public ConcurrentAsyncImportance importance() {
                    return ConcurrentAsyncImportance.TERTIARY; // as low priority
                }

                @Override
                public void callback() {
                    runner.run();
                }
            });
        };
    }

    // ===================================================================================
    //                                                                      Error Handling
    //                                                                      ==============
    @Override
    protected String convertBeanToDebugString(Object bean) {
        return Lato.string(bean); // because its toString() may not be overridden
    }
}
