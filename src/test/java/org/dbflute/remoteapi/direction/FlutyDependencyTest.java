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
package org.dbflute.remoteapi.direction;

import org.dbflute.utflute.core.PlainTestCase;
import org.dbflute.util.Srl;

/**
 * @author jflute
 */
public class FlutyDependencyTest extends PlainTestCase {

    public void test_cannotDependsOnLastaFlute() {
        policeStoryOfJavaClassChase((srcFile, clazz) -> {
            if (isDBFlutePackage(clazz)) {
                readLine(srcFile, "UTF-8", line -> {
                    if (isImportStatement(line)) {
                        verifyDependencyToLastaFlute(clazz, extractImportClassName(line));
                    }
                });
            }
        });
    }

    protected boolean isDBFlutePackage(Class<?> clazz) {
        return clazz.getName().startsWith("org.dbflute");
    }

    protected boolean isImportStatement(String line) {
        return line.startsWith("import ");
    }

    protected String extractImportClassName(String line) {
        return Srl.rtrim(Srl.substringFirstRear(line, "import "), ";");
    }

    protected void verifyDependencyToLastaFlute(Class<?> clazz, String importClassName) {
        if (isLastaFlutePackage(importClassName)) {
            String msg = "Cannot use lastaflute class: " + importClassName + " in " + clazz.getSimpleName();
            throw new IllegalStateException(msg);
        }
    }

    protected boolean isLastaFlutePackage(String importClassName) {
        return importClassName.startsWith("org.lastaflute");
    }
}
