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

import org.apache.tamaya.base.DefaultConfigValue;
import org.apache.tamaya.base.convert.ConversionContext;
import org.apache.tamaya.base.convert.ConverterManager;
import org.apache.tamaya.base.ConfigContext;
import org.apache.tamaya.base.ConfigContextSupplier;

import javax.config.Config;
import javax.config.ConfigValue;
import javax.config.spi.ConfigSource;
import javax.config.spi.Converter;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * /**
 * Configuration implementation that stores all current values of a given (possibly dynamic, contextual and non server
 * capable instance) and is fully serializable. Note that hereby only the scannable key/value pairs are considered.
 */
public final class FrozenConfig implements Config, Serializable {
    private static final long serialVersionUID = -6373137316556444171L;

    /**
     * The properties frozen.
     */
    private Map<String, String> properties = new HashMap<>();
    private long frozenAt = System.nanoTime();
    private UUID id = UUID.randomUUID();
    private ConverterManager converterManager = new ConverterManager();

    /**
     * Constructor.
     *
     * @param config The base configuration.
     */
    private FrozenConfig(Config config) {
        for(String key:config.getPropertyNames()) {
            this.properties.put(key, config.getValue(key, String.class));
        }
        this.properties = Collections.unmodifiableMap(this.properties);
        if(config instanceof ConfigContextSupplier){
            ConfigContext ctx = ((ConfigContextSupplier)config).getConfigContext();
            for(Map.Entry<Type, List<Converter>> en:ctx.getConverters().entrySet()) {
                this.converterManager.addConverter(en.getKey(), en.getValue());
            }
        }
    }

    /**
     * Creates a new FrozenConfiguration instance based on a Configuration given.
     *
     * @param config the configuration to be frozen, not null.
     * @return the frozen Configuration.
     */
    public static FrozenConfig of(Config config) {
        if (config instanceof FrozenConfig) {
            return (FrozenConfig) config;
        }
        return new FrozenConfig(config);
    }

    public String getValue(String key) {
        String val = this.properties.get(key);
        if(val==null){
            throw new NoSuchElementException("No such config found: " + key);
        }
        return val;
    }

    @Override
    public <T> Optional<T> getOptionalValue(String key, Class<T> type) {
        String value = this.properties.get(key);
        if (value != null) {
            List<Converter> converters = converterManager.getConverters(type);
            ConversionContext context = new ConversionContext.Builder(this, key,type).build();
            ConversionContext.setContext(context);
            try {
                for (Converter<T> converter : converters) {
                    try {
                        T t = converter.convert(value);
                        if (t != null) {
                            return Optional.of((T) t);
                        }
                    } catch (Exception e) {
                        Logger.getLogger(getClass().getName())
                                .log(Level.FINEST, "PropertyConverter: " + converter + " failed to convert value: " + value,
                                        e);
                    }
                }
            }finally{
                ConversionContext.reset();
            }
            if(String.class==type){
                return Optional.of((T)value);
            }
            throw new IllegalArgumentException("Unparseable config value for type: " + type.getName() + ": " + key
                    + ", supported formats: " + context.getSupportedFormats());
        }
        return Optional.empty();
    }

    @Override
    public ConfigValue<String> access(String key) {
        return new DefaultConfigValue<>(this, () -> ConfigContext.from(this), key, String.class);
    }


    @SuppressWarnings("unchecked")
	@Override
    public <T> T getValue(String key, Class<T> type) {
        return getOptionalValue(key, type)
                .orElseThrow(() -> new NoSuchElementException("No such config found: " + key));
    }

    @Override
    public Iterable<String> getPropertyNames() {
        return properties.keySet();
    }

    @Override
    public List<ConfigSource> getConfigSources() {
        return Collections.emptyList();
    }

    @Override
    public void registerConfigChangedListener(Consumer<Set<String>> consumer) {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FrozenConfig that = (FrozenConfig) o;

        if (frozenAt != that.frozenAt) {
            return false;
        }
        return (properties != null ? properties.equals(that.properties) : that.properties == null) && (id != null ? id.equals(that.id) : that.id == null);
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
