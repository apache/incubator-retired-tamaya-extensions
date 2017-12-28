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

import org.apache.tamaya.base.configsource.EnvironmentConfigSource;
import org.apache.tamaya.base.configsource.SimpleConfigSource;
import org.apache.tamaya.base.configsource.SystemConfigSource;
import org.junit.Test;

import javax.config.spi.ConfigSource;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests for {@link ConfigSourceChange} and its builder.
 */
public class ConfigSourceChangeTest {

    private static final ConfigSource myPS = new SystemConfigSource();

    @Test
    public void testGetPropertySource() throws Exception {
        ConfigSourceChange change = ConfigSourceChangeBuilder.of(myPS).build();
        assertEquals(change.getResource().getName(), myPS.getName());
    }

    @Test
    public void testGetVersion() throws Exception {
        ConfigSourceChange change = ConfigSourceChangeBuilder.of(myPS)
                .setVersion("myVersion1").build();
        assertEquals(change.getVersion(), "myVersion1");
    }

    @Test
    public void testGetTimestamp() throws Exception {
        ConfigSourceChange change = ConfigSourceChangeBuilder.of(myPS)
                .setTimestamp(111L).build();
        assertEquals(change.getTimestamp(), 111L);
    }

    @Test
    public void testGetEvents() throws Exception {
        ConfigSourceChange change = ConfigSourceChangeBuilder.of(myPS)
                .addChanges(
                        new EnvironmentConfigSource()
                ).build();
        assertTrue(change.getChanges().size()>0);
    }

    @Test
    public void testGetRemovedSize() throws Exception {
        ConfigSourceChange change = ConfigSourceChangeBuilder.of(myPS)
                .addChanges(
                        new EnvironmentConfigSource()
                ).build();
        assertTrue(change.getRemovedSize()>0);
    }

    @Test
    public void testGetAddedSize() throws Exception {
        ConfigSourceChange change = ConfigSourceChangeBuilder.of(myPS)
                .addChanges(
                        new EnvironmentConfigSource()
                ).build();
        assertTrue(change.getAddedSize()>0);
    }

    @Test
    public void testGetUpdatedSize() throws Exception {
        ConfigSourceChange change = ConfigSourceChangeBuilder.of(myPS)
                .addChanges(
                        new EnvironmentConfigSource()
                ).build();
        assertTrue(change.getUpdatedSize()==0);
    }

    @Test
    public void testIsRemoved() throws Exception {
        Map<String, String> testData = new HashMap<>();
        testData.put("key1", "value1");
        testData.put("key2", "value2");
        SimpleConfigSource ps1 = new SimpleConfigSource("test", testData);
        testData = new HashMap<>();
        testData.put("key1", "value2");
        testData.put("key3", "value3");
        SimpleConfigSource ps2 = new SimpleConfigSource("test", testData);
        ConfigSourceChange change = ConfigSourceChangeBuilder.of(ps1)
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
        ConfigSource ps1 = new SimpleConfigSource("test", testData);
        testData = new HashMap<>();
        testData.put("key1", "value2");
        testData.put("key3", "value3");
        ConfigSource ps2 = new SimpleConfigSource("test", testData);
        ConfigSourceChange change = ConfigSourceChangeBuilder.of(ps1)
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
        SimpleConfigSource ps1 = new SimpleConfigSource("test", testData);
        testData = new HashMap<>();
        testData.put("key1", "value2");
        testData.put("key3", "value3");
        SimpleConfigSource ps2 = new SimpleConfigSource("test", testData);
        ConfigSourceChange change = ConfigSourceChangeBuilder.of(ps1)
                .addChanges(
                        ps2
                ).build();
        assertTrue(change.isUpdated("key1"));
        assertFalse(change.isUpdated("key2"));
        assertFalse(change.isUpdated("key3"));
    }

    @Test
    public void testContainsKey() throws Exception {
        ConfigSourceChange change = ConfigSourceChangeBuilder.of(new EnvironmentConfigSource())
                .addChanges(
                        myPS
                ).build();
        assertTrue(change.isKeyAffected("java.version"));
    }

    @Test
    public void testIsEmpty() throws Exception {
        ConfigSourceChange change = ConfigSourceChangeBuilder.of(new EnvironmentConfigSource())
                .build();
        assertTrue(change.isEmpty());
        change = ConfigSourceChangeBuilder.of(new EnvironmentConfigSource())
                .addChanges(
                        myPS
                ).build();
        assertFalse(change.isEmpty());
    }

    @Test
    public void testToString() throws Exception {
        ConfigSourceChange change = ConfigSourceChangeBuilder.of(myPS).build();
        String toString = change.toString();
        assertNotNull(toString);
        assertTrue(toString.contains(myPS.getName()));
    }
}