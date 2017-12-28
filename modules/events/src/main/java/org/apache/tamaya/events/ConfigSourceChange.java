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
package org.apache.tamaya.events;

import javax.config.spi.ConfigSource;
import java.beans.PropertyChangeEvent;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Event that contains a set current changes that were applied or could be applied.
 * This class is immutable and thread-safe. To create instances use
 * {@link ConfigSourceChangeBuilder}.
 *
 * Created by Anatole on 22.10.2014.
 */
public final class ConfigSourceChange implements ConfigEvent<ConfigSource>, Serializable{

    private static final long serialVersionUID = 1L;
    /** The base property provider/configuration. */
    private final FrozenConfigSource configSource;
    /** The base version, usable for optimistic locking. */
    private String version = UUID.randomUUID().toString();
    /** The timestamp of the change set in millis from the epoch. */
    private long timestamp = System.currentTimeMillis();
    /** The recorded changes. */
    private final Map<String,PropertyChangeEvent> changes = new HashMap<>();

    /**
     * Constructor used by {@link ConfigSourceChangeBuilder}.
     * @param builder The builder used, not null.
     */
    ConfigSourceChange(ConfigSourceChangeBuilder builder) {
        this.configSource = FrozenConfigSource.of(builder.source);
        for (PropertyChangeEvent c : builder.delta.values()) {
            this.changes.put(c.getPropertyName(), c);
        }
        if(builder.version!=null){
            this.version = builder.version;
        }
        if(builder.timestamp!=null){
            this.timestamp = builder.timestamp;
        }
    }

    @Override
    public Class<ConfigSource> getResourceType() {
        return ConfigSource.class;
    }

    /**
     * Get the underlying property provider/configuration.
     * @return the underlying property provider/configuration, or null, if the change instance was deserialized.
     */
    @Override
    public ConfigSource getResource(){
        return this.configSource;
    }

    /**
     * Get the base version, usable for optimistic locking.
     * @return the base version.
     */
    @Override
    public String getVersion(){
        return version;
    }

    /**
     * Get the timestamp in millis from the current epoch. it is expected that the timestamp and the version are unique to
     * identify a changeset.
     * @return the timestamp, when this changeset was created.
     */
    @Override
    public long getTimestamp(){
        return timestamp;
    }

    /**
     * Get the changes recorded.
     * @return the recorded changes, never null.
     */
    public Collection<PropertyChangeEvent> getChanges(){
        return Collections.unmodifiableCollection(this.changes.values());
    }

    /**
     * Access the number current removed entries.
     * @return the number current removed entries.
     */
    public int getRemovedSize() {
        int removedCount = 0;
        for (PropertyChangeEvent ev : this.changes.values()) {
            if (ev.getNewValue() == null) {
                removedCount++;
            }
        }
        return removedCount;
//        return (int) this.changes.values().stream().filter((e) -> e.getNewValue() == null).count();
    }

    /**
     * Access the number current added entries.
     * @return the number current added entries.
     */
    public int getAddedSize() {
        int addedCount = 0;
        for (PropertyChangeEvent ev : this.changes.values()) {
            if (ev.getOldValue() == null &&
                    ev.getNewValue() != null) {
                addedCount++;
            }
        }
        return addedCount;
//        return (int) this.changes.values().stream().filter((e) -> e.getOldValue() == null).count();
    }

    /**
     * Access the number current updated entries.
     * @return the number current updated entries.
     */
    public int getUpdatedSize() {
        int updatedCount = 0;
        for (PropertyChangeEvent ev : this.changes.values()) {
            if (ev.getOldValue() != null && ev.getNewValue() != null) {
                updatedCount++;
            }
        }
        return updatedCount;
//        return (int) this.changes.values().stream().filter((e) -> e.getOldValue()!=null && e.getNewValue()!=null).count();
    }


    /**
     * Checks if the given key was removed.
     * @param key the target key, not null.
     * @return true, if the given key was removed.
     */
    public boolean isRemoved(String key) {
        PropertyChangeEvent change = this.changes.get(key);
        return change != null && change.getNewValue() == null;
    }

    /**
     * Checks if the given key was added.
     * @param key the target key, not null.
     * @return true, if the given key was added.
     */
    public boolean isAdded(String key) {
        PropertyChangeEvent change = this.changes.get(key);
        return change != null && change.getOldValue() == null;
    }

    /**
     * Checks if the given key was updated.
     * @param key the target key, not null.
     * @return true, if the given key was updated.
     */
    public boolean isUpdated(String key) {
        PropertyChangeEvent change = this.changes.get(key);
        return change != null && change.getOldValue() != null && change.getNewValue() != null;
    }

    /**
     * Checks if the given key is added, or updated AND NOT removed.
     * @param key the target key, not null.
     * @return true, if the given key was added, or updated BUT NOT removed.
     */
    public boolean isKeyAffected(String key) {
        PropertyChangeEvent change = this.changes.get(key);
        return change != null && change.getNewValue() != null;
    }

    /**
     * CHecks if the current change set does not contain any changes.
     * @return tru, if the change set is empty.
     */
    public boolean isEmpty(){
        return this.changes.isEmpty();
    }


    @Override
    public String toString() {
        return "ConfigSourceChange{" +
                ", configSource=" + configSource +
                ", version='" + version + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
