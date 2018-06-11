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
package org.apache.tamaya.events;

import org.apache.tamaya.spisupport.propertysource.EnvironmentPropertySource;
import org.apache.tamaya.spisupport.propertysource.SimplePropertySource;
import org.apache.tamaya.spisupport.propertysource.SystemPropertySource;
import org.apache.tamaya.spisupport.propertysource.MapPropertySource;
import org.apache.tamaya.spi.PropertySource;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests for {@link PropertySourceChange} and its builder.
 */
public class PropertySourceChangeTest {

    private static final PropertySource myPS = new SystemPropertySource();

    @Test
    public void testGetPropertySource() throws Exception {
        PropertySourceChange change = PropertySourceChangeBuilder.of(myPS).build();
        assertEquals(change.getResource().getName(), myPS.getName());
    }

    @Test
    public void testGetVersion() throws Exception {
        PropertySourceChange change = PropertySourceChangeBuilder.of(myPS)
                .setVersion("myVersion1").build();
        assertEquals(change.getVersion(), "myVersion1");
    }

    @Test
    public void testGetTimestamp() throws Exception {
        PropertySourceChange change = PropertySourceChangeBuilder.of(myPS)
                .setTimestamp(111L).build();
        assertEquals(change.getTimestamp(), 111L);
    }

    @Test
    public void testGetEvents() throws Exception {
        PropertySourceChange change = PropertySourceChangeBuilder.of(myPS)
                .addChanges(
                        new EnvironmentPropertySource()
                ).build();
        assertTrue(change.getChanges().size() > 0);
    }

    @Test
    public void testGetRemovedSize() throws Exception {
        PropertySourceChange change = PropertySourceChangeBuilder.of(myPS)
                .addChanges(
                        new EnvironmentPropertySource()
                ).build();
        assertTrue(change.getRemovedSize() > 0);
    }

    @Test
    public void testGetAddedSize() throws Exception {
        PropertySourceChange change = PropertySourceChangeBuilder.of(myPS)
                .addChanges(
                        new EnvironmentPropertySource()
                ).build();
        assertTrue(change.getAddedSize() > 0);
    }

    @Test
    public void testGetUpdatedSizeNoUpdates() throws Exception {
        Map<String, String> addableMap = new HashMap<>();
        
        addableMap.put("NonOverridingValue", "someValue");

        PropertySourceChange change = PropertySourceChangeBuilder.of(myPS)
                .addChanges(
                        //java.home and JAVA_HOME will often override each
                        //  other, so stay away from EnvironmentPropertySource
                        new SystemPropertySource()
                )
                .addChanges(
                        new MapPropertySource("addableMap", addableMap)
                )
                .build();
        assertTrue(change.getUpdatedSize() == 0);
    }

    @Test
    public void testGetUpdatedSizeWithUpdates() throws Exception {
        Map<String, String> addableMap = new HashMap<>();
        addableMap.put("java.home", "/new/java/home/value");

        PropertySourceChange change = PropertySourceChangeBuilder.of(myPS)
                .addChanges(
                        new EnvironmentPropertySource()
                )
                .addChanges(
                        new MapPropertySource("addableMap", addableMap)
                )
                .build();
        assertTrue(change.getUpdatedSize() > 0);
    }

    @Test
    public void testIsRemoved() throws Exception {
        Map<String, String> testData = new HashMap<>();
        testData.put("key1", "value1");
        testData.put("key2", "value2");
        PropertySource ps1 = new SimplePropertySource("test", testData);
        testData = new HashMap<>();
        testData.put("key1", "value2");
        testData.put("key3", "value3");
        PropertySource ps2 = new SimplePropertySource("test", testData);
        PropertySourceChange change = PropertySourceChangeBuilder.of(ps1)
                .addChanges(
                        ps2
                ).build();
        assertFalse(change.isRemoved("key1"));
        assertTrue(change.isRemoved("key2"));
        assertFalse(change.isRemoved("key3"));
    }

    @Test
    public void testIsAdded() throws Exception {
        Map<String, String> testData = new HashMap<>();
        testData.put("key1", "value1");
        testData.put("key2", "value2");
        PropertySource ps1 = new SimplePropertySource("test", testData);
        testData = new HashMap<>();
        testData.put("key1", "value2");
        testData.put("key3", "value3");
        PropertySource ps2 = new SimplePropertySource("test", testData);
        PropertySourceChange change = PropertySourceChangeBuilder.of(ps1)
                .addChanges(
                        ps2
                ).build();
        assertTrue(change.isAdded("key3"));
        assertFalse(change.isAdded("key2"));
        assertFalse(change.isAdded("key1"));
    }

    @Test
    public void testIsUpdated() throws Exception {
        Map<String, String> testData = new HashMap<>();
        testData.put("key1", "value1");
        testData.put("key2", "value2");
        PropertySource ps1 = new SimplePropertySource("test", testData);
        testData = new HashMap<>();
        testData.put("key1", "value2");
        testData.put("key3", "value3");
        PropertySource ps2 = new SimplePropertySource("test", testData);
        PropertySourceChange change = PropertySourceChangeBuilder.of(ps1)
                .addChanges(
                        ps2
                ).build();
        assertTrue(change.isUpdated("key1"));
        assertFalse(change.isUpdated("key2"));
        assertFalse(change.isUpdated("key3"));
    }

    @Test
    public void testContainsKey() throws Exception {
        PropertySourceChange change = PropertySourceChangeBuilder.of(new EnvironmentPropertySource())
                .addChanges(
                        myPS
                ).build();
        assertTrue(change.isKeyAffected("java.version"));
    }

    @Test
    public void testIsEmpty() throws Exception {
        PropertySourceChange change = PropertySourceChangeBuilder.of(new EnvironmentPropertySource())
                .build();
        assertTrue(change.isEmpty());
        change = PropertySourceChangeBuilder.of(new EnvironmentPropertySource())
                .addChanges(
                        myPS
                ).build();
        assertFalse(change.isEmpty());
    }

    @Test
    public void testToString() throws Exception {
        PropertySourceChange change = PropertySourceChangeBuilder.of(myPS).build();
        String toString = change.toString();
        assertNotNull(toString);
        assertTrue(toString.contains(myPS.getName()));
    }
}
