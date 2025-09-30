/*
 * Copyright 2015-2025 the original author or authors.
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

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.optional.OptionalThing;
import org.dbflute.remoteapi.exception.RemoteApiPathVariableNullElementException;
import org.dbflute.remoteapi.exception.RemoteApiPathVariableShortElementException;
import org.dbflute.remoteapi.mock.MockCDef;
import org.dbflute.utflute.core.PlainTestCase;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.Srl;

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

    // ===================================================================================
    //                                                                       Message Setup
    //                                                                       =============
    // -----------------------------------------------------
    //                                            Core Logic
    //                                            ----------
    public void test_setupRequestInfo_basic() {
        // ## Arrange ##
        FlutyRemoteApi remoteApi = new FlutyRemoteApi(rule -> {}, this);
        FlutyRemoteApiRule rule = new FlutyRemoteApiRule();
        ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        Object optOrParam = DfCollectionUtil.newArrayList("sea", "land", "piari");
        String url = "url:{mystic, oneman}";

        // ## Act ##
        remoteApi.setupRequestInfo(br, Integer.class, url, optOrParam, rule);

        // ## Assert ##
        String msg = br.buildExceptionMessage();
        log(ln() + msg);
        assertContains(msg, url);
        assertContains(msg, "[sea, land, piari]");
    }

    @SuppressWarnings("deprecation")
    public void test_setupRequestInfo_compatible() {
        // ## Arrange ##
        FlutyRemoteApi remoteApi = new FlutyRemoteApi(rule -> {}, this);
        ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        Object optOrParam = DfCollectionUtil.newArrayList("sea", "land", "piari");
        String url = "url:{mystic, oneman}";

        // ## Act ##
        remoteApi.setupRequestInfo(br, Integer.class, url, optOrParam);

        // ## Assert ##
        String msg = br.buildExceptionMessage();
        log(ln() + msg);
        assertContains(msg, url);
        assertContains(msg, "[sea, land, piari]");
    }

    // -----------------------------------------------------
    //                           filterMessageRemoteApiUrl()
    //                           ---------------------------
    public void test_setupRequestInfo_adjustRemoteApiException_filterMessageRemoteApiUrl_basic() {
        // ## Arrange ##
        FlutyRemoteApi remoteApi = new FlutyRemoteApi(rule -> {}, this);
        FlutyRemoteApiRule rule = new FlutyRemoteApiRule();
        rule.adjustRemoteApiException(op -> op.filterMessageRemoteApiUrl(url -> {
            return Srl.replace(url, "mystic", "showbase");
        }));
        ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        Object optOrParam = DfCollectionUtil.newArrayList("sea", "land", "piari");
        String url = "url:{mystic, oneman}";

        // ## Act ##
        remoteApi.setupRequestInfo(br, Integer.class, url, optOrParam, rule);

        // ## Assert ##
        String msg = br.buildExceptionMessage();
        log(ln() + msg);
        assertContains(msg, "url:{showbase, oneman}");
        assertContains(msg, "[sea, land, piari]");
    }

    public void test_setupRequestInfo_adjustRemoteApiException_filterMessageRemoteApiUrl_null() {
        // ## Arrange ##
        FlutyRemoteApi remoteApi = new FlutyRemoteApi(rule -> {}, this);
        FlutyRemoteApiRule rule = new FlutyRemoteApiRule();
        rule.adjustRemoteApiException(op -> op.filterMessageRemoteApiUrl(url -> {
            return null;
        }));
        ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        Object optOrParam = DfCollectionUtil.newArrayList("sea", "land", "piari");
        String url = "url:{mystic, oneman}";

        // ## Act ##
        remoteApi.setupRequestInfo(br, Integer.class, url, optOrParam, rule);

        // ## Assert ##
        String msg = br.buildExceptionMessage();
        log(ln() + msg);
        assertContains(msg, url);
        assertContains(msg, "[sea, land, piari]");
    }

    // -----------------------------------------------------
    //                       filterMessageRequestParameter()
    //                       -------------------------------
    public void test_setupRequestInfo_adjustRemoteApiException_filterMessageRequestParameter_basic() {
        // ## Arrange ##
        FlutyRemoteApi remoteApi = new FlutyRemoteApi(rule -> {}, this);
        FlutyRemoteApiRule rule = new FlutyRemoteApiRule();
        rule.adjustRemoteApiException(op -> op.filterMessageRequestParameter(param -> {
            return Srl.replace(param.toString(), "land", "showbase");
        }));
        ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        Object optOrParam = DfCollectionUtil.newArrayList("sea", "land", "piari");
        String url = "url:{mystic, oneman}";

        // ## Act ##
        remoteApi.setupRequestInfo(br, Integer.class, url, optOrParam, rule);

        // ## Assert ##
        String msg = br.buildExceptionMessage();
        log(ln() + msg);
        assertContains(msg, url);
        assertContains(msg, "[sea, showbase, piari]");
    }

    public void test_setupRequestInfo_adjustRemoteApiException_filterMessageRequestParameter_null() {
        // ## Arrange ##
        FlutyRemoteApi remoteApi = new FlutyRemoteApi(rule -> {}, this);
        FlutyRemoteApiRule rule = new FlutyRemoteApiRule();
        rule.adjustRemoteApiException(op -> op.filterMessageRequestParameter(param -> {
            return null;
        }));
        ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        Object optOrParam = DfCollectionUtil.newArrayList("sea", "land", "piari");
        String url = "url:{mystic, oneman}";

        // ## Act ##
        remoteApi.setupRequestInfo(br, Integer.class, url, optOrParam, rule);

        // ## Assert ##
        String msg = br.buildExceptionMessage();
        log(ln() + msg);
        assertContains(msg, url);
        assertContains(msg, "[sea, land, piari]");
    }
}
