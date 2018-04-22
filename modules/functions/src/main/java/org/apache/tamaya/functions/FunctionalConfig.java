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
package org.apache.tamaya.functions;

import org.apache.tamaya.base.ConfigContext;
import org.apache.tamaya.base.ConfigContextSupplier;
import org.apache.tamaya.base.TamayaConfigBuilder;

import javax.config.Config;
import javax.config.ConfigValue;
import javax.config.spi.ConfigSource;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;


/**
 * Config interface with functional extension points.
 */
public interface FunctionalConfig extends Config, ConfigContextSupplier {

    /**
     * Enriches a {@link Config} instance with functional access points.
     * @param config the config, not null
     * @return a functional config instance.
     */
    static FunctionalConfig of(Config config){
        if(config instanceof  FunctionalConfig){
            // Adapt it only once...
            return (FunctionalConfig)config;
        }
        return new FunctionalConfig() {
            @Override
            public ConfigContext getConfigContext() {
                return ConfigContext.from(config);
            }

            @Override
            public <T> T getValue(String key, Class<T> type) {
                return config.getValue(key, type);
            }

            @Override
            public <T> Optional<T> getOptionalValue(String key, Class<T> type) {
                return config.getOptionalValue(key, type);
            }

            @Override
            public ConfigValue<String> access(String key) {
                return config.access(key);
            }

            @Override
            public Iterable<String> getPropertyNames() {
                return config.getPropertyNames();
            }

            @Override
            public Iterable<ConfigSource> getConfigSources() {
                return config.getConfigSources();
            }

            @Override
            public void registerConfigChangedListener(Consumer<Set<String>> consumer) {
                config.registerConfigChangedListener(consumer);
            }

            @Override
            public String toString(){
                return "Functional:"+config.toString();
            }
        };
    }

    /**
     * Extension point for adjusting configuration.
     *
     * @param operator A configuration operator, e.g. a filter, or an adjuster
     *                 combining configurations, never  {@code null}.
     * @return the new adjusted configuration returned by the {@code operator}, never {@code null}.
     */
    default FunctionalConfig with(UnaryOperator<Config> operator){
        return FunctionalConfig.of(operator.apply(this));
    }

    /**
     * Query a configuration.
     *
     * @param <T> the type of the configuration.
     * @param query the query, not {@code null}.
     * @return the result returned by the {@code query}.
     */
    default <T> T query(Function<Config, T> query){
        return query.apply(this);
    }

    /**
     * Access a configuration's context.
     * @return the configuration context, never null.
     */
    default ConfigContext getContext(){
        return ConfigContext.from(this);
    }

    /**
     * Create a new builder using this instance as it's base.
     * @return a new builder, never null.
     */
    default TamayaConfigBuilder toBuilder() {
        return TamayaConfigBuilder.create(this);
    }
}
