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

import org.apache.tamaya.filter.ConfigurationFilter;
import org.apache.tamaya.spi.ConfigValue;
import org.apache.tamaya.spi.Filter;
import org.junit.Test;

import javax.config.Config;
import javax.config.ConfigProvider;

import static org.junit.Assert.*;

/**
 * Tests for {@link ConfigurationFilter}. Created by atsticks on 11.02.16.
 */
public class ConfigurationFilterTest {

    @Test
    public void testMetadataFiltered() throws Exception {
        ConfigurationFilter.setMetadataFiltered(true);
        assertTrue(ConfigurationFilter.isMetadataFiltered());
        ConfigurationFilter.setMetadataFiltered(false);
        assertFalse(ConfigurationFilter.isMetadataFiltered());
    }

    @Test
    public void testGetFilters() throws Exception {
        Config config = ConfigProvider.getConfig();
        assertNotNull(ConfigurationFilter.getFilters());
        Filter testFilter = (k,v) -> k + ":testGetSingleFilters";
        ConfigurationFilter.addFilter(testFilter);
        assertTrue(ConfigurationFilter.getFilters().contains(testFilter));
    }

    @Test
    public void testFiltering() throws Exception {
        Config config = ConfigProvider.getConfig();
        Filter testFilter = (k,v) -> k + ":testGetMapFilters";
        ConfigurationFilter.addFilter(testFilter);
        assertEquals("user.home:testGetMapFilters", config.getValue("user.home", String.class));
        ConfigurationFilter.removeFilter(testFilter);
        assertNotSame("user.home:testGetSingleFilters", config.getValue("user.home", String.class));
    }

    @Test
    public void testRemove() throws Exception {
        Config config = ConfigProvider.getConfig();
        Filter testFilter = (k,v) -> k + ":testGetMapFilters";
        ConfigurationFilter.addFilter(testFilter);
        assertTrue(ConfigurationFilter.getFilters().contains(testFilter));
        ConfigurationFilter.removeFilter(testFilter);
        assertFalse(ConfigurationFilter.getFilters().contains(testFilter));
    }

    @Test
    public void testRemoveFilterAt0() throws Exception {
        Config config = ConfigProvider.getConfig();
        Filter testFilter = (k,v) -> k + ":testGetMapFilters";
        ConfigurationFilter.addFilter(testFilter);
        assertTrue(ConfigurationFilter.getFilters().contains(testFilter));
        ConfigurationFilter.removeFilter(0);
        assertFalse(ConfigurationFilter.getFilters().contains(testFilter));
    }

    @Test
    public void testClearFilters() throws Exception {
        Config config = ConfigProvider.getConfig();
        Filter testFilter = (k,v) -> k + ":testGetSingleFilters";
        ConfigurationFilter.addFilter(testFilter);
        assertTrue(ConfigurationFilter.getFilters().contains(testFilter));
        assertEquals("user.home:testGetSingleFilters", config.getValue("user.home", String.class));
        ConfigurationFilter.cleanupFilterContext();
        assertFalse(ConfigurationFilter.getFilters().contains(testFilter));
        assertNotSame("user.home:testGetSingleFilters", config.getValue("user.home", String.class));
    }

}