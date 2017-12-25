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
 * Combines a set of child configurations to a new one, by overriding the first entries with result from
 * later instances.
 */
class CombinedConfiguration implements Config{
    /** The name of the new configuration. */
    private final String name;

    /**
     * The configuration's in evaluation order. Instances with higher indices
     * override results with lower ones.
     */
    private final ArrayList<Config> configurations = new ArrayList<>();

    /**
     * Creates a combined configuration instance.
     * @param configName the name of the new config.
     * @param configs the configurations hereby instances with higher indices override results with lower ones.
     */
    public CombinedConfiguration(String configName, Config... configs) {
        this.name = configName;

        if (null != configs) {
            for (Config config : configs) {
                if (config == null) {
                    continue;
                }
                configurations.add(config);
            }
        }
    }


    @Override
    public <T> T getValue(String key, Class<T> type) {
        T curValue = null;
        for(Config config: configurations){
            Optional<T> value = config.getOptionalValue(key, type);
            if(value.isPresent()){
                curValue = value.get();
            }
        }
        return curValue;
    }

    @Override
    public <T> Optional<T> getOptionalValue(String key, Class<T> type) {
        Objects.requireNonNull(key, "Key must be given.");
        Objects.requireNonNull(type, "Type must be given.");

        Optional<T> curValue = Optional.empty();
        for(Config config: configurations){
            Optional<T> value = config.getOptionalValue(key, type);
            if(value!=null && value.isPresent()){
                curValue = value;
            }
        }
        return curValue;
    }

    @Override
    public Iterable<String> getPropertyNames() {
        Set<String> result = new HashSet<>();
        for(Config ps : configurations){
            ps.getPropertyNames().forEach(result::add);
        }
        return result;
    }

    @Override
    public Iterable<ConfigSource> getConfigSources() {
        List<ConfigSource> configSources = new ArrayList<>();
        for(Config ps : configurations){
            ps.getConfigSources().forEach(configSources::add);
        }
        return configSources;
    }

    @Override
    public String toString() {
        return "CombinedConfiguration{" +
                "name='" + name + '\'' +
                ", configurations=" + configurations +
                '}';
    }

}
