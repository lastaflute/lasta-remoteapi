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
package org.dbflute.remoteapi.sender.query;

import java.nio.charset.Charset;

import org.dbflute.remoteapi.FlutyRemoteApiRule;

/**
 * The sender of query parameter.
 * @author jflute
 */
public interface QueryParameterSender {

    /**
     * @param param The object of query parameter. (NotNull)
     * @param charset The charset of query parameter. (NotNull)
     * @param rule The rule of remote API. (NotNull)
     * @return The converted query string. e.g. ?sea=mystic&amp;land=oneman (NotNull)
     */
    String toQueryString(Object param, Charset charset, FlutyRemoteApiRule rule);
}
