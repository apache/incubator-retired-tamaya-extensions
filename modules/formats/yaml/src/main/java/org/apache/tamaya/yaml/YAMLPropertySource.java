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

import org.apache.tamaya.format.ConfigurationData;
import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertyValue;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * Property source based on a JSON file.
 */
public class YAMLPropertySource implements PropertySource {
    /** The underlying resource. */
    private final URL urlResource;
    /** The values read. */
    private final Map<String, PropertyValue> values;
    /** The evaluated ordinal. */
    private int ordinal;
    /** The format implementation used for parsing. */
    private YAMLFormat format = new YAMLFormat();

    /**
     * Constructor, hereby using 0 as the default ordinal.
     * @param resource the resource modelled as URL, not null.
     */
    public YAMLPropertySource(URL resource) throws IOException {
        this(resource, 0);
    }

    /**
     * Constructor.
     * @param resource the resource modelled as URL, not null.
     * @param defaultOrdinal the defaultOrdinal to be used.
     */
    public YAMLPropertySource(URL resource, int defaultOrdinal) throws IOException {
        urlResource = Objects.requireNonNull(resource);
        this.ordinal = defaultOrdinal; // may be overriden by read...
        ConfigurationData data = format.readConfiguration(urlResource.toString(), resource.openStream());
        this.values = new HashMap<>();
        for(Map.Entry<String,String> en:data.getData().get(0).toMap().entrySet()){
            this.values.put(en.getKey(), PropertyValue.of(en.getKey(), en.getValue(), getName()));
        }
        if (data.getData().get(0).toMap().containsKey(TAMAYA_ORDINAL)) {
            this.ordinal = Integer.parseInt(data.getData().get(0).toMap().get(TAMAYA_ORDINAL));
        }
    }

    public int getOrdinal() {
        PropertyValue configuredOrdinal = get(TAMAYA_ORDINAL);
        if(configuredOrdinal!=null){
            try{
                return Integer.parseInt(configuredOrdinal.getValue());
            } catch(Exception e){
                Logger.getLogger(getClass().getName()).log(Level.WARNING,
                        "Configured Ordinal is not an int number: " + configuredOrdinal, e);
            }
        }
        return ordinal;
    }

    @Override
    public String getName() {
        return urlResource.toExternalForm();
    }

    @Override
    public PropertyValue get(String key) {
        return getProperties().get(key);
    }

    @Override
    public Map<String, PropertyValue> getProperties() {
        return Collections.unmodifiableMap(values);
    }


    @Override
    public boolean isScannable() {
        return true;
    }
}
