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

import javax.config.spi.ConfigSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InMemoryConfigSource implements ConfigSource {
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

    public InMemoryConfigSource setName(String name) {
        this.name = name;

        return this;
    }

    @Override
    public String getValue(String key) {
        return properties.get(key);
    }

    public InMemoryConfigSource add(String key, String value) {
        properties.put(key, value);

        return this;
    }

    @Override
    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

}
