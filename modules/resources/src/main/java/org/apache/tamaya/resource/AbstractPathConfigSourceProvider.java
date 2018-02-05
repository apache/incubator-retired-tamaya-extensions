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
package org.apache.tamaya.resource;

import org.apache.tamaya.base.ServiceContext;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


import javax.config.spi.ConfigSource;
import javax.config.spi.ConfigSourceProvider;

/**
 * Abstract base class that uses a descriptive resource path to define the locations of configuration files to be
 * included into the configuration. This is especially useful, when the current configuration policy in place
 * does not define the exact file names, but the file locations, where configuration can be provided.
 */
public abstract class AbstractPathConfigSourceProvider implements ConfigSourceProvider{
    /** The log used. */
    private static final Logger LOG = Logger.getLogger(AbstractPathConfigSourceProvider.class.getName());
    /** The resource paths. */
    private String[] resourcePaths;


    /**
     * Creates a new instance using the given resource paths.
     * @param resourcePaths the resource paths, not null, not empty.
     */
    public AbstractPathConfigSourceProvider(String... resourcePaths){
        if(resourcePaths.length==0){
            throw new IllegalArgumentException("At least one resource path should be configured.");
        }

        setResourcePaths(resourcePaths);
    }

    @Override
    public Collection<ConfigSource> getConfigSources(ClassLoader classLoader) {
        List<ConfigSource> propertySources = new ArrayList<>();
        if(classLoader == null){
            classLoader = ServiceContext.defaultClassLoader();
        }
        for (String resource : getResourcePaths()) {
            try {
                // TODO Get a resource resolver for a certain classloader
                Collection<URL> resources = ConfigResources.getResourceResolver().getResources(resource);
                for (URL url : resources) {
                    try {
                        Collection<ConfigSource>  propertySourcesToInclude = getConfigSources(url);
                        if(propertySourcesToInclude!=null){
                            propertySources.addAll(propertySourcesToInclude);
                        }
                    } catch (Exception e) {
                        LOG.log(Level.WARNING, "Failed to read configuration from " + url, e);
                    }
                }
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Invalid resource path: " + resource, e);
            }
        }
        return propertySources;
    }

    protected String[] getResourcePaths() {
        return resourcePaths;
    }

    protected void setResourcePaths(String[] paths) {
        resourcePaths = paths.clone();
    }

    /**
     * Factory method that creates a {@link ConfigSource} based on the URL found by
     * the resource locator.
     * @param url the URL, not null.
     * @return the {@link ConfigSource}s to be included into the current provider's sources
     * list. It is safe to return {@code null} here, in case the content of the URL has shown to be not relevant
     * as configuration input. In case the input is not valid or accessible an exception can be thrown or logged.
     */
    protected abstract Collection<ConfigSource> getConfigSources(URL url);

    /**
     * Utility method that reads a .properties file from the given url and creates a corresponding
     * {@link ConfigSource}.
     * @param url the url to read, not null.
     * @return the corresponding PropertySource, or null.
     */
    public static ConfigSource createConfigSource(URL url) {
        Properties props = new Properties();
        try (InputStream is = url.openStream()){
            props.load(is);
            return new PropertiesBasedConfigSource(url.toString(), props);
        }
        catch (Exception e){
            LOG.log(Level.WARNING, "Failed to read properties from " + url, e);
            return null;
        }
    }

    /**
     * Minimal {@link ConfigSource} implementation based on {@link Properties} or
     * {@link Map}.
     */
    private final static class PropertiesBasedConfigSource implements ConfigSource{
        /** The property source's name. */
        private final String name;
        /** The properties. */
        private final Map<String,String> properties = new HashMap<>();

        /**
         * Constructor for a simple properties configuration.
         * @param name the source's name, not null
         * @param props the properties, not null
         */
        public PropertiesBasedConfigSource(String name, Properties props) {
            this.name = Objects.requireNonNull(name);
            for (Entry<Object, Object> en : props.entrySet()) {
                this.properties.put(en.getKey().toString(),
                        String.valueOf(en.getValue()));
            }
        }

        /**
         * Constructor for a simple properties configuration.
         * @param name the source's name, not null
         * @param props the properties, not null
         */
        public PropertiesBasedConfigSource(String name, Map<String,String> props) {
            this.name = Objects.requireNonNull(name);
            for (Entry<String, String> en : props.entrySet()) {
                this.properties.put(en.getKey(),
                        en.getValue());
            }
        }

        public int getOrdinal() {
            String configuredOrdinal = getValue(CONFIG_ORDINAL);
            if (configuredOrdinal != null) {
                try {
                    return Integer.parseInt(configuredOrdinal);
                } catch (Exception e) {
                    Logger.getLogger(getClass().getName()).log(Level.WARNING,
                            "Configured Ordinal is not an int number: " + configuredOrdinal, e);
                }
            }
            return getDefaultOrdinal();
        }

        /**
         * Returns the  default ordinal used, when no ordinal is set, or the ordinal was not parseable to an int value.
         *
         * @return the  default ordinal used, by default 0.
         */
        public int getDefaultOrdinal() {
            return 0;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getValue(String key) {
            return this.properties.get(key);
        }

        @Override
        public Map<String, String> getProperties() {
            return properties;
        }

        @Override
        public String toString(){
            return "PropertiesBasedConfigSource["+name+']';
        }
    }

}
