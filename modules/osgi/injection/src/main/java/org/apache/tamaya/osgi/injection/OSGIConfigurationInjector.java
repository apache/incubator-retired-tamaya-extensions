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
package org.apache.tamaya.osgi.injection;

import org.apache.tamaya.Configuration;
import org.apache.tamaya.inject.ConfigurationInjector;
import org.osgi.service.cm.ConfigurationAdmin;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * This class wraps an OSGI Configuration based on PID and location and provides injection services for
 * configuration.
 */
final class OSGIConfigurationInjector{
    /** The corresponding Tamaya configuration. */
    private Configuration tamayaOSGIConfiguration;
    /** The target PID. */
    private String pid;
    /** The target location. */
    private String location;

    /**
     * Creates a new instance.
     * @param cm the OSGI ConfigManager, not null.
     * @param pid the target PID.
     */
    public OSGIConfigurationInjector(ConfigurationAdmin cm, String pid){
        this(cm, pid, null);
    }

    /**
     * Creates a new instance.
     * @param cm the OSGI ConfigManager, not null.
     * @param pid the target PID.
     * @param location the optional location.
     */
    public OSGIConfigurationInjector(ConfigurationAdmin cm, String pid, String location){
        /** The OSGI ConfigManager. */
        this.pid = Objects.requireNonNull(pid);
        this.location = location;
        tamayaOSGIConfiguration = Configuration.createConfigurationBuilder()
                .addDefaultPropertyConverters()
                .addDefaultPropertyFilters()
                .addPropertySources(new OSGIConfigAdminPropertySource(Objects.requireNonNull(cm), pid, location))
                .build();
    }

    /**
     * Get the target PID.
     * @return the target PID, not null.
     */
    public String getPid() {
        return pid;
    }

    /**
     * Get the location.
     * @return the location.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Configures the passed instance.
     * @param instance the instance, not null.
     * @param <T> the input and return type.
     * @return the configured instance.
     */
    public <T> T configure(T instance){
        return ConfigurationInjector.getInstance()
                .configure(instance, tamayaOSGIConfiguration);
    }

    /**
     * Creates a suzpplier, which supplies events as created by the basic supplier, which are
     * automatically configured, when supplying.
     * @param supplier the base supplier, not null.
     * @param <T> the type
     * @return a configuring supplier.
     */
    public <T> Supplier<T> getConfiguredSupplier(java.util.function.Supplier<T> supplier){
        return ConfigurationInjector.getInstance()
                .getConfiguredSupplier(supplier, tamayaOSGIConfiguration);
    }

    /**
     * Creates a template implementing the annotated methods based on current configuration data.
     *
     * @param <T> the type of the template.
     * @param templateType the type of the template to be created.
     * @return the configured template.
     */
    public <T> T createTemplate(Class<T> templateType){
        return ConfigurationInjector.getInstance()
                .createTemplate(templateType, tamayaOSGIConfiguration);
    }
}
