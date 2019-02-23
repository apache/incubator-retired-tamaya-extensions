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

import java.util.HashMap;
import java.util.Map;

public class InMemoryPropertySource implements PropertySource {
    private int ordinal;
    private String name;
    private Map<String, String> properties = new HashMap<>();
    private boolean isScannable;

    @Override
    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    @Override
    public String getName() {
        return name;
    }

    public InMemoryPropertySource setName(String name) {
        this.name = name;

        return this;
    }

    @Override
    public PropertyValue get(String key) {
        String value = properties.get(key);

        return PropertyValue.createValue(key, value).setMeta("source", getName());
    }

    public InMemoryPropertySource add(String key, String value) {
        properties.put(key, value);

        return this;
    }

    @Override
    public Map<String, PropertyValue> getProperties() {
        Map<String, PropertyValue> result = new HashMap<>();

        for (Map.Entry<String, String> entry : properties.entrySet()) {
            PropertyValue value = PropertyValue.createValue(entry.getKey(), entry.getValue()).setMeta("source", getName());
            result.put(entry.getKey(), value);
        }

        return result;
    }

    @Override
    public boolean isScannable() {
        return isScannable;
    }

    public void setScannable(boolean scannable) {
        isScannable = scannable;
    }
}
