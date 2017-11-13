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
package org.apache.tamaya.ext.examples.events;


import org.apache.tamaya.ConfigException;
import org.apache.tamaya.spisupport.propertysource.BasePropertySource;
import org.apache.tamaya.resource.AbstractPathPropertySourceProvider;
import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertyValue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import static org.apache.tamaya.ext.examples.events.Main.getPropertiesFilePath;

public class FilePropertySourceProvider extends AbstractPathPropertySourceProvider {
    public FilePropertySourceProvider() {
        super(getPropertiesFilePath().toString());
    }

    @Override
    protected Collection<PropertySource> getPropertySources(URL url) {
        return Arrays.asList(new PropertySource[] { new DumbReloadingPropertySource(url) });
    }

    public static class DumbReloadingPropertySource extends BasePropertySource {
        private final URL propertiesFile;

        public DumbReloadingPropertySource(URL url) {
            propertiesFile = url;
        }

        @Override
        public Map<String, PropertyValue> getProperties() {

            Map<String, PropertyValue> properties = new HashMap<>();
            try (InputStream stream = propertiesFile.openStream()) {
                Properties props = new Properties();
                if (stream != null) {
                    props.load(stream);
                }

                for (String key : props.stringPropertyNames()) {
                    properties.put(key, PropertyValue.of(key, props.getProperty(key), getName()));
                }
            } catch (IOException e) {
                throw new ConfigException("Error loading properties from " + propertiesFile, e);
            }

            return properties;

        }
    }
}
