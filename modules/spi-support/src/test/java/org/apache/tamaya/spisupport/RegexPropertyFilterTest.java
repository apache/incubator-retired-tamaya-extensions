/*
 * Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.tamaya.spisupport;

import org.apache.tamaya.spi.FilterContext;
import org.apache.tamaya.spi.PropertyValue;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests for {@link RegexPropertyFilter}. Created by anatole on 11.02.16.
 */
public class RegexPropertyFilterTest {

    @org.junit.Test
    public void testFilterProperty() throws Exception {
        RegexPropertyFilter filter = new RegexPropertyFilter();
        filter.setIncludes("test1.*");
        Map<String,PropertyValue> map = new HashMap<>();
        map.put("test1", PropertyValue.of("test1", "test1", "test"));
        map.put("test2", PropertyValue.of("test2", "test2", "test"));
        map.put("test1.test3", PropertyValue.of("test1.test3", "test.test3", "test"));
        assertEquals(filter.filterProperty(PropertyValue.of("test1.", "test1", "test"), new FilterContext("test1.", map)).getValue(), "test1");
        assertNull(filter.filterProperty(PropertyValue.of("test2", "test2", "test"), new FilterContext("test2.", map)));
        assertEquals(filter.filterProperty(
                PropertyValue.of("test1.test3", "testx.test3", "test"),
                new FilterContext("test1.test3", map)).getValue(), "testx.test3");
        assertEquals(filter.filterProperty(
                PropertyValue.of("test1.test3", "testx.test3", "test"),
                new FilterContext("test1.test3", map)).getValue(), "testx.test3");
        filter = new RegexPropertyFilter();
        filter.setIncludes("test1.*");
        assertNotNull(filter.filterProperty(PropertyValue.of("test1", "test1", "test"), new FilterContext("test1", map)));
        assertNull(filter.filterProperty(PropertyValue.of("test2", "test2", "test"), new FilterContext("test2", map)));
        assertNull(filter.filterProperty(PropertyValue.of("test.test3", "test1", "test"), new FilterContext("test.test3", map)));
    }

    @org.junit.Test
    public void testToString() throws Exception {
        RegexPropertyFilter filter = new RegexPropertyFilter();
        filter.setIncludes("test\\..*");
        assertTrue(filter.toString().contains("test\\..*"));
        assertTrue(filter.toString().contains("RegexPropertyFilter"));
    }
}