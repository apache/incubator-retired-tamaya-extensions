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

import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertyValue;
import org.junit.Test;

import javax.management.RuntimeMBeanException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.EMPTY_MAP;
import static org.apache.tamaya.functions.MethodNotMockedAnswer.NOT_MOCKED_ANSWER;
import static org.apache.tamaya.spi.PropertyValue.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class EnrichedPropertySourceTest {

    /*
     * Tests for getName()
     */

    @Test
    public void getNameReturnsTheNameOfTheBaseConfiguration() {
        PropertySource propertySource = mock(PropertySource.class, NOT_MOCKED_ANSWER);
        doReturn("abc").when(propertySource).getName();

        EnrichedPropertySource sut = new EnrichedPropertySource(propertySource, EMPTY_MAP, false);

        String name = sut.getName();

        assertThat(name).isEqualTo("abc");
    }


    /*
     * Tests for getOrdinal()
     */

    @Test
    public void getOrdinalReturnsTheValueOfTheBaseConfiguration() {
        PropertySource propertySource = mock(PropertySource.class, NOT_MOCKED_ANSWER);
        doReturn(13).when(propertySource).getOrdinal();

        EnrichedPropertySource sut = new EnrichedPropertySource(propertySource, EMPTY_MAP, false);

        int ordinal = sut.getOrdinal();

        assertThat(ordinal).isEqualTo(13);
    }

    /*
     * Tests for EnrichedPropertySource(PropertySource, Map<String, String>, boolean)
     */

    /*
     * Tests for current(String)
     */

    @Test
    public void getReturnsAdditional() {
        InMemoryPropertySource base = new InMemoryPropertySource();

        base.setName("name").add("a", "1").add("b", "2").add("c", "3").add("d", "4");

        Map<String, String> additions = new HashMap<>();
        additions.put("e", "9");
        additions.put("f", "11");

        EnrichedPropertySource sut = new EnrichedPropertySource(base, additions, false);

        PropertyValue result = sut.get("e");

        assertThat(result).isNotNull().isNotNull().isEqualTo(of("e", "9", "name"));
    }

    @Test
    public void getReturnsOverriddenValue() {
        InMemoryPropertySource base = new InMemoryPropertySource();

        base.setName("name").add("a", "1").add("b", "2").add("c", "3").add("d", "4");

        Map<String, String> additions = new HashMap<>();
        additions.put("b", "9");
        additions.put("d", "11");

        EnrichedPropertySource sut = new EnrichedPropertySource(base, additions, true);

        PropertyValue result = sut.get("b");

        assertThat(result).isNotNull().isEqualTo(of("b", "9", "name"));
    }

    @Test
    public void getReturnsGivenValueWhichIsNotOverridden() {
        InMemoryPropertySource base = new InMemoryPropertySource();

        base.setName("name").add("a", "1").add("b", "2").add("c", "3").add("d", "4");

        Map<String, String> additions = new HashMap<>();
        additions.put("b", "9");
        additions.put("d", "11");

        EnrichedPropertySource sut = new EnrichedPropertySource(base, additions, true);

        PropertyValue result = sut.get("a");

        assertThat(result).isNotNull().isEqualTo(of("a", "1", "name"));
    }

    @Test
    public void getPropertiesReturnsNotOverriddenValue() {
        InMemoryPropertySource base = new InMemoryPropertySource();

        base.setName("name").add("a", "1").add("b", "2").add("c", "3").add("d", "4");

        Map<String, String> additions = new HashMap<>();
        additions.put("b", "9");
        additions.put("d", "11");

        EnrichedPropertySource sut = new EnrichedPropertySource(base, additions, false);

        PropertyValue result = sut.get("b");

        assertThat(result).isNotNull().isEqualTo(of("b", "2", "name"));
    }


    /*
     * Tests for getProperties()
     */

    @Test
    public void getPropertiesReturnsAllAdditionalToo() {
        InMemoryPropertySource base = new InMemoryPropertySource();

        base.setName("name").add("a", "1").add("b", "2").add("c", "3").add("d", "4");

        Map<String, String> additions = new HashMap<>();
        additions.put("e", "9");
        additions.put("f", "11");

        EnrichedPropertySource sut = new EnrichedPropertySource(base, additions, false);

        Map<String, PropertyValue> properties = sut.getProperties();

        assertThat(properties).isNotNull().isNotEmpty()
                              .containsEntry("a", of("a", "1", "name"))
                              .containsEntry("b", of("b", "2", "name"))
                              .containsEntry("c", of("c", "3", "name"))
                              .containsEntry("d", of("d", "4", "name"))
                              .containsEntry("e", of("e", "9", "name"))
                              .containsEntry("f", of("f", "11", "name"))
                              .hasSize(6);
    }

    @Test
    public void getPropertiesReturnsAllWithOverriddenValues() {
        InMemoryPropertySource base = new InMemoryPropertySource();

        base.setName("name").add("a", "1").add("b", "2").add("c", "3").add("d", "4");

        Map<String, String> additions = new HashMap<>();
        additions.put("b", "9");
        additions.put("d", "11");

        EnrichedPropertySource sut = new EnrichedPropertySource(base, additions, true);

        Map<String, PropertyValue> properties = sut.getProperties();

        assertThat(properties).isNotNull().isNotEmpty()
                              .containsEntry("a", of("a", "1", "name"))
                              .containsEntry("b", of("b", "9", "name"))
                              .containsEntry("c", of("c", "3", "name"))
                              .containsEntry("d", of("d", "11", "name"))
                              .hasSize(4);

    }

    @Test
    public void getPropertiesReturnsAllNoOverriddenValues() {
        InMemoryPropertySource base = new InMemoryPropertySource();

        base.setName("name").add("a", "1").add("b", "2").add("c", "3").add("d", "4");

        Map<String, String> additions = new HashMap<>();
        additions.put("b", "9");
        additions.put("d", "11");

        EnrichedPropertySource sut = new EnrichedPropertySource(base, additions, false);

        Map<String, PropertyValue> properties = sut.getProperties();

        assertThat(properties).isNotNull().isNotEmpty()
                              .containsEntry("a", of("a", "1", "name"))
                              .containsEntry("b", of("b", "2", "name"))
                              .containsEntry("c", of("c", "3", "name"))
                              .containsEntry("d", of("d", "4", "name"))
                              .hasSize(4);
    }

    /*
     * Tests for isScannable()
     */

    @Test
    public void isScannableReturnsTheValueOfTheBaseConfigurationWhichIsTrue() {
        PropertySource propertySource = mock(PropertySource.class, NOT_MOCKED_ANSWER);
        doReturn(true).when(propertySource).isScannable();

        EnrichedPropertySource sut = new EnrichedPropertySource(propertySource, EMPTY_MAP, false);

        boolean isScannable = sut.isScannable();

        assertThat(isScannable).isEqualTo(true);
    }

    @Test
    public void isScannableReturnsTheValueOfTheBaseConfigurationWhichIsFalse() {
        PropertySource propertySource = mock(PropertySource.class, NOT_MOCKED_ANSWER);
        doReturn(false).when(propertySource).isScannable();

        EnrichedPropertySource sut = new EnrichedPropertySource(propertySource, EMPTY_MAP, false);

        boolean isScannable = sut.isScannable();

        assertThat(isScannable).isEqualTo(false);
    }

}