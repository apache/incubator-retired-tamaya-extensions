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

import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationSnapshot;
import org.apache.tamaya.TypeLiteral;
import org.apache.tamaya.spi.*;
import org.apache.tamaya.spisupport.*;

import java.io.Serializable;
import java.util.*;

/**
 * /**
 * Configuration implementation that stores all current values of a given (possibly dynamic, contextual and non server
 * capable instance) and is fully serializable. Note that hereby only the scannable key/createValue pairs are considered.
 * @deprecated Use {@link DefaultConfigurationSnapshot}
 */
@Deprecated
public final class FrozenConfiguration implements Configuration, Serializable {
    private static final long serialVersionUID = -6373137316556444172L;

    /**
     * The properties frozen.
     */
    private ConfigurationSnapshot snapshot;

    private UUID id = UUID.randomUUID();

    /**
     * Constructor.
     *
     * @param snapshot The snapshot.
     */
    private FrozenConfiguration(ConfigurationSnapshot snapshot) {
        this.snapshot = snapshot;
    }


    /**
     * Creates a new FrozenConfiguration instance based on the current Configuration.
     *
     * @param keys the keys, not null.
     * @return the frozen Configuration.
     * @see Configuration#current()
     */
    public static FrozenConfiguration ofCurrent(String... keys) {
        return new FrozenConfiguration(Configuration.current().getSnapshot(Arrays.asList(keys)));
    }

    /**
     * Creates a new FrozenConfiguration instance based on a Configuration given.
     *
     * @param config the configuration to be frozen, not null.
     * @param keys the keys, not null.
     * @return the frozen Configuration.
     */
    public static FrozenConfiguration of(Configuration config, String... keys) {
        return new FrozenConfiguration(config.getSnapshot(Arrays.asList(keys)));
    }

    /**
     * Creates a new FrozenConfiguration instance based on a Configuration given.
     *
     * @param config the configuration to be frozen, not null.
     * @param keys the keys, not null.
     * @return the frozen Configuration.
     */
    public static FrozenConfiguration of(Configuration config, Set<String> keys) {
        return new FrozenConfiguration(config.getSnapshot(keys));
    }

    /**
     * Get the evaluated keys of this frozen coinfiguration.
     * @return the keys, not null.
     */
    public Set<String> getKeys() {
        return snapshot.getKeys();
    }

    @Override
    public String get(String key) {
        return this.snapshot.get(key);
    }

    @Override
    public String getOrDefault(String key, String defaultValue) {
        return this.snapshot.getOrDefault(key, defaultValue);
    }

    @Override
    public <T> T getOrDefault(String key, Class<T> type, T defaultValue) {
        return this.snapshot.getOrDefault(key, type, defaultValue);
    }

    @SuppressWarnings("unchecked")
	@Override
    public <T> T get(String key, Class<T> type) {
        return snapshot.get(key, type);
    }

    @Override
    public <T> T get(String key, TypeLiteral<T> type) {
        return snapshot.get(key, type);
    }

    @Override
    public <T> T getOrDefault(String key, TypeLiteral<T> type, T defaultValue) {
        return snapshot.getOrDefault(key, type, defaultValue);
    }

    @Override
    public Map<String, String> getProperties() {
        return snapshot.getProperties();
    }

    @Override
    public ConfigurationContext getContext() {
        return snapshot.getContext();
    }

    @Override
    public ConfigurationSnapshot getSnapshot(Iterable<String> keys) {
        return this.snapshot.getSnapshot(keys);
    }

    /**
     * <p>Returns the moment in time when this frozen configuration has been created.</p>
     *
     * <p>The time is taken from {@linkplain System#currentTimeMillis()}</p>
     *
     * @see System#currentTimeMillis()
     * @return the moment in time when this configuration has been created
     */
    public long getFrozenAt() {
        return snapshot.getTimestamp();
    }

    /**
     * <p>Returns the unique id of this frozen configuration.</p>
     *
     * @return the unique id of this frozen configuration, never {@code null}
     */
    public UUID getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FrozenConfiguration that = (FrozenConfiguration) o;

        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(snapshot);
    }

    @Override
    public String toString() {
        return "FrozenConfiguration{" +
                "snapshot=" + snapshot + "," +
                '}';
    }
}
