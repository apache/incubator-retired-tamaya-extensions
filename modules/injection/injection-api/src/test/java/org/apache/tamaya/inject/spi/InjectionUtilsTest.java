/*
 * Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.tamaya.inject.spi;

import org.apache.tamaya.inject.api.ConfigDefaultSections;
import org.apache.tamaya.inject.api.ConfigFallbackKeys;
import org.junit.Test;

import javax.config.inject.ConfigProperty;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class InjectionUtilsTest {

    @Test
    public void getKeysMethod() {
        class Klazz {
            @ConfigProperty(name="val")
            @ConfigFallbackKeys({"val2", "[vvv]"})
            public void setValue(String field){}
        }

        Method method = Klazz.class.getMethods()[0];

        List<String> foundKeys = InjectionEvaluator.getKeys(method);

        assertThat(foundKeys).isNotNull()
                .contains("org.apache.tamaya.inject.spi.InjectionUtilsTest$1Klazz.val",
                        "org.apache.tamaya.inject.spi.InjectionUtilsTest$1Klazz.val2",
                        "Klazz.val",
                        "Klazz.val2",
                        "val",
                        "val2",
                        "vvv");
    }

    @Test
    public void getKeysReturnsEmptyListForNonAnnotatedField() {
        class Klazz {
            public String field;
            protected String protectedField;
            private String privateField;
        }

        Field field = Klazz.class.getFields()[0];

        List<String> foundKeys = InjectionEvaluator.getKeys(field);

        assertThat(foundKeys).isNotNull()
                             .contains("org.apache.tamaya.inject.spi.InjectionUtilsTest$2Klazz.field",
                                       "Klazz.field",
                                       "field");
    }

    @Test
    public void evaluateKeysWithSection() {
        @ConfigDefaultSections("basic")
        class Klazz {
            public String field;
            protected String protectedField;
            private String privateField;
        }

        Field field = Klazz.class.getFields()[0];

        List<String> foundKeys = InjectionEvaluator.getKeys(field);
        assertThat(foundKeys).isNotNull()
                .contains("basic.field",
                        "field");
    }

    @Test
    public void evaluateKeysWithSectionAndMemberAnnotation() {
        @ConfigDefaultSections("basic")
        class Klazz {
            @ConfigProperty(name="val")
            @ConfigFallbackKeys({"absoluteVal"})
            public String field;
            protected String protectedField;
            private String privateField;
        }

        Field field = Klazz.class.getFields()[0];

        List<String> foundKeys = InjectionEvaluator.getKeys(field);
        assertThat(foundKeys).isNotNull()
                .contains("basic.val", "val",
                        "absoluteVal");
    }

    @Test
    public void evaluateKeysWithMemberAnnotation() {
        class Klazz {
            @ConfigProperty(name="val")
            @ConfigFallbackKeys("[absoluteVal]")
            public String field;
            protected String protectedField;
            private String privateField;
        }

        Field field = Klazz.class.getFields()[0];

        List<String> foundKeys = InjectionEvaluator.getKeys(field);
        assertThat(foundKeys).isNotNull()
                .contains("Klazz.val", "val",
                        "absoluteVal");
    }
}