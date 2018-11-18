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
package org.apache.tamaya.events;

import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertyValue;

import java.util.HashMap;
import java.util.Map;

/**
 * PropertySource that provides a random entry, different on each access!
 */
public class RandomPropertySource implements PropertySource{

    private Map<String, PropertyValue> data = new HashMap<>();

    @Override
    public int getOrdinal() {
        return 0;
    }

    @Override
    public String getName() {
        return "random";
    }

    @Override
    public PropertyValue get(String key) {
        if(key.equals("random.new")){
            return PropertyValue.createValue(key, String.valueOf(Math.random()));
        }
        return null;
    }

    @Override
    public Map<String, PropertyValue> getProperties() {
        synchronized(data) {
            data.put("random.new", PropertyValue.createValue("random.new", String.valueOf(Math.random()))
            .setMeta("_random.new.timestamp", String.valueOf(System.currentTimeMillis())));
            return new HashMap<>(data);
        }
    }

}
