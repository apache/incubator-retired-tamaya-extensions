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
package org.apache.tamaya.events.folderobserver;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tamaya.base.configsource.BaseConfigSource;
import org.apache.tamaya.events.ConfigSourceChange;

import javax.config.spi.ConfigSource;
import javax.config.spi.ConfigSourceProvider;

/**
 * This implementation runs in a folder taking up all files compatible with the given
 * ConfigurationFormats. When a file is added, deleted or modified the PropertySourceProvider
 * will adapt the changes automatically and trigger according
 * {@link ConfigSourceChange} events.
 * The default folder is META-INF/config, but you can change it via an absolute path in the
 * "-Dtamaya.configdir" parameter.
 */
public class ObservingPropertySourceProvider implements ConfigSourceProvider, FileChangeObserver {
    /**
     * The logger.
     */
    private static final Logger LOG = Logger.getLogger(ObservingPropertySourceProvider.class.getName());
    /**
     * The current active property sources of this provider.
     */
    private final List<ConfigSource> configSources = Collections.synchronizedList(new LinkedList<ConfigSource>());
    /**
     * The thread pool used.
     */
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * Constructor using an explicit directory, ignoring all kind of configuration, if set.
     *
     * @param directory the target directory. If null, the default configuration and system property are used.
     */
    public ObservingPropertySourceProvider(Path directory) {
        if (directory == null) {
            directory = getDirectory();
        }
        if (directory!=null){
            synchronized (this.configSources) {
                this.configSources.addAll(readConfiguration(directory));
            }
            final Runnable runnable = new FileChangeListener(directory, this);
            executor.execute(runnable);
        } else {
            executor.shutdown();
        }
    }

    /**
     * Read the initial configuration.
     *
     * @param directory the target directory, not null.
     */
    private List<ConfigSource> readConfiguration(Path directory) {
        final List<ConfigSource> result = new ArrayList<>();
        try {
            synchronized (configSources) {
                for (final Path path : Files.newDirectoryStream(directory, "*")) {
                    result.addAll(getConfigSources(path));
                }
                return result;
            }
        } catch (final IOException e) {
            LOG.log(Level.WARNING, "Failed to read configuration from dir: " + directory, e);
        }
        return result;
    }

    /**
     * Read property sources from the given file.
     * 
     * @param file source of the property sources.
     * @return property sources from the given file.
     */
    protected Collection<ConfigSource> getConfigSources(final Path file) {
        return Arrays.asList(new ConfigSource[]{new BaseConfigSource(file.toString()) {
            private final Map<String,String> props = readProperties(file);

            @Override
            public Map<String, String> getProperties() {
                return props;
            }
        }});
    }

    /**
     * Load a single file.
     *
     * @param file the file, not null.
     * @return properties as read from the given file.
     */
    protected static Map<String,String> readProperties(Path file) {
        try (InputStream is = file.toUri().toURL().openStream()){
            final Properties props = new Properties();
                props.load(is);
            final Map<String,String> result = new HashMap<>();
            for(final Map.Entry<Object,Object> en:props.entrySet()){
                String key = String.valueOf(en.getKey());
                result.put(key, en.getValue().toString());
            }
            return result;
        } catch (final Exception e) {
            LOG.log(Level.INFO, "Error reading file: " + file.toString() +
                    ", using format: properties", e);
        }
        return Collections.emptyMap();
    }


    /**
     * Evaluates the target directory from system property (tamaya.configdir) or classpath.
     *
     * @return the directory to be read, or null.
     */
    private Path getDirectory() {
        final String absolutePath = System.getProperty("tamaya.configdir");
        if (null!=absolutePath) {
            final Path path = Paths.get(absolutePath);
            if (Files.isDirectory(path)) {
                return path;
            }
        }
        final URL resource = ObservingPropertySourceProvider.class.getResource("/META-INF/config/");
        if (null!=resource) {
            try {
                return Paths.get(resource.toURI());
            } catch (final URISyntaxException e) {
                throw new IllegalArgumentException("An error to find the directory to watch", e);
            }
        }
        return null;
    }


    @Override
    public void directoryChanged(Path directory) {
        synchronized (this.configSources) {
            configSources.clear();
            final Collection<ConfigSource> sourcesRead = readConfiguration(directory);
            this.configSources.addAll(sourcesRead);
        }
    }

    @Override
    public Collection<ConfigSource> getConfigSources(ClassLoader classLoader) {
        synchronized (configSources) {
            return new ArrayList<>(this.configSources);
        }
    }
}
