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


import org.apache.tamaya.base.ConfigContext;
import org.apache.tamaya.base.DefaultConfigValue;

import javax.config.Config;
import javax.config.ConfigValue;
import javax.config.spi.ConfigSource;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;


/**
 * Created by Anatole on 24.03.2015.
 */
public class TestConfigView implements UnaryOperator<Config>{

    private static final TestConfigView INSTANCE = new TestConfigView();

    private TestConfigView(){}

    public static TestConfigView of(){
        return INSTANCE;
    }

    @Override
    public Config apply(final Config config) {
        return new Config() {
            @Override
            public Iterable<String> getPropertyNames() {
                Set<String> result = new HashSet<>();
                for (String key: config.getPropertyNames()) {
                    if (key.startsWith("test")) {
                        result.add(key);
                    }
                }
                return result;
            }

            @Override
            public Iterable<ConfigSource> getConfigSources() {
                return config.getConfigSources();
            }

            @Override
            public void registerConfigChangedListener(Consumer<Set<String>> consumer) {

            }

            @Override
            public <T> T getValue(String key, Class<T> type) {
                if (key.startsWith("test")) {
                    return config.getValue(key, type);
                }
                throw new NoSuchElementException(key);
            }

            @Override
            public <T> Optional<T> getOptionalValue(String key, Class<T> type) {
                if (key.startsWith("test")) {
                    return config.getOptionalValue(key, type);
                }
                return Optional.empty();
            }

            @Override
            public ConfigValue<String> access(String key) {
                return new DefaultConfigValue<>(this, () -> ConfigContext.from(config), key, String.class);
            }

        };
    }
}
