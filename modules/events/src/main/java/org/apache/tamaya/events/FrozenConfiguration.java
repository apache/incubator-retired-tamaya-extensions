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

import org.apache.tamaya.ConfigException;
import org.apache.tamaya.ConfigOperator;
import org.apache.tamaya.ConfigQuery;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.TypeLiteral;
import org.apache.tamaya.functions.ConfigurationFunctions;
import org.apache.tamaya.spi.ConfigurationContext;
import org.apache.tamaya.spi.ConversionContext;
import org.apache.tamaya.spi.PropertyConverter;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * /**
 * Configuration implementation that stores all current values of a given (possibly dynamic, contextual and non server
 * capable instance) and is fully serializable. Note that hereby only the scannable key/createValue pairs are considered.
 */
public final class FrozenConfiguration implements Configuration, Serializable {
    private static final long serialVersionUID = -6373137316556444171L;

    /**
     * The properties frozen.
     */
    private Map<String, String> properties = new HashMap<>();
    private long frozenAt = System.nanoTime();
    private UUID id = UUID.randomUUID();

    /**
     * Constructor.
     *
     * @param config The base configuration.
     */
    private FrozenConfiguration(Configuration config) {
        this.properties.putAll(config.getProperties());
        this.properties = Collections.unmodifiableMap(this.properties);
    }

    /**
     * Creates a new FrozenConfiguration instance based on a Configuration given.
     *
     * @param config the configuration to be frozen, not null.
     * @return the frozen Configuration.
     */
    public static FrozenConfiguration of(Configuration config) {
        if (config instanceof FrozenConfiguration) {
            return (FrozenConfiguration) config;
        }
        return new FrozenConfiguration(config);
    }

    @Override
    public String get(String key) {
        return this.properties.get(key);
    }

    @Override
    public String getOrDefault(String key, String defaultValue) {
        String val = get(key);
        if(val==null){
            return defaultValue;
        }
        return val;
    }

    @Override
    public <T> T getOrDefault(String key, Class<T> type, T defaultValue) {
        T val = get(key, type);
        if(val==null){
            return defaultValue;
        }
        return val;
    }

    @SuppressWarnings("unchecked")
	@Override
    public <T> T get(String key, Class<T> type) {
        return (T) get(key, TypeLiteral.of(type));
    }

    /**
     * Accesses the current String createValue for the given key and tries to convert it
     * using the {@link org.apache.tamaya.spi.PropertyConverter} instances provided by the current
     * {@link org.apache.tamaya.spi.ConfigurationContext}.
     *
     * @param key  the property's absolute, or relative path, e.g. @code
     *             a/b/c/d.myProperty}.
     * @param type The target type required, not null.
     * @param <T>  the createValue type
     * @return the converted createValue, never null.
     */
    @Override
    public <T> T get(String key, TypeLiteral<T> type) {
        String value = get(key);
        if (value != null) {
            List<PropertyConverter<T>> converters = getContext()
                    .getPropertyConverters(type);
            ConversionContext context = new ConversionContext.Builder(this, key,type).build();
            for (PropertyConverter<T> converter : converters) {
                try {
                    T t = converter.convert(value, context);
                    if (t != null) {
                        return t;
                    }
                } catch (Exception e) {
                    Logger.getLogger(getClass().getName())
                            .log(Level.FINEST, "PropertyConverter: " + converter + " failed to convert createValue: " + value,
                                    e);
                }
            }
            throw new ConfigException("Unparseable config createValue for type: " + type.getRawType().getName() + ": " + key
                    + ", supported formats: " + context.getSupportedFormats());
        }

        return null;
    }

    @Override
    public <T> T getOrDefault(String key, TypeLiteral<T> type, T defaultValue) {
        T val = get(key, type);
        if(val==null){
            return defaultValue;
        }
        return val;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public Configuration with(ConfigOperator operator) {
        return operator.operate(this);
    }

    @Override
    public <T> T query(ConfigQuery<T> query) {
        return query.query(this);
    }

    @Override
    public ConfigurationContext getContext() {
        return ConfigurationFunctions.emptyConfigurationContext();
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

        if (frozenAt != that.frozenAt) {
            return false;
        }
        if (properties != null ? !properties.equals(that.properties) : that.properties != null) {
            return false;
        }
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        int result = properties != null ? properties.hashCode() : 0;
        result = 31 * result + (int) (frozenAt ^ (frozenAt >>> 32));
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FrozenConfiguration{" +
                "id=" + getId() + "," +
                "frozenAt=" + getFrozenAt() + "," +
                "properties=" + properties +
                '}';
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
        return frozenAt;
    }

    /**
     * <p>Returns the unique id of this frozen configuration.</p>
     *
     * @return the unique id of this frozen configuration, never {@code null}
     */
    public UUID getId() {
        return id;
    }
}
