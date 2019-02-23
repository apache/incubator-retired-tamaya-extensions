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

import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertyValue;
import org.apache.tamaya.spisupport.PropertySourceComparator;

import java.util.*;

/**
 * PropertySource, that has values added or overridden.
 */
class EnrichedPropertySource implements PropertySource {

    private final PropertySource basePropertySource;

    private final Map<String, PropertyValue> addedProperties = new HashMap<>();

    private final boolean overriding;

    /**
     * Constructor.
     *
     * @param propertySource the base property source, not null.
     * @param properties the properties to be added.
     * @param overriding flag if existing properties are overridden.
     */
    EnrichedPropertySource(PropertySource propertySource, Map<String, String> properties, boolean overriding) {
        this.basePropertySource = Objects.requireNonNull(propertySource);
        for(Map.Entry<String,String> en:properties.entrySet()){
            this.addedProperties.put(en.getKey(), PropertyValue.createValue(en.getKey(), en.getValue())
                    .setMeta("source", propertySource.getName()));
        }
        this.overriding = overriding;
    }


    @Override
    public int getOrdinal() {
        return PropertySourceComparator.getOrdinal(basePropertySource);
    }

    @Override
    public String getName() {
        return basePropertySource.getName();
    }

    @Override
    public PropertyValue get(String key) {
        if (overriding) {
            PropertyValue val = addedProperties.get(key);
            if (val != null && val.getValue()!=null) {
                return val;
            }
            return basePropertySource.get(key);
        }
        PropertyValue val = basePropertySource.get(key);
        if (val != null && val.getValue()!=null) {
            return val;
        }
        return addedProperties.get(key);

    }

    @Override
    public Map<String, PropertyValue> getProperties() {
        Map<String, PropertyValue> allProps;
        if (overriding) {
            allProps = new HashMap<>();
            for(PropertyValue val:basePropertySource.getProperties().values()){
                allProps.put(val.getKey(), val);
            }
            allProps.putAll(addedProperties);
        } else {
            allProps = new HashMap<>(addedProperties);
            for(PropertyValue val:basePropertySource.getProperties().values()){
                allProps.put(val.getKey(), val);
            }
        }
        return allProps;
    }

    @Override
    public boolean isScannable() {
        return basePropertySource.isScannable();
    }
}
