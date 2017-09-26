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
package org.dbflute.remoteapi;

import org.dbflute.optional.OptionalThing;
import org.dbflute.remoteapi.exception.RemoteApiPathVariableNullElementException;
import org.dbflute.remoteapi.mock.MockCDef;
import org.dbflute.utflute.core.PlainTestCase;

/**
 * @author jflute
 * @since 0.3.1 (2017/09/19 Tuesday at rainbow bird rendezvous)
 */
public class FlutyRemoteApiTest extends PlainTestCase {

    public void test_buildPathVariablePart_classification() {
        // ## Arrange ##
        FlutyRemoteApi remoteApi = new FlutyRemoteApi(rule -> {}, this);
        FlutyRemoteApiRule rule = new FlutyRemoteApiRule();

        // ## Act ##
        String part = remoteApi.buildPathVariablePart(String.class, "/harbor", "/mypage",
                new Object[] { "sea", MockCDef.MemberStatus.Formalized }, OptionalThing.empty(), rule);

        // ## Assert ##
        log(part);
        assertEquals("sea/FML", part);
    }

    public void test_buildPathVariablePart_encoded() {
        // ## Arrange ##
        FlutyRemoteApi remoteApi = new FlutyRemoteApi(rule -> {}, this);
        FlutyRemoteApiRule rule = new FlutyRemoteApiRule();

        // ## Act ##
        String part = remoteApi.buildPathVariablePart(String.class, "/harbor", "/mypage", new Object[] { "sea", "my/s ti-c" },
                OptionalThing.empty(), rule);

        // ## Assert ##
        log(part);
        assertEquals("sea/my%2Fs+ti-c", part);
    }

    public void test_buildPathVariablePart_nullElement() {
        // ## Arrange ##
        FlutyRemoteApi remoteApi = new FlutyRemoteApi(rule -> {}, this);
        FlutyRemoteApiRule rule = new FlutyRemoteApiRule();

        // ## Act ##
        // ## Assert ##
        assertException(RemoteApiPathVariableNullElementException.class, () -> {
            remoteApi.buildPathVariablePart(String.class, "/harbor", "/mypage", new Object[] { "sea", null }, OptionalThing.empty(), rule);
        });
    }
}
