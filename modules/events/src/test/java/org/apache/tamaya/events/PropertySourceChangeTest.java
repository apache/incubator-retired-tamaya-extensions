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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spisupport.propertysource.EnvironmentPropertySource;
import org.apache.tamaya.spisupport.propertysource.MapPropertySource;
import org.apache.tamaya.spisupport.propertysource.SimplePropertySource;
import org.apache.tamaya.spisupport.propertysource.SystemPropertySource;
import org.junit.Test;
/**
 * Tests for {@link PropertySourceChange} and its builder.
 */
public class PropertySourceChangeTest {

    private static final PropertySource MY_PS = new SystemPropertySource();

    @Test
    public void testGetPropertySource() throws Exception {
        PropertySourceChange change = PropertySourceChangeBuilder.of(MY_PS).build();
        assertThat(change.getResource().getName()).isEqualTo(MY_PS.getName());
    }

    @Test
    public void testGetVersion() throws Exception {
        PropertySourceChange change = PropertySourceChangeBuilder.of(MY_PS)
                .setVersion("myVersion1").build();
        assertThat(change.getVersion()).isEqualTo("myVersion1");
    }

    @Test
    public void testGetTimestamp() throws Exception {
        PropertySourceChange change = PropertySourceChangeBuilder.of(MY_PS)
                .setTimestamp(111L).build();
        assertThat(change.getTimestamp()).isEqualTo(111L);
    }

    @Test
    public void testGetEvents() throws Exception {
        PropertySourceChange change = PropertySourceChangeBuilder.of(MY_PS)
                .addChanges(
                        new EnvironmentPropertySource()
                ).build();
        assertThat(change.getChanges()).isNotEmpty();
    }

    @Test
    public void testGetRemovedSize() throws Exception {
        PropertySourceChange change = PropertySourceChangeBuilder.of(MY_PS)
                .addChanges(
                        new EnvironmentPropertySource()
                ).build();
        assertThat(change.getRemovedSize() > 0).isTrue();
    }

    @Test
    public void testGetAddedSize() throws Exception {
        PropertySourceChange change = PropertySourceChangeBuilder.of(MY_PS)
                .addChanges(
                        new EnvironmentPropertySource()
                ).build();
        assertThat(change.getAddedSize() > 0).isTrue();
    }

    @Test
    public void testGetUpdatedSizeNoUpdates() throws Exception {
        Map<String, String> addableMap = new HashMap<>();

        addableMap.put("NonOverridingValue", "someValue");

        PropertySourceChange change = PropertySourceChangeBuilder.of(MY_PS)
                .addChanges(
                        // java.home and JAVA_HOME will often override each
                        //  other, so stay away from EnvironmentPropertySource
                        new SystemPropertySource()
                )
                .addChanges(
                        new MapPropertySource("addableMap", addableMap)
                )
                .build();
        assertThat(change.getUpdatedSize()).isZero();
    }

    @Test
    public void testGetUpdatedSizeWithUpdates() throws Exception {
        Map<String, String> addableMap = new HashMap<>();
        addableMap.put("java.home", "/new/java/home/createValue");

        PropertySourceChange change = PropertySourceChangeBuilder.of(MY_PS)
                .addChanges(
                        new EnvironmentPropertySource()
                )
                .addChanges(
                        new MapPropertySource("addableMap", addableMap)
                )
                .build();
        assertThat(change.getUpdatedSize() > 0).isTrue();
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
        assertThat(change.isRemoved("key1")).isFalse();
        assertThat(change.isRemoved("key2")).isTrue();
        assertThat(change.isRemoved("key3")).isFalse();
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
        assertThat(change.isAdded("key3")).isTrue();
        assertThat(change.isAdded("key2")).isFalse();
        assertThat(change.isAdded("key1")).isFalse();
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
        assertThat(change.isUpdated("key1")).isTrue();
        assertThat(change.isUpdated("key2")).isFalse();
        assertThat(change.isUpdated("key3")).isFalse();
    }

    @Test
    public void testContainsKey() throws Exception {
        PropertySourceChange change = PropertySourceChangeBuilder.of(new EnvironmentPropertySource())
                .addChanges(
                        MY_PS
                ).build();
        assertThat(change.isKeyAffected("java.version")).isTrue();
    }

    @Test
    public void testIsEmpty() throws Exception {
        PropertySourceChange change = PropertySourceChangeBuilder.of(new EnvironmentPropertySource())
                .build();
        assertThat(change.isEmpty()).isTrue();
        change = PropertySourceChangeBuilder.of(new EnvironmentPropertySource())
                .addChanges(
                        MY_PS
                ).build();
        assertThat(change.isEmpty()).isFalse();
    }

    @Test
    public void testToString() throws Exception {
        PropertySourceChange change = PropertySourceChangeBuilder.of(MY_PS).build();
        String toString = change.toString();
        assertThat(toString).isNotNull().contains(MY_PS.getName());
    }
}
