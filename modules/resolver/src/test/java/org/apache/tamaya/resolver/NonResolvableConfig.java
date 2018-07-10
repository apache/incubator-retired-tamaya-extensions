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
package org.apache.tamaya.resolver;

import org.apache.tamaya.base.ConfigContextSupplier;
import org.apache.tamaya.base.DefaultConfigValue;

import javax.config.Config;
import javax.config.ConfigValue;
import javax.config.spi.ConfigSource;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Implements s simple config just based on the {@link MyTestConfigSource}, without any
 * resolution logic.
 */
public class NonResolvableConfig implements Config{

    private MyTestConfigSource configDelegate = new MyTestConfigSource();

    @Override
    public <T> T getValue(String propertyName, Class<T> propertyType) {
        if(propertyType.equals(String.class)) {
            return (T)configDelegate.getValue(propertyName);
        }
        return null;
    }

    @Override
    public <T> Optional<T> getOptionalValue(String propertyName, Class<T> propertyType) {
        return Optional.ofNullable(getValue(propertyName, propertyType));
    }

    @Override
    public ConfigValue<String> access(String s) {
        return new DefaultConfigValue<>(this, ConfigContextSupplier.of(this), s, String.class);
    }

    @Override
    public Iterable<String> getPropertyNames() {
        return configDelegate.getPropertyNames();
    }

    @Override
    public Iterable<ConfigSource> getConfigSources() {
        return Arrays.asList(new ConfigSource[]{configDelegate});
    }

    @Override
    public void registerConfigChangedListener(Consumer<Set<String>> consumer) {

    }
}
