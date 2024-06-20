/*
 * Copyright 2015-2024 the original author or authors.
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.codec.Charsets;
import org.dbflute.remoteapi.FlutyRemoteApiRule;
import org.dbflute.utflute.core.PlainTestCase;
import org.dbflute.util.Srl;
import org.lastaflute.remoteapi.mapping.LaVacantMappingPolicy;

/**
 * @author jflute
 */
public class FlQuerySenderTest extends PlainTestCase {

    public void test_toQueryString_basic() {
        // ## Arrange ##
        FlQuerySender sender = new FlQuerySender(new LaVacantMappingPolicy());
        SeaParam param = new SeaParam();
        param.location = "maihama";
        param.birthdate = LocalDate.of(2001, 9, 4);
        param.todayBeginningTime = currentLocalDateTime();
        param.stageList = newArrayList("dockside", "hangar");

        // ## Act ##
        String query = sender.toQueryString(param, Charsets.UTF_8, new FlutyRemoteApiRule());

        // ## Assert ##
        log(query);
        assertTrue(Srl.count(query, "?") == 1);
        assertTrue(Srl.count(query, "&") == 4);
    }

    public void test_toQueryString_listAsFirstParameter() {
        // ## Arrange ##
        FlQuerySender sender = new FlQuerySender(new LaVacantMappingPolicy());
        SeaParam param = new SeaParam();
        param.stageList = newArrayList("dockside", "hangar"); // as first parameter

        // ## Act ##
        String query = sender.toQueryString(param, Charsets.UTF_8, new FlutyRemoteApiRule());

        // ## Assert ##
        log(query);
        assertTrue(Srl.count(query, "?") == 1);
        assertTrue(Srl.count(query, "&") == 1);
    }

    public void test_toQueryString_allNull() {
        // ## Arrange ##
        FlQuerySender sender = new FlQuerySender(new LaVacantMappingPolicy());
        SeaParam param = new SeaParam();

        // ## Act ##
        String query = sender.toQueryString(param, Charsets.UTF_8, new FlutyRemoteApiRule());

        // ## Assert ##
        log(query);
        assertTrue(query.length() == 0);
    }

    public void test_toQueryString_empty() {
        // ## Arrange ##
        FlQuerySender sender = new FlQuerySender(new LaVacantMappingPolicy());
        SeaParam param = new SeaParam();
        param.location = "";
        param.stageList = newArrayList("", "");

        // ## Act ##
        String query = sender.toQueryString(param, Charsets.UTF_8, new FlutyRemoteApiRule());

        // ## Assert ##
        log(query);
        assertTrue(Srl.count(query, "?") == 1);
        assertTrue(Srl.count(query, "&") == 2);
    }

    public static class SeaParam {

        public String location;

        public LocalDate birthdate;

        public LocalDateTime todayBeginningTime;

        public List<String> stageList;
    }
}
