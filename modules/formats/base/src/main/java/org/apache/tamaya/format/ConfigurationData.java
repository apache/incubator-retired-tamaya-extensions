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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <p>Data that abstracts the data read from a configuration resources using a certain format. The data can be divided
 * into different sections, similar to ini-files. Herebey different sections the best map to entries with different
 * priorities to be applied, when integrated into PropertySource instances.</p>
 * New instances of this class can be created using a {@link org.apache.tamaya.format.ConfigurationDataBuilder}.
 * <h3>Implementation Specification</h3>
 * This class is
 * <ul>
 *     <li>immutable</li>
 *     <li>thread-safe</li>
 * </ul>
 */
public final class ConfigurationData {
    public static final String DEFAULT_SECTION_NAME = "default";
    /**
     * The sections read.
     */
    private Map<String, Map<String, String>> namedSections = new HashMap<>();
    /** The format instance used to read this instance. */
    private final ConfigurationFormat format;
    /** The resource read. */
    private final String resource;


    /**
     * COnstructor used by builder.
     * @param builder the builder instance passing the read configuration data.
     */
    ConfigurationData(ConfigurationDataBuilder builder){
        this.format = builder.format;
        this.resource = builder.resource;
        this.namedSections.putAll(builder.namedSections);
    }

    /**
     * Get the {@link org.apache.tamaya.format.ConfigurationFormat} that read this data.
     * @return the {@link org.apache.tamaya.format.ConfigurationFormat} that read this data, never null.
     */
    public ConfigurationFormat getFormat(){
        return format;
    }

    /**
     * Get the resource from which this data was read.
     * @return the resource from which this data was read, never null.
     */
    public String getResource(){
        return resource;
    }

    /**
     * Access an immutable Set of all present section names, including the default section (if any).
     * @return the set of present section names, never null.
     */
    public Set<String> getSectionNames() {
        if (namedSections == null) {
            return Collections.emptySet();
        }
        return namedSections.keySet();
    }

    /**
     * Get a section's data.
     * @param name the section name, not null.
     * @return the unmodifiable data of this section, or null,
     *         if no such section exists.
     */
    public Map<String, String> getSection(String name) {
        return this.namedSections.get(name);
    }

    /**
     * Convenience accessor for accessing the 'default' section.
     * @return the default section's data, or null, if no such section exists.
     */
    public Map<String, String> getDefaultProperties() {
        Map<String,String> props = getSection(DEFAULT_SECTION_NAME);
        if(props!=null){
            return Collections.unmodifiableMap(props);
        }
        return Collections.emptyMap();
    }

    /**
     * Get combined properties for this config data instance, which contains all
     * properties of all sections in the form {@code Entry<section::property,value>}.
     *
     * @return the normalized properties.
     */
    public Map<String, String> getCombinedProperties() {
        Map<String, String> combinedProperties = new HashMap<>();
        // populate it with sections...
        for (String sectionName : getSectionNames()) {
            Map<String, String> section = getSection(sectionName);
            for (Map.Entry<String, String> en : section.entrySet()) {
                String key = sectionName + "::" + en.getKey();
                combinedProperties.put(key, en.getValue());
            }
        }
        return combinedProperties;
    }

    /**
     * Immutable accessor to ckeck, if there are default properties present.
     * @param section the section, not null.
     * @return true, if default properties are present.
     */
    public boolean containsSection(String section) {
        return this.namedSections.containsKey(section);
    }

    /**
     * Immutable accessor to ckeck, if there are default properties present.
     *
     * @return true, if default properties are present.
     */
    public boolean hasDefaultProperties() {
        return containsSection(DEFAULT_SECTION_NAME);
    }

    /**
     * Checks if no properties are contained in this data item.
     *
     * @return true, if no properties are contained in this data item.
     */
    public boolean isEmpty() {
        return !namedSections.isEmpty();
    }

    @Override
    public String toString() {
        return "ConfigurationData{" +
                "\n  format        = " + format +
                "\n, resource      = " + resource +
                "\n, sections      = " + namedSections.keySet() +
                "\n  default count = " + getDefaultProperties().size() +
                '}';
    }

}
