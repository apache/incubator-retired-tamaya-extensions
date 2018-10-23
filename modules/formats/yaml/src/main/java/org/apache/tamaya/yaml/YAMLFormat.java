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
package org.apache.tamaya.yaml;

import org.apache.tamaya.ConfigException;
import org.apache.tamaya.format.ConfigurationData;
import org.apache.tamaya.format.ConfigurationFormat;
import org.apache.tamaya.spi.PropertyValue;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;


/**
 * Implementation of the {@link org.apache.tamaya.format.ConfigurationFormat}
 * able to read configuration properties represented in JSON
 *
 * @see <a href="http://www.json.org">JSON format specification</a>
 */
public class YAMLFormat implements ConfigurationFormat {
    /**
     * THe logger.
     */
    private static final Logger LOG = Logger.getLogger(YAMLFormat.class.getName());

    @Override
    public String getName() {
        return "yaml";
    }

    @Override
    public boolean accepts(URL url) {
        return Objects.requireNonNull(url).getPath().endsWith(".yaml");
    }

    @Override
    public ConfigurationData readConfiguration(String resource, InputStream inputStream) {
        try{
            Yaml yaml = new Yaml();
            PropertyValue data = PropertyValue.create();
            data.setMeta("resource", resource);
            data.setMeta("format", "yaml");
            Object config = yaml.load(inputStream);
            if(config instanceof Map){
                addObject((Map)config, data, null);
            }else if(config instanceof List){
                addArray((List)config, data, null);
            }else {
                throw new ConfigException("Unknown YamlType encountered: " + config.getClass().getName());
            }
            if(LOG.isLoggable(Level.FINEST)){
                LOG.finest(String.format("Read data from " + resource + " : " + data.asString()));
            }
            return new ConfigurationData(resource, this, data);
        }
        catch (Throwable t) {
            throw new ConfigException(format("Failed to read properties from %s", resource), t);
        }
    }


    private void addObject(Map<String,Object> object, PropertyValue parent, String objectKey){
        PropertyValue dataNode = objectKey==null?parent:parent.getOrCreateChild(objectKey);
        object.entrySet().forEach(en -> {
            if (en.getValue() instanceof List) {
                addArray((List) en.getValue(), dataNode, en.getKey());
            } else if (en.getValue() instanceof Map) {
                addObject((Map) en.getValue(), dataNode, en.getKey());
            } else{
                dataNode.createChild(en.getKey(), String.valueOf(en.getValue()));
            }
        });
    }

    private void addArray(List<Object> array, PropertyValue parent, String arrayKey) {
        array.forEach(val -> {
            PropertyValue dataNode = parent.createChild(arrayKey, true);
            if (val instanceof List) {
                addArray((List) val, dataNode, "");
            } else if (val instanceof Map) {
                addObject((Map) val, dataNode, null);
            } else{
                dataNode.setValue(String.valueOf(val));
            }
        });
    }

}
