/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tamaya.functions;

import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class MappedConfigSourceTest {
    private static final KeyMapper KEY_MAPPER = new KeyMapper() {
        @Override
        public String mapKey(String key) {
            String result = key;

            if ("M".compareTo(key.toUpperCase()) <= 0) {
                result = key.toUpperCase();
            }

            return result;
        }
    };

    /*
     * Tests for getProperties()
     */

    @Test
    public void getPropertiesWithMappedKeys() {
        InMemoryConfigSource propertySource = new InMemoryConfigSource();
        propertySource.setName("PS");
        propertySource.add("a", "1");
        propertySource.add("b", "2");
        propertySource.add("m", "3");

        MappedConfigSource mappedConfigSource = new MappedConfigSource(propertySource, KEY_MAPPER);

        Map<String, String> result = mappedConfigSource.getProperties();

        assertThat(result).isNotNull()
                          .containsEntry("a", "1")
                          .containsEntry("b", "2")
                          .containsEntry("M", "3")
                          .hasSize(3);
    }

    @Test
    public void getPropertiesWithoutMappedKeys() {
        InMemoryConfigSource propertySource = new InMemoryConfigSource();
        propertySource.setName("PS");
        propertySource.add("a", "1");
        propertySource.add("b", "2");
        propertySource.add("c", "3");

        MappedConfigSource mappedConfigSource = new MappedConfigSource(propertySource, KEY_MAPPER);

        Map<String, String> result = mappedConfigSource.getProperties();

        assertThat(result).isNotNull()
                          .containsEntry("a", "1")
                          .containsEntry("b", "2")
                          .containsEntry("c", "3")
                          .hasSize(3);
    }

    @Test
    public void getPropertiesMapperDiscardsOneKey() {
        InMemoryConfigSource propertySource = new InMemoryConfigSource();
        propertySource.setName("PS");
        propertySource.add("a", "1");
        propertySource.add("b", "2");
        propertySource.add("c", "3");

        MappedConfigSource mappedConfigSource = new MappedConfigSource(propertySource, new KeyMapper() {
            @Override
            public String mapKey(String key) {
                return "c".equals(key) ? null : key;
            }
        });

        Map<String, String> result = mappedConfigSource.getProperties();

        assertThat(result).isNotNull()
                          .containsEntry("a", "1")
                          .containsEntry("b", "2")
                          .hasSize(2);
    }

    @Test
    public void getPropertiesAndNoKeys() {
        InMemoryConfigSource propertySource = new InMemoryConfigSource();
        propertySource.setName("PS");

        MappedConfigSource mappedConfigSource = new MappedConfigSource(propertySource, KEY_MAPPER);

        Map<String, String> result = mappedConfigSource.getProperties();

        assertThat(result).isNotNull()
                          .isEmpty();
    }

    /*
     * Test for getOrdinal()
     */

    @Test
    public void getOrdinalReturnsCorrectOrdinal() {
        InMemoryConfigSource propertySource = new InMemoryConfigSource();
        MappedConfigSource mappedConfigSource = new MappedConfigSource(propertySource, KEY_MAPPER);

        propertySource.setOrdinal(999);

        assertThat(mappedConfigSource.getOrdinal()).isEqualTo(999);
    }

    /*
     * Tests for get(String)
     */

    @Test
    public void getReturnsNullIfKeyIsNotInUnderlayingConfiguration() {
        InMemoryConfigSource propertySource = new InMemoryConfigSource();
        MappedConfigSource mappedConfigSource = new MappedConfigSource(propertySource, KEY_MAPPER);

        assertThat(mappedConfigSource.getValue("nonexisting")).isNull();
    }

    @Test
    public void getReturnsCorrectValueIfKeyIsMapped() {
        InMemoryConfigSource propertySource = new InMemoryConfigSource();
        propertySource.add("m", "_a_");
        propertySource.setName("PS");

        MappedConfigSource mappedConfigSource = new MappedConfigSource(propertySource, KEY_MAPPER);

        assertThat(mappedConfigSource.getValue("M")).isNotNull().isEqualTo("_a_");
    }




}