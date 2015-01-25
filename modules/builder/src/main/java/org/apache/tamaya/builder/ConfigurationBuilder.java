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
package org.apache.tamaya.builder;

import org.apache.tamaya.Configuration;
import org.apache.tamaya.PropertyConverter;
import org.apache.tamaya.TypeLiteral;
import org.apache.tamaya.spi.ConfigurationContext;
import org.apache.tamaya.spi.PropertyFilter;
import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertySourceProvider;
import org.apache.tamaya.spi.PropertyValueCombinationPolicy;

import java.util.Objects;

/**
 * Builder that allows to build a Configuration completely manually.
 */
public class ConfigurationBuilder {
    /**
     * Flag to indicate if this builder instance has already been used
     * to build a configuration.
     */
    private boolean burned = false;

    private ConfigurationContext context = new ProgrammaticConfigurationContext();

    /**
     * Allows to set configuration context during unit tests.
     */
    ConfigurationBuilder setConfigurationContext(ConfigurationContext configurationContext) {
        context = configurationContext;

        return this;
    }

    public ConfigurationBuilder addPropertySources(PropertySource... sources){
        Objects.requireNonNull(sources);

        context.addPropertySources(sources);

        return this;
    }

    public ConfigurationBuilder addPropertySourceProviders(PropertySourceProvider... propertySourceProviders){

        return this;
    }

    public ConfigurationBuilder addPropertyFilters(PropertyFilter... propertyFilters){
        return this;
    }

    public ConfigurationBuilder setPropertyValueCombinationPolicy(PropertyValueCombinationPolicy propertyValueCombinationPolicy){
        return this;
    }

    public ConfigurationBuilder addPropertyConverter(PropertyConverter<?> propertyConverter){

        return this;
    }

    public <T> ConfigurationBuilder addPropertyConverter(TypeLiteral<T> type, PropertyConverter<T> propertyConverter){
        return this;
    }

    public Configuration build() {
        checkStateOfBuilder();
        burned = true;

        return new ProgrammaticConfiguration(context);
    }

    private void checkStateOfBuilder() {
        if (burned) {
            throw new IllegalStateException("Configuration has been already build.");
        }
    }
}
