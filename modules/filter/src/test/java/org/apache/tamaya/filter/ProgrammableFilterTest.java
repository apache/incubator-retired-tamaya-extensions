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
import org.apache.tamaya.spi.FilterContext;
import org.apache.tamaya.spi.PropertyFilter;
import org.apache.tamaya.spi.PropertyValue;
import org.apache.tamaya.spisupport.DefaultConfigurationBuilder;
import org.apache.tamaya.spisupport.RegexPropertyFilter;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ThreadFilterContext}. Created by atsticks on 11.02.16.
 */
public class ProgrammableFilterTest {

    private static ConfigurationContext context = new DefaultConfigurationBuilder().build().getContext();
    private static PropertyValue test1Property = PropertyValue.createValue("test1","test1");
    private static PropertyValue test2Property = PropertyValue.createValue("test2","test2");
    private static PropertyValue test3Property = PropertyValue.createValue("test.test3","test.test3");

    @Test
    public void testAddRemoveFilter() throws Exception {
        ThreadFilterContext filter = new ThreadFilterContext();
        Map<String,PropertyValue> map = new HashMap<>();
        FilterContext context1 = new FilterContext(test1Property, map, context);
        FilterContext context2 = new FilterContext(test2Property, map, context);
        FilterContext context3 = new FilterContext(test3Property, map, context);
        assertThat(filter.filterProperty(test1Property, context1)).isEqualTo(test1Property);
        assertThat(filter.filterProperty(test2Property, context2)).isEqualTo(test2Property);
        assertThat(filter.filterProperty(test3Property, context3)).isEqualTo(test3Property);
        RegexPropertyFilter regexFilter = new RegexPropertyFilter();
        regexFilter.setIncludes("test\\..*");
        filter.addFilter(regexFilter);
        assertThat(filter.filterProperty(test1Property, context1)).isNull();
        assertThat(filter.filterProperty(test2Property, context2)).isNull();
        assertThat(filter.filterProperty(test3Property, context3)).isEqualTo(test3Property);
        filter.removeFilter(0);
        assertThat(filter.filterProperty(test1Property, context1)).isEqualTo(test1Property);
        assertThat(filter.filterProperty(test2Property, context2)).isEqualTo(test2Property);
        assertThat(filter.filterProperty(test3Property, context3)).isEqualTo(test3Property);
        filter.addFilter(0, regexFilter);
        assertThat(filter.filterProperty(test1Property, context1)).isNull();
        assertThat(filter.filterProperty(test2Property, context2)).isNull();
        assertThat(filter.filterProperty(test3Property, context3)).isEqualTo(test3Property);
    }

    @Test
    public void testClearFilters() throws Exception {
        ThreadFilterContext filter = new ThreadFilterContext();
        RegexPropertyFilter regexFilter = new RegexPropertyFilter();
        regexFilter.setIncludes("test1.*");
        Map<String,String> map = new HashMap<>();
        map.put("test1", "test1");
        map.put("test2", "test2");
        map.put("test.test3", "test.test3");

        FilterContext context1 = new FilterContext(test1Property, context);
        FilterContext context2 = new FilterContext(test2Property, context);
        FilterContext context3 = new FilterContext(test3Property, context);
        assertThat(filter.filterProperty(test1Property, context1)).isEqualTo(test1Property);
        assertThat(filter.filterProperty(test2Property, context2)).isEqualTo(test2Property);
        assertThat(filter.filterProperty(test3Property, context3)).isEqualTo(test3Property);
        filter.addFilter(regexFilter);
        assertThat(filter.filterProperty(test1Property, context1)).isEqualTo(test1Property);
        assertThat(filter.filterProperty(test2Property, context2)).isNull();
        assertThat(filter.filterProperty(test3Property, context3)).isNull();
        filter.clearFilters();
        assertThat(filter.filterProperty(test1Property, context1)).isEqualTo(test1Property);
        assertThat(filter.filterProperty(test2Property, context2)).isEqualTo(test2Property);
        assertThat(filter.filterProperty(test3Property, context3)).isEqualTo(test3Property);
    }

    @Test
    public void testSetFilters() throws Exception {
        ThreadFilterContext filter = new ThreadFilterContext();
        RegexPropertyFilter regexFilter = new RegexPropertyFilter();
        regexFilter.setIncludes("test\\..*");
        Map<String,PropertyValue> map = new HashMap<>();
        map.put("test1", test1Property);
        map.put("test2", test1Property);
        map.put("test.test3", test3Property);

        FilterContext context1 = new FilterContext(test1Property, map, context);
        FilterContext context2 = new FilterContext(test2Property, map, context);
        FilterContext context3 = new FilterContext(test3Property, map, context);
        assertThat(filter.filterProperty(test1Property, context1)).isEqualTo(test1Property);
        assertThat(filter.filterProperty(test2Property, context2)).isEqualTo(test2Property);
        assertThat(filter.filterProperty(test3Property, context3)).isEqualTo(test3Property);
        assertThat(filter.filterProperty(test3Property, context1)).isEqualTo(test3Property);
    }

    @Test
    public void testSetFilters1() throws Exception {
        ThreadFilterContext filter = new ThreadFilterContext();
        RegexPropertyFilter regexFilter = new RegexPropertyFilter();
        regexFilter.setIncludes("test1.*");

        FilterContext context1 = new FilterContext(test1Property, context);
        FilterContext context2 = new FilterContext(test2Property, context);
        FilterContext context3 = new FilterContext(test3Property, context);
        assertThat(filter.filterProperty(test1Property, context1)).isEqualTo(test1Property);
        assertThat(filter.filterProperty(test2Property, context2)).isEqualTo(test2Property);
        assertThat(filter.filterProperty(test3Property, context3)).isEqualTo(test3Property);
        filter.setFilters(Arrays.asList(new PropertyFilter[]{regexFilter}));
        assertThat(filter.filterProperty(test1Property, context1)).isEqualTo(test1Property);
        assertThat(filter.filterProperty(test2Property, context2)).isNull();
        assertThat(filter.filterProperty(test3Property, context3)).isNull();
    }

    @Test
    public void testGetFilters() throws Exception {
        ThreadFilterContext filter = new ThreadFilterContext();
        assertThat(filter.getFilters()).isNotNull().isEmpty();
        RegexPropertyFilter regexFilter = new RegexPropertyFilter();
        regexFilter.setIncludes("test\\..*");
        filter.addFilter(regexFilter);
        assertThat(filter.getFilters()).isNotNull().isNotEmpty().hasSize(1).contains(regexFilter);
    }

    @Test
    public void testToString() throws Exception {
        ThreadFilterContext filter = new ThreadFilterContext();
        assertThat(filter.toString()).contains("ProgrammableFilter")
            .doesNotContain("RegexPropertyFilter").doesNotContain("test\\..*");
        RegexPropertyFilter regexFilter = new RegexPropertyFilter();
        regexFilter.setIncludes("test\\..*");
        filter.addFilter(regexFilter);
        assertThat(filter.toString()).contains("ProgrammableFilter")
            .contains("RegexPropertyFilter").contains("test\\..*");
    }
}
