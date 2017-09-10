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
package org.dbflute.remoteapi.exception;

/**
 * @param <EXP> The type of exception.
 * @author jflute
 * @since 0.2.1 (2017/09/10 Sunday at bay maihama)
 */
public interface TranslatedExceptionProvider<EXP extends Exception> {

    /**
     * @return The new-created exception to be thrown as translating. (NotNull)
     */
    EXP provide();
}
