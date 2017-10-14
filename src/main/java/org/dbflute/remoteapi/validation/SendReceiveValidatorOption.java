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
package org.dbflute.remoteapi.validation;

/**
 * @author jflute
 * @since 0.3.6 (2017/09/28 Thursday)
 */
public class SendReceiveValidatorOption {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected boolean handleAsWarnParam;
    protected boolean handleAsWarnReturn;
    protected boolean suppressParam;
    protected boolean suppressReturn;

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
    public SendReceiveValidatorOption handleAsWarnParam() {
        handleAsWarnParam = true;
        return this;
    }

    public SendReceiveValidatorOption handleAsWarnReturn() {
        handleAsWarnReturn = true;
        return this;
    }

    public SendReceiveValidatorOption suppressParam() {
        suppressParam = true;
        return this;
    }

    public SendReceiveValidatorOption suppressReturn() {
        suppressReturn = true;
        return this;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("validator:{");
        sb.append("warn:{").append(handleAsWarnParam);
        sb.append(", ").append(handleAsWarnReturn);
        sb.append("}, suppress:{").append(suppressParam);
        sb.append(", ").append(suppressReturn);
        sb.append("}}");
        return sb.toString();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public boolean isHandleAsWarnParam() {
        return handleAsWarnParam;
    }

    public boolean isHandleAsWarnReturn() {
        return handleAsWarnReturn;
    }

    public boolean isSuppressParam() {
        return suppressParam;
    }

    public boolean isSuppressReturn() {
        return suppressReturn;
    }
}
