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
package org.apache.tamaya.integration.cdi;

import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.TypeLiteral;
import org.apache.tamaya.spi.ConfigurationContext;
import org.apache.tamaya.spi.ConfigurationContextBuilder;
import org.apache.tamaya.spi.PropertyConverter;
import org.apache.tamaya.spi.PropertyFilter;
import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertySourceProvider;
import org.apache.tamaya.spi.PropertyValueCombinationPolicy;
import org.apache.tamaya.spi.ServiceContextManager;
import org.apache.tamaya.spisupport.PropertyFilterComparator;
import org.apache.tamaya.spisupport.PropertySourceComparator;

import javax.enterprise.inject.Vetoed;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

/**
 * Default Implementation of a simple ConfigurationContext.
 */
@SuppressWarnings("FieldCanBeLocal")
@Vetoed
public class DefaultConfigurationContext implements ConfigurationContext {
    /** The logger used. */
    private final static Logger LOG = Logger.getLogger(DefaultConfigurationContext.class.getName());
    /**
     * Cubcomponent handling {@link PropertyConverter} instances.
     */
    private final PropertyConverterManager propertyConverterManager = new PropertyConverterManager();

    /**
     * The current unmodifiable list of loaded {@link PropertySource} instances.
     */
    private List<PropertySource> immutablePropertySources;

    /**
     * The current unmodifiable list of loaded {@link PropertyFilter} instances.
     */
    private List<PropertyFilter> immutablePropertyFilters;

    /**
     * The overriding policy used when combining PropertySources registered to evalute the final configuration
     * values.
     */
    private PropertyValueCombinationPolicy propertyValueCombinationPolicy;

    /**
     * Lock for internal synchronization.
     */
    private final ReentrantReadWriteLock propertySourceLock = new ReentrantReadWriteLock();

    /**
     * The first time the Configuration system gets invoked we do initialize
     * all our {@link PropertySource}s and
     * {@link PropertyFilter}s which are known at startup.
     */
    public DefaultConfigurationContext() {
        List<PropertySource> propertySources = new ArrayList<>();

        // first we load all PropertySources which got registered via java.util.ServiceLoader
        propertySources.addAll(ServiceContextManager.getServiceContext().getServices(PropertySource.class));

        // after that we add all PropertySources which get dynamically registered via their PropertySourceProviders
        propertySources.addAll(evaluatePropertySourcesFromProviders());

        // now sort them according to their ordinal values
        Collections.sort(propertySources, PropertySourceComparator.getInstance());

        immutablePropertySources = Collections.unmodifiableList(propertySources);
        LOG.info("Registered " + immutablePropertySources.size() + " property sources: " +
                immutablePropertySources);

        // as next step we pick up the PropertyFilters pretty much the same way
        List<PropertyFilter> propertyFilters = new ArrayList<>();
        propertyFilters.addAll(ServiceContextManager.getServiceContext().getServices(PropertyFilter.class));
        Collections.sort(propertyFilters, PropertyFilterComparator.getInstance());
        immutablePropertyFilters = Collections.unmodifiableList(propertyFilters);
        LOG.info("Registered " + immutablePropertyFilters.size() + " property filters: " +
                immutablePropertyFilters);

        immutablePropertyFilters = Collections.unmodifiableList(propertyFilters);
        LOG.info("Registered " + immutablePropertyFilters.size() + " property filters: " +
                immutablePropertyFilters);
        propertyValueCombinationPolicy = ServiceContextManager.getServiceContext().getService(PropertyValueCombinationPolicy.class);
        if(propertyValueCombinationPolicy==null) {
            propertyValueCombinationPolicy = PropertyValueCombinationPolicy.DEFAULT_OVERRIDING_COLLECTOR;
        }
        LOG.info("Using PropertyValueCombinationPolicy: " + propertyValueCombinationPolicy);
    }

