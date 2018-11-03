/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tamaya.functions;

import org.apache.tamaya.Configuration;
import org.apache.tamaya.TypeLiteral;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.EMPTY_MAP;
import static org.apache.tamaya.functions.MethodNotMockedAnswer.NOT_MOCKED_ANSWER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class EnrichedConfigurationTest {

    /*
     * Tests for current(String)
     */

    @Test
    public void getKeyIsNull() throws Exception {
        Configuration base = mock(Configuration.class, NOT_MOCKED_ANSWER);
        final EnrichedConfiguration sut = new EnrichedConfiguration(base, EMPTY_MAP, true);

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                sut.get(null);
            }
        }).isInstanceOf(NullPointerException.class)
          .hasMessage("Key must be given.");
    }

    @Test
    public void getKeyIsNotKownAndHasNotAnOverriderWithOverridingOn() throws Exception {
        Configuration base = mock(Configuration.class, NOT_MOCKED_ANSWER);
        doReturn("9").when(base).get(eq("b"));
        doReturn(null).when(base).get(eq("y"));

        Map<String, Object> additions = new HashMap<>();
        additions.put("z0", "0");
        additions.put("b", "1");

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, true);

        String result = sut.get("y");

        assertThat(result).isNull();

    }

    @Test
    public void getKeyIsNotKownAndHasNotAnOverriderWithOverridingOff() throws Exception {
        Configuration base = mock(Configuration.class, NOT_MOCKED_ANSWER);
        doReturn("9").when(base).get(eq("b"));
        doReturn(null).when(base).get(eq("y"));

        Map<String, Object> additions = new HashMap<>();
        additions.put("z0", "0");
        additions.put("b", "1");

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, false);

        String result = sut.get("y");

        assertThat(result).isNull();
    }

    @Test
    public void getKeyIsNotKownAndHasOverriderAndConfigurationIsOverridingIsOn() throws Exception {
        Configuration base = mock(Configuration.class, NOT_MOCKED_ANSWER);
        doReturn("9").when(base).get(eq("y"));

        Map<String, Object> additions = new HashMap<>();
        additions.put("z0", "0");
        additions.put("b", "1");

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, true);

        String result = sut.get("b");

        assertThat(result).isNotNull().isEqualTo("1");
    }

    @Test
    public void getKeyIsNotKownAndHasOverriderAndConfigurationIsOverridingIsOff() throws Exception {
        Configuration base = mock(Configuration.class, NOT_MOCKED_ANSWER);
        doReturn(null).when(base).get(eq("b"));

        Map<String, Object> additions = new HashMap<>();
        additions.put("z0", "0");
        additions.put("b", "1");

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, false);

        String result = sut.get("b");

        assertThat(result).isNotNull().isEqualTo("1");
    }

    @Test
    public void getKeyIsKownAndHasOverriderAndConfigurationIsNotOverriding() throws Exception {
        Configuration base = mock(Configuration.class, NOT_MOCKED_ANSWER);
        doReturn("9").when(base).get(eq("b"));

        Map<String, Object> additions = new HashMap<>();
        additions.put("z0", "0");
        additions.put("b", "1");

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, false);

        String result = sut.get("b");

        assertThat(result).isNotNull().isEqualTo("9");
    }

    @Test
    public void getKeyIsKownAndHasOverriderAndConfigurationIsOverriding() throws Exception {
        Configuration base = mock(Configuration.class, NOT_MOCKED_ANSWER);
        doReturn("9").when(base).get(eq("b"));

        Map<String, Object> additions = new HashMap<>();
        additions.put("z0", "0");
        additions.put("b", "1");

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, true);

        String result = sut.get("b");

        assertThat(result).isNotNull().isEqualTo("1");
    }

    @Test
    public void getKeyIsKnownAndHasNoOverrider() throws Exception {
        Configuration base = mock(Configuration.class, NOT_MOCKED_ANSWER);
        doReturn("9").when(base).get(eq("b"));

        Map<String, Object> additions = new HashMap<>();
        additions.put("z0", "0");

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, true);

        String result = sut.get("b");

        assertThat(result).isNotNull().isEqualTo("9");
    }

    /*
     * Tests for getOrDefault(String, String)
     */

    @Test
    public void getOrDefaultStringStringWithKeyIsNull() throws Exception {
        Configuration base = mock(Configuration.class, NOT_MOCKED_ANSWER);

        final EnrichedConfiguration sut = new EnrichedConfiguration(base, EMPTY_MAP, true);

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                sut.getOrDefault(null, "v");
            }
        }).isInstanceOf(NullPointerException.class)
          .hasMessage("Key must be given.");
    }

    @Test
    public void getOrDefaultStringStringWithDefaultValueIsNull() throws Exception {
        Configuration base = mock(Configuration.class, NOT_MOCKED_ANSWER);

        final EnrichedConfiguration sut = new EnrichedConfiguration(base, EMPTY_MAP, true);

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                sut.getOrDefault("v", null);
            }
        }).isInstanceOf(NullPointerException.class)
          .hasMessage("Default createValue must be given.");
    }

    @Test
    public void getOrDefaultStringStringWithKeyIsOverriddenAndOverridingOn() throws Exception {
        Configuration base = mock(Configuration.class, NOT_MOCKED_ANSWER);
        doReturn("9").when(base).get(eq("b"));

        Map<String, Object> additions = new HashMap<>();
        additions.put("b", "0");

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, true);

        String result = sut.getOrDefault("b", "100");

        assertThat(result).isNotNull().isEqualTo("0");
    }

    @Test
    public void getOrDefaultStringStringWithKeyIsOverriddenAndOverridingOff() throws Exception {
        Configuration base = mock(Configuration.class, NOT_MOCKED_ANSWER);
        doReturn("9").when(base).get(eq("b"));

        Map<String, Object> additions = new HashMap<>();
        additions.put("b", "0");

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, false);

        String result = sut.get("b");

        assertThat(result).isNotNull().isEqualTo("9");
    }

    @Test
    public void getOrDefaultStringStringWithKeyIsKnownAndIsNotOverridden() throws Exception {
        Configuration base = mock(Configuration.class, NOT_MOCKED_ANSWER);
        doReturn("9").when(base).get(eq("b"));

        Map<String, Object> additions = new HashMap<>();
        additions.put("z0", "0");

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, true);

        String result = sut.getOrDefault("b", "100");

        assertThat(result).isNotNull().isEqualTo("9");
    }

    @Test
    public void getOrDefaultStringStringWithKeyIsNotKnownButIsOverridden() throws Exception {
        Configuration base = mock(Configuration.class, NOT_MOCKED_ANSWER);
        doReturn(null).when(base).get(eq("b"));

        Map<String, Object> additions = new HashMap<>();
        additions.put("b", "0");

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, true);

        String result = sut.get("b");

        assertThat(result).isNotNull().isEqualTo("0");
    }

    @Test
    public void getOrDefaultStringStringWithKeyIsUnKnownAndIsNotOverridden() throws Exception {
        Configuration base = mock(Configuration.class, NOT_MOCKED_ANSWER);
        doReturn(null).when(base).get(eq("b"));
        EnrichedConfiguration sut = new EnrichedConfiguration(base, EMPTY_MAP, true);

        String result = sut.getOrDefault("b", "1000");

        assertThat(result).isNotNull().isEqualTo("1000");
    }

    /*
     * Tests for getOrDefault(String, Class<T>, T)
     */

    @Test
    public void getOrDefaultStringClassTThrowsNPEIfKeyIsNull() throws Exception {
        final EnrichedConfiguration sut = mock(EnrichedConfiguration.class, NOT_MOCKED_ANSWER);
        doCallRealMethod().when(sut).getOrDefault(anyString(), any(Class.class), anyString());

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                sut.getOrDefault("b", String.class, null);
            }
        }).isInstanceOf(NullPointerException.class)
          .hasMessage("Default createValue not given.");
    }

    @Test
    public void getOrDefaultStringClassTThrowsNPEIfClassIsNull() throws Exception {
        final EnrichedConfiguration sut = mock(EnrichedConfiguration.class, NOT_MOCKED_ANSWER);
        doCallRealMethod().when(sut).getOrDefault(anyString(), any(Class.class), anyString());

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                sut.getOrDefault("b", (Class<String>)null, "20");
            }
        }).isInstanceOf(NullPointerException.class)
          .hasMessage("Class not given.");
    }

    @Test
    public void getOrDefaultStringClassTThrowsNPEIfDefaultValueIsNull() throws Exception {
        final EnrichedConfiguration sut = mock(EnrichedConfiguration.class, NOT_MOCKED_ANSWER);
        doCallRealMethod().when(sut).getOrDefault(anyString(), any(Class.class), anyString());

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                sut.getOrDefault("b", String.class, null);
            }
        }).isInstanceOf(NullPointerException.class)
          .hasMessage("Default createValue not given.");
    }

    @Test
    public void getOrDefaultStringClassTKeyInBaseAndInAdditionsNotOverriding() throws Exception {
        Configuration base = mock(Configuration.class, NOT_MOCKED_ANSWER);
        doReturn("1").when(base).get(eq("b"), any(TypeLiteral.class));

        Map<String, Object> additions = new HashMap<>();
        additions.put("b", "2");

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, false);

        String result = sut.getOrDefault("b", String.class, "3");

        assertThat(result).isEqualTo("1");
    }


    @Test
    public void getOrDefaultStringClassTKeyInBaseAndInAddtionsOverriding() throws Exception {
        Configuration base = mock(Configuration.class, NOT_MOCKED_ANSWER);
        doReturn("1").when(base).get(eq("b"), any(TypeLiteral.class));

        Map<String, Object> additions = new HashMap<>();
        additions.put("b", "2");

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, true);

        String result = sut.getOrDefault("b", String.class, "3");

        assertThat(result).isEqualTo("2");
    }

    @Test
    public void getOrDefaultStringClassTKeyNotInBaseInAdditionsNotOverriding() throws Exception {
        Configuration base = mock(Configuration.class, NOT_MOCKED_ANSWER);
        doReturn(null).when(base).get(eq("b"), any(TypeLiteral.class));

        Map<String, Object> additions = new HashMap<>();
        additions.put("b", "20");

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, false);

        String result = sut.getOrDefault("b", String.class, "B");

        assertThat(result).isEqualTo("20");
    }



    @Test
    public void getOrDefaultStringClassTKeyNotInBaseInAddtionsOverriding() throws Exception {
        Configuration base = mock(Configuration.class, NOT_MOCKED_ANSWER);
        doReturn(null).when(base).get(eq("b"), any(TypeLiteral.class));

        Map<String, Object> additions = new HashMap<>();
        additions.put("b", "20");

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, true);

        String result = sut.getOrDefault("b", String.class, "B");

        assertThat(result).isEqualTo("20");
    }


    @Test
    public void getOrDefaultStringClassTKeyNotInBaseNotInAdditionsAndNotOverrding() throws Exception {
        Configuration base = mock(Configuration.class, NOT_MOCKED_ANSWER);
        doReturn(null).when(base).get(eq("b"), any(TypeLiteral.class));

        Map<String, Object> additions = new HashMap<>();

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, false);

        String result = sut.getOrDefault("b", String.class, "B");

        assertThat(result).isEqualTo("B");
    }

    @Test
    public void getOrDefaultStringClassTKeyNotInBaseNotInAdditionsAndOverriding() throws Exception {
        Configuration base = mock(Configuration.class, NOT_MOCKED_ANSWER);
        doReturn(null).when(base).get(eq("b"), any(TypeLiteral.class));

        Map<String, Object> additions = new HashMap<>();

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, true);

        String result = sut.getOrDefault("b", String.class, "3");

        assertThat(result).isEqualTo("3");
    }




    /*
     * Tests for current(String, Class<T>)
     */

    /*
     * Tests for current(String, TypeLiteral)
     */

    /*
     * Tests for getOrDefault(String, TypeLiteral<T>, T)
     */

    @Test
    public void getOrDefaultStringTypeLiteralTThrowsNPEIfKeyIsNull() throws Exception {
        final EnrichedConfiguration sut = mock(EnrichedConfiguration.class, NOT_MOCKED_ANSWER);
        doCallRealMethod().when(sut).getOrDefault(anyString(), any(TypeLiteral.class), anyString());

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                sut.getOrDefault("b", TypeLiteral.<String>of(String.class), null);
            }
        }).isInstanceOf(NullPointerException.class)
          .hasMessage("Default createValue not given.");

    }

    @Test
    public void getOrDefaultStringLiteralTThrowsNPEIfClassIsNull() throws Exception {
        final EnrichedConfiguration sut = mock(EnrichedConfiguration.class, NOT_MOCKED_ANSWER);
        doCallRealMethod().when(sut).getOrDefault(anyString(), any(TypeLiteral.class), anyString());

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                sut.getOrDefault("b", (TypeLiteral<String>)null, "20");
            }
        }).isInstanceOf(NullPointerException.class)
          .hasMessage("Type not given.");

    }

    @Test
    public void getOrDefaultStringTypeLiteralThrowsNPEIfDefaultValueIsNull() throws Exception {
        final EnrichedConfiguration sut = mock(EnrichedConfiguration.class, NOT_MOCKED_ANSWER);
        doCallRealMethod().when(sut).getOrDefault(anyString(), any(TypeLiteral.class), anyString());

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                sut.getOrDefault("b", TypeLiteral.<String>of(String.class), null);
            }
        }).isInstanceOf(NullPointerException.class)
          .hasMessage("Default createValue not given.");
    }

    @Test
    public void getOrDefaultStringTypeLiteralTKeyInBaseAndInAdditionsNotOverriding() throws Exception {
        Configuration base = mock(Configuration.class, NOT_MOCKED_ANSWER);
        doReturn("1").when(base).get(eq("b"), any(TypeLiteral.class));

        Map<String, Object> additions = new HashMap<>();
        additions.put("b", "2");

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, false);

        String result = sut.getOrDefault("b", TypeLiteral.<String>of(String.class), "3");

        assertThat(result).isEqualTo("1");
    }


    @Test
    public void getOrDefaultStringTypeLiteralTKeyInBaseAndInAddtionsOverriding() throws Exception {
        Configuration base = mock(Configuration.class, NOT_MOCKED_ANSWER);
        doReturn("1").when(base).get(eq("b"), any(TypeLiteral.class));

        Map<String, Object> additions = new HashMap<>();
        additions.put("b", "2");

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, true);

        String result = sut.getOrDefault("b", TypeLiteral.<String>of(String.class), "3");

        assertThat(result).isEqualTo("2");
    }

    @Test
    public void getOrDefaultStringTypeLiteralTKeyNotInBaseInAdditionsNotOverriding() throws Exception {
        Configuration base = mock(Configuration.class, NOT_MOCKED_ANSWER);
        doReturn(null).when(base).get(eq("b"), any(TypeLiteral.class));

        Map<String, Object> additions = new HashMap<>();
        additions.put("b", "20");

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, false);

        String result = sut.getOrDefault("b", TypeLiteral.<String>of(String.class), "B");

        assertThat(result).isEqualTo("20");
    }



    @Test
    public void getOrDefaultStringTypeLiteralTKeyNotInBaseInAddtionsOverriding() throws Exception {
        Configuration base = mock(Configuration.class, NOT_MOCKED_ANSWER);
        doReturn(null).when(base).get(eq("b"), any(TypeLiteral.class));

        Map<String, Object> additions = new HashMap<>();
        additions.put("b", "20");

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, true);

        String result = sut.getOrDefault("b", TypeLiteral.<String>of(String.class), "B");

        assertThat(result).isEqualTo("20");
    }


    @Test
    public void getOrDefaultStringTypeLiteralTKeyNotInBaseNotInAdditionsAndNotOverrding() throws Exception {
        Configuration base = mock(Configuration.class, NOT_MOCKED_ANSWER);
        doReturn(null).when(base).get(eq("b"), any(TypeLiteral.class));

        Map<String, Object> additions = new HashMap<>();

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, false);

        String result = sut.getOrDefault("b", TypeLiteral.<String>of(String.class), "B");

        assertThat(result).isEqualTo("B");
    }

    @Test
    public void getOrDefaultStringTypeLiteralTKeyNotInBaseNotInAdditionsAndOverriding() throws Exception {
        Configuration base = mock(Configuration.class, NOT_MOCKED_ANSWER);
        doReturn(null).when(base).get(eq("b"), any(TypeLiteral.class));

        Map<String, Object> additions = new HashMap<>();

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, true);

        String result = sut.getOrDefault("b", TypeLiteral.<String>of(String.class), "3");

        assertThat(result).isEqualTo("3");
    }

    /*
     * Tests for getProperties()
     */

    // all in base, not additions
    @Test
    public void getPropertiesAllInBaseAndNoneInAdditions() {
        Map<String, Object> baseProps = new HashMap<>();
        baseProps.put("a", "A");
        baseProps.put("b", "B");
        baseProps.put("c", "C");

        Configuration base = mock(Configuration.class, NOT_MOCKED_ANSWER);
        doReturn(baseProps).when(base).getProperties();

        EnrichedConfiguration enrichedConfiguration = new EnrichedConfiguration(base, EMPTY_MAP, true);

        Map<String, String> result = enrichedConfiguration.getProperties();

        assertThat(result).isNotEmpty()
                          .hasSize(3)
                          .containsEntry("a", "A")
                          .containsEntry("b", "B")
                          .containsEntry("c", "C");

    }

    @Test
    public void getPropertiesAllInBaseAndSomeOverriddenByAdditions() {
        Map<String, Object> baseProps = new HashMap<>();
        baseProps.put("a", "A");
        baseProps.put("b", "B");
        baseProps.put("c", "C");

        Configuration base = mock(Configuration.class, NOT_MOCKED_ANSWER);
        doReturn(baseProps).when(base).getProperties();

        Map<String, Object> additionalProps = new HashMap<>();
        additionalProps.put("b", "b");

        EnrichedConfiguration enrichedConfiguration = new EnrichedConfiguration(base, additionalProps, true);

        Map<String, String> result = enrichedConfiguration.getProperties();

        assertThat(result).isNotEmpty()
                          .hasSize(3)
                          .containsEntry("a", "A")
                          .containsEntry("b", "b")
                          .containsEntry("c", "C");
    }

    @Test
    public void getPropertiesWithAdditionalPropertiesWhichAreNotInBaseOverriding() {
        Map<String, Object> baseProps = new HashMap<>();
        baseProps.put("a", "A");
        baseProps.put("b", "B");
        baseProps.put("c", "C");

        Configuration base = mock(Configuration.class, NOT_MOCKED_ANSWER);
        doReturn(baseProps).when(base).getProperties();

        Map<String, Object> additionalProps = new HashMap<>();
        additionalProps.put("e", "E");

        EnrichedConfiguration enrichedConfiguration = new EnrichedConfiguration(base, additionalProps, true);

        Map<String, String> result = enrichedConfiguration.getProperties();

        assertThat(result).isNotEmpty()
                          .hasSize(4)
                          .containsEntry("a", "A")
                          .containsEntry("b", "B")
                          .containsEntry("c", "C")
                          .containsEntry("e", "E");

    }

    @Test
    public void getPropertiesWithAdditionalPropertiesWhichAreNotInBaseNotOverriding() {
        Map<String, Object> baseProps = new HashMap<>();
        baseProps.put("a", "A");
        baseProps.put("b", "B");
        baseProps.put("c", "C");

        Configuration base = mock(Configuration.class, NOT_MOCKED_ANSWER);
        doReturn(baseProps).when(base).getProperties();

        Map<String, Object> additionalProps = new HashMap<>();
        additionalProps.put("e", "E");

        EnrichedConfiguration enrichedConfiguration = new EnrichedConfiguration(base, additionalProps, false);

        Map<String, String> result = enrichedConfiguration.getProperties();

        assertThat(result).isNotEmpty()
                          .hasSize(4)
                          .containsEntry("a", "A")
                          .containsEntry("b", "B")
                          .containsEntry("c", "C")
                          .containsEntry("e", "E");

    }

    @Test
    public void getPropertiesSomeAlsoInAdditionsNotOverriding() {
        Map<String, Object> baseProps = new HashMap<>();
        baseProps.put("a", "A");
        baseProps.put("b", "B");
        baseProps.put("c", "C");

        Configuration base = mock(Configuration.class, NOT_MOCKED_ANSWER);
        doReturn(baseProps).when(base).getProperties();

        Map<String, Object> additionalProps = new HashMap<>();
        additionalProps.put("b", "b");

        EnrichedConfiguration enrichedConfiguration = new EnrichedConfiguration(base, additionalProps, false);

        Map<String, String> result = enrichedConfiguration.getProperties();

        assertThat(result).isNotEmpty()
                          .hasSize(3)
                          .containsEntry("a", "A")
                          .containsEntry("b", "B")
                          .containsEntry("c", "C");
    }


    /*
     * Tests for with(ConfigOperator)
     */

    /*
     * Tests for query(ConfigQuery)
     */

    /*
     * Tests for current()
     */
}