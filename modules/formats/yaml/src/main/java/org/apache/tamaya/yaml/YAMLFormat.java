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
import org.apache.tamaya.spi.ListValue;
import org.apache.tamaya.spi.ObjectValue;
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
            PropertyValue data;
            Object config = yaml.load(inputStream);
            if(config instanceof Map){
                data = PropertyValue.createObject("");
                data.setMeta("resource", resource);
                data.setMeta("format", "yaml");
                addObject((Map)config, (ObjectValue)data);
            }else if(config instanceof List){
                data = PropertyValue.createList("");
                data.setMeta("resource", resource);
                data.setMeta("format", "yaml");
                addList((List)config, (ListValue)data);
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


    private void addObject(Map<String,Object> values, ObjectValue dataNode){
        values.entrySet().forEach(en -> {
            if (en.getValue() instanceof List) {
                ListValue list = dataNode.setList(en.getKey());
                addList((List) en.getValue(), list);
            } else if (en.getValue() instanceof Map) {
                ObjectValue object = dataNode.setObject(en.getKey());
                addObject((Map) en.getValue(), object);
            } else{
                if (en.getValue() == null) {
                    dataNode.setValue(en.getKey(), null);
                }else {
                    dataNode.setValue(en.getKey(), String.valueOf(en.getValue()));
                }
            }
        });
    }

    private void addList(List<Object> values, ListValue dataNode) {
        values.forEach(val -> {
            if (val instanceof List) {
                ListValue list = dataNode.addList();
                addList((List) val, list);
            } else if (val instanceof Map) {
                ObjectValue ov = dataNode.addObject();
                addObject((Map) val, ov);
            } else{
                dataNode.setValue(String.valueOf(val));
            }
        });
    }

}
