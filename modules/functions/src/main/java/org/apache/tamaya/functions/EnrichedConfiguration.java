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

import javax.config.Config;
import javax.config.spi.ConfigSource;
import java.util.*;

/**
 * Configuration, that has values added or overridden.
 */
class EnrichedConfiguration implements Config {

    private final Config baseConfiguration;

    private final Map<String, Object> addedProperties = new HashMap<>();

    private final boolean overriding;

    /**
     * Constructor.
     *
     * @param configuration the base config, not null.
     * @param properties the properties to be added, not null.
     * @param overriding true, if existing keys should be overriden, or config should be extended only.
     */
    EnrichedConfiguration(Config configuration, Map<String, Object> properties, boolean overriding) {
        this.baseConfiguration = Objects.requireNonNull(configuration);
        this.addedProperties.putAll(properties);
        this.overriding = overriding;
    }

    @Override
    public <T> T getValue(String key, Class<T> type) {
        return getOptionalValue(key, type).orElse(null);
    }

    @Override
    public <T> Optional<T> getOptionalValue(String key, Class<T> type) {
        Objects.requireNonNull(key, "Key must be given.");
        Objects.requireNonNull(type, "Type must be given.");
        if (overriding) {
            Object val = addedProperties.get(key);
            if (val != null){
                if(type.isAssignableFrom(type)){
                    return Optional.of((T)val);
                }else if(type == String.class) {
                    return Optional.of((T)val.toString());
                }
                return baseConfiguration.getOptionalValue(key, type);
            }
        }
        Optional<T> val = baseConfiguration.getOptionalValue(key, type);
        if (val !=null && val.isPresent()) {
            return val;
        }
        Object val2 = addedProperties.get(key);
        if (val2 != null){
            if(type.isAssignableFrom(val2.getClass())) {
                return Optional.of((T) val2);
            }else if(type == String.class) {
                return Optional.of((T)val2.toString());
            }
        }
        return Optional.empty();
    }


    @Override
    public Iterable<String> getPropertyNames() {
        Set<String> allKeys = new HashSet<>();
        baseConfiguration.getPropertyNames().forEach(allKeys::add);
        addedProperties.keySet().forEach(allKeys::add);
        return allKeys;
    }

    @Override
    public Iterable<ConfigSource> getConfigSources() {
        return baseConfiguration.getConfigSources();
    }

}
