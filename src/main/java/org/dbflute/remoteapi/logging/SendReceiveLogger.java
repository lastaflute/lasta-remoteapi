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
package org.dbflute.remoteapi.logging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.function.Consumer;

import org.dbflute.optional.OptionalThing;
import org.dbflute.remoteapi.http.SupportedHttpMethod;
import org.dbflute.util.DfTraceViewUtil;
import org.dbflute.util.DfTypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 0.3.7 (2017/10/13 Friday at showbase)
 */
public class SendReceiveLogger {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String LOGGER_NAME = "lastaflute.remoteapi.sendreceive";
    protected static final Logger baseLogger = LoggerFactory.getLogger(LOGGER_NAME);
    protected static final DateTimeFormatter beginTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    // ===================================================================================
    //                                                                             Logging
    //                                                                             =======
    protected void log(Logger logger, String msg) { // define at top for small line number
        logger.info(msg);
    }

    public static boolean isLoggerEnabled(Logger logger) { // used by keeper's determination
        return logger.isInfoEnabled();
    }

    // ===================================================================================
    //                                                                               Show
    //                                                                              ======
    public void show(SupportedHttpMethod httpMethod, String requestPath, SendReceiveLogOption option, Consumer<Runnable> async) {
        final Logger logger = deriveLogger(option);
        if (!isLoggerEnabled(logger)) { // e.g. option is true but no logger settings
            return;
        }
        try {
            doShow(httpMethod, requestPath, option, async, logger);
        } catch (RuntimeException continued) { // not main process, just in case of empty async
            logger.info("*Failed to show send-receive log: ", continued);
        }
    }

    protected Logger deriveLogger(SendReceiveLogOption option) {
        return option.getCategoryName().map(name -> LoggerFactory.getLogger(LOGGER_NAME + "." + name)).orElse(baseLogger);
    }

    protected void doShow(SupportedHttpMethod httpMethod, String requestPath, SendReceiveLogOption option, Consumer<Runnable> async,
            Logger logger) {
        async.accept(() -> {
            final String whole = buildWhole(httpMethod, requestPath, option);
            log(logger, whole);
        });
    }

    // ===================================================================================
    //                                                                         Build Whole
    //                                                                         ===========
    protected String buildWhole(SupportedHttpMethod httpMethod, String requestPath, SendReceiveLogOption option) {
        final SendReceiveLogKeeper keeper = option.keeper();
        final StringBuilder sb = new StringBuilder();
        setupBasic(sb, httpMethod, requestPath, keeper);
        setupFacade(sb, keeper);
        setupBegin(sb, keeper);
        setupPerformance(sb, keeper);
        setupCaller(sb, option);
        setupCause(sb, keeper);

        // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
        // Request: requestHeader, requestParameter, requestBody
        // _/_/_/_/_/_/_/_/_/_/
        {
            final String headerExp = buildMapExp(keeper.getRequestHeaderMap());
            if (headerExp != null) {
                buildSendReceive(sb, "requestHeader", headerExp);
            }
            final String paramsExp = buildRequestParameterExp(keeper);
            if (paramsExp != null) {
                final String realExp = option.getRequestParameterFilter().map(filter -> filter.apply(paramsExp)).orElse(paramsExp);
                buildSendReceive(sb, "requestParameter", realExp);
            }
            keeper.getRequestBodyContent().ifPresent(body -> {
                final String title = "requestBody(" + keeper.getRequestBodyType().orElse("unknown") + ")";
                final String realExp = option.getRequestBodyFilter().map(filter -> filter.apply(body)).orElse(body);
                buildSendReceive(sb, title, realExp);
            });
        }

        // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
        // Response: responseHeader, responseBody
        // _/_/_/_/_/_/_/_/_/_/
        {
            final String headerExp = buildMapExp(keeper.getResponseHeaderMap());
            if (headerExp != null) {
                buildSendReceive(sb, "responseHeader", headerExp);
            }
            if (!option.isSuppressResponseBody()) {
                keeper.getResponseBodyContent().ifPresent(body -> {
                    final String title = "responseBody(" + keeper.getResponseBodyType().orElse("unknown") + ")";
                    final String realExp = option.getResponseBodyFilter().map(filter -> filter.apply(body)).orElse(body);
                    buildSendReceive(sb, title, realExp);
                });
            }
        }
        return sb.toString();
    }

