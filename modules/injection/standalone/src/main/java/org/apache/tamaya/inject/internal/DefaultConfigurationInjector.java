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
package org.apache.tamaya.inject.internal;

import org.apache.tamaya.inject.ConfigurationInjector;

import javax.annotation.Priority;
import javax.config.ConfigProvider;
import javax.config.inject.ConfigProperty;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.logging.Logger;

import org.apache.tamaya.inject.api.NoConfig;
import org.apache.tamaya.inject.api.ConfigDefaultSections;
import org.apache.tamaya.inject.spi.ConfiguredType;
import org.osgi.service.component.annotations.Component;

/**
 * Simple injector singleton that also registers instances configured using weak references.
 */
@Priority(0)
@Component
public class DefaultConfigurationInjector implements ConfigurationInjector {

    private final Map<Class<?>, ConfiguredType> configuredTypes = new ConcurrentHashMap<>();

    private static final Logger LOG = Logger.getLogger(DefaultConfigurationInjector.class.getName());

    private boolean autoConfigureEnabled = true;

    /**
     * Extract the configuration annotation config and registers it per class, for later reuse.
     *
     * @param type the type to be configured.
     * @return the configured type registered.
     */
    public ConfiguredType getConfiguredType(Class<?> type) {
        ConfiguredType confType = configuredTypes.get(type);
        if (confType == null) {
            if(!isConfigAnnotated(type) && !autoConfigureEnabled){
                return null;
            }
            confType = new ConfiguredTypeImpl(type);
        }
        return confType;
    }

    /**
     * Extract the configuration annotation config and registers it per class, for later reuse.
     *
     * @param type the type to be configured.
     * @return the configured type registered.
     */
    public ConfiguredType registerType(Class<?> type) {
        ConfiguredType confType = getConfiguredType(type);
        if (confType != null) {
            configuredTypes.put(type, confType);
            InjectionHelper.sendConfigurationEvent(confType);
        }
        return confType;
    }

    /**
     * If set also non annotated instances can be configured or created as templates.
     * @return true, if autoConfigureEnabled.
     */
    public boolean isAutoConfigureEnabled(){
        return autoConfigureEnabled;
    }

    /**
     * Setting to true enables also configuration/templating of non annotated classes or
     * interfaces.
     * @param enabled true enables also configuration/templating of
     */
    public void setAutoConfigureEnabled(boolean enabled){
        this.autoConfigureEnabled = enabled;
    }

    /**
     * CHecks if type is eligible for configuration injection.
     * @param type the target type, not null.
     * @return true, if the type, a method or field has Tamaya config annotation on it.
     */
    private boolean isConfigAnnotated(Class<?> type) {
        if(type.getClass().isAnnotationPresent(ConfigDefaultSections.class)){
            return true;
        }
        for (Field f : type.getDeclaredFields()) {
            if (f.isAnnotationPresent(NoConfig.class) || f.isAnnotationPresent(ConfigProperty.class)) {
                return true;
            }
        }
        for (Method m : type.getDeclaredMethods()) {
            if (m.isAnnotationPresent(NoConfig.class) || m.isAnnotationPresent(ConfigProperty.class)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Configured the current instance and registerd necessary listener to forward config change events as
     * defined by the current annotations in place.
     *
     * @param instance the instance to be configured
     */
    @Override
    public <T> T configure(T instance) {
        return configure(instance, ConfigProvider.getConfig());
    }

    /**
     * Configured the current instance and registerd necessary listener to forward config change events as
     * defined by the current annotations in place.
     *
     * @param instance the instance to be configured
     * @param config the target configuration, not null.
     */
    @Override
    public <T> T configure(T instance, javax.config.Config config) {
        Class<?> type = Objects.requireNonNull(instance).getClass();
        ConfiguredType configuredType = registerType(type);
        if(configuredType!=null){
            configuredType.configure(instance, config);
        }else{
            LOG.info("Instance passed is not configurable: " + instance);
        }
        return instance;
    }

    /**
     * Create a template implementing the annotated methods based on current configuration data.
     *
     * @param templateType the type of the template to be created.
     */
    @Override
    public <T> T createTemplate(Class<T> templateType) {
        return createTemplate(templateType, ConfigProvider.getConfig());
    }

    /**
     * Create a template implementing the annotated methods based on current configuration data.
     *
     * @param templateType the type of the template to be created.
     * @param config the target configuration, not null.
     */
    @Override
    public <T> T createTemplate(Class<T> templateType, javax.config.Config config) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if(cl==null){
            cl = this.getClass().getClassLoader();
        }
        return templateType.cast(Proxy.newProxyInstance(cl, new Class[]{Supplier.class, Objects.requireNonNull(templateType)},
                new ConfigTemplateInvocationHandler(templateType, config)));
    }

    @Override
    public <T> Supplier<T> getConfiguredSupplier(final Supplier<T> supplier) {
        return getConfiguredSupplier(supplier, ConfigProvider.getConfig());
    }

    @Override
    public <T> Supplier<T> getConfiguredSupplier(final Supplier<T> supplier, final javax.config.Config config) {
        return () -> configure(supplier.get(), config);
    }

    @Override
    public boolean isConfigured(Object o) {
        return getConfiguredType(o.getClass())!=null;
    }
}
