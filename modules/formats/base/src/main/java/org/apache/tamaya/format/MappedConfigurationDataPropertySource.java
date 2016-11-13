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
import org.apache.tamaya.spisupport.BasePropertySource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Mapped PropertySource that uses the flattened config data read from an URL by a
 * {@link org.apache.tamaya.format.ConfigurationFormat}.
 */
public class MappedConfigurationDataPropertySource extends BasePropertySource {
    private static final Logger LOG = Logger.getLogger(MappedConfigurationDataPropertySource.class.getName());
    private final Map<String, String> properties;
    private final ConfigurationData data;


    /*
     * Constructor, uses hereby the flattened config data read from an URL by a
     * ${@link org.apache.tamaya.format.ConfigurationFormat}.
     * Hereby it reads the <i>default</i> properties as is and adds properties
     * contained in a section as {@code Entry<section.propertyName,value>}.
     * @see ConfigurationData#getCombinedProperties()
     */
    public MappedConfigurationDataPropertySource(ConfigurationData data) {
        this(0, data);
    }

    /*
     * Constructor, uses hereby the flattened config data read from an URL by a
     * ${@link org.apache.tamaya.format.ConfigurationFormat}.
     * Hereby it reads the <i>default</i> properties as is and adds properties
     * contained in a section as {@code Entry<section.propertyName,value>}.
     * @see ConfigurationData#getCombinedProperties()
     */
    public MappedConfigurationDataPropertySource(int defaultOrdinal, ConfigurationData data) {
        super(defaultOrdinal);
        this.properties = Collections.unmodifiableMap(populateData(data));
        this.data = data;
        String name = this.properties.get("_name");
        if (name == null) {
            name = this.data.getResource();
        }
        if (name == null) {
            name = getClass().getSimpleName();
        }
        setName(name);
    }

    /**
     * Method that copies and converts the properties read from the data instance
     * provided.
     * @param data the data returned from the format, not null.
     * @return the final properties to be included.
     */
    protected Map<String, String> populateData(ConfigurationData data) {
        Map<String, String> result = new HashMap<>();
        for(String section:data.getSectionNames()){
            for(Map.Entry<String,String> en:data.getSection(section).entrySet()){
                if("default".equals(section)){
                    result.put(en.getKey(), en.getValue());
                }else {
                    result.put(section + '.' + en.getKey(), en.getValue());
                }
            }
        }
        return result;
    }

    @Override
    public PropertyValue get(String key) {
        String val = properties.get(key);
        return PropertyValue.of(key, val, getName());
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

}
