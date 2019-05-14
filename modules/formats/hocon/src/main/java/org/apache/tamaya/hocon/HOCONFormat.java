/*
 * Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.tamaya.hocon;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigList;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import org.apache.tamaya.format.ConfigurationData;
import org.apache.tamaya.format.ConfigurationFormat;
import org.apache.tamaya.spi.ListValue;
import org.apache.tamaya.spi.ObjectValue;
import org.apache.tamaya.spi.PropertyValue;

/**
 * Typesafe (Lightbend) Config (HOCON) {@link org.apache.tamaya.format.ConfigurationFormat} implementation.
 */
public class HOCONFormat implements ConfigurationFormat {

    @Override
    public String getName() {
        return "hocon";
    }

    @Override
    public boolean accepts(URL url) {
        String path = Objects.requireNonNull(url).getPath().toLowerCase();
        return path.endsWith(".conf") ||
                path.endsWith(".hocon") ;
    }

    @Override
    public ConfigurationData readConfiguration(String resource, InputStream inputStream)
            throws IOException {
        Config typesafeConfig;
        try{
            typesafeConfig = ConfigFactory.parseReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            return new ConfigurationData(resource, this, buildConfigurationData(typesafeConfig.root()));
        } catch(Exception e) {
            throw new IOException("Failed to read data from " + resource, e);
        }
    }

    private static Collection<PropertyValue> buildConfigurationData(ConfigObject config) {
        ObjectValue root = PropertyValue.createObject();
        config.entrySet()
                .forEach(entry -> {
                    String key = entry.getKey();
                    ConfigValue value = entry.getValue();
                    if (value instanceof ConfigList) {
                        fillList(root.addList(key), (ConfigList) value);
                    } else if (value instanceof ConfigObject) {
                        fillObject(root.addObject(key), (ConfigObject) value);
                    } else {
                        root.setValue(key, value.unwrapped().toString());
                    }
                });
        return Collections.singletonList(root);
    }

    private static ListValue fillList(ListValue listValue, ConfigList list) {
        list.forEach(v -> {
            if (v instanceof ConfigList) {
                fillList(listValue.addList(), (ConfigList) v);
            } else if (v instanceof ConfigObject) {
                fillObject(listValue.addObject(), (ConfigObject) v);
            } else {
                listValue.addValue(v.unwrapped().toString());
            }
        });
        return listValue;
    }

    private static ObjectValue fillObject(ObjectValue objectValue, ConfigObject object) {
        object.forEach((k, v) -> {
            if (v instanceof ConfigList) {
                fillList(objectValue.addList(k), (ConfigList) v);
            } else if (v instanceof ConfigObject) {
                fillObject(objectValue.addObject(k), (ConfigObject) v);
            } else {
                objectValue.setValue(k, v.unwrapped().toString());
            }
        });
        return objectValue;
    }

}