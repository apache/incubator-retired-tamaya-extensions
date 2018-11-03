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

import org.apache.tamaya.Configuration;
import org.apache.tamaya.spi.PropertyFilter;
import org.apache.tamaya.spi.PropertyValue;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link ThreadBasedConfigurationFilter}. Created by atsticks on 11.02.16.
 */
public class ConfigurationFilterTest {

    @Test
    public void testMetadataFiltered() throws Exception {
        ThreadBasedConfigurationFilter.setMetadataFiltered(true);
        assertTrue(ThreadBasedConfigurationFilter.isMetadataFiltered());
        ThreadBasedConfigurationFilter.setMetadataFiltered(false);
        assertFalse(ThreadBasedConfigurationFilter.isMetadataFiltered());
    }

    @Test
    public void testGetSingleFilters() throws Exception {
        Configuration config = Configuration.current();
        assertNotNull(ThreadBasedConfigurationFilter.getSingleValueFilterContext());
        PropertyFilter testFilter = new PropertyFilter() {
            @Override
            public PropertyValue filterProperty(PropertyValue value) {
                return value.setValue(value.getKey() + ":testGetSingleFilters");
            }
        };
        ThreadBasedConfigurationFilter.getSingleValueFilterContext().addFilter(testFilter);
        assertEquals("user.home:testGetSingleFilters", config.get("user.home"));
        ThreadBasedConfigurationFilter.getSingleValueFilterContext().removeFilter(testFilter);
        assertNotSame("user.home:testGetSingleFilters", config.get("user.home"));
    }

    @Test
    public void testRemoveSingleFiltersAt0() throws Exception {
        Configuration config = Configuration.current();
        assertNotNull(ThreadBasedConfigurationFilter.getSingleValueFilterContext());
        PropertyFilter testFilter = new PropertyFilter() {
            @Override
            public PropertyValue filterProperty(PropertyValue value) {
                return value.setValue(value.getKey() + ":testGetSingleFilters");
            }
        };
        ThreadBasedConfigurationFilter.getSingleValueFilterContext().addFilter(testFilter);
        assertEquals("user.home:testGetSingleFilters", config.get("user.home"));
        ThreadBasedConfigurationFilter.getSingleValueFilterContext().removeFilter(0);
        assertNotSame("user.home:testGetSingleFilters", config.get("user.home"));
    }

    @Test
    public void testGetMapFilters() throws Exception {
        Configuration config = Configuration.current();
        assertNotNull(ThreadBasedConfigurationFilter.getMapFilterContext());
        PropertyFilter testFilter = new PropertyFilter() {
            @Override
            public PropertyValue filterProperty(PropertyValue value) {
                return value.setValue(value.getKey() + ":testGetMapFilters");
            }
        };
        ThreadBasedConfigurationFilter.getMapFilterContext().addFilter(testFilter);
        assertEquals("user.home:testGetMapFilters", config.getProperties().get("user.home"));
        ThreadBasedConfigurationFilter.getSingleValueFilterContext().removeFilter(testFilter);
        assertNotSame("user.home:testGetSingleFilters", config.getProperties().get("user.home"));
    }

    @Test
    public void testRemoveMapFilterAt0() throws Exception {
        Configuration config = Configuration.current();
        assertNotNull(ThreadBasedConfigurationFilter.getMapFilterContext());
        PropertyFilter testFilter = new PropertyFilter() {
            @Override
            public PropertyValue filterProperty(PropertyValue value) {
                return value.setValue(value.getKey() + ":testGetMapFilters");
            }
        };
        ThreadBasedConfigurationFilter.getMapFilterContext().addFilter(testFilter);
        assertEquals("user.home:testGetMapFilters", config.getProperties().get("user.home"));
        ThreadBasedConfigurationFilter.getMapFilterContext().removeFilter(0);
        assertNotSame("user.home:testGetSingleFilters", config.getProperties().get("user.home"));
    }

    @Test
    public void testClearFilters() throws Exception {
        Configuration config = Configuration.current();
        assertNotNull(ThreadBasedConfigurationFilter.getSingleValueFilterContext());
        PropertyFilter testFilter = new PropertyFilter() {
            @Override
            public PropertyValue filterProperty(PropertyValue value) {
                return value.setValue(value.getKey() + ":testGetSingleFilters");
            }
        };
        ThreadBasedConfigurationFilter.getSingleValueFilterContext().addFilter(testFilter);
        assertEquals("user.home:testGetSingleFilters", config.get("user.home"));
        ThreadBasedConfigurationFilter.cleanupFilterContext();
        assertNotSame("user.home:testGetSingleFilters", config.get("user.home"));
    }

}