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

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

import java.util.function.Supplier;

/**
 * OSGI service interface registered by the Tamaya configuration injector plugin.
 */
public interface ConfigInjectionService {
    /** The manifest entry to enable Tamaya injection. */
    String TAMAYA_INJECTION_ENABLED_MANIFEST = "Tamaya-Config-Inject";
    /** The OSGI config entry to enable Tamaya injection. */
    String TAMAYA_INJECTION_ENABLED_PROP = "tamaya-config-inject";

    /**
     * Checks if injection is enabled on the given service.
     * @param reference the service reference, not null.
     * @return true, if enjection is enabled.
     */
    boolean isInjectionEnabled(ServiceReference reference);

    /**
     * Checks if injection is enabled on the given service.
     * @param bundle the bundle, not null.
     * @return true, if enjection is enabled.
     */
    boolean isInjectionEnabled(Bundle bundle);

    /**
     * Configures the passed instance.
     * @param instance the instance, not null.
     * @param <T> the input and return type.
     * @param pid the target PID, not null.
     * @param location the optional location
     * @return the configured instance.
     */
    <T> T configure(String pid, String location, T instance);

    /**
     * Creates a suzpplier, which supplies events as created by the basic supplier, which are
     * automatically configured, when supplying.
     * @param supplier the base supplier, not null.
     * @param pid the target PID, not null.
     * @param location the optional location
     * @param <T> the type
     * @return a configuring supplier.
     */
    <T> Supplier<T> getConfiguredSupplier(String pid, String location, java.util.function.Supplier<T> supplier);

    /**
     * Creates a template implementing the annotated methods based on current configuration data.
     *
     * @param <T> the type of the template.
     * @param templateType the type of the template to be created.
     * @param pid the target PID, not null.
     * @param location the optional location
     * @return the configured template.
     */
    <T> T createTemplate(String pid, String location, Class<T> templateType);

    /**
     * Configures the passed instance.
     * @param instance the instance, not null.
     * @param <T> the input and return type.
     * @param bundle the target bundle, not null.
     * @return the configured instance.
     */
    default <T> T configure(Bundle bundle, T instance){
        return configure(bundle.getSymbolicName(), bundle.getLocation(), instance);
    }

    /**
     * Creates a suzpplier, which supplies events as created by the basic supplier, which are
     * automatically configured, when supplying.
     * @param supplier the base supplier, not null.
     * @param bundle the target bundle, not null.
     * @param <T> the type
     * @return a configuring supplier.
     */
    default <T> Supplier<T> getConfiguredSupplier(Bundle bundle, java.util.function.Supplier<T> supplier){
        return getConfiguredSupplier(bundle.getSymbolicName(), bundle.getLocation(), supplier);
    }

    /**
     * Creates a template implementing the annotated methods based on current configuration data.
     *
     * @param <T> the type of the template.
     * @param templateType the type of the template to be created.
     * @param bundle the target bundle, not null.
     * @return the configured template.
     */
    default <T> T createTemplate(Bundle bundle, Class<T> templateType){
        return createTemplate(bundle.getSymbolicName(), bundle.getLocation(), templateType);
    }
}
