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

import static org.apache.tamaya.functions.MethodNotMockedAnswer.NOT_MOCKED_ANSWER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class FilteredConfigSourceTest {

    /*
     * Tests for getName()
     */

    @Test
    public void getNameReturnsTheNameOfTheBaseConfiguration() {
        ConfigSource propertySource = mock(ConfigSource.class, NOT_MOCKED_ANSWER);
        doReturn("abc").when(propertySource).getName();

        Predicate<String> filter = new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return false;
            }
        };

        FilteredConfigSource sut = new FilteredConfigSource(propertySource, filter);

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

        Predicate<String> filter = new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return false;
            }
        };

        FilteredConfigSource sut = new FilteredConfigSource(propertySource, filter);

        int ordinal = sut.getOrdinal();

        assertThat(ordinal).isEqualTo(13);
    }

    /*
     * Tests for get(String)
     */

    @Test
    public void getReturnsNullInsteadOfValueBecausOfFilter() {
        ConfigSource propertySource = mock(ConfigSource.class, NOT_MOCKED_ANSWER);
        doReturn("000").when(propertySource).getValue(eq("abc"));

        Predicate<String> filter = new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return !"abc".equals(s);
            }
        };

        FilteredConfigSource sut = new FilteredConfigSource(propertySource, filter);

        String result = sut.getValue("abc");

        assertThat(result).isNull();
    }

    @Test
    public void getReturnsValueBecauseItIsNotFiltered() {
        ConfigSource propertySource = mock(ConfigSource.class, NOT_MOCKED_ANSWER);
        doReturn("000").when(propertySource).getValue(eq("abc"));

        Predicate<String> filter = new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return true;
            }
        };

        FilteredConfigSource sut = new FilteredConfigSource(propertySource, filter);

        String result = sut.getValue("abc");

        assertThat(result).isNotNull();
    }

    /*
     * Tests for getProperties()
     */

    @Test
    public void getPropertiesAndFilterRemovesAllProperties() {
        InMemoryConfigSource imps = new InMemoryConfigSource();
        imps.add("a", "1").add("b", "2").add("c", "3");
        imps.setName("s");

        Predicate<String> filter = new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return false;
            }
        };

        FilteredConfigSource fps = new FilteredConfigSource(imps, filter);

        assertThat(fps.getProperties()).isEmpty();;
    }

    @Test
    public void getPropertiesAndFilterRemovesNoProperties() {
        InMemoryConfigSource imps = new InMemoryConfigSource();
        imps.add("a", "1").add("b", "2").add("c", "3");
        imps.setName("s");

        Predicate<String> filter = new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return true;
            }
        };

        FilteredConfigSource fps = new FilteredConfigSource(imps, filter);

        assertThat(fps.getProperties()).isNotEmpty()
                                       .containsEntry("a", "1")
                                       .containsEntry("b", "2")
                                       .containsEntry("c","3")
                                       .hasSize(3);
    }

    @Test
    public void getPropertiesAndFilterRemovesSomeProperties() {
        InMemoryConfigSource imps = new InMemoryConfigSource();
        imps.add("a", "1").add("b", "2").add("c", "3");
        imps.setName("s");

        Predicate<String> filter = new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return !s.startsWith("a");
            }
        };

        FilteredConfigSource fps = new FilteredConfigSource(imps, filter);

        assertThat(fps.getProperties()).isNotEmpty()
                                       .containsEntry("b", "2")
                                       .containsEntry("c", "3")
                                       .hasSize(2);

    }


}