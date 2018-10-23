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

import org.apache.tamaya.spi.PropertyValue;
import org.junit.Test;

import javax.management.ImmutableDescriptor;
import java.util.Map;

import static org.apache.tamaya.spi.PropertyValue.of;
import static org.assertj.core.api.Assertions.assertThat;

public class MappedPropertySourceTest {
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
        InMemoryPropertySource propertySource = new InMemoryPropertySource();
        propertySource.setName("PS");
        propertySource.add("a", "1");
        propertySource.add("b", "2");
        propertySource.add("m", "3");

        MappedPropertySource mappedPropertySource = new MappedPropertySource(propertySource, KEY_MAPPER);

        Map<String, PropertyValue> result = mappedPropertySource.getProperties();

        assertThat(result).isNotNull()
                          .containsEntry("a", of("a", "1", "PS[mapped]"))
                          .containsEntry("b", of("b", "2", "PS[mapped]"))
                          .containsEntry("M", of("M", "3", "PS[mapped]"))
                          .hasSize(3);
    }

    @Test
    public void getPropertiesWithoutMappedKeys() {
        InMemoryPropertySource propertySource = new InMemoryPropertySource();
        propertySource.setName("PS");
        propertySource.add("a", "1");
        propertySource.add("b", "2");
        propertySource.add("c", "3");

        MappedPropertySource mappedPropertySource = new MappedPropertySource(propertySource, KEY_MAPPER);

        Map<String, PropertyValue> result = mappedPropertySource.getProperties();

        assertThat(result).isNotNull()
                          .containsEntry("a", of("a", "1", "PS[mapped]"))
                          .containsEntry("b", of("b", "2", "PS[mapped]"))
                          .containsEntry("c", of("c", "3", "PS[mapped]"))
                          .hasSize(3);
    }

    @Test
    public void getPropertiesMapperDiscardsOneKey() {
        InMemoryPropertySource propertySource = new InMemoryPropertySource();
        propertySource.setName("PS");
        propertySource.add("a", "1");
        propertySource.add("b", "2");
        propertySource.add("c", "3");

        MappedPropertySource mappedPropertySource = new MappedPropertySource(propertySource, new KeyMapper() {
            @Override
            public String mapKey(String key) {
                return "c".equals(key) ? null : key;
            }
        });

        Map<String, PropertyValue> result = mappedPropertySource.getProperties();

        assertThat(result).isNotNull()
                          .containsEntry("a", of("a", "1", "PS[mapped]"))
                          .containsEntry("b", of("b", "2", "PS[mapped]"))
                          .hasSize(2);
    }

    @Test
    public void getPropertiesAndNoKeys() {
        InMemoryPropertySource propertySource = new InMemoryPropertySource();
        propertySource.setName("PS");

        MappedPropertySource mappedPropertySource = new MappedPropertySource(propertySource, KEY_MAPPER);

        Map<String, PropertyValue> result = mappedPropertySource.getProperties();

        assertThat(result).isNotNull()
                          .isEmpty();
    }

    /*
     * Test for getOrdinal()
     */

    @Test
    public void getOrdinalReturnsCorrectOrdinal() {
        InMemoryPropertySource propertySource = new InMemoryPropertySource();
        MappedPropertySource mappedPropertySource = new MappedPropertySource(propertySource, KEY_MAPPER);

        propertySource.setOrdinal(999);

        assertThat(mappedPropertySource.getOrdinal()).isEqualTo(999);
    }

    /*
     * Tests for isScannable()
     */

    @Test
    public void isScannableReturnsTrueIfIsTrue() {
        InMemoryPropertySource propertySource = new InMemoryPropertySource();
        MappedPropertySource mappedPropertySource = new MappedPropertySource(propertySource, KEY_MAPPER);

        propertySource.setScannable(false);

        assertThat(mappedPropertySource.isScannable()).isFalse();
    }

    /*
     * Tests for current(String)
     */

    @Test
    public void getReturnsNullIfKeyIsNotInUnderlayingConfiguration() {
        InMemoryPropertySource propertySource = new InMemoryPropertySource();
        MappedPropertySource mappedPropertySource = new MappedPropertySource(propertySource, KEY_MAPPER);

        assertThat(mappedPropertySource.get("nonexisting")).isNull();
    }

    @Test
    public void getReturnsCorrectValueIfKeyIsMapped() {
        InMemoryPropertySource propertySource = new InMemoryPropertySource();
        propertySource.add("m", "_a_");
        propertySource.setName("PS");

        MappedPropertySource mappedPropertySource = new MappedPropertySource(propertySource, KEY_MAPPER);

        assertThat(mappedPropertySource.get("M")).isNotNull().isEqualTo(of("M", "_a_", "PS[mapped]"));
    }




}