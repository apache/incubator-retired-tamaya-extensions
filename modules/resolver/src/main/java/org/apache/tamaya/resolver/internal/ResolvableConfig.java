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
package org.apache.tamaya.resolver.internal;

import org.apache.tamaya.base.DefaultConfig;
import org.apache.tamaya.base.DefaultConfigBuilder;
import org.apache.tamaya.base.convert.ConverterManager;
import org.apache.tamaya.base.filter.FilterManager;
import org.apache.tamaya.spi.ConfigContext;
import org.apache.tamaya.spi.ConfigContextSupplier;
import org.apache.tamaya.spi.ConfigValue;

import javax.config.Config;
import javax.config.spi.ConfigSource;
import java.util.Objects;
import java.util.Optional;

/**
 * Wrapper that intercepts evaluation of String configuration extending with value resolution capabilities
 * as provided by registered instances of type {@link org.apache.tamaya.spi.Filter}.
 */
public final class ResolvableConfig implements Config{

    /** The original instance. */
    private Config delegate;
    private FilterManager filterManager = new FilterManager();
    private ConverterManager converterManager = new ConverterManager();

    private ResolvableConfig(Config config){
        this.delegate = Objects.requireNonNull(config);
        filterManager.addDefaultFilters();
        converterManager.addDiscoveredConverters();
    }

    /**
     * Creates a new resolvable configuration instance, based on the given config. This actually performs the following:
     * <ol>
     *     <li>If the instance passed is of type {@link ResolvableConfig}, the instance is passed through.</li>
     *     <li>If the instance passed is of type {@link DefaultConfig}, the instance is passed through.</li>
     *     <li>It the instance implements {@link ConfigContextSupplier}, a new {@link DefaultConfig} is
     *     created and returned, using the returned {@link org.apache.tamaya.spi.ConfigContext}.</li>
     *     <li>Otherwise a new instance of this class is created, with filtering and conversion added on top, based
     *     on the discoverable filters and converters only.</li>
     * </ol>
     * Summarizing this function adds filter resolution functionality to the instance, if needed (Tamaya configuration
     * instances support filtering out of the box) and intercepts all calls for applying resolution and, as
     * needed, subsequent type conversion.
     *
     * @param config the config instance, potentially not resolvable.
     * @return a resolvable config instance.
     */
    public static Config from(Config config){
        if(config instanceof ResolvableConfig){
            return (ResolvableConfig)config;
        }else if(config instanceof DefaultConfig){
            return config;
        }else if(config instanceof ConfigContextSupplier){
            ConfigContext ctx = ((ConfigContextSupplier)config).getConfigContext();
            return new DefaultConfigBuilder(ctx).build();
        }else{
            return new ResolvableConfig(config);
        }
    }

    @Override
    public <T> T getValue(String propertyName, Class<T> propertyType) {
        return getOptionalValue(propertyName, propertyType).orElse(null);
    }

    @Override
    public <T> Optional<T> getOptionalValue(String propertyName, Class<T> propertyType) {
        ConfigValue value = ConfigValue.of(
                propertyName, delegate.getValue(propertyName, String.class), null);
        value = filterManager.filterValue(value);
        if(value!=null){
            if(String.class.equals(propertyType)) {
                return Optional.ofNullable((T) value.getValue());
            }
            return Optional.ofNullable(
                    (T)converterManager.convertValue(propertyName, value.getValue(), propertyType, this));
        }
        return Optional.empty();
    }

    @Override
    public Iterable<String> getPropertyNames() {
        return delegate.getPropertyNames();
    }

    @Override
    public Iterable<ConfigSource> getConfigSources() {
        return delegate.getConfigSources();
    }

    @Override
    public String toString() {
        return "ResolvableConfig{" +
                "delegate=" + delegate +
                ", filterManager=" + filterManager +
                ", converterManager=" + converterManager +
                '}';
    }
}
