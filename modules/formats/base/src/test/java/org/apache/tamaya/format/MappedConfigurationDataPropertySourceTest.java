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
package org.apache.tamaya.format;

import org.apache.tamaya.format.formats.PropertiesFormat;
import org.apache.tamaya.spi.PropertySource;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Tests for {@link MappedConfigurationDataPropertySource}.
 */
public class MappedConfigurationDataPropertySourceTest {

    @Test
    public void testGetName() throws Exception {
        MappedConfigurationDataPropertySource ps = new MappedConfigurationDataPropertySource(createConfigurationData("test1"));
        assertEquals("test1", ps.getName());
    }

    private ConfigurationData createConfigurationData(String sourceName) {
        return ConfigurationDataBuilder.of(sourceName, new PropertiesFormat())
                .addDefaultProperty("a", "aValue").addSectionProperty("section1", "sectionKey1", "sectionValue11")
                .addSections("section1", "section12")
                .addSectionProperty("section2", "sectionKey1", "sectionValue21").build();
    }

    private ConfigurationData createConfigurationData(String sourceName, int ordinal) {
        return ConfigurationDataBuilder.of(sourceName, new PropertiesFormat())
                .addDefaultProperty("a", "aValue").addSectionProperty("section1", "sectionKey1", "sectionValue11")
                .addSections("section1", "section12").addDefaultProperty(PropertySource.TAMAYA_ORDINAL, String.valueOf(ordinal))
                .addSectionProperty("section2", "sectionKey1", "sectionValue21").build();
    }

    private ConfigurationData createConfigurationDataNoDefault(String sourceName) {
        return ConfigurationDataBuilder.of(sourceName, new PropertiesFormat())
                .addSectionProperty("section1", "sectionKey1", "sectionValue11")
                .addSections("section1", "section12")
                .addSectionProperty("section2", "sectionKey1", "sectionValue21").build();
    }

    @Test
    public void testGetOrdinal() throws Exception {
        MappedConfigurationDataPropertySource ps = new MappedConfigurationDataPropertySource(createConfigurationData("test1", 11));
        assertEquals(11, ps.getOrdinal());
    }

    @Test
    public void testGet() throws Exception {
        MappedConfigurationDataPropertySource ps = new MappedConfigurationDataPropertySource(createConfigurationData("test2"));
        assertEquals("aValue", ps.get("a").getValue());
        assertNotNull(ps.get("section1.sectionKey1").getValue());
        assertNotNull(ps.get("section2.sectionKey1").getValue());
        assertNull(ps.get("sectionKey1"));
        ps = new MappedConfigurationDataPropertySource(createConfigurationDataNoDefault("test2"));
        assertEquals("sectionValue11", ps.get("section1.sectionKey1").getValue());
        assertEquals("sectionValue21", ps.get("section2.sectionKey1").getValue());
        assertNull(ps.get("a"));
        assertNull(ps.get("section1"));
    }

    @Test
    public void testGetProperties() throws Exception {
        MappedConfigurationDataPropertySource ps = new MappedConfigurationDataPropertySource(createConfigurationData("test3"));
        assertNotNull(ps.getProperties());
        assertEquals("aValue", ps.getProperties().get("a").getValue());
        assertNotNull(ps.getProperties().get("section1.sectionKey1"));
        assertNotNull(ps.getProperties().get("section2.sectionKey1"));
        assertNull(ps.getProperties().get("section1.sectionKey2"));
        assertNull(ps.getProperties().get("section2.sectionKey2"));
        assertNull(ps.getProperties().get("sectionKey1"));
        assertNull(ps.getProperties().get("sectionKey2"));
        ps = new MappedConfigurationDataPropertySource(createConfigurationDataNoDefault("test3"));
        assertNotNull(ps.getProperties());
        assertEquals("sectionValue11", ps.getProperties().get("section1.sectionKey1").getValue());
        assertEquals("sectionValue21", ps.getProperties().get("section2.sectionKey1").getValue());
        assertNull(ps.get("section1"));
    }
}