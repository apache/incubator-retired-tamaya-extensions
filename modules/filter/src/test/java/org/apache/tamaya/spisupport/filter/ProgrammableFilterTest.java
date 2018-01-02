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
package org.apache.tamaya.spisupport.filter;

import org.apache.tamaya.base.filter.FilterContext;
import org.apache.tamaya.base.filter.RegexPropertyFilter;
import org.apache.tamaya.filter.Context;
import org.apache.tamaya.spi.Filter;
import org.junit.Test;

import javax.config.Config;
import javax.config.ConfigProvider;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests for {@link Context}. Created by atsticks on 11.02.16.
 */
public class ProgrammableFilterTest {

    private static Config config = ConfigProvider.getConfig();
    private static String test1Property = "test1";
    private static String test2Property = "test2";
    private static String test3Property = "test.test3";

    @Test
    public void testAddRemoveFilter() throws Exception {
        Context filter = new Context();
        Map<String,String> map = new HashMap<>();
        FilterContext.setContext(new FilterContext(map, config));
        assertEquals(filter.filterProperty(test1Property,test1Property), test1Property);
        FilterContext.setContext(new FilterContext(map, config));
        assertEquals(filter.filterProperty(test2Property, test2Property), test2Property);
        FilterContext.setContext(new FilterContext(map, config));
        assertEquals(filter.filterProperty(test3Property, test3Property), test3Property);
        RegexPropertyFilter regexFilter = new RegexPropertyFilter();
        regexFilter.setIncludes("test\\..*");
        filter.addFilter(regexFilter);
        FilterContext.setContext(new FilterContext(map, config));
        assertNull(filter.filterProperty(test1Property, test1Property));
        FilterContext.setContext(new FilterContext(map, config));
        assertNull(filter.filterProperty(test2Property, test2Property));
        FilterContext.setContext(new FilterContext(map, config));
        assertEquals(filter.filterProperty(test3Property, test3Property), test3Property);
        filter.removeFilter(0);
        FilterContext.setContext(new FilterContext(map, config));
        assertEquals(filter.filterProperty(test1Property, test1Property), test1Property);
        FilterContext.setContext(new FilterContext(map, config));
        assertEquals(filter.filterProperty(test2Property, test2Property), test2Property);
        FilterContext.setContext(new FilterContext(map, config));
        assertEquals(filter.filterProperty(test3Property, test3Property), test3Property);
        filter.addFilter(0, regexFilter);
        FilterContext.setContext(new FilterContext(map, config));
        assertNull(filter.filterProperty(test1Property, test1Property));
        FilterContext.setContext(new FilterContext(map, config));
        assertNull(filter.filterProperty(test2Property, test2Property));
        FilterContext.setContext(new FilterContext(map, config));
        assertEquals(filter.filterProperty(test3Property, test3Property), test3Property);
    }

    @Test
    public void testClearFilters() throws Exception {
        Context filter = new Context();
        RegexPropertyFilter regexFilter = new RegexPropertyFilter();
        regexFilter.setIncludes("test1.*");
        Map<String,String> map = new HashMap<>();
        map.put("test1", "test1");
        map.put("test2", "test2");
        map.put("test.test3", "test.test3");
        FilterContext.setContext(new FilterContext(config));
        assertEquals(filter.filterProperty(test1Property, test1Property), test1Property);
        FilterContext.setContext(new FilterContext(config));
        assertEquals(filter.filterProperty(test2Property, test2Property), test2Property);
        FilterContext.setContext(new FilterContext(config));
        assertEquals(filter.filterProperty(test3Property, test3Property), test3Property);
        filter.addFilter(regexFilter);
        FilterContext.setContext(new FilterContext(config));
        assertEquals(filter.filterProperty(test1Property, test1Property), test1Property);
        FilterContext.setContext(new FilterContext(config));
        assertNull(filter.filterProperty(test2Property, test2Property));
        FilterContext.setContext(new FilterContext(config));
        assertNull(filter.filterProperty(test3Property, test3Property));
        filter.clearFilters();
        FilterContext.setContext(new FilterContext(config));
        assertEquals(filter.filterProperty(test1Property, test1Property), test1Property);
        FilterContext.setContext(new FilterContext(config));
        assertEquals(filter.filterProperty(test2Property, test2Property), test2Property);
        FilterContext.setContext(new FilterContext(config));
        assertEquals(filter.filterProperty(test3Property, test3Property), test3Property);
    }

    @Test
    public void testSetFilters() throws Exception {
        Context filter = new Context();
        RegexPropertyFilter regexFilter = new RegexPropertyFilter();
        regexFilter.setIncludes("test\\..*");
        Map<String,String> map = new HashMap<>();
        map.put("test1", test1Property);
        map.put("test2", test1Property);
        map.put("test.test3", test3Property);
        FilterContext.setContext(new FilterContext(map, config));
        assertEquals(filter.filterProperty(test1Property, test1Property), test1Property);
        FilterContext.setContext(new FilterContext(map, config));
        assertEquals(filter.filterProperty(test2Property, test2Property), test2Property);
        FilterContext.setContext(new FilterContext(map, config));
        assertEquals(filter.filterProperty(test3Property, test3Property), test3Property);
        filter.setFilters(regexFilter);
        FilterContext.setContext(new FilterContext(map, config));
        assertEquals(filter.filterProperty(test3Property, test3Property), test3Property);
        FilterContext.setContext(new FilterContext(map, config));
        assertNull(filter.filterProperty(test2Property, test2Property));
        FilterContext.setContext(new FilterContext(map, config));
        assertNull(filter.filterProperty(test1Property, test1Property));
    }

    @Test
    public void testSetFilters1() throws Exception {
        Context filter = new Context();
        RegexPropertyFilter regexFilter = new RegexPropertyFilter();
        regexFilter.setIncludes("test1.*");
        Map<String,String> map = new HashMap<>();
        FilterContext.setContext(new FilterContext(map, config));
        assertEquals(filter.filterProperty(test1Property, test1Property), test1Property);
        FilterContext.setContext(new FilterContext(map, config));
        assertEquals(filter.filterProperty(test2Property, test2Property), test2Property);
        FilterContext.setContext(new FilterContext(map, config));
        assertEquals(filter.filterProperty(test3Property, test3Property), test3Property);
        filter.setFilters(Arrays.asList(new Filter[]{regexFilter}));
        FilterContext.setContext(new FilterContext(config));
        assertEquals(filter.filterProperty(test1Property, test1Property), test1Property);
        FilterContext.setContext(new FilterContext(config));
        assertNull(filter.filterProperty(test2Property, test2Property));
        FilterContext.setContext(new FilterContext(config));
        assertNull(filter.filterProperty(test3Property, test3Property));
    }

    @Test
    public void testGetFilters() throws Exception {
        Context filter = new Context();
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
        Context filter = new Context();
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