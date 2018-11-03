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
 * PropertySource implementation that maps certain parts (defined by an {@code UnaryOperator<String>}) to alternate sections.
 */
class MappedPropertySource implements PropertySource {

    private static final long serialVersionUID = 8690637705511432083L;

    /**
     * The mapping operator.
     */
    private final KeyMapper keyMapper;

    /**
     * The base configuration.
     */
    private final PropertySource propertySource;

    /**
     * Creates a new instance.
     *
     * @param config    the base configuration, not null
     * @param keyMapper The mapping operator, not null
     */
    public MappedPropertySource(PropertySource config, KeyMapper keyMapper) {
        this.propertySource = Objects.requireNonNull(config);
        this.keyMapper = Objects.requireNonNull(keyMapper);
    }

    @Override
    public int getOrdinal() {
        return PropertySourceComparator.getOrdinal(this.propertySource);
    }

    @Override
    public String getName() {
        return this.propertySource.getName() + "[mapped]";
    }

    @Override
    public Map<String, PropertyValue> getProperties() {
        Map<String,PropertyValue> result = new HashMap<>();
        for (PropertyValue en : this.propertySource.getProperties().values()) {
            String targetKey = keyMapper.mapKey(en.getKey());
            if (targetKey != null) {
                result.put(targetKey, PropertyValue.of(targetKey, en.getValue(), getName()));
            }
        }
        return result;
    }

    @Override
    public boolean isScannable() {
        return propertySource.isScannable();
    }


    /**
     * <p>Access a property by its key.</p>
     *
     * <p>
     *  The key of the property to be returned must be equal to the key
     *  returned by the mapping operator (key mapper) and not equal
     *  to the key of the base configuration.
     * </p>
     *
     * @param key the property's key, not {@code null}.
     * @return the property createValue map, where {@code map.current(key) == createValue},
     *         including also any metadata. In case a createValue is {@code null},
     *         simply return {@code null}.
     */
    @Override
    public PropertyValue get(String key) {
        Objects.requireNonNull(key, "Key must be given.");

        String mappedKey = keyMapper.mapKey(key);
        PropertyValue result = null;

        if (mappedKey != null) {
            for (PropertyValue property : propertySource.getProperties().values()) {
                String newKey = keyMapper.mapKey(property.getKey());

                if (mappedKey.equals(newKey)) {
                    String mappedName = getName();
                    return PropertyValue.createValue(newKey, property.getValue())
                                   .setMeta("source", mappedName);
                }
            }
        }

        return result;
    }

}