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

import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.function.Consumer;

import org.dbflute.remoteapi.http.SupportedHttpMethod;
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
    protected static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

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
    public void show(SupportedHttpMethod httpMethod, String url, SendReceiveLogOption option, Consumer<Runnable> async) {
        final Logger logger = deriveLogger(option);
        if (!isLoggerEnabled(logger)) { // e.g. option is true but no logger settings
            return;
        }
        try {
            doShow(httpMethod, url, option, async, logger);
        } catch (RuntimeException continued) { // not main process, just in case of empty async
            logger.info("*Failed to show send-receive log: ", continued);
        }
    }

    protected Logger deriveLogger(SendReceiveLogOption option) {
        return option.getCategoryName().map(name -> LoggerFactory.getLogger(LOGGER_NAME + "." + name)).orElse(baseLogger);
    }

    protected void doShow(SupportedHttpMethod httpMethod, String url, SendReceiveLogOption option, Consumer<Runnable> async,
            Logger logger) {
        async.accept(() -> {
            final String whole = buildWhole(httpMethod, url, option);
            log(logger, whole);
        });
    }

    protected String buildWhole(SupportedHttpMethod httpMethod, String url, SendReceiveLogOption option) {
        final StringBuilder sb = new StringBuilder();
        //        final String requestPath = requestManager.getRequestPath();
        //        final String httpMethod = requestManager.getHttpMethod().orElse("unknown");
        //        sb.append(httpMethod).append(" ").append(requestPath);
        //        // not use HTTP status because of not fiexed yet here when e.g. exception
        //        // (and in-out logging is not access log and you can derive it by exception type)
        //        //requestManager.getResponseManager().getResponse().getStatus();
        //        final String actionName = runtime.getActionType().getSimpleName();
        //        final String methodName = runtime.getActionExecute().getExecuteMethod().getName();
        //        sb.append(" ").append(actionName).append("@").append(methodName).append("()");
        //        final String beginExp = keeper.getBeginDateTime().map(begin -> {
        //            return dateTimeFormatter.format(begin);
        //        }).orElse("no begun"); // basically no way, just in case
        //        sb.append(" (").append(beginExp).append(")");
        //        keeper.getBeginDateTime().ifPresent(begin -> {
        //            final long before = DfTypeUtil.toDate(begin).getTime();
        //            final long after = DfTypeUtil.toDate(requestManager.getTimeManager().currentDateTime()).getTime();
        //            sb.append(" [").append(DfTraceViewUtil.convertToPerformanceView(after - before)).append("]");
        //        });
        //        requestManager.getHeaderUserAgent().ifPresent(userAgent -> {
        //            sb.append(" {").append(Srl.cut(userAgent, 50, "...")).append("}");
        //        });
        //        final RuntimeException failureCause = runtime.getFailureCause();
        //        if (failureCause != null) {
        //            sb.append(" *").append(failureCause.getClass().getSimpleName());
        //            sb.append(" #").append(Integer.toHexString(failureCause.hashCode()));
        //        }
        //        boolean alreadyLineSep = false;
        //        final String paramsExp = buildRequestParameterExp(keeper);
        //        if (paramsExp != null) {
        //            final String realExp = option.getRequestParameterFilter().map(filter -> filter.apply(paramsExp)).orElse(paramsExp);
        //            alreadyLineSep = buildSendReceive(sb, "requestParameter", realExp, alreadyLineSep);
        //        }
        //        if (keeper.getRequestBodyContent().isPresent()) {
        //            final String body = keeper.getRequestBodyContent().get();
        //            final String realExp = option.getRequestBodyFilter().map(filter -> filter.apply(body)).orElse(body);
        //            alreadyLineSep = buildSendReceive(sb, "requestBody", realExp, alreadyLineSep);
        //        }
        //        if (keeper.getResponseBodyContent().isPresent()) {
        //            if (!keeper.getOption().isSuppressResponseBody()) {
        //                final String body = keeper.getResponseBodyContent().get();
        //                final String realExp = option.getResponseBodyFilter().map(filter -> filter.apply(body)).orElse(body);
        //                alreadyLineSep = buildSendReceive(sb, "responseBody", realExp, alreadyLineSep);
        //            }
        //        }
        //        final OptionalThing<RequestedSqlCount> optSql =
        //                requestManager.getAttribute(LastaWebKey.DBFLUTE_SQL_COUNT_KEY, RequestedSqlCount.class);
        //        if (optSql.isPresent()) {
        //            final RequestedSqlCount count = optSql.get();
        //            if (count.getTotalCountOfSql() > 0) {
        //                alreadyLineSep = buildSendReceive(sb, "sqlCount", count.toString(), alreadyLineSep);
        //            }
        //        }
        //        final OptionalThing<RequestedMailCount> optMail =
        //                requestManager.getAttribute(LastaWebKey.MAILFLUTE_MAIL_COUNT_KEY, RequestedMailCount.class);
        //        if (optMail.isPresent()) {
        //            final RequestedMailCount count = optMail.get();
        //            if (count.getCountOfPosting() > 0) {
        //                alreadyLineSep = buildSendReceive(sb, "mailCount", count.toString(), alreadyLineSep);
        //            }
        //        }
        return sb.toString();
    }

    protected boolean buildSendReceive(StringBuilder sb, String title, String value, boolean alreadyLineSep) {
        boolean nowLineSep = alreadyLineSep;
        if (value != null && value.contains("\n")) {
            sb.append("\n").append(title).append(":").append("\n");
            nowLineSep = true;
        } else {
            sb.append(alreadyLineSep ? "\n" : " ").append(title).append(":");
        }
        sb.append(value == null || !value.isEmpty() ? value : "(empty)");
        return nowLineSep;
    }

    // ===================================================================================
    //                                                                   Request Parameter
    //                                                                   =================
    protected String buildRequestParameterExp(SendReceiveLogKeeper keeper) {
        final Map<String, Object> parameterMap = keeper.getFormParameterMap();
        if (parameterMap.isEmpty()) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        parameterMap.forEach((key, value) -> {
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

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    protected String getDelimiter(String exp) {
        return exp.contains("\n") ? "\n" : " ";
    }
}
