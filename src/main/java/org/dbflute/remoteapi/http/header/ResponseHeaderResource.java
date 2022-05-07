/*
 * Copyright 2015-2022 the original author or authors.
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
package org.dbflute.remoteapi.http.header;

import org.dbflute.optional.OptionalThing;
import org.dbflute.remoteapi.exception.RemoteApiHttpBasisErrorException;

/**
 * @author jflute
 * @since 0.4.3 (2019/03/13 Wednesday)
 */
public class ResponseHeaderResource {

    protected final ResponseHeaderProvider headerProvider; // not null
    protected final Object mappedBodyReturn; // null allowed (if e.g. no failure response), not yet validation
    protected final RemoteApiHttpBasisErrorException remoteErrorCause; // null allowed if success

    public ResponseHeaderResource(ResponseHeaderProvider headerProvider, Object mappedBodyReturn,
            RemoteApiHttpBasisErrorException remoteErrorCause) {
        this.headerProvider = headerProvider;
        this.mappedBodyReturn = mappedBodyReturn;
        this.remoteErrorCause = remoteErrorCause;
    }

    public ResponseHeaderProvider getHeaderProvider() {
        return headerProvider;
    }

    public OptionalThing<Object> getMappedBodyReturn() {
        return OptionalThing.ofNullable(mappedBodyReturn, () -> {
            throw new IllegalStateException("Not found the mapped body return.");
        });
    }

    public OptionalThing<RemoteApiHttpBasisErrorException> getRemoteErrorCause() {
        return OptionalThing.ofNullable(remoteErrorCause, () -> {
            throw new IllegalStateException("Not found the remote error cause.");
        });
    }
}
