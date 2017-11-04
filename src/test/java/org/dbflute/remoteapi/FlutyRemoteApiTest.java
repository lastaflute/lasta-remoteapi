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
import org.dbflute.remoteapi.exception.RemoteApiPathVariableShortElementException;
import org.dbflute.remoteapi.mock.MockCDef;
import org.dbflute.utflute.core.PlainTestCase;

/**
 * @author jflute
 * @since 0.3.1 (2017/09/19 Tuesday at rainbow bird rendezvous)
 */
public class FlutyRemoteApiTest extends PlainTestCase {

    // ===================================================================================
    //                                                                          buildUrl()
    //                                                                          ==========
    public void test_buildUrl_actionPathVariables_basic() {
        assertEquals("8080/sea/land", buildUrl_withPath("/sea/land"));
        assertEquals("8080/sea/land/mystic/2", buildUrl_withPath("/sea/land", "mystic", 2));
        assertEquals("8080/sea/mystic/land/2", buildUrl_withPath("/sea/{hangar}/land", "mystic", 2));
        assertEquals("8080/sea/land/oneman/2", buildUrl_withPath("/sea/land/{showbase}", "oneman", 2));
        assertEquals("8080/sea/land/mystic/oneman", buildUrl_withPath("/sea/land/{hangar}/{showbase}", "mystic", "oneman"));
        assertEquals("8080/sea/land/mystic/oneman/", buildUrl_withPath("/sea/land/{show1}/{show2}/", "mystic", "oneman"));
        assertEquals("8080/sea/land/mystic/oneman", buildUrl_withPath("/sea/land/{}/{show2}", "mystic", "oneman"));
        assertEquals("8080sea/land/mystic/oneman", buildUrl_withPath("sea/land/{}/{show2}", "mystic", "oneman"));
        assertEquals("8080/sea/land/mystic/oneman", buildUrl_withPath("/sea/land", "mystic", "oneman"));
        assertException(RemoteApiPathVariableNullElementException.class,
                () -> buildUrl_withPath("/sea/land/{show1}/{show2}", "mystic", null, "oneman"));
        assertException(RemoteApiPathVariableShortElementException.class, () -> buildUrl_withPath("/sea/land/{show1}}"));
        assertException(RemoteApiPathVariableShortElementException.class, () -> buildUrl_withPath("/sea/land/{show1}/{show2}", "mystic"));

        // optional as rear
        assertEquals("8080/sea/land/mystic/oneman", buildUrl_withPath("/sea/land", "mystic", OptionalThing.of("oneman")));
        assertEquals("8080/sea/land/mystic", buildUrl_withPath("/sea/land", "mystic", OptionalThing.empty()));

        // optional as variable
        {
            String path = "/sea/land/{hangar}/{showbase}";
            assertEquals("8080/sea/land/mystic/oneman", buildUrl_withPath(path, "mystic", OptionalThing.of("oneman")));
            assertEquals("8080/sea/land/mystic", buildUrl_withPath(path, OptionalThing.of("mystic"), OptionalThing.empty()));
            assertEquals("8080/sea/land/oneman", buildUrl_withPath(path, OptionalThing.empty(), OptionalThing.of("oneman")));
            assertEquals("8080/sea/land", buildUrl_withPath(path, OptionalThing.empty(), OptionalThing.empty()));
        }
    }

    private String buildUrl_withPath(String actionPath, Object... pathVariables) {
        FlutyRemoteApi remoteApi = new FlutyRemoteApi(rule -> {}, this);
        Class<Object> beanType = Object.class;
        OptionalThing<Object> noQuery = OptionalThing.empty();
        FlutyRemoteApiRule rule = new FlutyRemoteApiRule();
        String requestPath = remoteApi.buildRequestPath(beanType, "8080", actionPath, pathVariables, noQuery, rule);
        String url = remoteApi.buildUrl(beanType, "8080", requestPath, noQuery, rule);
        log(url);
        return url;
    }

    // ===================================================================================
    //                                                             buildPathVariablePart()
    //                                                             =======================
    public void test_buildPathVariablePart_classification() {
        // ## Arrange ##
        FlutyRemoteApi remoteApi = new FlutyRemoteApi(rule -> {}, this);
        FlutyRemoteApiRule rule = new FlutyRemoteApiRule();

        // ## Act ##
        String part = remoteApi.buildPathVariableRearPart(String.class, "/harbor", "/mypage",
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
        String part = remoteApi.buildPathVariableRearPart(String.class, "/harbor", "/mypage", new Object[] { "sea", "my/s ti-c" },
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
            remoteApi.buildPathVariableRearPart(String.class, "/harbor", "/mypage", new Object[] { "sea", null }, OptionalThing.empty(),
                    rule);
        });
    }
}
