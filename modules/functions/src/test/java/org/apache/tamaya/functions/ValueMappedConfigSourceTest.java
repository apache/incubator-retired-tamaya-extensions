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

import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ValueMappedConfigSourceTest {

    private PropertyMapper mapper = new PropertyMapper() {
        @Override
        public String mapProperty(String key, String value) {
            String startOfOtherKey = key.toLowerCase().substring(0, 1);
            if ("m".compareTo(startOfOtherKey) <= 0) {
                return value + "m";
            }

            return value;
        }
    };

    /*
     * Tests for getOrdinal()
     */

    @Test
    public void getOrdinalReturnsGivenOrdinal() throws Exception {
        InMemoryConfigSource source = new InMemoryConfigSource();
        source.add("a", "1").add("b", "2").add("c", "3").setName("S");
        source.setOrdinal(99);

        ValueMappedConfigSource mappingSource = new ValueMappedConfigSource("vmps", mapper, source);

        assertThat(mappingSource.getOrdinal()).isEqualTo(99);
    }

    /*
     * Tests for getName()
     */

    @Test
    public void getNameReturnsGivenName() throws Exception {
        InMemoryConfigSource source = new InMemoryConfigSource();
        source.add("a", "1").add("b", "2").add("c", "3").setName("S");

        ValueMappedConfigSource mappingSource = new ValueMappedConfigSource("vmps", mapper, source);

        assertThat(mappingSource.getName()).isEqualTo("vmps");
    }

    /*
     * Tests for get(String)
     */

    @Test
    public void getReturnNullIfKeyIsNotInBasePropertySource() throws Exception {
        InMemoryConfigSource source = new InMemoryConfigSource();
        source.add("a", "1").add("b", "2").add("m", "3").setName("S");

        ValueMappedConfigSource mappingSource = new ValueMappedConfigSource("vmps", mapper, source);

        assertThat(mappingSource.getValue("z")).isNull();
    }

    @Test
    public void getReturnsUnmappedValue() throws Exception {
        InMemoryConfigSource source = new InMemoryConfigSource();
        source.add("a", "1").add("b", "2").add("m", "3").setName("S");

        ValueMappedConfigSource mappingSource = new ValueMappedConfigSource("vmps", mapper, source);

        assertThat(mappingSource.getValue("a")).isNotNull()
                          .isEqualTo("1");
    }

    @Test
    public void getReturnsMappedValue() throws Exception {
        InMemoryConfigSource source = new InMemoryConfigSource();
        source.add("a", "1").add("b", "2").add("m", "3").setName("S");

        ValueMappedConfigSource mappingSource = new ValueMappedConfigSource("vmps", mapper, source);

        assertThat(mappingSource.getValue("m")).isNotNull()
                          .isEqualTo("3");
    }

    /*
     * Tests for getProperties()
     */

    @Test
    public void getPropertiesMapperMapsNoValue() {
        InMemoryConfigSource source = new InMemoryConfigSource();
        source.add("a", "1").add("b", "2").add("c", "3").setName("S");

        ValueMappedConfigSource mappingSource = new ValueMappedConfigSource("vmps", mapper, source);

        Map<String, String> result = mappingSource.getProperties();

        assertThat(result).isNotNull()
                          .isNotEmpty()
                          .containsEntry("a", "1")
                          .containsEntry("b", "2")
                          .containsEntry("c", "3")
                          .hasSize(3);

    }

    @Test
    public void getPropertiesMapperMapsSomeValues() throws Exception {
        InMemoryConfigSource source = new InMemoryConfigSource();
        source.add("a", "1").add("b", "2").add("m", "3").setName("S");

        ValueMappedConfigSource mappingSource = new ValueMappedConfigSource("vmps", mapper, source);

        Map<String, String> result = mappingSource.getProperties();

        assertThat(result).isNotNull()
                          .isNotEmpty()
                          .containsEntry("a", "1")
                          .containsEntry("b", "2")
                          .containsEntry("m", "3m")
                          .hasSize(3);
    }




}