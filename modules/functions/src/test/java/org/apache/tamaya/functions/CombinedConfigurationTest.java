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
import org.apache.tamaya.spi.ConfigurationContext;
import org.apache.tamaya.spi.ConfigurationContextBuilder;
import org.apache.tamaya.spisupport.DefaultConfiguration;
import org.apache.tamaya.spisupport.DefaultConfigurationContext;
import org.apache.tamaya.spisupport.DefaultConfigurationContextBuilder;
import org.apache.tamaya.spisupport.SimplePropertySource;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;


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
        SimplePropertySource sourceWithoutKeys = new SimplePropertySource("NONE", Collections.<String, String>emptyMap());

        ConfigurationContext ccWithA1 = new DefaultConfigurationContextBuilder().addPropertySources(sourceWithKeyA1)
                                                                                .build();
        ConfigurationContext ccWithA2 = new DefaultConfigurationContextBuilder().addPropertySources(sourceWithKeyA2)
                                                                                .build();
        ConfigurationContext ccWithB = new DefaultConfigurationContextBuilder().addPropertySources(sourceWithKeyB)
                                                                               .build();
        ConfigurationContext ccWithC = new DefaultConfigurationContextBuilder().addPropertySources(sourceWithKeyC)
                                                                               .build();
        ConfigurationContext ccWithoutEntries = new DefaultConfigurationContextBuilder().addPropertySources(sourceWithoutKeys)
                                                                                        .build();

        configWithA1 = new DefaultConfiguration(ccWithA1);
        configWithA2 = new DefaultConfiguration(ccWithA2);
        configWithB = new DefaultConfiguration(ccWithB);
        configWithC = new DefaultConfiguration(ccWithC);
        configWithoutEntries = new DefaultConfiguration(ccWithoutEntries);
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

    // null, null
    // a, b
    // a,  null
    // getOrDefault none one three

    // String getOrDefault(String var1, String var2); none one three

    // <T> T getOrDefault(String var1, Class<T> var2, T var3);  none one three

    // <T> T get(String var1, Class<T> var2);  none one three

    // <T> T get(String var1, TypeLiteral<T> var2);  none one three

    // <T> T getOrDefault(String var1, TypeLiteral<T> var2, T var3);  none one three

    // Map<String, String> getProperties();  none one three

    // Configuration with(ConfigOperator var1);  none one three

    // <T> T query(ConfigQuery<T> var1);  none one three

    // ConfigurationContext getContext();  none one three

}