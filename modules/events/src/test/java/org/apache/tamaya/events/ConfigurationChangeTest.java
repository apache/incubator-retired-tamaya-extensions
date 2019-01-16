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

import org.apache.tamaya.Configuration;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for {@link ConfigurationChange}.
 */
public class ConfigurationChangeTest {

    @Test
    public void testEmptyChangeSet() throws Exception {
        ConfigurationChange change = ConfigurationChange.emptyChangeSet(Configuration.current());
        assertThat(change).isNotNull();
        assertThat(change.getChanges()).isEmpty();
    }

    @Test
    public void testGetConfiguration() throws Exception {
        Configuration config = Configuration.current();
        ConfigurationChange change = ConfigurationChangeBuilder.of(config).build();
        assertThat(change).isNotNull();
        assertThat(change.getUpdatedSize()).isEqualTo(0);
        assertThat(change.getAddedSize()).isEqualTo(0);
        assertThat(change.getRemovedSize()).isEqualTo(0);
        assertThat(change.getChanges().size()).isEqualTo(0);
        for (Map.Entry<String, String> en : config.getProperties().entrySet()) {
            if (!"[meta]frozenAt".equals(en.getKey())) {
                if(en.getKey().contains("random.new")){ // dynamic generated value!
                    continue;
                }
                assertThat(en.getValue()).describedAs("Error for " + en.getKey())
                    .isEqualTo(change.getResource().get(en.getKey()));
            }
        }
    }

    @Test
    public void testGetVersion() throws Exception {
        Configuration config = Configuration.current();
        ConfigurationChange change = ConfigurationChangeBuilder.of(config).build();
        assertThat(change.getVersion()).isNotNull();
        change = ConfigurationChangeBuilder.of(config).setVersion("version2").build();
        assertThat("version2").isEqualTo(change.getVersion());
    }

    @Test
    public void testGetTimestamp() throws Exception {
        long startTS = System.currentTimeMillis();
        Configuration config = Configuration.current();
        Thread.sleep(20L);
        ConfigurationChange change = ConfigurationChangeBuilder.of(config).build();
        assertThat((change.getTimestamp() - startTS) > 0L).isTrue();
        change = ConfigurationChangeBuilder.of(config).setTimestamp(10L).build();
        assertThat(10L).isEqualTo(change.getTimestamp());
    }

    @Test
    public void testGetEvents() throws Exception {
        Configuration config = Configuration.current();
        ConfigurationChange change = ConfigurationChangeBuilder.of(config).removeKey("key1", "key2").build();
        assertThat(change.getChanges().size()).isEqualTo(2);
        change = ConfigurationChangeBuilder.of(config).addChange("key1Added", "value1Added").build();
        assertThat(change.getChanges().size()).isEqualTo(1);
    }

    @Test
    public void testGetRemovedSize() throws Exception {
        Configuration config = Configuration.current();
        ConfigurationChange change = ConfigurationChangeBuilder.of(config).removeKey("java.version", "key2").build();
        assertThat(change.getRemovedSize()).isEqualTo(2);
        assertThat(change.getAddedSize()).isEqualTo(0);
    }

    @Test
    public void testGetAddedSize() throws Exception {
        Configuration config = Configuration.current();
        ConfigurationChange change = ConfigurationChangeBuilder.of(config).addChange("key1", "key2").build();
        assertThat(change.getAddedSize()).isEqualTo(1);
        assertThat(change.getRemovedSize()).isEqualTo(0);
    }

    @Test
    public void testGetUpdatedSize() throws Exception {
        Configuration config = Configuration.current();
        ConfigurationChange change = ConfigurationChangeBuilder.of(config).addChange("java.version", "1.8").build();
        assertThat(change.getUpdatedSize()).isEqualTo(1);
    }

    @Test
    public void testIsRemoved() throws Exception {
        Configuration config = Configuration.current();
        ConfigurationChange change = ConfigurationChangeBuilder.of(config).removeKey("java.version").build();
        assertThat(change.isRemoved("java.version")).isTrue();
    }

    @Test
    public void testIsAdded() throws Exception {
        Configuration config = Configuration.current();
        ConfigurationChange change = ConfigurationChangeBuilder.of(config).addChange("key1", "key2").build();
        assertThat(change.isAdded("key1")).isTrue();
    }

    @Test
    public void testIsUpdated() throws Exception {
        Configuration config = Configuration.current();
        ConfigurationChange change = ConfigurationChangeBuilder.of(config).addChange("java.version", "1.8").build();
        assertThat(change.isUpdated("java.version")).isTrue();
    }

    @Test
    public void testContainsKey() throws Exception {
        Configuration config = Configuration.current();
        ConfigurationChange change = ConfigurationChangeBuilder.of(config).addChange("key1", "key2").build();
        assertThat(change.isKeyAffected("key1")).isTrue();
        assertThat(change.isKeyAffected("key2")).isFalse();
        change = ConfigurationChangeBuilder.of(config).removeKey("java.version").build();
        assertThat(change.isKeyAffected("java.version")).isFalse();
        assertThat(change.isKeyAffected("key2")).isFalse();
    }

    @Test
    public void testIsEmpty() throws Exception {
        Configuration config = Configuration.current();
        ConfigurationChange change = ConfigurationChangeBuilder.of(config).build();
        assertThat(change.isEmpty()).isTrue();
    }

    @Test
    public void testToString() throws Exception {
        Configuration config = Configuration.current();
        ConfigurationChange change = ConfigurationChangeBuilder.of(config).removeKey("java.version").build();
        String toString =
                change.toString();
        assertThat(toString.contains("timestamp")).isTrue();
        assertThat(toString.contains("change-id")).isTrue();
        assertThat(toString.contains("snapshot-id")).isTrue();
        assertThat(toString.contains("key1")).isFalse();
        assertThat(toString.contains("key2")).isFalse();
    }
}
