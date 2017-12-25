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

import org.apache.tamaya.base.DefaultConfigBuilder;
import org.apache.tamaya.base.configsource.SimpleConfigSource;
import org.apache.tamaya.spisupport.DefaultConfiguration;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Test;
import org.mockito.Mockito;

import javax.config.Config;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.apache.tamaya.functions.MethodNotMockedAnswer.NOT_MOCKED_ANSWER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class CombinedConfigurationTest {
    private Config configWithA1;
    private Config configWithA2;
    private Config configWithB;
    private Config configWithC;
    private Config configWithoutEntries;

    {
        SimpleConfigSource sourceWithKeyA1 = new SimpleConfigSource("A", singletonMap("a", "a1"));
        SimpleConfigSource sourceWithKeyA2 = new SimpleConfigSource("A", singletonMap("a", "a2"));
        SimpleConfigSource sourceWithKeyB = new SimpleConfigSource("B", singletonMap("b", "b"));
        SimpleConfigSource sourceWithKeyC = new SimpleConfigSource("C", singletonMap("c", "c"));
        SimpleConfigSource sourceWithoutKeys = new SimpleConfigSource("NONE", Collections.<String, String>emptyMap());

        configWithA1 = new DefaultConfigBuilder().withSources(sourceWithKeyA1).build();
        configWithA2 = new DefaultConfigBuilder().withSources(sourceWithKeyA2).build();
        configWithB = new DefaultConfigBuilder().withSources(sourceWithKeyB).build();
        configWithC = new DefaultConfigBuilder().withSources(sourceWithKeyC).build();
        configWithoutEntries = new DefaultConfigBuilder().withSources(sourceWithoutKeys).build();
    }

    @Test
    public void createCombinedConfigurationWithNullAsSingleConfiguration() {
        CombinedConfiguration cc = new CombinedConfiguration("abc", null);

        assertThat(cc.getValue("nil", String.class)).isNull();
    }

    @Test
    public void createCombinedConfigurationWithNullNullAsSingleConfiguration() {
        CombinedConfiguration cc = new CombinedConfiguration("abc", null, null);

        assertThat(cc.getValue("nil", String.class)).isNull();
    }

    @Test
    public void requestedEntryIsntInAnyConfigration() throws Exception {

        CombinedConfiguration cc = new CombinedConfiguration("abc", configWithA1, configWithB, configWithC);

        assertThat(cc.getValue("key", String.class)).isNull();
    }

    @Test
    public void requestedEntryIsInTheFirstAndThridConfiguration() {
        CombinedConfiguration cc = new CombinedConfiguration("abc", configWithA1, configWithB, configWithA2);

        assertThat(cc.getValue("a", String.class)).isEqualTo("a2");
    }

    @Test
    public void requestedEntryIsOnlyInOneConfiguration() {
        CombinedConfiguration cc = new CombinedConfiguration("abc", configWithA1, configWithB, configWithC);

        assertThat(cc.getValue("b", String.class)).isEqualTo("b");
    }

    /*
     * Tests for getOrDefault(String, String)
     */

    @Test
    public void getOptionalValueWithSignatureStringStringThrowsNPEIfKeyIsNull() {
        final CombinedConfiguration cc = mock(CombinedConfiguration.class, CALLS_REAL_METHODS);

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                cc.getOptionalValue(null, String.class).orElse("d");
            }
        }).isInstanceOf(NullPointerException.class)
          .hasMessage("Key must be given.");
    }

    @Test
    public void getOptionalValueWithSignatureStringStringReturnsFoundValueIfKeyIsKnown() {
        Config cfg = mock(Config.class);
        doReturn(Optional.of("b")).when(cfg).getOptionalValue("a", String.class);

        String result =  new CombinedConfiguration("test", cfg)
                .getOptionalValue("a", String.class).get();

        assertThat(result).isEqualTo("b");
    }

    /*
     * Tests for getOrDefault(String, TypeLiteral<T>, T>
     */

    @Test
    public void getOptionalValueStringTypeTThrowsNPEIfKeyIsNull() throws Exception {
        final Config cfg = mock(Config.class);
        doReturn(Optional.of(Integer.valueOf(67))).when(cfg).getOptionalValue("a", Integer.class);

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                new CombinedConfiguration("test", cfg)
                        .getOptionalValue(null, Integer.class).orElse(1);
            }
        }).isInstanceOf(NullPointerException.class)
          .hasMessage("Key must be given.");

    }

    @Test
    public void getOptionalValueStringTypeThrowsNPEIfTypeIsNull() throws Exception {
        final CombinedConfiguration cc = mock(CombinedConfiguration.class, NOT_MOCKED_ANSWER);
        doCallRealMethod().when(cc).getOptionalValue(eq("a"), any(Class.class));

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                cc.<Integer>getOptionalValue("a", (Class)null);
            }
        }).isInstanceOf(NullPointerException.class)
          .hasMessage("Type must be given.");
    }

    @Test
    public void getOptionalValueStringTypeTReturnsEmptyOptionalIfKeyIsUnknown() throws Exception {
        final Config cfg = mock(Config.class);
        doReturn(null).when(cfg).getOptionalValue(any(), any(Class.class));
        Optional<Integer> val = new CombinedConfiguration("test", cfg)
                .getOptionalValue("b", Integer.class);
        assertNotNull(val);
        assertFalse(val.isPresent());
    }


    @Test
    public void getOptionalValueStringTypeReturnsFoundValueIfKeyIsKnown() throws Exception {
        final Config cfg = mock(Config.class);
        doReturn(Optional.of(Integer.valueOf(768))).when(cfg).getOptionalValue("a", Integer.class);

        Optional<Integer> val = new CombinedConfiguration("test", cfg)
                .getOptionalValue("a", Integer.class);
        assertNotNull(val);
        assertTrue(val.isPresent());
        assertEquals(Integer.valueOf(768), val.get());
    }

    /*
     * Tests for getOrDefault(String, Class<T>, T>
     */

    @Test
    public void getOptionalValueStringClassTThrowsNPEIfKeyIsNull() throws Exception {
        final Config cfg = mock(Config.class,NOT_MOCKED_ANSWER);
        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                new CombinedConfiguration("test", cfg).getOptionalValue(null, Integer.class);
            }
        }).isInstanceOf(NullPointerException.class)
          .hasMessage("Key must be given.");
    }

    @Test
    public void getOptionalValueStringClassTThrowsNPEIfTypeIsNull() throws Exception {
        final Config cfg = mock(Config.class,NOT_MOCKED_ANSWER);

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                new CombinedConfiguration("test", cfg)
                        .getOptionalValue("a", (Class<Integer>) null);
            }
        }).isInstanceOf(NullPointerException.class)
          .hasMessage("Type must be given.");
    }

    @Test
    public void getOptionalValueStringClassTReturnsFoundValueIfKeyIsKnown() throws Exception {
        final Config cfg = mock(Config.class);
        doReturn(Optional.of(Integer.valueOf(999))).when(cfg).getOptionalValue(eq("a"), any(Class.class));
        doReturn(Optional.of(Integer.valueOf(999))).when(cfg).getOptionalValue(eq("a"), any(Class.class));
        Integer result = new CombinedConfiguration("test", cfg)
                .getOptionalValue("a", Integer.class).orElse(789);
        assertThat(result).isEqualTo(999);
    }

    /*
     * Tests for getProperties();
     */

    @Test
    public void getPropertiesReturnsEmptyMapIfAllConfigurationsAreEmpty() throws Exception {
        Iterable<String> namesOfA = new HashSet<>();
        Iterable<String> namesOfB = new HashSet<>();
        Iterable<String> namesOfC = new HashSet<>();

        Config configA = Mockito.mock(Config.class, NOT_MOCKED_ANSWER);
        Config configB = Mockito.mock(Config.class, NOT_MOCKED_ANSWER);
        Config configC = Mockito.mock(Config.class, NOT_MOCKED_ANSWER);

        doReturn(namesOfA).when(configA).getPropertyNames();
        doReturn(namesOfB).when(configB).getPropertyNames();
        doReturn(namesOfC).when(configC).getPropertyNames();

        CombinedConfiguration cc = new CombinedConfiguration("test", configA, configB, configC);
        Iterable<String> result = cc.getPropertyNames();
        assertThat(result).isEmpty();
    }

    @Test
    public void getPropertyValueReturnsLastValueOfManyForAGivenKey() throws Exception {
        Set<String> propsOfA = new HashSet<String>() {{ add("a"); }};
        Set<String> propsOfB = new HashSet<String>() {{ add("a"); }};
        Set<String> propsOfC = new HashSet<String>() {{ add("a"); }};

        Config configA = Mockito.mock(Config.class, NOT_MOCKED_ANSWER);
        Config configB = Mockito.mock(Config.class, NOT_MOCKED_ANSWER);
        Config configC = Mockito.mock(Config.class, NOT_MOCKED_ANSWER);

        doReturn(Optional.of("A")).when(configA).getOptionalValue(eq("a"), any());
        doReturn(Optional.of("B")).when(configB).getOptionalValue(eq("a"), any());
        doReturn(Optional.of("C")).when(configC).getOptionalValue(eq("a"), any());
        doReturn(Optional.empty()).when(configA).getOptionalValue(eq("foo"), any());
        doReturn(Optional.empty()).when(configB).getOptionalValue(eq("foo"), any());
        doReturn(Optional.empty()).when(configC).getOptionalValue(eq("foo"), any());
        doReturn(propsOfA).when(configA).getPropertyNames();
        doReturn(propsOfB).when(configB).getPropertyNames();
        doReturn(propsOfC).when(configC).getPropertyNames();

        CombinedConfiguration cc = new CombinedConfiguration("test", configA, configB, configC);
        Iterable<String> result = cc.getPropertyNames();

        assertThat(result).hasSize(1)
                .contains("a");
        assertThat(cc.getValue("a", String.class))
                .isEqualTo("C");
        assertThat(cc.getValue("foo", String.class))
                .isNull();
    }

    @Test
    public void getPropertyNamesReturnsAllProperties() throws Exception {
        Set<String> propsOfA = new HashSet<String>() {{ add("a"); }};
        Set<String> propsOfB = new HashSet<String>() {{ add("b"); }};
        Set<String> propsOfC = new HashSet<String>() {{ add("c"); }};

        Config configA = Mockito.mock(Config.class, NOT_MOCKED_ANSWER);
        Config configB = Mockito.mock(Config.class, NOT_MOCKED_ANSWER);
        Config configC = Mockito.mock(Config.class, NOT_MOCKED_ANSWER);

        doReturn("A").when(configA).getValue("a", String.class);
        doReturn("B").when(configB).getValue("b", String.class);
        doReturn("C").when(configC).getValue("c", String.class);
        doReturn(propsOfA).when(configA).getPropertyNames();
        doReturn(propsOfB).when(configB).getPropertyNames();
        doReturn(propsOfC).when(configC).getPropertyNames();

        CombinedConfiguration cc = new CombinedConfiguration("test", configA, configB, configC);
        Iterable<String> result = cc.getPropertyNames();

        assertThat(result).hasSize(3)
                          .contains("a")
                          .contains("b")
                          .contains("c");
    }


}