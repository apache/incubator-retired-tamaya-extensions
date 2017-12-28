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

import org.junit.Test;

import javax.config.Config;
import javax.config.ConfigProvider;

import static org.junit.Assert.*;


/**
 * Test class for {@link ConfigChange}.
 */
public class ConfigChangeTest {

    @Test
    public void testEmptyChangeSet() throws Exception {
        ConfigChange change = ConfigChange.emptyChangeSet(ConfigProvider.getConfig());
        assertNotNull(change);
        assertTrue(change.getChanges().isEmpty());
    }

    @Test
    public void testGetConfig() throws Exception {
        Config config = ConfigProvider.getConfig();
        ConfigChange change = ConfigChangeBuilder.of(config).build();
        assertNotNull(change);
        assertTrue(change.getUpdatedSize()==0);
        assertTrue(change.getAddedSize()==0);
        assertTrue(change.getRemovedSize()==0);
        assertTrue(change.getChanges().size()==0);
        for (String key : config.getPropertyNames()) {
            if (!"[meta]frozenAt".equals(key)) {
                if(key.contains("random.new")){ // dynamic generated value!
                    continue;
                }
            }
        }
    }

    @Test
    public void testGetVersion() throws Exception {
        Config config = ConfigProvider.getConfig();
        ConfigChange change = ConfigChangeBuilder.of(config).build();
        assertNotNull(change.getVersion());
        change = ConfigChangeBuilder.of(config).setVersion("version2").build();
        assertEquals("version2", change.getVersion());
    }

    @Test
    public void testGetTimestamp() throws Exception {
        Config config = ConfigProvider.getConfig();
        Thread.sleep(10L);
        ConfigChange change = ConfigChangeBuilder.of(config).build();
        assertTrue((System.currentTimeMillis() - change.getTimestamp()) > 0L);
        change = ConfigChangeBuilder.of(config).setTimestamp(10L).build();
        assertEquals(10L, change.getTimestamp());
    }

    @Test
    public void testGetEvents() throws Exception {
        Config config = ConfigProvider.getConfig();
        ConfigChange change = ConfigChangeBuilder.of(config).removeKey("key1", "key2").build();
        assertTrue(change.getChanges().size() == 2);
        change = ConfigChangeBuilder.of(config).addChange("key1Added", "value1Added").build();
        assertTrue(change.getChanges().size() == 1);
    }

    @Test
    public void testGetRemovedSize() throws Exception {
        Config config = ConfigProvider.getConfig();
        ConfigChange change = ConfigChangeBuilder.of(config).removeKey("java.version", "key2").build();
        assertTrue(change.getRemovedSize() == 2);
        assertTrue(change.getAddedSize() == 0);
    }

    @Test
    public void testGetAddedSize() throws Exception {
        Config config = ConfigProvider.getConfig();
        ConfigChange change = ConfigChangeBuilder.of(config).addChange("key1", "key2").build();
        assertTrue(change.getAddedSize() == 1);
        assertTrue(change.getRemovedSize() == 0);
    }

    @Test
    public void testGetUpdatedSize() throws Exception {
        Config config = ConfigProvider.getConfig();
        ConfigChange change = ConfigChangeBuilder.of(config).addChange("java.version", "1.8").build();
        assertTrue(change.getUpdatedSize() == 1);
    }

    @Test
    public void testIsRemoved() throws Exception {
        Config config = ConfigProvider.getConfig();
        ConfigChange change = ConfigChangeBuilder.of(config).removeKey("java.version").build();
        assertTrue(change.isRemoved("java.version"));
    }

    @Test
    public void testIsAdded() throws Exception {
        Config config = ConfigProvider.getConfig();
        ConfigChange change = ConfigChangeBuilder.of(config).addChange("key1", "key2").build();
        assertTrue(change.isAdded("key1"));
    }

    @Test
    public void testIsUpdated() throws Exception {
        Config config = ConfigProvider.getConfig();
        ConfigChange change = ConfigChangeBuilder.of(config).addChange("java.version", "1.8").build();
        assertTrue(change.isUpdated("java.version"));
    }

    @Test
    public void testContainsKey() throws Exception {
        Config config = ConfigProvider.getConfig();
        ConfigChange change = ConfigChangeBuilder.of(config).addChange("key1", "key2").build();
        assertTrue(change.isKeyAffected("key1"));
        assertFalse(change.isKeyAffected("key2"));
        change = ConfigChangeBuilder.of(config).removeKey("java.version").build();
        assertFalse(change.isKeyAffected("java.version"));
        assertFalse(change.isKeyAffected("key2"));
    }

    @Test
    public void testIsEmpty() throws Exception {
        Config config = ConfigProvider.getConfig();
        ConfigChange change = ConfigChangeBuilder.of(config).build();
        assertTrue(change.isEmpty());
    }

    @Test
    public void testToString() throws Exception {
        Config config = ConfigProvider.getConfig();
        ConfigChange change = ConfigChangeBuilder.of(config).removeKey("java.version").build();
        String toString =
                change.toString();
        assertTrue(toString.contains("timestamp"));
        assertTrue(toString.contains("change-id"));
        assertTrue(toString.contains("config-id"));
        assertFalse(toString.contains("key1"));
        assertFalse(toString.contains("key2"));
    }
}