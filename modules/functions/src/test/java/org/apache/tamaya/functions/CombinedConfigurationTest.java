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

import org.apache.tamaya.ConfigOperator;
import org.apache.tamaya.ConfigQuery;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.TypeLiteral;
import org.apache.tamaya.spisupport.DefaultConfiguration;
import org.apache.tamaya.core.internal.CoreConfigurationBuilder;
import org.apache.tamaya.spisupport.propertysource.SimplePropertySource;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.apache.tamaya.functions.MethodNotMockedAnswer.NOT_MOCKED_ANSWER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;


public class CombinedConfigurationTest {
    private Configuration configWithA1;
    private Configuration configWithA2;
    private Configuration configWithB;
    private Configuration configWithC;
    private Configuration configWithoutEntries;

    {
        SimplePropertySource sourceWithKeyA1 = new SimplePropertySource("A", singletonMap("a", "a1"));
        SimplePropertySource sourceWithKeyA2 = new SimplePropertySource("A", singletonMap("a", "a2"));
        SimplePropertySource sourceWithKeyB = new SimplePropertySource("B", singletonMap("b", "b"));
        SimplePropertySource sourceWithKeyC = new SimplePropertySource("C", singletonMap("c", "c"));
        SimplePropertySource sourceWithoutKeys = new SimplePropertySource("NONE", Collections.emptyMap());

        Configuration ccWithA1 = new CoreConfigurationBuilder().addPropertySources(sourceWithKeyA1)
                                                                                .build();
        Configuration ccWithA2 = new CoreConfigurationBuilder().addPropertySources(sourceWithKeyA2)
                                                                                .build();
        Configuration ccWithB = new CoreConfigurationBuilder().addPropertySources(sourceWithKeyB)
                                                                               .build();
        Configuration ccWithC = new CoreConfigurationBuilder().addPropertySources(sourceWithKeyC)
                                                                               .build();
        Configuration ccWithoutEntries = new CoreConfigurationBuilder().addPropertySources(sourceWithoutKeys)
                                                                                        .build();

        configWithA1 = new DefaultConfiguration(ccWithA1.getContext());
        configWithA2 = new DefaultConfiguration(ccWithA2.getContext());
        configWithB = new DefaultConfiguration(ccWithB.getContext());
        configWithC = new DefaultConfiguration(ccWithC.getContext());
        configWithoutEntries = new DefaultConfiguration(ccWithoutEntries.getContext());
    }

    @Test
    public void createCombinedConfigurationWithNullAsSingleConfiguration() {
        CombinedConfiguration cc = new CombinedConfiguration("abc", null);

        assertThat(cc.get("nil")).isNull();
    }

    @Test
    public void createCombinedConfigurationWithNullNullAsSingleConfiguration() {
        CombinedConfiguration cc = new CombinedConfiguration("abc", null, null);

        assertThat(cc.get("nil")).isNull();
    }

    @Test
    public void requestedEntryIsntInAnyConfigration() throws Exception {

        CombinedConfiguration cc = new CombinedConfiguration("abc", configWithA1, configWithB, configWithC);

        assertThat(cc.get("key")).isNull();
    }

    @Test
    public void requestedEntryIsInTheFirstAndThridConfiguration() {
        CombinedConfiguration cc = new CombinedConfiguration("abc", configWithA1, configWithB, configWithA2);

        assertThat(cc.get("a")).isEqualTo("a2");
    }

    @Test
    public void requestedEntryIsOnlyInOneConfiguration() {
        CombinedConfiguration cc = new CombinedConfiguration("abc", configWithA1, configWithB, configWithC);

        assertThat(cc.get("b")).isEqualTo("b");
    }

    /*
     * Tests for getOrDefault(String, String)
     */

