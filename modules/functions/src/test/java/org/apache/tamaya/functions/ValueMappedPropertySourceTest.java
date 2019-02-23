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

import org.apache.tamaya.spi.PropertyValue;
import org.junit.Test;

import java.util.Map;

import static org.apache.tamaya.spi.PropertyValue.of;
import static org.assertj.core.api.Assertions.assertThat;

public class ValueMappedPropertySourceTest {

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
        InMemoryPropertySource source = new InMemoryPropertySource();
        source.add("a", "1").add("b", "2").add("c", "3").setName("S");
        source.setOrdinal(99);

        ValueMappedPropertySource mappingSource = new ValueMappedPropertySource("vmps", mapper, source);

        assertThat(mappingSource.getOrdinal()).isEqualTo(99);
    }

    /*
     * Tests for isScannable()
     */

    @Test
    public void isScannableReturnsTrueIfSetToTrue() throws Exception {
        InMemoryPropertySource source = new InMemoryPropertySource();
        source.add("a", "1").add("b", "2").add("c", "3").setName("S");
        source.setScannable(true);

        ValueMappedPropertySource mappingSource = new ValueMappedPropertySource("vmps", mapper, source);

        assertThat(mappingSource.isScannable()).isTrue();
    }


    /*
     * Tests for getName()
     */

    @Test
    public void getNameReturnsGivenName() throws Exception {
        InMemoryPropertySource source = new InMemoryPropertySource();
        source.add("a", "1").add("b", "2").add("c", "3").setName("S");

        ValueMappedPropertySource mappingSource = new ValueMappedPropertySource("vmps", mapper, source);

        assertThat(mappingSource.getName()).isEqualTo("vmps");
    }

    /*
     * Tests for current(String)
     */

    @Test
    public void getReturnNullIfKeyIsNotInBasePropertySource() throws Exception {
        InMemoryPropertySource source = new InMemoryPropertySource();
        source.add("a", "1").add("b", "2").add("m", "3").setName("S");

        ValueMappedPropertySource mappingSource = new ValueMappedPropertySource("vmps", mapper, source);

        PropertyValue result = mappingSource.get("z");

        assertThat(result).isNull();
    }

    @Test
    public void getReturnsUnmappedValue() throws Exception {
        InMemoryPropertySource source = new InMemoryPropertySource();
        source.add("a", "1").add("b", "2").add("m", "3").setName("S");

        ValueMappedPropertySource mappingSource = new ValueMappedPropertySource("vmps", mapper, source);

        PropertyValue result = mappingSource.get("a");

//        assertThat(result).isNotNull()
//                          .has(new Condition<PropertyValue>() {
//                              @Override
//                              public boolean matches(PropertyValue propertyValue) {
//                                  return "1".equals(propertyValue.getValue());
//                              }
//                          });
    }

    @Test
    public void getReturnsMappedValue() throws Exception {
        InMemoryPropertySource source = new InMemoryPropertySource();
        source.add("a", "1").add("b", "2").add("m", "3").setName("S");

        ValueMappedPropertySource mappingSource = new ValueMappedPropertySource("vmps", mapper, source);

        PropertyValue result = mappingSource.get("m");

//        assertThat(result).isNotNull()
//                          .has(new Condition<PropertyValue>() {
//                              @Override
//                              public boolean matches(PropertyValue propertyValue) {
//                                  return "3m".equals(propertyValue.getValue());
//                              }
//                          });
    }

    /*
     * Tests for getProperties()
     */

    @Test
    public void getPropertiesMapperMapsNoValue() {
        InMemoryPropertySource source = new InMemoryPropertySource();
        source.add("a", "1").add("b", "2").add("c", "3").setName("S");

        ValueMappedPropertySource mappingSource = new ValueMappedPropertySource("vmps", mapper, source);

        Map<String, PropertyValue> result = mappingSource.getProperties();

        assertThat(result).isNotNull()
                          .isNotEmpty()
                          .containsEntry("a", of("a", "1", "S"))
                          .containsEntry("b", of("b", "2", "S"))
                          .containsEntry("c", of("c", "3", "S"))
                          .hasSize(3);

    }

    @Test
    public void getPropertiesMapperMapsSomeValues() throws Exception {
        InMemoryPropertySource source = new InMemoryPropertySource();
        source.add("a", "1").add("b", "2").add("m", "3").setName("S");

        ValueMappedPropertySource mappingSource = new ValueMappedPropertySource("vmps", mapper, source);

        Map<String, PropertyValue> result = mappingSource.getProperties();

        assertThat(result).isNotNull()
                          .isNotEmpty()
                          .containsEntry("a", of("a", "1", "S"))
                          .containsEntry("b", of("b", "2", "S"))
                          .containsEntry("m", of("m", "3m", "S"))
                          .hasSize(3);
    }




}