    protected void setupBasic(StringBuilder sb, SupportedHttpMethod httpMethod, String requestPath, SendReceiveLogKeeper keeper) {
        sb.append(httpMethod.name().toUpperCase()); // originally upper but just in case
        sb.append(" ");
        sb.append(requestPath);
        sb.append(" ");
        final String statusExp = keeper.getHttpStatus().map(stat -> {
            return String.valueOf(stat);
        }).orElse("---"); // until response
        sb.append(statusExp);
    }

    protected void setupFacade(StringBuilder sb, SendReceiveLogKeeper keeper) {
        final String facadeExp = keeper.getFacadeExp().map(exp -> {
            if (exp instanceof Class<?>) { // basically here
                return ((Class<?>) exp).getSimpleName();
            } else {
                return exp.toString();
            }
        }).orElse("unknown"); // basically no way, just in case
        sb.append(" ").append(facadeExp);
    }

    protected void setupBegin(StringBuilder sb, SendReceiveLogKeeper keeper) {
        final String beginExp = keeper.getBeginDateTime().map(time -> {
            return beginTimeFormatter.format(time);
        }).orElse("no begun"); // basically no way, just in case
        sb.append(" (").append(beginExp).append(")");
    }

    protected void setupPerformance(StringBuilder sb, SendReceiveLogKeeper keeper) {
        final OptionalThing<LocalDateTime> optBegin = keeper.getBeginDateTime();
        final OptionalThing<LocalDateTime> optEnd = keeper.getEndDateTime();
        final String performanceCost;
        if (optBegin.isPresent() && optEnd.isPresent()) {
            final long before = DfTypeUtil.toDate(optBegin.get()).getTime();
            final long after = DfTypeUtil.toDate(optEnd.get()).getTime();
            performanceCost = DfTraceViewUtil.convertToPerformanceView(after - before);
        } else {
            performanceCost = "no ended";
        }
        sb.append(" [").append(performanceCost).append("]");
    }

    protected void setupCaller(StringBuilder sb, SendReceiveLogOption option) {
        final String callerExp = findCallerExp(option);
        if (callerExp != null) {
            sb.append(" caller:{").append(callerExp).append("}");
        }
    }

    protected void setupCause(StringBuilder sb, SendReceiveLogKeeper keeper) {
        keeper.getCause().ifPresent(cause -> {
            sb.append(" *").append(cause.getClass().getSimpleName());
            sb.append(" #").append(Integer.toHexString(cause.hashCode()));
        });
    }

    protected String buildRequestParameterExp(SendReceiveLogKeeper keeper) {
        String requestParameterExp = buildMapExp(keeper.getQueryParameterMap());
        if (requestParameterExp == null) {
            requestParameterExp = buildMapExp(keeper.getFormParameterMap());
        }
        return requestParameterExp;
    }

    // ===================================================================================
    //                                                                   Caller Expression
    //                                                                   =================
    protected String findCallerExp(SendReceiveLogOption option) { // may be overridden
        return null; // no expression as default
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    protected String buildMapExp(Map<String, Object> map) { // returns null allowed
        if (map.isEmpty()) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        map.forEach((key, value) -> {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(key).append("=");
            if (value instanceof Object[]) {
                final Object[] objArray = (Object[]) value;
                if (objArray.length == 1) {
                    sb.append(objArray[0]);
                } else {
                    int index = 0;
                    sb.append("[");
                    for (Object obj : objArray) {
                        if (index > 0) {
                            sb.append(", ");
                        }
                        sb.append(obj);
                        ++index;
                    }
                    sb.append("]");
                }
            } else {
                sb.append(value);
            }
        });
        sb.insert(0, "{").append("}");
        return sb.toString();
    }

    protected void buildSendReceive(StringBuilder sb, String title, String value) {
        // always line separator because many remote APIs have large data and many items
        sb.append("\n").append(title).append(":");
        if (value != null && value.contains("\n")) {
            sb.append("\n");
        }
        sb.append(value == null || !value.isEmpty() ? value : "(empty)");
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    protected String getDelimiter(String exp) {
        return exp.contains("\n") ? "\n" : " ";
    }
}
