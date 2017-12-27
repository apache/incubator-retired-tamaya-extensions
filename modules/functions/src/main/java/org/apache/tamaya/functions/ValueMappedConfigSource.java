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

import org.apache.tamaya.base.configsource.ConfigSourceComparator;

import javax.config.spi.ConfigSource;
import java.util.*;


/**
 * Property source which filters any key/values dynamically.
 */
class ValueMappedConfigSource implements ConfigSource{

    private final String name;
    private final PropertyMapper valueFilter;
    private final ConfigSource source;

    public ValueMappedConfigSource(String name, PropertyMapper valueFilter, ConfigSource current) {
        this.name =  name!=null?name:"<valueFiltered> -> name="+current.getName()+", valueFilter="+valueFilter.toString();
        this.valueFilter = valueFilter;
        this.source = Objects.requireNonNull(current);
    }

    @Override
    public int getOrdinal() {
        return ConfigSourceComparator.getOrdinal(source);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue(String key) {
        return this.source.getValue(key);
    }

    @Override
    public Map<String, String> getProperties() {
        Map<String,String> result = new HashMap<>();
        for(Map.Entry<String,String> en: source.getProperties().entrySet()) {
            String mappedValue = valueFilter.mapProperty(en.getKey(), en.getValue());
            result.put(en.getKey(), mappedValue);
        }
        return result;
    }

    @Override
    public String toString() {
        return "ValueMappedConfigSource{" +
                "source=" + source.getName() +
                ", name='" + name + '\'' +
                ", valueFilter=" + valueFilter +
                '}';
    }
}
