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
package org.dbflute.remoteapi.exception.translation;

import java.lang.reflect.Type;

import javax.validation.groups.Default;

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.optional.OptionalThing;
import org.dbflute.remoteapi.exception.RemoteApiHttpClientErrorException;
import org.dbflute.remoteapi.exception.RemoteApiValidationErrorHookNotFoundException;
import org.lastaflute.core.message.UserMessages;
import org.lastaflute.web.validation.VaErrorHook;
import org.lastaflute.web.validation.exception.ValidationErrorException;

/**
 * @author jflute
 * @since 0.3.3 (2017/09/21 Thursday)
 */
public class ClientErrorTranslatingResource {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Type returnType; // not null
    protected final String url; // not null
    protected final VaErrorHook validationErrorHook; // null allowed
    protected final RemoteApiHttpClientErrorException clientError; // not null

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ClientErrorTranslatingResource(Type returnType, String url, VaErrorHook validationErrorHook,
            RemoteApiHttpClientErrorException clientError) {
        this.returnType = returnType;
        this.url = url;
        this.validationErrorHook = validationErrorHook;
        this.clientError = clientError;
    }

    // ===================================================================================
    //                                                                              Facade
    //                                                                              ======
    /**
     * @param messages The messages from error response. (NotNull)
     * @return The exception of validation error for HTML response. (NotNull)
     */
    public ValidationErrorException asHtmlValidationError(UserMessages messages) {
        if (messages == null) {
            throw new IllegalArgumentException("The argument 'messages' should not be null.");
        }
        if (validationErrorHook == null) {
            throwRemoteApiValidationErrorHookNotFoundException(messages);
        }
        final Class<?>[] runtimeGroups = new Class<?>[] { Default.class }; // not supported in remote-api so default
        return new ValidationErrorException(runtimeGroups, messages, validationErrorHook, clientError);
    }

    protected void throwRemoteApiValidationErrorHookNotFoundException(UserMessages messages) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the validation error hook for client error translation.");
        br.addItem("Advice");
        br.addElement("Calling validate() is required in your action of HTML response");
        br.addElement("if you treat remote API's validation error as HTML validation error.");
        br.addElement("(You should specify basic validator annotations in your form)");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    public HtmlResponse index(SigninForm form) {");
        br.addElement("        SigninParam param = mappingToParam(form);");
        br.addElement("        remoteHarborBhv.requestSignin(param);");
        br.addElement("    }");
        br.addElement("  (o):");
        br.addElement("    public HtmlResponse index(SigninForm form) {");
        br.addElement("        validate(form, messages -> {}, () -> { // OK");
        br.addElement("            return asHtml(path_Signin_SigninHtml);");
        br.addElement("        });");
        br.addElement("        SigninParam param = mappingToParam(form);");
        br.addElement("        remoteHarborBhv.requestSignin(param);");
        br.addElement("    }");
        br.addItem("Bean Type");
        br.addElement(returnType);
        br.addItem("Remote API");
        br.addElement(url);
        br.addItem("Messages");
        br.addElement(messages);
        final String msg = br.buildExceptionMessage();
        throw new RemoteApiValidationErrorHookNotFoundException(msg, clientError);
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public Type getReturnType() {
        return returnType;
    }

    public String getUrl() {
        return url;
    }

    public OptionalThing<VaErrorHook> getValidationErrorHook() {
        return OptionalThing.ofNullable(validationErrorHook, () -> {
            throw new IllegalStateException("Not found the validation error hook: url=" + url);
        });
    }

    @Deprecated
    public RemoteApiHttpClientErrorException getCause() { // use getClientError()
        return clientError;
    }

    public RemoteApiHttpClientErrorException getClientError() {
        return clientError;
    }
}
