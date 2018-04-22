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
package org.apache.tamaya.mutableconfig.internal;

import org.apache.tamaya.mutableconfig.ChangePropagationPolicy;
import org.apache.tamaya.mutableconfig.MutableConfig;
import org.apache.tamaya.mutableconfig.ConfigChangeRequest;
import org.apache.tamaya.mutableconfig.spi.MutableConfigSource;
import org.osgi.service.component.annotations.Component;

import javax.config.Config;
import javax.config.spi.ConfigSource;
import java.util.*;
import java.util.logging.Logger;


/**
 * Default implementation of a {@link MutableConfig}.
 */
@Component
public class DefaultMutableConfiguration implements MutableConfig {
    private static final Logger LOG = Logger.getLogger(DefaultMutableConfiguration.class.getName());
    private ConfigChangeRequest changeRequest = new ConfigChangeRequest(UUID.randomUUID().toString());
    private final Config config;
    private ChangePropagationPolicy changePropagationPolicy;

    public DefaultMutableConfiguration(Config config, ChangePropagationPolicy changePropagationPolicy){
        this.config = Objects.requireNonNull(config);
        this.changePropagationPolicy = Objects.requireNonNull(changePropagationPolicy);
    }

    @Override
    public ChangePropagationPolicy getChangePropagationPolicy(){
        return changePropagationPolicy;
    }

    @Override
    public ConfigChangeRequest getConfigChangeRequest(){
        return changeRequest;
    }

    protected List<MutableConfigSource> getMutablePropertySources() {
        List<MutableConfigSource> result = new ArrayList<>();
        for(ConfigSource propertySource:this.config.getConfigSources()) {
            if(propertySource instanceof MutableConfigSource){
                result.add((MutableConfigSource)propertySource);
            }
        }
        return result;
    }


    @Override
    public MutableConfig put(String key, String value) {
        changeRequest.put(key, value);
        return this;
    }

    @Override
    public MutableConfig putAll(Map<String, String> properties) {
        changeRequest.putAll(properties);
        return this;
    }

    @Override
    public MutableConfig remove(String... keys) {
        changeRequest.removeAll(Arrays.asList(keys));
        return this;
    }


    @Override
    public void store() {
        this.changePropagationPolicy.applyChange(changeRequest, config.getConfigSources());
    }

    @Override
    public MutableConfig remove(Collection<String> keys) {
        for(MutableConfigSource target:getMutablePropertySources()) {
            changeRequest.removeAll(keys);
        }
        return this;
    }

    @Override
    public <T> T getValue(String key, Class<T> type) {
        return this.config.getValue(key, type);
    }

    @Override
    public <T> Optional<T> getOptionalValue(String key, Class<T> type) {
        return this.config.getOptionalValue(key,type);
    }

    @Override
    public Iterable<String> getPropertyNames() {
        return this.config.getPropertyNames();
    }

    @Override
    public Iterable<ConfigSource> getConfigSources() {
        return this.config.getConfigSources();
    }

    @Override
    public String toString() {
        return "DefaultMutableConfiguration{" +
                "config=" + config +
                '}';
    }

}