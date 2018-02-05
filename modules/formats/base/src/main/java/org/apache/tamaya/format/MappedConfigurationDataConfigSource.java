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

import org.apache.tamaya.base.configsource.BaseConfigSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Mapped PropertySource that uses the flattened config data read from an URL by a
 * {@link org.apache.tamaya.format.ConfigurationFormat}. Use of a {@link Supplier}
 * allows deferring the load until a resource is available.
 */
public class MappedConfigurationDataConfigSource extends BaseConfigSource {
    private static final Logger LOG = Logger.getLogger(MappedConfigurationDataConfigSource.class.getName());
    private Map<String, String> properties;
    private final Supplier<ConfigurationData> dataSupplier;

    /*
     * Constructor, uses hereby the flattened config data read from an URL by a
     * ${@link org.apache.tamaya.format.ConfigurationFormat}.
     * Hereby it reads the <i>default</i> properties as is and adds properties
     * contained in a section as {@code Entry<section.propertyName,value>}.
     * @see ConfigurationData#getCombinedProperties()
     */
    public MappedConfigurationDataConfigSource(String name, final Supplier<ConfigurationData> dataSupplier) {
        this(name, 0, dataSupplier);
    }

    /*
     * Constructor, uses hereby the flattened config data read from an URL by a
     * ${@link org.apache.tamaya.format.ConfigurationFormat}.
     * Hereby it reads the <i>default</i> properties as is and adds properties
     * contained in a section as {@code Entry<section.propertyName,value>}.
     * @see ConfigurationData#getCombinedProperties()
     */
    public MappedConfigurationDataConfigSource(final ConfigurationData data) {
        this(data.getResource(), 0, () -> data);
    }

    /*
     * Constructor, uses hereby the flattened config data read from an URL by a
     * ${@link org.apache.tamaya.format.ConfigurationFormat}.
     * Hereby it reads the <i>default</i> properties as is and adds properties
     * contained in a section as {@code Entry<section.propertyName,value>}.
     * @see ConfigurationData#getCombinedProperties()
     */
    public MappedConfigurationDataConfigSource(int defaultOrdinal, final ConfigurationData data) {
        this(data.getResource(), defaultOrdinal, () -> data);
    }

    /*
     * Constructor, uses hereby the flattened config data read from an URL by a
     * ${@link org.apache.tamaya.format.ConfigurationFormat}.
     * Hereby it reads the <i>default</i> properties as is and adds properties
     * contained in a section as {@code Entry<section.propertyName,value>}.
     * @see ConfigurationData#getCombinedProperties()
     */
    public MappedConfigurationDataConfigSource(String name, int defaultOrdinal, Supplier<ConfigurationData> dataSupplier) {
        super(defaultOrdinal);
        setName(name);
        this.dataSupplier = dataSupplier;
        load();
    }

    public void load(){
        try{
            this.properties = populateData(dataSupplier.get());
        }catch(Exception e){
            LOG.log(Level.INFO, "Failed to load property source: " + getName(), e);
            if(this.properties==null) {
                this.properties = new HashMap<>();
            }
            this.properties.put("_exception", e.getLocalizedMessage());
            this.properties.put("_state", "ERROR");
        }finally{
            this.properties.put("_timestamp", String.valueOf(System.currentTimeMillis()));
        }
    }

    /**
     * Method that copies and converts the properties read from the data instance
     * provided.
     * @param data the data returned from the format, not null.
     * @return the final properties to be included.
     */
    protected Map<String, String> populateData(ConfigurationData data) {
        Map<String, String> result = new HashMap<>();
        if(data!=null) {
            for (String section : data.getSectionNames()) {
                for (Map.Entry<String, String> en : data.getSection(section).entrySet()) {
                    if ("default".equals(section)) {
                        result.put(en.getKey(), en.getValue());
                    } else {
                        result.put(section + '.' + en.getKey(), en.getValue());
                    }
                }
            }
            result.put("_propertySource", getName());
            result.put("_source", data.getResource());
        }
        return result;
    }

    @Override
    public String getValue(String key) {
        return properties.get(key);
    }

    @Override
    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(this.properties);
    }

    @Override
    protected String toStringValues() {
        return  super.toStringValues() +
                "  dataSupplier=" + dataSupplier + '\n';
    }

}
