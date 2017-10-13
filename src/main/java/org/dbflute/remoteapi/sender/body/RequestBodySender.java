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
import org.dbflute.remoteapi.FlutyRemoteApiRule;

/**
 * The sender of request body.
 * @author inoue
 * @author jflute
 */
public interface RequestBodySender {

    /**
     * @param enclosingRequest The HTTP request as entity enclosing, e.g. POST/PUT/PATCH. (NotNull)
     * @param param The parmeater object for body part. (NotNull)
     * @param rule The rule of remote API. (NotNull)
     */
    void prepareEnclosingRequest(HttpEntityEnclosingRequest enclosingRequest, Object param, FlutyRemoteApiRule rule);
}