    @Test
    public void getOrDefaultWithSignatureStringStringThrowsNPEIfKeyIsNull() {
        final CombinedConfiguration cc = mock(CombinedConfiguration.class, CALLS_REAL_METHODS);

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                cc.getOrDefault((String)null, "d");
            }
        }).isInstanceOf(NullPointerException.class)
          .hasMessage("Key must be given.");
    }

    @Test
    public void getOrDefaultWithSignatureStringStringThrowsNPEIfValueIsNull() {
        final CombinedConfiguration cc = mock(CombinedConfiguration.class, CALLS_REAL_METHODS);

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                cc.getOrDefault("key", null);
            }
        }).isInstanceOf(NullPointerException.class)
          .hasMessage("Value must be given.");
    }

    @Test
    public void getOrDefaultWithSignatureStringStringReturnsDefaultIfKeyIsUnknown() {
        CombinedConfiguration cc = mock(CombinedConfiguration.class);
        doReturn(null).when(cc).get("a");
        doCallRealMethod().when(cc).getOrDefault(anyString(), anyString());

        String result = cc.getOrDefault("a", "tzui");

        assertThat(result).isEqualTo("tzui");
    }

    @Test
    public void getOrDefaultWithSignatureStringStringReturnsFoundValueIfKeyIsKnown() {
        CombinedConfiguration cc = mock(CombinedConfiguration.class);
        doReturn("b").when(cc).get(Mockito.eq("a"));
        doCallRealMethod().when(cc).getOrDefault(anyString(), anyString());

        String result = cc.getOrDefault("a", "z");

        assertThat(result).isEqualTo("b");
    }

    /*
     * Tests for getOrDefault(String, TypeLiteral<T>, T>
     */

    @Test
    public void getOrDefaultStringTypeLiteralTThrowsNPEIfKeyIsNull() throws Exception {
        final CombinedConfiguration cc = mock(CombinedConfiguration.class);
        doCallRealMethod().when(cc).getOrDefault(anyString(), eq(TypeLiteral.of(Integer.class)),
                                                 Mockito.any(Integer.class));

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                cc.getOrDefault((String)null, TypeLiteral.of(Integer.class), 1);
            }
        }).isInstanceOf(NullPointerException.class)
          .hasMessage("Key must be given.");

    }

    @Test
    public void getOrDefaultStringTypeLiteralTThrowsNPEIfTypeIsNull() throws Exception {
        final CombinedConfiguration cc = mock(CombinedConfiguration.class, NOT_MOCKED_ANSWER);
        doCallRealMethod().when(cc).getOrDefault(anyString(), eq((TypeLiteral<Integer>)null),
                                                 Mockito.any(Integer.class));

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                cc.<Integer>getOrDefault("a", (TypeLiteral<Integer>)null, 1);
            }
        }).isInstanceOf(NullPointerException.class)
          .hasMessage("Type must be given.");
    }

    @Test
    public void getOrDefaultStringTypeLiteralTThrowsNPEIfDefaultValueIsNull() throws Exception {
        final CombinedConfiguration cc = mock(CombinedConfiguration.class, NOT_MOCKED_ANSWER);
        doCallRealMethod().when(cc).getOrDefault(anyString(), eq(TypeLiteral.of(Integer.class)),
                                                 Mockito.any(Integer.class));

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                cc.getOrDefault("a", TypeLiteral.of(Integer.class), null);
            }
        }).isInstanceOf(NullPointerException.class)
          .hasMessage("Default createValue must be given.");
    }

    @Test
    public void getOrDefaultStringTypeLiteralTReturnsDefaultValueIfKeyIsUnknown() throws Exception {
        final CombinedConfiguration cc = mock(CombinedConfiguration.class, NOT_MOCKED_ANSWER);
        doReturn(null).when(cc).get(eq("a"), eq(TypeLiteral.<Integer>of(Integer.class)));
        doCallRealMethod().when(cc).getOrDefault(anyString(), eq(TypeLiteral.of(Integer.class)),
                                                 Mockito.any(Integer.class));

        TypeLiteral<Integer> typeLiteral = TypeLiteral.of(Integer.class);
        Integer result = cc.<Integer>getOrDefault("a", typeLiteral, 789);

        assertThat(result).isEqualTo(789);
    }


    @Test
    public void getOrDefaultStringTypeLiteralTReturnsFoundValueIfKeyIsKnown() throws Exception {
        final CombinedConfiguration cc = mock(CombinedConfiguration.class, NOT_MOCKED_ANSWER);
        doReturn(999).when(cc).get(eq("a"), eq(TypeLiteral.<Integer>of(Integer.class)));
        doCallRealMethod().when(cc).getOrDefault(anyString(), eq(TypeLiteral.of(Integer.class)),
                                                 Mockito.anyInt());

        Integer result = cc.<Integer>getOrDefault("a", TypeLiteral.of(Integer.class), 789);

        assertThat(result).isEqualTo(999);
    }

    /*
     * Tests for getOrDefault(String, Class<T>, T>
     */

    @Test
    public void getOrDefaultStringClassTThrowsNPEIfKeyIsNull() throws Exception {
        final CombinedConfiguration cc = mock(CombinedConfiguration.class);
        doCallRealMethod().when(cc).getOrDefault(anyString(), Mockito.any(Class.class),
                                                 Mockito.any(Integer.class));

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                cc.getOrDefault((String)null, Integer.class, 1);
            }
        }).isInstanceOf(NullPointerException.class)
          .hasMessage("Key must be given.");
    }

    @Test
    public void getOrDefaultStringClassTThrowsNPEIfTypeIsNull() throws Exception {
        final CombinedConfiguration cc = mock(CombinedConfiguration.class);
        doCallRealMethod().when(cc).getOrDefault(anyString(), Mockito.any(Class.class), Mockito.anyInt());

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                cc.getOrDefault("a", (Class<Integer>) null, 1);
            }
        }).isInstanceOf(NullPointerException.class)
          .hasMessage("Type must be given.");
    }

    @Test
    public void getOrDefaultStringClassTThrowsNPEIfDefaultValueIsNull() throws Exception {
        final CombinedConfiguration cc = mock(CombinedConfiguration.class, NOT_MOCKED_ANSWER);
        doCallRealMethod().when(cc).getOrDefault(anyString(), any(Class.class),
                                                 Mockito.any(Integer.class));

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                cc.getOrDefault("a", Integer.class, null);
            }
        }).isInstanceOf(NullPointerException.class)
          .hasMessage("Default createValue must be given.");
    }

    @Test
    public void getOrDefaultStringClassTReturnsDefaultValueIfKeyIsUnknown() throws Exception {
        final CombinedConfiguration cc = mock(CombinedConfiguration.class, NOT_MOCKED_ANSWER);
        doReturn(null).when(cc).get(eq("a"), any(Class.class));
        doCallRealMethod().when(cc).getOrDefault(anyString(), any(Class.class),
                                                 Mockito.any(Integer.class));

        TypeLiteral<Integer> typeLiteral = TypeLiteral.of(Integer.class);
        Integer result = cc.<Integer>getOrDefault("a", Integer.class, 789);

        assertThat(result).isEqualTo(789);
    }


    @Test
    public void getOrDefaultStringClassTReturnsFoundValueIfKeyIsKnown() throws Exception {
        final CombinedConfiguration cc = mock(CombinedConfiguration.class, NOT_MOCKED_ANSWER);
        doReturn(999).when(cc).get(eq("a"), any(Class.class));
        doCallRealMethod().when(cc).getOrDefault(anyString(), any(Class.class),
                                                 Mockito.anyInt());

        Integer result = cc.<Integer>getOrDefault("a", Integer.class, 789);

        assertThat(result).isEqualTo(999);
    }

    /*
     * Tests for getProperties();
     */

    @Test
    public void getPropertiesReturnsEmptyMapIfAllConfigurationsAreEmpty() throws Exception {
        Map<String, String> propsOfA = new HashMap<>();
        Map<String, String> propsOfB = new HashMap<>();
        Map<String, String> propsOfC = new HashMap<>();

        Configuration configA = Mockito.mock(Configuration.class, NOT_MOCKED_ANSWER);
        Configuration configB = Mockito.mock(Configuration.class, NOT_MOCKED_ANSWER);
        Configuration configC = Mockito.mock(Configuration.class, NOT_MOCKED_ANSWER);

        doReturn(propsOfA).when(configA).getProperties();
        doReturn(propsOfB).when(configB).getProperties();
        doReturn(propsOfC).when(configC).getProperties();

        CombinedConfiguration cc = mock(CombinedConfiguration.class, NOT_MOCKED_ANSWER);

        doReturn(asList(configA, configB, configC)).when(cc).getConfigurations();
        doCallRealMethod().when(cc).getProperties();

        Map<String, String> result = cc.getProperties();

        assertThat(result).isEmpty();
    }

    @Test
    public void getPropertiesReturnsLastValueOfManyForAGivenKey() throws Exception {
        Map<String, String> propsOfA = new HashMap<String, String>() {{ put("a", "A"); }};
        Map<String, String> propsOfB = new HashMap<String, String>() {{ put("b", "B"); }};
        Map<String, String> propsOfC = new HashMap<String, String>() {{ put("a", "Z"); }};

        Configuration configA = Mockito.mock(Configuration.class, NOT_MOCKED_ANSWER);
        Configuration configB = Mockito.mock(Configuration.class, NOT_MOCKED_ANSWER);
        Configuration configC = Mockito.mock(Configuration.class, NOT_MOCKED_ANSWER);

        doReturn(propsOfA).when(configA).getProperties();
        doReturn(propsOfB).when(configB).getProperties();
        doReturn(propsOfC).when(configC).getProperties();

        CombinedConfiguration cc = mock(CombinedConfiguration.class, NOT_MOCKED_ANSWER);

        doReturn(asList(configA, configB, configC)).when(cc).getConfigurations();
        doCallRealMethod().when(cc).getProperties();

        Map<String, String> result = cc.getProperties();

        assertThat(result).containsEntry("a", "Z")
                          .doesNotContainEntry("a", "A");
    }

    @Test
    public void getPropertiesReturnsAllProperties() throws Exception {
        Map<String, String> propsOfA = new HashMap<String, String>() {{ put("a", "A"); }};
        Map<String, String> propsOfB = new HashMap<String, String>() {{ put("b", "B"); }};
        Map<String, String> propsOfC = new HashMap<String, String>() {{ put("c", "C"); }};

        Configuration configA = Mockito.mock(Configuration.class, NOT_MOCKED_ANSWER);
        Configuration configB = Mockito.mock(Configuration.class, NOT_MOCKED_ANSWER);
        Configuration configC = Mockito.mock(Configuration.class, NOT_MOCKED_ANSWER);

        doReturn(propsOfA).when(configA).getProperties();
        doReturn(propsOfB).when(configB).getProperties();
        doReturn(propsOfC).when(configC).getProperties();

        CombinedConfiguration cc = mock(CombinedConfiguration.class, NOT_MOCKED_ANSWER);

        doReturn(asList(configA, configB, configC)).when(cc).getConfigurations();
        doCallRealMethod().when(cc).getProperties();

        Map<String, String> result = cc.getProperties();

        assertThat(result).hasSize(3)
                          .containsEntry("a", "A")
                          .containsEntry("b", "B")
                          .containsEntry("c", "C");
    }

    /*
     * Tests for with(ConfigOperator)
     */

    @Test
    public void withWithIndentityOperatorReturnsEqualConfiguration() throws Exception {
        class IdentityOpr implements ConfigOperator {
            @Override
            public Configuration operate(Configuration config) {
                return config;
            }
        }

        final CombinedConfiguration cc = mock(CombinedConfiguration.class, NOT_MOCKED_ANSWER);
        doCallRealMethod().when(cc).with(Mockito.any(ConfigOperator.class));

        Configuration result = cc.with(new IdentityOpr());

        assertThat(result).isNotNull()
                          .isEqualTo(result);
    }

    @Test
    public void withWithNullAsOperatorParmeterThrowsNPE() throws Exception {
        final CombinedConfiguration cc = mock(CombinedConfiguration.class, NOT_MOCKED_ANSWER);
        doCallRealMethod().when(cc).with(Mockito.any(ConfigOperator.class));

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                cc.with(null);
            }
        }).isInstanceOf(NullPointerException.class)
          .hasMessage("Operator must be given.");
    }

    /*
     * Tests for query(ConfigQuery)
     */

    @Test
    public void queryWithNullAsQueryParameterThrowsNPE() throws Exception {
        final CombinedConfiguration cc = mock(CombinedConfiguration.class, NOT_MOCKED_ANSWER);
        doCallRealMethod().when(cc).query(Mockito.any(ConfigQuery.class));

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                cc.query(null);
            }
        }).isInstanceOf(NullPointerException.class)
          .hasMessage("Query must be given.");
    }

    @Test
    public void queryWithRealQueryReturnsCorrectResult() throws Exception {
        class GetZahl implements ConfigQuery<Integer> {
            @Override
            public Integer query(Configuration config) {
                return config.get("zahl", Integer.class);
            }
        }

        final CombinedConfiguration cc = mock(CombinedConfiguration.class, NOT_MOCKED_ANSWER);
        doCallRealMethod().when(cc).query(Mockito.any(ConfigQuery.class));
        doReturn(1).when(cc).<Integer>get(eq("zahl"), eq(Integer.class));

        Integer result = cc.query(new GetZahl());

        assertThat(result).isEqualTo(1);
    }

    // ConfigurationContext current();  none one three

}