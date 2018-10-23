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

import org.apache.tamaya.spi.PropertyValue;

import java.util.*;

/**
 * Data that abstracts the data read from a configuration resources using a certain format. The data can be divided
 * into different sections, similar to ini-files. Herebey different sections the best map to entries with different
 * priorities to be applied, when integrated into PropertySource instances.
 */
public final class ConfigurationData {
    public static final String DEFAULT_SECTION_NAME = "default";
    /**
     * The sections read.
     */
    private List<PropertyValue> data = new ArrayList<>();
    /** The format instance used to read this instance. */
    private final ConfigurationFormat format;
    /** The resource read. */
    private final String resource;


    /**
     * COnstructor used by builder.
     * @param data the data read, not null.
     * @param format the format, not null.
     * @param resource the underlying resource, not null.
     */
    public ConfigurationData(String resource, ConfigurationFormat format, Collection<PropertyValue> data){
        this.format = Objects.requireNonNull(format);
        this.resource =Objects.requireNonNull(resource);
        this.data.addAll(Objects.requireNonNull(data));
    }

    /**
     * COnstructor used by builder.
     * @param data the data read, not null.
     * @param format the format, not null.
     * @param resource the underlying resource, not null.
     */
    public ConfigurationData(String resource, ConfigurationFormat format, PropertyValue... data){
        this.format = Objects.requireNonNull(format);
        this.resource =Objects.requireNonNull(resource);
        this.data.addAll(Arrays.asList(data));
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
     * @return the setCurrent of present section names, never null.
     */
    public List<PropertyValue> getData() {
        return data;
    }

    /**
     * Checks if no properties are contained in this data item.
     *
     * @return true, if no properties are contained in this data item.
     */
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public String toString() {
        return "ConfigurationData{" +
                "\n  format        = " + format +
                "\n  resource      = " + resource +
                "\n  data          = " + data +
                "\n}";
    }

}
