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

import javax.config.Config;
import javax.config.ConfigProvider;
import java.beans.PropertyChangeEvent;
import java.util.*;

/**
 * Models a set current changes applied to a {@link org.apache.tamaya.spi.PropertySource}. Consumers of these events
 * can observe changes to property sources and
 * <ol>
 * <li>Check if their current configuration instance ({@link org.apache.tamaya.spi.ConfigurationContext}
 * contains the changed {@link org.apache.tamaya.spi.PropertySource} (Note: the reference to a property source is never affected by a
 * change, its only the data of the property source).</li>
 * <li>If so corresponding actions might be taken, such as reevaluating the configuration values (depending on
 * the update policy) or reevaluating the complete {@link org.apache.tamaya.Configuration} to create a change
 * event on configuration level.
 * </ol>
 */
public final class ConfigChangeBuilder {
    /**
     * The recorded changes.
     */
    final SortedMap<String, PropertyChangeEvent> delta = new TreeMap<>();
    /**
     * The underlying configuration/provider.
     */
    final Config source;
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
     * @param configuration the underlying configuration, not null.
     */
    private ConfigChangeBuilder(Config configuration) {
        this.source = Objects.requireNonNull(configuration);
    }

    /**
     * Creates a new instance current this builder using the current COnfiguration as root resource.
     *
     * @return the builder for chaining.
     */
    public static ConfigChangeBuilder of() {
        return new ConfigChangeBuilder(ConfigProvider.getConfig());
    }

    /**
     * Creates a new instance current this builder.
     *
     * @param configuration the configuration changed, not null.
     * @return the builder for chaining.
     */
    public static ConfigChangeBuilder of(Config configuration) {
        return new ConfigChangeBuilder(configuration);
    }

    /**
     * Compares the two property config/configurations and creates a collection with all changes
     * that must be applied to render {@code previous} into {@code target}.
     *
     * @param previous the previous map, not null.
     * @param current the target map, not null.
     * @return a collection current change events, never {@code null}.
     */
    public static Collection<PropertyChangeEvent> compare(Config previous, Config current) {
        TreeMap<String, PropertyChangeEvent> events = new TreeMap<>();

        for (String key : previous.getPropertyNames()) {
            String previousValue = previous.getValue(key, String.class);
            Optional<String> currentValue = current.getOptionalValue(key, String.class);
            if(Objects.equals(currentValue.orElse(null), previousValue)){
                continue;
            }else {
                PropertyChangeEvent event = new PropertyChangeEvent(previous, key, previousValue, currentValue.orElse(null));
                events.put(key, event);
            }
        }

        for (String key : current.getPropertyNames()){
            Optional<String> previousValue = previous.getOptionalValue(key, String.class);
            String currentValue = current.getOptionalValue(key, String.class).orElse(null);
            if(Objects.equals(currentValue, previousValue.orElse(null))){
                continue;
            }else{
                if (!previousValue.isPresent()) {
                    PropertyChangeEvent event = new PropertyChangeEvent(current, key, null, currentValue);
                    events.put(key, event);
                }
                // the other cases were already covered by the previous loop.
            }
        }
        return events.values();
    }

    /*
     * Apply a version/UUID to the set being built.
     * @param version the version to apply, or null, to let the system generate a version for you.
     * @return the builder for chaining.
     */
    public ConfigChangeBuilder setVersion(String version) {
        this.version = version;
        return this;
    }

    /*
     * Apply given timestamp to the set being built.
     * @param version the version to apply, or null, to let the system generate a version for you.
     * @return the builder for chaining.
     */
    public ConfigChangeBuilder setTimestamp(long timestamp) {
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
    public ConfigChangeBuilder addChanges(Config newState) {
        for (PropertyChangeEvent c : compare(this.source, newState)) {
            this.delta.put(c.getPropertyName(), c);
        }
        return this;
    }

    /**
     * Applies a single key/value change.
     *
     * @param key   the changed key
     * @param value the new value.
     * @return this instance for chaining.
     */
    public ConfigChangeBuilder addChange(String key, String value) {
        this.delta.put(key, new PropertyChangeEvent(this.source, key,
                this.source.getOptionalValue(key, String.class).orElse(null),
                value));
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
    public ConfigChangeBuilder removeKey(String key, String... otherKeys) {
        Optional<String> oldValue = this.source.getOptionalValue(key, String.class);
        if (!oldValue.isPresent()) {
            this.delta.remove(key);
        }
        this.delta.put(key, new PropertyChangeEvent(this.source, key, oldValue, null));
        for (String addKey : otherKeys) {
            oldValue = this.source.getOptionalValue(key, String.class);
            if (!oldValue.isPresent()) {
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
    public ConfigChangeBuilder putAll(Map<String, String> changes) {
        for (Map.Entry<String, String> en : changes.entrySet()) {
            this.delta.put(en.getKey(), new PropertyChangeEvent(this.source, en.getKey(), null, en.getValue()));
        }
        return this;
    }

    /**
     * This method will create a change set that clears all entries fromMap the given base configuration/properties.
     *
     * @return the builder for chaining.
     */
    public ConfigChangeBuilder removeAllKeys() {
        this.delta.clear();
        for (String key : this.source.getPropertyNames()) {
            this.delta.put(key, new PropertyChangeEvent(this.source, key, this.source.getValue(key, String.class),
                    null));
        }
//        this.source.getProperties().forEach((k, v) ->
//                this.delta.put(k, new PropertyChangeEvent(this.source, k, v, null)));
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
    public ConfigChange build() {
        return new ConfigChange(this);
    }

    @Override
    public String toString() {
        return "ConfigurationChangeSetBuilder [config=" + source + ", " +
                ", delta=" + delta + "]";
    }

}