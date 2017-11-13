/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tamaya.mutableconfig.propertysources;

import org.apache.tamaya.ConfigException;
import org.apache.tamaya.mutableconfig.ConfigChangeRequest;
import org.apache.tamaya.mutableconfig.spi.MutablePropertySource;
import org.apache.tamaya.spi.PropertyValue;
import org.apache.tamaya.spisupport.propertysource.BasePropertySource;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple implementation of a mutable {@link org.apache.tamaya.spi.PropertySource} for .xml properties files.
 */
public class MutableXmlPropertiesPropertySource extends BasePropertySource
implements MutablePropertySource{

    /**
     * The logger.
     */
    private static final Logger LOG = Logger.getLogger(MutableXmlPropertiesPropertySource.class.getName());

    /**
     * The configuration resource's URL.
     */
    private File file;

    /**
     * The current properties.
     */
    private Map<String, String> properties = new HashMap<>();


    /**
     * Creates a new Properties based PropertySource based on the given URL.
     *
     * @param propertiesLocation the URL encoded location, not null.
     */
    public MutableXmlPropertiesPropertySource(File propertiesLocation) {
        this(propertiesLocation, 0);
    }

    /**
     * Creates a new Properties based PropertySource based on the given URL.
     *
     * @param propertiesLocation the URL encoded location, not null.
     * @param defaultOrdinal the default ordinal to be used, when no ordinal is provided with the property
     *                       source's properties.
     */
    public MutableXmlPropertiesPropertySource(File propertiesLocation, int defaultOrdinal) {
        super(propertiesLocation.toString(), defaultOrdinal);
        try {
            this.file = propertiesLocation;
            load();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Cannot convert file to URL: " + propertiesLocation, e);
        }
    }



    @Override
    public PropertyValue get(String key) {
        String val = this.properties.get(key);
        if(val!=null) {
            return PropertyValue.of(key, val, getName());
        }
        return null;
    }

    @Override
    public Map<String, PropertyValue> getProperties() {
        return PropertyValue.map(this.properties,getName());
    }

    /**
     * loads the Properties from the given URL
     *
     * @throws IllegalStateException in case of an error while reading properties-file
     */
    private void load() {
        try (InputStream stream = new FileInputStream(file)) {
            Map<String, String> properties = new HashMap<>();
            Properties props = new Properties();
            props.loadFromXML(stream);
            for (String key : props.stringPropertyNames()) {
                properties.put(key, props.getProperty(key));
            }
            this.properties = properties;
            LOG.log(Level.FINEST, "Loaded properties from " + file);
            this.properties = properties;
        } catch (IOException e) {
            LOG.log(Level.FINEST, "Cannot refresh properties from " + file, e);
        }
    }

    @Override
    public void applyChange(ConfigChangeRequest configChange) {
        if(configChange.isEmpty()){
            LOG.info("Nothing to commit for transaction: " + configChange.getTransactionID());
            return;
        }
        if(!file.exists()){
            try {
                if(!file.createNewFile()){
                    throw new ConfigException("Failed to create config file " + file);
                }
            } catch (IOException e) {
                throw new ConfigException("Failed to create config file " + file, e);
            }
        }
        for(Map.Entry<String,String> en:configChange.getAddedProperties().entrySet()){
            int index = en.getKey().indexOf('?');
            if(index>0){
                this.properties.put(en.getKey().substring(0, index), en.getValue());
            }else{
                this.properties.put(en.getKey(), en.getValue());
            }
        }
        for(String rmKey:configChange.getRemovedProperties()){
            this.properties.remove(rmKey);
        }
        try(BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))){
            Properties props = new Properties();
            for (Map.Entry<String,String> en : this.properties.entrySet()) {
                props.setProperty(en.getKey(), en.getValue());
            }
            props.storeToXML(bos, "Properties written from Tamaya on " + new Date());
            bos.flush();
        }
        catch(Exception e){
            throw new ConfigException("Failed to write config to " + file, e);
        }
    }

    @Override
    protected String toStringValues() {
        return  super.toStringValues() +
                "  file=" + file + '\n';
    }


}
