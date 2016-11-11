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
package org.apache.tamaya.format;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


/**
 * Builder for creating {@link org.apache.tamaya.format.ConfigurationData} instances. This class is not thread-safe.
 */
public final class ConfigurationDataBuilder {

    /** The format instance used to read this instance. */
    final ConfigurationFormat format;
    /** The resource read. */
    final String resource;
    /**
     * The sections read.
     */
    Map<String, Map<String, String>> namedSections = new HashMap<>();

    /**
     * Private constructor.
     * @param resource the configuration resource URL, not null.
     * @param format the format that read this data, not null.
     */
    private ConfigurationDataBuilder(String resource, ConfigurationFormat format){
        this.format = Objects.requireNonNull(format);
        this.resource = Objects.requireNonNull(resource);
    }

    /**
     * Creates a new instance.
     * @param resource the configuration resource URL, not null.
     * @param format the format that read this data, not null.
     * @return new instance of this class.
     */
    public static ConfigurationDataBuilder of(String resource, ConfigurationFormat format){
        return new ConfigurationDataBuilder(resource, format);
    }

    /**
     * Creates a new instance.
     * @param data an existing ConfigurationData instances used to initialize the builder.
     * @return new instance of this class from the given configuration.
     */
    public static ConfigurationDataBuilder of(ConfigurationData data){
        ConfigurationDataBuilder b = new ConfigurationDataBuilder(data.getResource(), data.getFormat());
        if (!data.isEmpty()) {
            for(String section:data.getSectionNames()) {
                b.addSectionProperties(section, data.getSection(section));
            }
        }
        return b;
    }

    /**
     * Adds (empty) sections,if they are not yet existing. Already existing sections will not be touched.
     * @param sections the new sections to put.
     * @return the builder for chaining.
     */
    public ConfigurationDataBuilder addSections(String... sections){
        for (String section : sections) {
            if (!namedSections.containsKey(section)) {
                namedSections.put(section, new HashMap<String, String>());
            }
        }
        return this;
    }

    /**
     * Adds a single entry to a target section.
     * @param section the target section (will be created if not existing).
     * @param key the entry's key
     * @param value the entry's value
     * @return the builder for chaining.
     */
    public ConfigurationDataBuilder addSectionProperty(String section, String key, String value) {
        Map<String, String> map = namedSections.get(section);
        if (map == null) {
            map = new HashMap<>();
            namedSections.put(section, map);
        }
        map.put(key, value);
        return this;
    }

    /**
     * Adds a single entry to the <i>default</i> section.
     * @param key the entry's key
     * @param value the entry's value
     * @return the builder for chaining.
     */
    public ConfigurationDataBuilder addDefaultProperty(String key, String value) {
        return addSectionProperty("default", key, value);
    }

    /**
     * Adds a single entry to the <i>default</i> section.
     * @param key the entry's key
     * @param value the entry's value
     * @return the builder for chaining.
     * @deprecated Use {@link #addDefaultProperty(String, String)} instead of.
     */
    @Deprecated
    public ConfigurationDataBuilder addProperty(String key, String value) {
        return addDefaultProperty(key, value);
    }

    /**
     * Adds the given entries to the given section, all existing values will be overridden.
     * @param section the target section (will be created if not existing).
     * @param properties the entry's data
     * @return the builder for chaining.
     */
    public ConfigurationDataBuilder addSectionProperties(String section, Map<String, String> properties) {
        Map<String, String> map = namedSections.get(section);
        if (map == null) {
            map = new HashMap<>();
            namedSections.put(section, map);
        }
        map.putAll(properties);
        return this;
    }

    /**
     * Adds the given entries to the <i>default</i> section, all existing values will be overridden.
     * @param properties the entry's data
     * @return the builder for chaining.
     * @deprecated Use {@link #addDefaultProperties(Map)} instead of.
     */
    @Deprecated
    public ConfigurationDataBuilder addProperties(Map<String, String> properties) {
        return addDefaultProperties(properties);
    }


        /**
         * Adds the given entries to the <i>default</i> section, all existing values will be overridden.
         * @param properties the entry's data
         * @return the builder for chaining.
         */
    public ConfigurationDataBuilder addDefaultProperties(Map<String, String> properties) {
        Map<String,String> defaultProps = this.namedSections.get("default");
        if(defaultProps==null){
            defaultProps = new HashMap<>();
            this.namedSections.put("default", defaultProps);
        }
        defaultProps.putAll(properties);
        return this;
    }


    /**
     * Access the current named sections, if not present a new instance is initialized.
     *
     * @return the current named sections, never null.
     */
    public Set<String> getSectionNames() {
        return namedSections.keySet();
    }

    /**
     * Builds a new {@link org.apache.tamaya.format.ConfigurationData} instance.
     * @return a new {@link org.apache.tamaya.format.ConfigurationData} instance, not null.
     */
    public ConfigurationData build(){
        return new ConfigurationData(this);
    }

    @Override
    public String toString() {
        return "ConfigurationDataBuilder{" +
                "\n  format=" + format +
                "\n  sections=" + namedSections.keySet() +
                "\n  resource=" + resource +
                "\n}";
    }
}
