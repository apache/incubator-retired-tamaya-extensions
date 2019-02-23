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
 * Property source which filters any key/values dynamically.
 */
class ValueMappedPropertySource implements PropertySource{

    private final String name;
    private final PropertyMapper valueFilter;
    private final PropertySource source;

    public ValueMappedPropertySource(String name, PropertyMapper valueFilter, PropertySource current) {
        this.name =  name!=null?name:"<valueFiltered> -> name="+current.getName()+", valueFilter="+valueFilter.toString();
        this.valueFilter = valueFilter;
        this.source = Objects.requireNonNull(current);
    }

    @Override
    public int getOrdinal() {
        return PropertySourceComparator.getOrdinal(source);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public PropertyValue get(String key) {
        PropertyValue value = this.source.get(key);
        if(value!=null && value.getValue()!=null) {
            return PropertyValue.createValue(key, valueFilter.mapProperty(key, value.getValue())).setMeta("source",
                    getName());
        }
        return null;
    }

    @Override
    public Map<String, PropertyValue> getProperties() {
        Map<String,PropertyValue> result = new HashMap<>();
        for(PropertyValue val : source.getProperties().values()) {
            String mappedValue = valueFilter.mapProperty(val.getKey(), val.getValue());
            PropertyValue value = val.setValue(mappedValue);
            result.put(val.getKey(), value);
        }
        return result;
    }

    @Override
    public boolean isScannable() {
        return source.isScannable();
    }

    @Override
    public String toString() {
        return "ValueMappedPropertySource{" +
                "source=" + source.getName() +
                ", name='" + name + '\'' +
                ", valueFilter=" + valueFilter +
                '}';
    }
}
