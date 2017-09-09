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

import java.util.Arrays;

import javax.annotation.Resource;

import org.dbflute.remoteapi.FlutyRemoteApi;
import org.dbflute.remoteapi.FlutyRemoteBehavior;
import org.dbflute.util.DfStringUtil;
import org.lastaflute.core.direction.AccessibleConfig;
import org.lastaflute.di.naming.NamingConvention;
import org.lastaflute.web.servlet.request.RequestManager;

/**
 * The base class of behavior for remote API. <br>
 * Reference: <a href="http://dbflute.seasar.org/ja/lastaflute/howto/impldesign/jsondesign.html">JSON Design of JSON API</a>
 * @author awane
 * @author jflute
 * @author inoue
 */
public abstract class LastaRemoteBehavior extends FlutyRemoteBehavior {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final Object[] EMPTY_PARAMS = new Object[] {};

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                          DI Component
    //                                          ------------
    @Resource
    private NamingConvention namingConvention;
    @Resource
    private AccessibleConfig config;

    // -----------------------------------------------------
    //                                        Basic Resource
    //                                        --------------
    protected final RequestManager requestManager; // injected via constructor

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public LastaRemoteBehavior(RequestManager requestManager) {
        this.requestManager = requestManager;
    }

    @Override
    protected FlutyRemoteApi createRemoteApi() {
        return new LastaRemoteApi(requestManager, op -> setupDefaultRemoteApiOption(op), getClass());
    }

    @Override
    protected String getUserAgentServiceName() {
        return Arrays.stream(namingConvention.getRootPackageNames()).filter(name -> name.contains(".app")).map(name -> {
            return DfStringUtil.substringLastRear(DfStringUtil.substringFirstFront(name, ".app"), ".");
        }).findFirst().orElse(null);
    }

    @Override
    protected String getUserAgentAppName() {
        return config.getOrDefault("domain.name", null);
    }
}
