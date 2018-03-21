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
import org.apache.tamaya.base.DefaultConfigValue;

import javax.config.Config;
import javax.config.ConfigValue;
import javax.config.spi.ConfigSource;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;


/**
 * Configuration that filters part of the entries defined by a filter predicate.
 */
class MappedConfiguration implements Config {

    @Override
    public ConfigValue<String> access(String key) {
        return new DefaultConfigValue<>(this, () -> ConfigContext.from(this), key, String.class);
    }

    @Override
    public void registerConfigChangedListener(Consumer<Set<String>> consumer) {
        this.baseConfiguration.registerConfigChangedListener(consumer);
    }

    private static final Logger LOG = Logger.getLogger(MappedConfiguration.class.getName());
    private final Config baseConfiguration;
    private final KeyMapper keyMapper;
    private final String mapType;

    MappedConfiguration(Config baseConfiguration, KeyMapper keyMapper, String mapType) {
        this.baseConfiguration = Objects.requireNonNull(baseConfiguration);
        this.keyMapper = Objects.requireNonNull(keyMapper);
        this.mapType = mapType!=null?mapType:this.keyMapper.toString();
    }

    @Override
    public <T> T getValue(String key, Class<T> type) {
        String targetKey = keyMapper.mapKey(key);
        if (targetKey != null) {
            return baseConfiguration.getValue(targetKey, type);
        }
        LOG.finest("Configuration property hidden by KeyMapper, key="+key+", mapper="+keyMapper+", config="+this);
        return null;

    }

    @Override
    public <T> Optional<T> getOptionalValue(String key, Class<T> type) {
        String targetKey = keyMapper.mapKey(key);
        if (targetKey != null) {
            return baseConfiguration.getOptionalValue(targetKey, type);
        }
        LOG.finest("Configuration property hidden by KeyMapper, key="+key+", mapper="+keyMapper+", config="+this);
        return Optional.empty();
    }


    @Override
    public Iterable<String> getPropertyNames() {
        Iterable<String> propertyNames = baseConfiguration.getPropertyNames();
        Set<String> result = new HashSet<>();
        for(String key:propertyNames){
            String targetKey = keyMapper.mapKey(key);
            if (targetKey != null) {
                result.add(targetKey);
            }
        }
        return result;
    }

    @Override
    public Iterable<ConfigSource> getConfigSources() {
        return baseConfiguration.getConfigSources();
    }

    @Override
    public String toString() {
        return "FilteredConfiguration{" +
                "baseConfiguration=" + baseConfiguration +
                ", mapping=" + mapType +
                '}';
    }

}
