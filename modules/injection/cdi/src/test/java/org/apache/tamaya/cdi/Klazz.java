/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tamaya.cdi;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.AccessDeniedException;
import java.util.Collection;
import java.util.Collections;

public class Klazz {
    private OtherKlazz<String> value;

    public <T> Collection<T> methodWithTypeParameter(Collection<T> in) {
        return Collections.emptyList();
    }

    public void methodWithOneParameter(String p) {
    }

    public void methodWithTwoParameters(String p, File f) {
    }

    public void voidMethod() {
    }

    public int getPrimitiveIntValue() {
        return 1;
    }

    public OtherKlazz<String> getValue() {
        return value;
    }

    public void setValue(OtherKlazz<String> value) {
        this.value = value;
    }

    public void methodWithExceptions() throws FileNotFoundException, AccessDeniedException {
    }

    public void methodWithException() throws FileNotFoundException {
    }

}
