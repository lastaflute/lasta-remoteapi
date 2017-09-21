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
package org.dbflute.remoteapi.exception.translator;

/**
 * @author jflute
 * @since 0.3.3 (2017/09/21 Thursday)
 */
public interface RemoteApiClientErrorTranslator {

    /**
     * @param resource The resource of translating that has thrown client error exception. (NotNull)
     * @return The translated exception which should have thrown exception. (NullAllowed: no translation)
     */
    RuntimeException translate(RemoteApiClientErrorResource resource);
}
