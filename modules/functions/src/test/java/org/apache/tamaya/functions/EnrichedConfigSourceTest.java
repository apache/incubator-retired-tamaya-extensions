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

import javax.config.spi.ConfigSource;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.EMPTY_MAP;
import static org.apache.tamaya.functions.MethodNotMockedAnswer.NOT_MOCKED_ANSWER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class EnrichedConfigSourceTest {

    /*
     * Tests for getName()
     */

    @Test
    public void getNameReturnsTheNameOfTheBaseConfiguration() {
        ConfigSource propertySource = mock(ConfigSource.class, NOT_MOCKED_ANSWER);
        doReturn("abc").when(propertySource).getName();

        EnrichedConfigSource sut = new EnrichedConfigSource(propertySource, EMPTY_MAP, false);

        String name = sut.getName();

        assertThat(name).isEqualTo("abc");
    }


    /*
     * Tests for getOrdinal()
     */

    @Test
    public void getOrdinalReturnsTheValueOfTheBaseConfiguration() {
        ConfigSource propertySource = mock(ConfigSource.class, NOT_MOCKED_ANSWER);
        doReturn(13).when(propertySource).getOrdinal();

        EnrichedConfigSource sut = new EnrichedConfigSource(propertySource, EMPTY_MAP, false);

        int ordinal = sut.getOrdinal();

        assertThat(ordinal).isEqualTo(13);
    }

    /*
     * Tests for EnrichedConfigSource(ConfigSource, Map<String, String>, boolean)
     */

    /*
     * Tests for get(String)
     */

    @Test
    public void getReturnsAdditional() {
        InMemoryConfigSource base = new InMemoryConfigSource();

        base.setName("name").add("a", "1").add("b", "2").add("c", "3").add("d", "4");

        Map<String, String> additions = new HashMap<>();
        additions.put("e", "9");
        additions.put("f", "11");

        EnrichedConfigSource sut = new EnrichedConfigSource(base, additions, false);

        assertThat(sut.getValue("e")).isNotNull().isNotNull().isEqualTo("9");
    }

    @Test
    public void getReturnsOverriddenValue() {
        InMemoryConfigSource base = new InMemoryConfigSource();

        base.setName("name").add("a", "1").add("b", "2").add("c", "3").add("d", "4");

        Map<String, String> additions = new HashMap<>();
        additions.put("b", "9");
        additions.put("d", "11");

        EnrichedConfigSource sut = new EnrichedConfigSource(base, additions, true);

        String result = sut.getValue("b");

        assertThat(result).isNotNull().isEqualTo("9");
    }

    @Test
    public void getReturnsGivenValueWhichIsNotOverridden() {
        InMemoryConfigSource base = new InMemoryConfigSource();

        base.setName("name").add("a", "1").add("b", "2").add("c", "3").add("d", "4");

        Map<String, String> additions = new HashMap<>();
        additions.put("b", "9");
        additions.put("d", "11");

        EnrichedConfigSource sut = new EnrichedConfigSource(base, additions, true);

        String result = sut.getValue("a");

        assertThat(result).isNotNull().isEqualTo("1");
    }

    @Test
    public void getPropertiesReturnsNotOverriddenValue() {
        InMemoryConfigSource base = new InMemoryConfigSource();

        base.setName("name").add("a", "1").add("b", "2").add("c", "3").add("d", "4");

        Map<String, String> additions = new HashMap<>();
        additions.put("b", "9");
        additions.put("d", "11");

        EnrichedConfigSource sut = new EnrichedConfigSource(base, additions, false);

        String result = sut.getValue("b");

        assertThat(result).isNotNull().isEqualTo("2");
    }


    /*
     * Tests for getProperties()
     */

    @Test
    public void getPropertiesReturnsAllAdditionalToo() {
        InMemoryConfigSource base = new InMemoryConfigSource();

        base.setName("name").add("a", "1").add("b", "2").add("c", "3").add("d", "4");

        Map<String, String> additions = new HashMap<>();
        additions.put("e", "9");
        additions.put("f", "11");

        EnrichedConfigSource sut = new EnrichedConfigSource(base, additions, false);

        Map<String, String> properties = sut.getProperties();

        assertThat(properties).isNotNull().isNotEmpty()
                              .containsEntry("a", "1")
                              .containsEntry("b", "2")
                              .containsEntry("c", "3")
                              .containsEntry("d", "4")
                              .containsEntry("e", "9")
                              .containsEntry("f", "11")
                              .hasSize(6);
    }

    @Test
    public void getPropertiesReturnsAllWithOverriddenValues() {
        InMemoryConfigSource base = new InMemoryConfigSource();

        base.setName("name").add("a", "1").add("b", "2").add("c", "3").add("d", "4");

        Map<String, String> additions = new HashMap<>();
        additions.put("b", "9");
        additions.put("d", "11");

        EnrichedConfigSource sut = new EnrichedConfigSource(base, additions, true);

        Map<String, String> properties = sut.getProperties();

        assertThat(properties).isNotNull().isNotEmpty()
                              .containsEntry("a", "1")
                              .containsEntry("b", "9")
                              .containsEntry("c", "3")
                              .containsEntry("d", "11")
                              .hasSize(4);

    }

    @Test
    public void getPropertiesReturnsAllNoOverriddenValues() {
        InMemoryConfigSource base = new InMemoryConfigSource();

        base.setName("name").add("a", "1").add("b", "2").add("c", "3").add("d", "4");

        Map<String, String> additions = new HashMap<>();
        additions.put("b", "9");
        additions.put("d", "11");

        EnrichedConfigSource sut = new EnrichedConfigSource(base, additions, false);

        Map<String, String> properties = sut.getProperties();

        assertThat(properties).isNotNull().isNotEmpty()
                              .containsEntry("a", "1")
                              .containsEntry("b", "2")
                              .containsEntry("c", "3")
                              .containsEntry("d", "4")
                              .hasSize(4);
    }

}