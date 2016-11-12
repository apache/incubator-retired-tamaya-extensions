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
package org.apache.tamaya.mutableconfig.propertysources;

import org.apache.tamaya.events.ConfigEventManager;
import org.apache.tamaya.events.FrozenPropertySource;
import org.apache.tamaya.events.PropertySourceChange;
import org.apache.tamaya.events.PropertySourceChangeBuilder;
import org.apache.tamaya.functions.Supplier;
import org.apache.tamaya.mutableconfig.RefreshablePropertySource;
import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertyValue;
import org.apache.tamaya.spisupport.BasePropertySource;

import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple implementation of a mutable {@link PropertySource} for .properties files.
 */
public class LazyRefreshablePropertySource extends BasePropertySource
implements RefreshablePropertySource {

    /**
     * The logger.
     */
    private static final Logger LOG = Logger.getLogger(LazyRefreshablePropertySource.class.getName());

    /**
     * Default update interval is 1 minute.
     */
    private static final long DEFAULT_UPDATE_INTERVAL = 60000L;

    /**
     * The property source name.
     */
    private Supplier<PropertySource> propertySourceSupplier;

    /**
     * The current propertySource.
     */
    private PropertySource propertySource;

    /**
     * Timestamp of last read.
     */
    private long lastRead;

    /**
     * Interval, when the resource should try to update its contents.
     */
    private long updateInterval = DEFAULT_UPDATE_INTERVAL;

    private static boolean eventSupportLoaded = checkEventSupport();

    private static boolean checkEventSupport() {
        try{
            Class.forName("org.apache.tamaya.events.ConfigEventManager");
            return true;
        }catch(Exception e){
            return false;
        }
    }


    /**
     * Creates a new Properties based PropertySource based on the given URL.
     *
     * @param defaultOrdinal the default ordinal to be used.
     * @param propertySourceSupplier the property source supplier, not null.
     */
    private LazyRefreshablePropertySource(Supplier<PropertySource> propertySourceSupplier, int defaultOrdinal) {
        super(defaultOrdinal);
        this.propertySourceSupplier = Objects.requireNonNull(propertySourceSupplier);
        this.propertySource = Objects.requireNonNull(propertySourceSupplier.get());
    }

    /**
     * Creates a new Properties based PropertySource based on the given URL.
     *
     * @param propertySourceSupplier the property source supplier, not null.
     */
    private LazyRefreshablePropertySource(Supplier<PropertySource> propertySourceSupplier) {
        this.propertySourceSupplier = Objects.requireNonNull(propertySourceSupplier);
        this.propertySource = Objects.requireNonNull(propertySourceSupplier.get());
    }

    /**
     * Creates a new Properties based PropertySource based on the given URL.
     *
     * @param defaultOrdinal the default ordinal to be used.
     * @param propertySourceSupplier the property source supplier, not null.
     */
    public static LazyRefreshablePropertySource of(Supplier<PropertySource> propertySourceSupplier, int defaultOrdinal) {
        return new LazyRefreshablePropertySource(propertySourceSupplier, defaultOrdinal);
    }

    /**
     * Creates a new Properties based PropertySource based on the given URL.
     *
     * @param propertySourceSupplier the property source supplier, not null.
     */
    public static LazyRefreshablePropertySource of(Supplier<PropertySource> propertySourceSupplier) {
        return new LazyRefreshablePropertySource(propertySourceSupplier);
    }

    /**
     * Sets the current refreh interval.
     * @param millis the new refreh interval in millis.
     */
    public void setUpdateInterval(long millis){
        this.updateInterval = millis;
    }

    /**
     * Access the current refresh interval.
     * @return the current refresh interval.
     */
    public long getDefaultUpdateInterval(){
        return this.updateInterval;
    }

    @Override
    public PropertyValue get(String key) {
        checkLoad();
        return this.propertySource.get(key);
    }

    @Override
    public String getName() {
        return this.propertySource.getName();
    }

    @Override
    public Map<String, String> getProperties() {
        checkLoad();
        return this.propertySource.getProperties();
    }


    private void checkLoad() {
        if((lastRead+updateInterval)<System.currentTimeMillis()){
            refresh();
        }
    }

    /**
     * Reloads the property source from its supplier. If Tamaya's event module is loaded corresoinding
     * change events are triggered if changes were detected.
     */
    @Override
    public void refresh() {
        try{
            Object previous = null;
            if(eventSupportLoaded){
                previous = FrozenPropertySource.of(this.propertySource);
            }
            this.propertySource = Objects.requireNonNull(propertySourceSupplier.get());
            if(eventSupportLoaded){
                PropertySourceChange changeEvent = PropertySourceChangeBuilder.of(
                        (PropertySource)previous)
                        .addChanges(this.propertySource).build();
                if(!changeEvent.isEmpty()) {
                    ConfigEventManager.fireEvent(changeEvent);
                }
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Cannot refresh property source " + propertySource.getName(), e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        if (!(o instanceof LazyRefreshablePropertySource)){
            return false;
        }

        LazyRefreshablePropertySource that = (LazyRefreshablePropertySource) o;

        return propertySource.getName().equals(that.propertySource.getName());

    }

    @Override
    public int hashCode() {
        return propertySource.getName().hashCode();
    }

    @Override
    public String toString() {
        return "RefreshablePropertySource{" +
                "\n  name=" + getName() +
                "\n  delegate=" + propertySource +
                "\n  lastRead=" + lastRead +
                "\n  updateInterval=" + updateInterval +
                "\n}";
    }
}
