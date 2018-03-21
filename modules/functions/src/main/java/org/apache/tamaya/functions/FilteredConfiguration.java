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

/**
 * Configuration that filters part of the entries defined by a matcher predicate.
 */
class FilteredConfiguration implements Config {

    private final Config baseConfiguration;
    private final PropertyMatcher matcher;
    private final String filterType;

    FilteredConfiguration(Config baseConfiguration, PropertyMatcher matcher, String filterType) {
        this.baseConfiguration = Objects.requireNonNull(baseConfiguration);
        this.matcher = Objects.requireNonNull(matcher);
        this.filterType = filterType!=null?filterType:this.matcher.toString();
    }

    @Override
    public <T> T getValue(String key, Class<T> type) {
        String stringValue = baseConfiguration.getValue(key, String.class);
        if (matcher.test(key, stringValue)) {
            return baseConfiguration.getValue(key, type);
        }
        return null;
    }

    @Override
    public <T> Optional<T> getOptionalValue(String key, Class<T> type) {
        Optional<String> value = baseConfiguration.getOptionalValue(key, String.class);
        if(value.isPresent() && matcher.test(key, value.get())) {
            return baseConfiguration.getOptionalValue(key, type);
        }
        return Optional.empty();
    }

    @Override
    public ConfigValue<String> access(String key) {
        return new DefaultConfigValue<>(this, () -> ConfigContext.from(this.baseConfiguration), key, String.class);
    }

    @Override
    public Iterable<String> getPropertyNames() {
        Set<String> result = new HashSet<>();
        for(String name:baseConfiguration.getPropertyNames()){
            if(matcher.test(name, null)){
                result.add(name);
            }
        }
        return result;
    }

    @Override
    public Iterable<ConfigSource> getConfigSources() {
        return this.baseConfiguration.getConfigSources();
    }

    @Override
    public void registerConfigChangedListener(Consumer<Set<String>> consumer) {

    }

    @Override
    public String toString() {
        return "FilteredConfiguration{" +
                "baseConfiguration=" + baseConfiguration +
                ", matcher=" + filterType +
                '}';
    }

}
