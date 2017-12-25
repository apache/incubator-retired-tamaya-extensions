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

import javax.config.Config;
import java.lang.reflect.Type;
import java.util.*;

import static java.util.Collections.EMPTY_MAP;
import static org.apache.tamaya.functions.MethodNotMockedAnswer.NOT_MOCKED_ANSWER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class EnrichedConfigurationTest {

    /*
     * Tests for get(String)
     */

    @Test
    public void getKeyIsNull() throws Exception {
        Config base = mock(Config.class, NOT_MOCKED_ANSWER);
        final EnrichedConfiguration sut = new EnrichedConfiguration(base, EMPTY_MAP, true);

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                sut.getValue(null, String.class);
            }
        }).isInstanceOf(NullPointerException.class)
          .hasMessage("Key must be given.");
    }

    @Test
    public void getKeyIsNotKownAndHasNotAnOverriderWithOverridingOn() throws Exception {
        Config base = mock(Config.class, NOT_MOCKED_ANSWER);
        doReturn(Optional.of("9")).when(base).getOptionalValue(eq("b"), any());
        doReturn(Optional.empty()).when(base).getOptionalValue(eq("y"), any());

        Map<String, Object> additions = new HashMap<>();
        additions.put("z0", "0");
        additions.put("b", "1");

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, true);

        String result = sut.getValue("y", String.class);

        assertThat(result).isNull();

    }

    @Test
    public void getKeyIsNotKownAndHasNotAnOverriderWithOverridingOff() throws Exception {
        Config base = mock(Config.class, NOT_MOCKED_ANSWER);
        doReturn(Optional.of("9")).when(base).getOptionalValue(eq("b"), any());
        doReturn(Optional.empty()).when(base).getOptionalValue(eq("y"), eq(String.class));

        Map<String, Object> additions = new HashMap<>();
        additions.put("z0", "0");
        additions.put("b", "1");

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, false);

        String result = sut.getValue("y", String.class);

        assertThat(result).isNull();
    }

    @Test
    public void getKeyIsNotKownAndHasOverriderAndConfigurationIsOverridingIsOn() throws Exception {
        Config base = mock(Config.class, NOT_MOCKED_ANSWER);
        doReturn("9").when(base).getValue(eq("y"),any() );

        Map<String, Object> additions = new HashMap<>();
        additions.put("z0", "0");
        additions.put("b", "1");

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, true);

        String result = sut.getValue("b", String.class);

        assertThat(result).isNotNull().isEqualTo("1");
    }

    @Test
    public void getKeyIsNotKownAndHasOverriderAndConfigurationIsOverridingIsOff() throws Exception {
        Config base = mock(Config.class, NOT_MOCKED_ANSWER);
        doReturn(Optional.empty()).when(base).getOptionalValue(eq("y"),any() );
        doReturn(Optional.empty()).when(base).getOptionalValue(eq("b"),any() );

        Map<String, Object> additions = new HashMap<>();
        additions.put("z0", "0");
        additions.put("b", "1");

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, false);

        String result = sut.getValue("b", String.class);

        assertThat(result).isNotNull().isEqualTo("1");
    }

    @Test
    public void getKeyIsKownAndHasOverriderAndConfigurationIsNotOverriding() throws Exception {
        Config base = mock(Config.class, NOT_MOCKED_ANSWER);
        doReturn(Optional.of("9")).when(base).getOptionalValue(eq("b"),any() );

        Map<String, Object> additions = new HashMap<>();
        additions.put("z0", "0");
        additions.put("b", "1");

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, false);

        String result = sut.getValue("b", String.class);

        assertThat(result).isNotNull().isEqualTo("9");
    }

    @Test
    public void getKeyIsKownAndHasOverriderAndConfigurationIsOverriding() throws Exception {
        Config base = mock(Config.class, NOT_MOCKED_ANSWER);
        doReturn("9").when(base).getValue(eq("b"),any() );

        Map<String, Object> additions = new HashMap<>();
        additions.put("z0", "0");
        additions.put("b", "1");

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, true);

        String result = sut.getValue("b", String.class);

        assertThat(result).isNotNull().isEqualTo("1");
    }

    @Test
    public void getKeyIsKnownAndHasNoOverrider() throws Exception {
        Config base = mock(Config.class, NOT_MOCKED_ANSWER);
        doReturn(Optional.of("9")).when(base).getOptionalValue(eq("b"),any() );

        Map<String, Object> additions = new HashMap<>();
        additions.put("z0", "0");

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, true);

        String result = sut.getValue("b", String.class);

        assertThat(result).isNotNull().isEqualTo("9");
    }

    /*
     * Tests for getOrDefault(String, String)
     */

    @Test
    public void getOptionalValueStringWithKeyIsNull() throws Exception {
        Config base = mock(Config.class, NOT_MOCKED_ANSWER);

        final EnrichedConfiguration sut = new EnrichedConfiguration(base, EMPTY_MAP, true);

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                sut.getOptionalValue(null, String.class);
            }
        }).isInstanceOf(NullPointerException.class)
          .hasMessage("Key must be given.");
    }

    @Test
    public void getOptionalValueStringWithKeyIsOverriddenAndOverridingOn() throws Exception {
        Config base = mock(Config.class, NOT_MOCKED_ANSWER);
        doReturn(Optional.of("9")).when(base).getOptionalValue(eq("b"), eq(String.class));

        Map<String, Object> additions = new HashMap<>();
        additions.put("b", "0");

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, true);

        Optional<String> result = sut.getOptionalValue("b", String.class);
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals("0", result.get());
    }

    @Test
    public void getOptionalValueStringWithKeyIsOverriddenAndOverridingOff() throws Exception {
        Config base = mock(Config.class, NOT_MOCKED_ANSWER);
        doReturn(Optional.of("9")).when(base).getOptionalValue(eq("b"), any());
        doReturn(Optional.of("9")).when(base).getOptionalValue(eq("b"), any());

        Map<String, Object> additions = new HashMap<>();
        additions.put("b", "0");

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, false);

        String result = sut.getValue("b", String.class);

        assertThat(result).isNotNull().isEqualTo("9");
    }

    @Test
    public void getOrDefaultStringStringWithKeyIsKnownAndIsNotOverridden() throws Exception {
        Config base = mock(Config.class, NOT_MOCKED_ANSWER);
        doReturn(Optional.of("9")).when(base).getOptionalValue(eq("b"), any());

        Map<String, Object> additions = new HashMap<>();
        additions.put("z0", "0");

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, true);

        String result = sut.getOptionalValue("b", String.class).orElse("100");

        assertThat(result).isNotNull().isEqualTo("9");
    }

    @Test
    public void getOrDefaultStringStringWithKeyIsNotKnownButIsOverridden() throws Exception {
        Config base = mock(Config.class, NOT_MOCKED_ANSWER);
        doReturn("9").when(base).getValue(eq("b"), any());

        Map<String, Object> additions = new HashMap<>();
        additions.put("b", "0");

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, true);

        String result = sut.getValue("b", String.class);

        assertThat(result).isNotNull().isEqualTo("0");
    }

    @Test
    public void getOrDefaultStringStringWithKeyIsUnKnownAndIsNotOverridden() throws Exception {
        Config base = mock(Config.class, NOT_MOCKED_ANSWER);
        doReturn(Optional.of("9")).when(base).getOptionalValue("b", String.class);
        doReturn(Optional.empty()).when(base).getOptionalValue("z", String.class);
        EnrichedConfiguration sut = new EnrichedConfiguration(base, EMPTY_MAP, true);

        String result = sut.getOptionalValue("z", String.class).orElse("1000");

        assertThat(result).isNotNull().isEqualTo("1000");
    }

    /*
     * Tests for getOptionalValue(String, Class<T>)
     */


    @Test
    public void getOptionalValueStringClassTThrowsNPEIfClassIsNull() throws Exception {
        final EnrichedConfiguration sut = mock(EnrichedConfiguration.class, NOT_MOCKED_ANSWER);
        doCallRealMethod().when(sut).getOptionalValue(anyString(), any(Class.class));

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                sut.getOptionalValue("b", (Class<String>)null);
            }
        }).isInstanceOf(NullPointerException.class)
          .hasMessage("Type must be given.");
    }

    @Test
    public void getOptionalValueStringClassTKeyInBaseAndInAdditionsNotOverriding() throws Exception {
        Config base = mock(Config.class, NOT_MOCKED_ANSWER);
        doReturn(Optional.of("1")).when(base).getOptionalValue(eq("b"), any());

        Map<String, Object> additions = new HashMap<>();
        additions.put("b", "2");

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, false);

        Optional<String> result = sut.getOptionalValue("b", String.class);
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals("1", result.get());
    }


    @Test
    public void getOrDefaultStringClassTKeyInBaseAndInAddtionsOverriding() throws Exception {
        Config base = mock(Config.class, NOT_MOCKED_ANSWER);
        doReturn("1").when(base).getValue(eq("b"), any());

        Map<String, Object> additions = new HashMap<>();
        additions.put("b", "2");

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, true);

        String result = sut.getOptionalValue("b", String.class).orElse("3");

        assertThat(result).isEqualTo("2");
    }

    @Test
    public void getOrDefaultStringClassTKeyNotInBaseInAdditionsNotOverriding() throws Exception {
        Config base = mock(Config.class, NOT_MOCKED_ANSWER);
        doReturn(Optional.empty()).when(base).getOptionalValue(eq("b"), any());

        Map<String, Object> additions = new HashMap<>();
        additions.put("b", "20");

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, false);

        String result = sut.getOptionalValue("b", String.class).orElse("B");

        assertThat(result).isEqualTo("20");
    }



    @Test
    public void getOrDefaultStringClassTKeyNotInBaseInAddtionsOverriding() throws Exception {
        Config base = mock(Config.class, NOT_MOCKED_ANSWER);
        doReturn(null).when(base).getValue(eq("b"), any());

        Map<String, Object> additions = new HashMap<>();
        additions.put("b", "20");

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, true);

        String result = sut.getOptionalValue("b", String.class).orElse("B");

        assertThat(result).isEqualTo("20");
    }


    @Test
    public void getOrDefaultStringClassTKeyNotInBaseNotInAdditionsAndNotOverrding() throws Exception {
        Config base = mock(Config.class, NOT_MOCKED_ANSWER);
        doReturn(Optional.empty()).when(base).getOptionalValue(eq("b"), any());

        Map<String, Object> additions = new HashMap<>();

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, false);

        String result = sut.getOptionalValue("b", String.class).orElse("B");

        assertThat(result).isEqualTo("B");
    }

    @Test
    public void getOrDefaultStringClassTKeyNotInBaseNotInAdditionsAndOverriding() throws Exception {
        Config base = mock(Config.class, NOT_MOCKED_ANSWER);
        doReturn(null).when(base).getOptionalValue(eq("b"), any());

        Map<String, Object> additions = new HashMap<>();

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, true);

        String result = sut.getOptionalValue("b", String.class).orElse("3");

        assertThat(result).isEqualTo("3");
    }




    /*
     * Tests for get(String, Class<T>)
     */

    /*
     * Tests for get(String, TypeLiteral)
     */

    /*
     * Tests for getOrDefault(String, TypeLiteral<T>, T)
     */

    @Test
    public void getOrDefaultStringTypeLiteralTThrowsNPEIfKeyIsNull() throws Exception {
        final EnrichedConfiguration sut = mock(EnrichedConfiguration.class, NOT_MOCKED_ANSWER);
        doCallRealMethod().when(sut).getOptionalValue(anyString(), any(Class.class));

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                sut.getOptionalValue(null,String.class);
            }
        }).isInstanceOf(NullPointerException.class);

    }

    @Test
    public void getOrDefaultStringLiteralTThrowsNPEIfClassIsNull() throws Exception {
        final EnrichedConfiguration sut = mock(EnrichedConfiguration.class, NOT_MOCKED_ANSWER);
        doCallRealMethod().when(sut).getOptionalValue(anyString(), any(Class.class));

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                sut.getOptionalValue("b", (Class)null).orElse("20");
            }
        }).isInstanceOf(NullPointerException.class)
          .hasMessage("Type must be given.");

    }

    @Test
    public void getOrDefaultStringTypeLiteralTKeyInBaseAndInAdditionsNotOverriding() throws Exception {
        Config base = mock(Config.class, NOT_MOCKED_ANSWER);
        doReturn(Optional.of("1")).when(base).getOptionalValue(eq("b"), any(Class.class));

        Map<String, Object> additions = new HashMap<>();
        additions.put("b", "2");

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, false);

        String result = sut.getOptionalValue("b", String.class).orElse("3");

        assertThat(result).isEqualTo("1");
    }


    @Test
    public void getOrDefaultStringTypeLiteralTKeyInBaseAndInAddtionsOverriding() throws Exception {
        Config base = mock(Config.class, NOT_MOCKED_ANSWER);
        doReturn("1").when(base).getValue(eq("b"), any(Class.class));

        Map<String, Object> additions = new HashMap<>();
        additions.put("b", "2");

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, true);

        String result = sut.getOptionalValue("b", String.class).orElse("3");

        assertThat(result).isEqualTo("2");
    }

    @Test
    public void getOrDefaultStringTypeLiteralTKeyNotInBaseInAdditionsNotOverriding() throws Exception {
        Config base = mock(Config.class, NOT_MOCKED_ANSWER);
        doReturn(Optional.empty()).when(base).getOptionalValue(eq("b"), any());

        Map<String, Object> additions = new HashMap<>();
        additions.put("b", "20");

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, false);

        Optional<String> result = sut.getOptionalValue("b", String.class);
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals("20", result.get());
    }



    @Test
    public void getOrDefaultStringTypeLiteralTKeyNotInBaseInAddtionsOverriding() throws Exception {
        Config base = mock(Config.class, NOT_MOCKED_ANSWER);
        doReturn(null).when(base).getValue(eq("b"), any(Class.class));

        Map<String, Object> additions = new HashMap<>();
        additions.put("b", "20");

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, true);

        String result = sut.getOptionalValue("b", String.class).orElse("B");

        assertThat(result).isEqualTo("20");
    }


    @Test
    public void getOrDefaultStringTypeLiteralTKeyNotInBaseNotInAdditionsAndNotOverrding() throws Exception {
        Config base = mock(Config.class, NOT_MOCKED_ANSWER);
        doReturn(Optional.empty()).when(base).getOptionalValue(eq("b"), any(Class.class));

        Map<String, Object> additions = new HashMap<>();

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, false);

        String result = sut.getOptionalValue("b", String.class).orElse("B");

        assertThat(result).isEqualTo("B");
    }

    @Test
    public void getOrDefaultStringTypeLiteralTKeyNotInBaseNotInAdditionsAndOverriding() throws Exception {
        Config base = mock(Config.class, NOT_MOCKED_ANSWER);
        doReturn(null).when(base).getOptionalValue("b", String.class);

        Map<String, Object> additions = new HashMap<>();

        EnrichedConfiguration sut = new EnrichedConfiguration(base, additions, true);

        String result = sut.getOptionalValue("b", String.class).orElse("3");

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

        Config base = mock(Config.class, NOT_MOCKED_ANSWER);
        doReturn(baseProps.keySet()).when(base).getPropertyNames();
        doReturn(Optional.of("A")).when(base).getOptionalValue("a", String.class);
        doReturn(Optional.of("B")).when(base).getOptionalValue("b", String.class);
        doReturn(Optional.of("C")).when(base).getOptionalValue("c", String.class);

        EnrichedConfiguration enrichedConfiguration = new EnrichedConfiguration(base, EMPTY_MAP, true);

        Iterable<String> result = enrichedConfiguration.getPropertyNames();

        assertThat(result).isNotEmpty()
                          .hasSize(3);
        assertEquals("A", enrichedConfiguration.getValue("a", String.class));
        assertEquals("B", enrichedConfiguration.getValue("b", String.class));
        assertEquals("C", enrichedConfiguration.getValue("c", String.class));

    }

    @Test
    public void getPropertiesAllInBaseAndSomeOverriddenByAdditions() {
        Map<String, Object> baseProps = new HashMap<>();
        baseProps.put("a", "A");
        baseProps.put("b", "B");
        baseProps.put("c", "C");

        Config base = mock(Config.class, NOT_MOCKED_ANSWER);
        doReturn(baseProps.keySet()).when(base).getPropertyNames();
        doReturn(Optional.empty()).when(base).getOptionalValue(any(), any());

        Map<String, Object> additionalProps = new HashMap<>();
        additionalProps.put("b", "B");

        EnrichedConfiguration enrichedConfiguration = new EnrichedConfiguration(base, additionalProps, true);

        Iterable<String> result = enrichedConfiguration.getPropertyNames();

        assertThat(result).isNotEmpty()
                          .hasSize(3)
                .contains("a").contains("b").contains("c");
        assertEquals(null, enrichedConfiguration.getValue("a", String.class));
        assertEquals("B", enrichedConfiguration.getValue("b", String.class));
    }

    @Test
    public void getPropertiesWithAdditionalPropertiesWhichAreNotInBaseOverriding() {
        Map<String, Object> baseProps = new HashMap<>();
        baseProps.put("a", "A");
        baseProps.put("b", "B");
        baseProps.put("c", "C");

        Config base = mock(Config.class, NOT_MOCKED_ANSWER);
        doReturn(baseProps.keySet()).when(base).getPropertyNames();
        doReturn(Optional.empty()).when(base).getOptionalValue(any(), any());

        Map<String, Object> additionalProps = new HashMap<>();
        additionalProps.put("e", "E");

        EnrichedConfiguration enrichedConfiguration = new EnrichedConfiguration(base, additionalProps, true);

        Iterable<String> result = enrichedConfiguration.getPropertyNames();

        assertThat(result)
                .isNotEmpty()
                .hasSize(4)
                .contains("a")
                .contains("b")
                .contains("c")
                .contains("e");
    }

    @Test
    public void getPropertiesWithAdditionalPropertiesWhichAreNotInBaseNotOverriding() {
        Map<String, Object> baseProps = new HashMap<>();
        baseProps.put("a", "A");
        baseProps.put("b", "B");
        baseProps.put("c", "C");

        Config base = mock(Config.class, NOT_MOCKED_ANSWER);
        doReturn(baseProps.keySet()).when(base).getPropertyNames();
        doReturn(Optional.of("A")).when(base).getOptionalValue(eq("a"), any());
        doReturn(Optional.of("B")).when(base).getOptionalValue(eq("b"), any());
        doReturn(Optional.of("C")).when(base).getOptionalValue(eq("c"), any());
        doReturn(Optional.empty()).when(base).getOptionalValue(eq("e"), any());

        Map<String, Object> additionalProps = new HashMap<>();
        additionalProps.put("e", "E");

        EnrichedConfiguration enrichedConfiguration = new EnrichedConfiguration(base, additionalProps, false);

        Iterable<String> result = enrichedConfiguration.getPropertyNames();

        assertThat(result).isNotEmpty()
                          .hasSize(4);
        assertEquals("A", enrichedConfiguration.getValue("a", String.class));
        assertEquals("B", enrichedConfiguration.getValue("b", String.class));
        assertEquals("C", enrichedConfiguration.getValue("c", String.class));
        assertEquals("E", enrichedConfiguration.getValue("e", String.class));

    }

    @Test
    public void getPropertiesSomeAlsoInAdditionsNotOverriding() {
        Map<String, Object> baseProps = new HashMap<>();
        baseProps.put("a", "A");
        baseProps.put("b", "B");
        baseProps.put("c", "C");

        Config base = mock(Config.class, NOT_MOCKED_ANSWER);
        doReturn(baseProps.keySet()).when(base).getPropertyNames();
        doReturn(Optional.empty()).when(base).getOptionalValue(any(), any());

        Map<String, Object> additionalProps = new HashMap<>();
        additionalProps.put("b", "b");

        EnrichedConfiguration enrichedConfiguration = new EnrichedConfiguration(base, additionalProps, false);

        Iterable<String> result = enrichedConfiguration.getPropertyNames();

        assertThat(result).isNotEmpty()
                          .hasSize(3)
                            .contains("a")
                            .contains("b")
                            .contains("c");
    }


    /*
     * Tests for with(ConfigOperator)
     */

    /*
     * Tests for query(ConfigQuery)
     */

    /*
     * Tests for getContext()
     */
}