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
package org.dbflute.remoteapi.sender.body;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.dbflute.remoteapi.FlutyRemoteApiRule;
import org.dbflute.remoteapi.logging.SendReceiveLogOption;

/**
 * @author inoue
 * @author jflute
 */
public abstract class FlJsonSender implements RequestBodySender {

    // ===================================================================================
    //                                                                             Prepare
    //                                                                             =======
    @Override
    public void prepareEnclosingRequest(HttpEntityEnclosingRequest enclosingRequest, Object param, FlutyRemoteApiRule rule) {
        final String json = toJson(param);
        final String charsetName = rule.getRequestBodyCharset().name();
        final StringEntity entity = new StringEntity(json, charsetName);
        entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, buildContentType(charsetName)));
        enclosingRequest.setEntity(entity);
        readySendReceiveLogIfNeeds(rule, param, json);
    }

    protected abstract String toJson(Object param);

    protected String buildContentType(String charsetName) {
        return "application/json; charset=" + charsetName;
    }

    // -----------------------------------------------------
    //                                  Send/Receive Logging
    //                                  --------------------
    protected void readySendReceiveLogIfNeeds(FlutyRemoteApiRule rule, Object param, String json) {
        final SendReceiveLogOption option = rule.getSendReceiveLogOption();
        if (option.isEnabled()) {
            option.keeper().keepRequestBody(json, getSendReceiveLogRequestBodyType());
        }
    }

    protected String getSendReceiveLogRequestBodyType() {
        return "json";
    }
}
