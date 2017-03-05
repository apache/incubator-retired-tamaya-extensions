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
package org.apache.tamaya.filter;

import org.apache.tamaya.spi.ConfigurationContext;
import org.apache.tamaya.spi.PropertyFilter;
import org.apache.tamaya.spi.PropertyValue;
import org.apache.tamaya.spisupport.DefaultConfigurationContext;
import org.apache.tamaya.spisupport.DefaultConfigurationContextBuilder;
import org.apache.tamaya.spisupport.RegexPropertyFilter;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests for {@link FilterContext}. Created by atsticks on 11.02.16.
 */
public class ProgrammableFilterTest {

    private static ConfigurationContext context = new DefaultConfigurationContextBuilder().build();
    private static PropertyValue test1Property = PropertyValue.of("test1","test1","test");
    private static PropertyValue test2Property = PropertyValue.of("test2","test2","test");
    private static PropertyValue test3Property = PropertyValue.of("test.test3","test.test3","test");

    @Test
    public void testAddRemoveFilter() throws Exception {
        FilterContext filter = new FilterContext();
        Map<String,PropertyValue> map = new HashMap<>();
        assertEquals(filter.filterProperty(test1Property, new org.apache.tamaya.spi.FilterContext(test1Property, map, context)), test1Property);
        assertEquals(filter.filterProperty(test2Property, new org.apache.tamaya.spi.FilterContext(test2Property, map, context)), test2Property);
        assertEquals(filter.filterProperty(test3Property, new org.apache.tamaya.spi.FilterContext(test3Property, map, context)), test3Property);
        RegexPropertyFilter regexFilter = new RegexPropertyFilter();
        regexFilter.setIncludes("test\\..*");
        filter.addFilter(regexFilter);
        assertNull(filter.filterProperty(test1Property, new org.apache.tamaya.spi.FilterContext(test1Property, map, context)));
        assertNull(filter.filterProperty(test2Property, new org.apache.tamaya.spi.FilterContext(test2Property, map, context)));
        assertEquals(filter.filterProperty(test3Property, new org.apache.tamaya.spi.FilterContext(test3Property, map, context)), test3Property);
        filter.removeFilter(0);
        assertEquals(filter.filterProperty(test1Property, new org.apache.tamaya.spi.FilterContext(test1Property, map, context)), test1Property);
        assertEquals(filter.filterProperty(test2Property, new org.apache.tamaya.spi.FilterContext(test2Property, map, context)), test2Property);
        assertEquals(filter.filterProperty(test3Property, new org.apache.tamaya.spi.FilterContext(test3Property, map, context)), test3Property);
        filter.addFilter(0, regexFilter);
        assertNull(filter.filterProperty(test1Property, new org.apache.tamaya.spi.FilterContext(test1Property, map, context)));
        assertNull(filter.filterProperty(test2Property, new org.apache.tamaya.spi.FilterContext(test2Property, map, context)));
        assertEquals(filter.filterProperty(test3Property, new org.apache.tamaya.spi.FilterContext(test3Property, map, context)), test3Property);
    }

    @Test
    public void testClearFilters() throws Exception {
        FilterContext filter = new FilterContext();
        RegexPropertyFilter regexFilter = new RegexPropertyFilter();
        regexFilter.setIncludes("test1.*");
        Map<String,String> map = new HashMap<>();
        map.put("test1", "test1");
        map.put("test2", "test2");
        map.put("test.test3", "test.test3");
        assertEquals(filter.filterProperty(test1Property, new org.apache.tamaya.spi.FilterContext(test1Property, context)), test1Property);
        assertEquals(filter.filterProperty(test2Property, new org.apache.tamaya.spi.FilterContext(test2Property, context)), test2Property);
        assertEquals(filter.filterProperty(test3Property, new org.apache.tamaya.spi.FilterContext(test3Property, context)), test3Property);
        filter.addFilter(regexFilter);
        assertEquals(filter.filterProperty(test1Property, new org.apache.tamaya.spi.FilterContext(test1Property, context)), test1Property);
        assertNull(filter.filterProperty(test2Property, new org.apache.tamaya.spi.FilterContext(test2Property, context)));
        assertNull(filter.filterProperty(test3Property, new org.apache.tamaya.spi.FilterContext(test3Property, context)));
        filter.clearFilters();
        assertEquals(filter.filterProperty(test1Property, new org.apache.tamaya.spi.FilterContext(test1Property, context)), test1Property);
        assertEquals(filter.filterProperty(test2Property, new org.apache.tamaya.spi.FilterContext(test2Property, context)), test2Property);
        assertEquals(filter.filterProperty(test3Property, new org.apache.tamaya.spi.FilterContext(test3Property, context)), test3Property);
    }

