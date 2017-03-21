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

import static org.apache.tamaya.functions.MethodNotMockedAnswer.NOT_MOCKED_ANSWER;
import static org.apache.tamaya.spi.PropertyValue.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class FilteredPropertySourceTest {

    /*
     * Tests for getName()
     */

    @Test
    public void getNameReturnsTheNameOfTheBaseConfiguration() {
        PropertySource propertySource = mock(PropertySource.class, NOT_MOCKED_ANSWER);
        doReturn("abc").when(propertySource).getName();

        Predicate<String> filter = new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return false;
            }
        };

        FilteredPropertySource sut = new FilteredPropertySource(propertySource, filter);

        String name = sut.getName();

        assertThat(name).isEqualTo("abc");
    }

    /*
     * Tests for isScannable()
     */

    @Test
    public void isScannableReturnsTheValueOfTheBaseConfiguration() {
        PropertySource propertySource = mock(PropertySource.class, NOT_MOCKED_ANSWER);
        doReturn(true).when(propertySource).isScannable();

        Predicate<String> filter = new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return false;
            }
        };

        FilteredPropertySource sut = new FilteredPropertySource(propertySource, filter);

        boolean isScannable = sut.isScannable();

        assertThat(isScannable).isEqualTo(true);
    }

    /*
     * Tests for getOrdinal()
     */

    @Test
    public void getOrdinalReturnsTheValueOfTheBaseConfiguration() {
        PropertySource propertySource = mock(PropertySource.class, NOT_MOCKED_ANSWER);
        doReturn(13).when(propertySource).getOrdinal();

        Predicate<String> filter = new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return false;
            }
        };

        FilteredPropertySource sut = new FilteredPropertySource(propertySource, filter);

        int ordinal = sut.getOrdinal();

        assertThat(ordinal).isEqualTo(13);
    }

    /*
     * Tests for get(String)
     */

    @Test
    public void getReturnsNullInsteadOfValueBecausOfFilter() {
        PropertyValue pv = of("abc", "000", "UT");
        PropertySource propertySource = mock(PropertySource.class, NOT_MOCKED_ANSWER);
        doReturn(pv).when(propertySource).get(eq("abc"));

        Predicate<String> filter = new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return !"abc".equals(s);
            }
        };

        FilteredPropertySource sut = new FilteredPropertySource(propertySource, filter);

        PropertyValue result = sut.get("abc");

        assertThat(result).isNull();
    }

    @Test
    public void getReturnsValueBecauseItIsNotFiltered() {
        PropertyValue pv = of("abc", "000", "UT");
        PropertySource propertySource = mock(PropertySource.class, NOT_MOCKED_ANSWER);
        doReturn(pv).when(propertySource).get(eq("abc"));

        Predicate<String> filter = new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return true;
            }
        };

        FilteredPropertySource sut = new FilteredPropertySource(propertySource, filter);

        PropertyValue result = sut.get("abc");

        assertThat(result).isNotNull();
    }

    /*
     * Tests for getProperties()
     */

    @Test
    public void getPropertiesAndFilterRemovesAllProperties() {
        InMemoryPropertySource imps = new InMemoryPropertySource();
        imps.add("a", "1").add("b", "2").add("c", "3");
        imps.setName("s");

        Predicate<String> filter = new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return false;
            }
        };

        FilteredPropertySource fps = new FilteredPropertySource(imps, filter);

        assertThat(fps.getProperties()).isEmpty();;
    }

    @Test
    public void getPropertiesAndFilterRemovesNoProperties() {
        InMemoryPropertySource imps = new InMemoryPropertySource();
        imps.add("a", "1").add("b", "2").add("c", "3");
        imps.setName("s");

        Predicate<String> filter = new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return true;
            }
        };

        FilteredPropertySource fps = new FilteredPropertySource(imps, filter);

        assertThat(fps.getProperties()).isNotEmpty()
                                       .containsEntry("a", of("a", "1", "s"))
                                       .containsEntry("b", of("b", "2", "s"))
                                       .containsEntry("c", of("c", "3", "s"))
                                       .hasSize(3);
    }

    @Test
    public void getPropertiesAndFilterRemovesSomeProperties() {
        InMemoryPropertySource imps = new InMemoryPropertySource();
        imps.add("a", "1").add("b", "2").add("c", "3");
        imps.setName("s");

        Predicate<String> filter = new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return !s.startsWith("a");
            }
        };

        FilteredPropertySource fps = new FilteredPropertySource(imps, filter);

        assertThat(fps.getProperties()).isNotEmpty()
                                       .containsEntry("b", of("b", "2", "s"))
                                       .containsEntry("c", of("c", "3", "s"))
                                       .hasSize(2);

    }


}