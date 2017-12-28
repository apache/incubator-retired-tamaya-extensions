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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Models a set current changes applied to a {@link ConfigSource}. Consumers of these events
 * can observing changes to property sources and
 * <ol>
 *     <li>Check if their current configuration instance ({@link javax.config.Config}
 *     contains the changed {@link ConfigSource} (Note: the reference tova property source is never affected by a
 *     change, its only the data of the property source).</li>
 *     <li>If so corresponding action may be taken, such as reevaluating the configuration values (depending on
 *     the update policy) or reevaluating the complete {@link javax.config.Config} to create a change
 *     event on configuration level.
 * </ol>
 */
public final class ConfigSourceChangeBuilder {
    /**
     * The recorded changes.
     */
    final SortedMap<String, PropertyChangeEvent> delta = new TreeMap<>();
    /**
     * The underlying configuration/provider.
     */
    final ConfigSource source;
    /**
     * The version configured, or null, for generating a default.
     */
    String version;
    /**
     * The optional timestamp in millis of this epoch.
     */
    Long timestamp;

    /**
     * Constructor.
     *
     * @param source the underlying configuration/provider, not null.
     */
    private ConfigSourceChangeBuilder(ConfigSource source) {
        this.source = Objects.requireNonNull(source);
    }

    /**
     * Creates a new instance of this builder.
     *
     * @param source the underlying property provider/configuration, not null.
     * @return the builder for chaining.
     */
    public static ConfigSourceChangeBuilder of(ConfigSource source) {
        return new ConfigSourceChangeBuilder(source);
    }

    /**
     * Compares the two property config/configurations and creates a collection current all changes
     * that must be applied to render {@code map1} into {@code map2}.
     *
     * @param map1 the source map, not null.
     * @param map2 the target map, not null.
     * @return a collection current change events, never null.
     */
    public static Collection<PropertyChangeEvent> compare(ConfigSource map1, ConfigSource map2) {
        List<PropertyChangeEvent> changes = new ArrayList<>();
        for (Map.Entry<String, String> en : map1.getProperties().entrySet()) {
            String val = map2.getValue(en.getKey());
            if (val == null) {
                changes.add(new PropertyChangeEvent(map1, en.getKey(), null, en.getValue()));
            } else if (!val.equals(en.getValue())) {
                changes.add(new PropertyChangeEvent(map1, en.getKey(), val, en.getValue()));
            }
        }
        for (Map.Entry<String, String> en : map2.getProperties().entrySet()) {
            String val = map1.getValue(en.getKey());
            if (val == null) {
                changes.add(new PropertyChangeEvent(map1, en.getKey(), en.getValue(), null));
            } else if (!val.equals(en.getValue())) {
                changes.add(new PropertyChangeEvent(map1, en.getKey(), en.getValue(), val));
            }
        }
        return changes;
    }

    /*
     * Apply a version/UUID to the set being built.
     * @param version the version to apply, or null, to let the system generate a version for you.
     * @return the builder for chaining.
     */
    public ConfigSourceChangeBuilder setVersion(String version) {
        this.version = version;
        return this;
    }

    /*
     * Apply given timestamp to the set being built.
     * @param version the version to apply, or null, to let the system generate a version for you.
     * @return the builder for chaining.
     */
    public ConfigSourceChangeBuilder setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * This method records all changes to be applied to the base property provider/configuration to
     * achieve the given target state.
     *
     * @param newState the new target state, not null.
     * @return the builder for chaining.
     */
    public ConfigSourceChangeBuilder addChanges(ConfigSource newState) {
        Collection<PropertyChangeEvent> events = ConfigSourceChangeBuilder.compare(newState, this.source);
        for (PropertyChangeEvent c : events) {
            this.delta.put(c.getPropertyName(), c);
        }
        return this;
    }

    /**
     * Get the current values, also considering any changes recorded within this change set.
     *
     * @param key the key current the entry, not null.
     * @return the keys, or null.
     */
    public String get(String key) {
        PropertyChangeEvent change = this.delta.get(key);
        if (change != null && !(change.getNewValue() == null)) {
            return (String) change.getNewValue();
        }
        return null;
    }

    /**
     * Marks the given key(s) fromMap the configuration/properties to be removed.
     *
     * @param key       the key current the entry, not null.
     * @param otherKeys additional keys to be removed (convenience), not null.
     * @return the builder for chaining.
     */
    public ConfigSourceChangeBuilder remove(String key, String... otherKeys) {
        String oldValue = this.source.getValue(key);
        if (oldValue == null) {
            this.delta.remove(key);
        }
        this.delta.put(key, new PropertyChangeEvent(this.source, key, oldValue, null));
        for (String addKey : otherKeys) {
            oldValue = this.source.getValue(addKey);
            if (oldValue == null) {
                this.delta.remove(addKey);
            }
            this.delta.put(addKey, new PropertyChangeEvent(this.source, addKey, oldValue, null));
        }
        return this;
    }

    /**
     * Apply all the given values to the base configuration/properties.
     * Note that all values passed must be convertible to String, either
     * <ul>
     * <li>the registered codecs provider provides codecs for the corresponding keys, or </li>
     * <li>default codecs are present for the given type, or</li>
     * <li>the value is an instanceof String</li>
     * </ul>
     *
     * @param changes the changes to be applied, not null.
     * @return the builder for chaining.
     */
    public ConfigSourceChangeBuilder putAll(Map<String, String> changes) {
        for (Map.Entry<String, String> en : this.source.getProperties().entrySet()) {
            this.delta.put(en.getKey(), new PropertyChangeEvent(this.source, en.getKey(), null, en.getValue()));
        }
        return this;
    }

    /**
     * This method will create a change set that clears all entries fromMap the given base configuration/properties.
     *
     * @return the builder for chaining.
     */
    public ConfigSourceChangeBuilder deleteAll() {
        this.delta.clear();
        for (Map.Entry<String, String> en : this.source.getProperties().entrySet()) {
            this.delta.put(en.getKey(), new PropertyChangeEvent(this.source, en.getKey(), en.getValue(), null));
        }
        return this;
    }

    /**
     * Checks if the change set is empty, i.e. does not contain any changes.
     *
     * @return true, if the set is empty.
     */
    public boolean isEmpty() {
        return this.delta.isEmpty();
    }

    /**
     * Resets this change set instance. This will clear all changes done to this builder, so the
     * set will be empty.
     */
    public void reset() {
        this.delta.clear();
    }


    /**
     * Builds the corresponding change set.
     *
     * @return the new change set, never null.
     */
    public ConfigSourceChange build() {
        return new ConfigSourceChange(this);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ConfigSourceChangeBuilder [source=" + source + ", " +
                ", delta=" + delta + "]";
    }

}