    @Test
    public void testSetFilters() throws Exception {
        FilterContext filter = new FilterContext();
        RegexPropertyFilter regexFilter = new RegexPropertyFilter();
        regexFilter.setIncludes("test\\..*");
        Map<String,PropertyValue> map = new HashMap<>();
        map.put("test1", test1Property);
        map.put("test2", test1Property);
        map.put("test.test3", test3Property);
        assertEquals(filter.filterProperty(test1Property, new org.apache.tamaya.spi.FilterContext(test1Property, context)), test1Property);
        assertEquals(filter.filterProperty(test2Property, new org.apache.tamaya.spi.FilterContext(test2Property, map, context)), test2Property);
        assertEquals(filter.filterProperty(test3Property, new org.apache.tamaya.spi.FilterContext(test3Property, map, context)), test3Property);
        filter.setFilters(regexFilter);
        assertEquals(filter.filterProperty(test3Property, new org.apache.tamaya.spi.FilterContext(test3Property, map, context)), test3Property);
        assertNull(filter.filterProperty(test2Property, new org.apache.tamaya.spi.FilterContext(test2Property, map, context)));
        assertNull(filter.filterProperty(test1Property, new org.apache.tamaya.spi.FilterContext(test1Property, map, context)));
    }

    @Test
    public void testSetFilters1() throws Exception {
        FilterContext filter = new FilterContext();
        RegexPropertyFilter regexFilter = new RegexPropertyFilter();
        regexFilter.setIncludes("test1.*");
        Map<String,String> map = new HashMap<>();
        assertEquals(filter.filterProperty(test1Property, new org.apache.tamaya.spi.FilterContext(test1Property, context)), test1Property);
        assertEquals(filter.filterProperty(test2Property, new org.apache.tamaya.spi.FilterContext(test2Property, context)), test2Property);
        assertEquals(filter.filterProperty(test3Property, new org.apache.tamaya.spi.FilterContext(test3Property, context)), test3Property);
        filter.setFilters(Arrays.asList(new PropertyFilter[]{regexFilter}));
        assertEquals(filter.filterProperty(test1Property, new org.apache.tamaya.spi.FilterContext(test1Property, context)), test1Property);
        assertNull(filter.filterProperty(test2Property, new org.apache.tamaya.spi.FilterContext(test2Property, context)));
        assertNull(filter.filterProperty(test3Property, new org.apache.tamaya.spi.FilterContext(test3Property, context)));
    }

    @Test
    public void testGetFilters() throws Exception {
        FilterContext filter = new FilterContext();
        assertNotNull(filter.getFilters());
        assertTrue(filter.getFilters().isEmpty());
        RegexPropertyFilter regexFilter = new RegexPropertyFilter();
        regexFilter.setIncludes("test\\..*");
        filter.addFilter(regexFilter);
        assertNotNull(filter.getFilters());
        assertFalse(filter.getFilters().isEmpty());
        assertEquals(1, filter.getFilters().size());
        assertEquals(regexFilter, filter.getFilters().get(0));
    }

    @Test
    public void testToString() throws Exception {
        FilterContext filter = new FilterContext();
        assertFalse(filter.toString().contains("test\\..*"));
        assertTrue(filter.toString().contains("ProgrammableFilter"));
        assertFalse(filter.toString().contains("RegexPropertyFilter"));
        RegexPropertyFilter regexFilter = new RegexPropertyFilter();
        regexFilter.setIncludes("test\\..*");
        filter.addFilter(regexFilter);
        assertTrue(filter.toString().contains("test\\..*"));
        assertTrue(filter.toString().contains("ProgrammableFilter"));
        assertTrue(filter.toString().contains("RegexPropertyFilter"));
    }
}