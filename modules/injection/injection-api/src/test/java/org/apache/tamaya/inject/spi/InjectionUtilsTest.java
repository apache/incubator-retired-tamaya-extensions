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

import org.apache.tamaya.inject.api.Config;
import org.apache.tamaya.inject.api.ConfigSection;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class InjectionUtilsTest {

    @Test
    public void getKeysMethod() {
        class Klazz {
            @Config(key = "val", alternateKeys = {"[val2]", "vvv"})
            public void setValue(String field){}
        }

        Method method = Klazz.class.getMethods()[0];

        List<String> foundKeys = InjectionUtils.getKeys(method);

        assertThat(foundKeys)
                .isNotNull()
                .hasSize(3)
                .contains("val",
                        "val2",
                        "vvv");
    }

    @Test
    public void getKeysMethod_Resolution_Absolute() {
        class Klazz {
            @Config(key = "val", keyResolver = AbsoluteKeyResolver.class, alternateKeys = {"val2", "vvv"})
            public void setValue(String field){}
        }

        Method method = Klazz.class.getMethods()[0];

        List<String> foundKeys = InjectionUtils.getKeys(method);

        assertThat(foundKeys)
                .isNotNull()
                .hasSize(3)
                .contains("val",
                        "val2",
                        "vvv");
    }

    @Test
    public void getKeysMethod_Resolution_RELATIVE_FQN() {
        class Klazz {
            @Config(key = "val", keyResolver = FullKeyResolver.class, alternateKeys = {"val2", "vvv"})
            public void setValue(String field){}
        }

        Method method = Klazz.class.getMethods()[0];

        List<String> foundKeys = InjectionUtils.getKeys(method);

        assertThat(foundKeys)
                .isNotNull()
                .hasSize(3)
                .contains("org.apache.tamaya.inject.spi.InjectionUtilsTest$3Klazz.val",
                        "org.apache.tamaya.inject.spi.InjectionUtilsTest$3Klazz.val2",
                        "org.apache.tamaya.inject.spi.InjectionUtilsTest$3Klazz.vvv");
    }

    @Test
    public void getKeysMethod_Resolution_RELATIVE_SIMPLE() {
        @ConfigSection("nottobetaken")
        class Klazz {
            @Config(key = "val", keyResolver = SimpleKeyResolver.class, alternateKeys = {"val2", "vvv"})
            public void setValue(String field){}
        }

        Method method = Klazz.class.getMethods()[0];

        List<String> foundKeys = InjectionUtils.getKeys(method);

        assertThat(foundKeys)
                .isNotNull()
                .hasSize(3)
                .contains("Klazz.val",
                        "Klazz.val2",
                        "Klazz.vvv");
    }

    @Test
    public void getKeysReturnsEmptyListForNonAnnotatedField() {
        class Klazz {
            public String field;
            protected String protectedField;
            private String privateField;
        }

        Field field = Klazz.class.getFields()[0];

        List<String> foundKeys = InjectionUtils.getKeys(field);
        assertThat(foundKeys).hasSize(1);
        assertThat(foundKeys.get(0)).isEqualTo("field");
    }

    @Test
    public void getKeysReturns2ForNonAnnotatedField_Underscore() {
        class Klazz {
            public String a_field;
        }

        Field field = Klazz.class.getFields()[0];

        List<String> foundKeys = InjectionUtils.getKeys(field);
        assertThat(foundKeys).hasSize(2);
        assertThat(foundKeys.get(0)).isEqualTo("a_field");
        assertThat(foundKeys.get(1)).isEqualTo("a.field");
    }

    @Test
    public void getKeysReturns2ForNonAnnotatedField_CamelCase() {
        class Klazz {
            public String aField;
        }

        Field field = Klazz.class.getFields()[0];

        List<String> foundKeys = InjectionUtils.getKeys(field);
        assertThat(foundKeys).hasSize(2);
        assertThat(foundKeys.get(0)).isEqualTo("aField");
        assertThat(foundKeys.get(1)).isEqualTo("a.field");
    }

    @Test
    public void getKeysWithSection() {
        @ConfigSection("basic")
        class Klazz {
            public String field;
            protected String protectedField;
            private String privateField;
        }

        Field field = Klazz.class.getFields()[0];

        List<String> foundKeys = InjectionUtils.getKeys(field);
        assertThat(foundKeys).hasSize(1);
        assertThat(foundKeys.get(0)).isEqualTo("basic.field");
    }

    @Test
    public void getKeysWithSectionAndMemberAnnotation() {
        @ConfigSection("basic")
        class Klazz {
            @Config(key = "val", alternateKeys = {"relativeVal", "[absoluteVal]"})
            public String field;
            protected String protectedField;
            private String privateField;
        }

        Field field = Klazz.class.getFields()[0];

        List<String> foundKeys = InjectionUtils.getKeys(field);
        assertThat(foundKeys).hasSize(3);
        assertThat(foundKeys.get(0)).isEqualTo("basic.val");
        assertThat(foundKeys).isNotNull()
                .contains("basic.val",
                        "basic.relativeVal",
                        "absoluteVal");
    }

    @Test
    public void getKeysWithMemberAnnotation() {
        @ConfigSection
        class Klazz {
            @Config(key="val", alternateKeys = "[absoluteVal]")
            public String field;
            protected String protectedField;
            private String privateField;
        }

        Field field = Klazz.class.getFields()[0];

        List<String> foundKeys = InjectionUtils.getKeys(field);
        assertThat(foundKeys).hasSize(2);
        assertThat(foundKeys.get(0)).isEqualTo("Klazz.val");
        assertThat(foundKeys).isNotNull()
                .contains("Klazz.val",
                        "absoluteVal");
    }
}