    DefaultConfigurationContext(ConfigurationContextBuilder builder) {
        List<PropertySource> propertySources = new ArrayList<>();
        // first we load all PropertySources which got registered via java.util.ServiceLoader
        propertySources.addAll(builder.getPropertySources());
        // now sort them according to their ordinal values
        Collections.sort(propertySources, PropertySourceComparator.getInstance());
        immutablePropertySources = Collections.unmodifiableList(propertySources);
        LOG.info("Registered " + immutablePropertySources.size() + " property sources: " +
                immutablePropertySources);

        // as next step we pick up the PropertyFilters pretty much the same way
        List<PropertyFilter> propertyFilters = new ArrayList<>();
        propertyFilters.addAll(ServiceContextManager.getServiceContext().getServices(PropertyFilter.class));
        Collections.sort(propertyFilters, PropertyFilterComparator.getInstance());
        immutablePropertyFilters = Collections.unmodifiableList(propertyFilters);
        LOG.info("Registered " + immutablePropertyFilters.size() + " property filters: " +
                immutablePropertyFilters);

        propertyValueCombinationPolicy = ServiceContextManager.getServiceContext().getService(PropertyValueCombinationPolicy.class);
        if(propertyValueCombinationPolicy==null){
            propertyValueCombinationPolicy = PropertyValueCombinationPolicy.DEFAULT_OVERRIDING_COLLECTOR;
        }
        LOG.info("Using PropertyValueCombinationPolicy: " + propertyValueCombinationPolicy);
    }



    /**
     * Pick up all {@link PropertySourceProvider}s and return all the
     * {@link PropertySource}s they like to register.
     */
    private Collection<? extends PropertySource> evaluatePropertySourcesFromProviders() {
        List<PropertySource> propertySources = new ArrayList<>();
        Collection<PropertySourceProvider> propertySourceProviders = ServiceContextManager.getServiceContext().getServices(PropertySourceProvider.class);
        for (PropertySourceProvider propertySourceProvider : propertySourceProviders) {
            Collection<PropertySource> sources = propertySourceProvider.getPropertySources();
            LOG.finer("PropertySourceProvider " + propertySourceProvider.getClass().getName() +
                    " provided the following property sources: " + sources);
                propertySources.addAll(sources);
        }
        return propertySources;
    }

    @Deprecated
    @Override
    public void addPropertySources(PropertySource... propertySourcesToAdd) {
        Lock writeLock = propertySourceLock.writeLock();
        try {
            writeLock.lock();
            List<PropertySource> newPropertySources = new ArrayList<>(this.immutablePropertySources);
            newPropertySources.addAll(Arrays.asList(propertySourcesToAdd));
            Collections.sort(newPropertySources, PropertySourceComparator.getInstance());

            this.immutablePropertySources = Collections.unmodifiableList(newPropertySources);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public List<PropertySource> getPropertySources() {
        return immutablePropertySources;
    }

    @Override
    public PropertySource getPropertySource(String name) {
        for(PropertySource ps:getPropertySources()){
            if(ps.getName().equals(name)){
                return ps;
            }
        }
        return null;
    }

    @Override
    public <T> void addPropertyConverter(TypeLiteral<T> typeToConvert, PropertyConverter<T> propertyConverter) {
        propertyConverterManager.register(typeToConvert, propertyConverter);
        LOG.info("Added PropertyConverter: " + propertyConverter.getClass().getName());
    }

    @Override
    public Map<TypeLiteral<?>, List<PropertyConverter<?>>> getPropertyConverters() {
        return propertyConverterManager.getPropertyConverters();
    }

    @Override
    public <T> List<PropertyConverter<T>> getPropertyConverters(TypeLiteral<T> targetType) {
        return propertyConverterManager.getPropertyConverters(targetType);
    }

    @Override
    public List<PropertyFilter> getPropertyFilters() {
        return immutablePropertyFilters;
    }

    @Override
    public PropertyValueCombinationPolicy getPropertyValueCombinationPolicy(){
        return propertyValueCombinationPolicy;
    }

    @Override
    public ConfigurationContextBuilder toBuilder() {
        return ConfigurationProvider.getConfigurationContextBuilder().setContext(this);
    }

}
