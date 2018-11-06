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

import org.apache.tamaya.functions.Supplier;
import org.apache.tamaya.spi.PropertyValue;
import org.apache.tamaya.spisupport.propertysource.BasePropertySource;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Mapped PropertySource that uses the flattened config data read from an URL by a
 * {@link org.apache.tamaya.format.ConfigurationFormat}. Use of a {@link Supplier}
 * allows deferring the load until a resource is available.
 */
public class MappedConfigurationDataPropertySource extends BasePropertySource {
    private static final Logger LOG = Logger.getLogger(MappedConfigurationDataPropertySource.class.getName());
    private Map<String, PropertyValue> properties = new HashMap<>();
    private final Supplier<ConfigurationData> dataSupplier;

    /*
     * Constructor, uses hereby the flattened config data read from an URL by a
     * ${@link org.apache.tamaya.format.ConfigurationFormat}.
     * Hereby it reads the <i>default</i> properties as is and adds properties
     * contained in a section as {@code Entry<section.propertyName,createValue>}.
     * @see ConfigurationData#getCombinedProperties()
     */
    public MappedConfigurationDataPropertySource(String name, final Supplier<ConfigurationData> dataSupplier) {
        this(name, 0, dataSupplier);
    }

    /*
     * Constructor, uses hereby the flattened config data read from an URL by a
     * ${@link org.apache.tamaya.format.ConfigurationFormat}.
     * Hereby it reads the <i>default</i> properties as is and adds properties
     * contained in a section as {@code Entry<section.propertyName,createValue>}.
     * @see ConfigurationData#getCombinedProperties()
     */
    public MappedConfigurationDataPropertySource(final ConfigurationData data) {
        this(data.getResource(), 0, () -> data);
    }

    /*
     * Constructor, uses hereby the flattened config data read from an URL by a
     * ${@link org.apache.tamaya.format.ConfigurationFormat}.
     * Hereby it reads the <i>default</i> properties as is and adds properties
     * contained in a section as {@code Entry<section.propertyName,createValue>}.
     * @see ConfigurationData#getCombinedProperties()
     */
    public MappedConfigurationDataPropertySource(int defaultOrdinal, final ConfigurationData data) {
        this(data.getResource(), defaultOrdinal, () -> data);
    }

    /*
     * Constructor, uses hereby the flattened config data read from an URL by a
     * ${@link org.apache.tamaya.format.ConfigurationFormat}.
     * Hereby it reads the <i>default</i> properties as is and adds properties
     * contained in a section as {@code Entry<section.propertyName,createValue>}.
     * @see ConfigurationData#getCombinedProperties()
     */
    public MappedConfigurationDataPropertySource(String name, int defaultOrdinal, Supplier<ConfigurationData> dataSupplier) {
        super(defaultOrdinal);
        setName(name);
        this.dataSupplier = dataSupplier;
        load();
    }

    public void load(){
        ConfigurationData data = dataSupplier.get();
        if(data==null){
            return;
        }
        Map<String, String> meta = new HashMap<>();
        meta.put("source", data.getResource());
        meta.put("timestamp",String.valueOf(System.currentTimeMillis()));
        try{
            this.properties.putAll(populateData(data, meta));
        }catch(Exception e){
            LOG.log(Level.INFO, "Failed to load property source: " + getName(), e);
            if(this.properties==null) {
                this.properties = new HashMap<>();
            }
            this.properties.put("[error]propertysource."+getName()+".exception",
                    PropertyValue.createValue("[meta]propertysource."+getName()+".exception",
                            e.getLocalizedMessage()).setMeta("source",
                            data.getResource()));
            this.properties.put("[error]propertysource."+getName()+".exception",
                    PropertyValue.createValue("[meta]propertysource."+getName()+".state",
                            "ERROR").setMeta("source",
                            data.getResource()));
        }
    }

    /**
     * Method that copies and converts the properties read from the data instance
     * provided.
     * @param data the data returned from the format, not null.
     * @param meta the metadata to add.
     * @return the final properties to be included.
     */
    protected Map<String, PropertyValue> populateData(ConfigurationData data, Map<String, String> meta) {
        Map<String, PropertyValue> result = new HashMap<>();
        for(PropertyValue val:data.getData()) {
            if(!val.getKey().isEmpty()) {
                addNode(val, result, meta);
            }
            for(PropertyValue child:val) {
                addNode(child, result, meta);
            }
        }
        return result;
    }

    protected void addNode(PropertyValue val, Map<String, PropertyValue> map, Map<String, String> meta){
        if(val.isLeaf()){
            val.setMeta(meta);
            map.put(val.getQualifiedKey(), val);
        }else{
            for(PropertyValue child:val) {
                addNode(child, map, meta);
            }
        }
    }

    @Override
    public PropertyValue get(String key) {
       return properties.get(key);
    }

    @Override
    public Map<String, PropertyValue> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    @Override
    protected String toStringValues() {
        return  super.toStringValues() +
                "  dataSupplier=" + dataSupplier + '\n';
    }

}
