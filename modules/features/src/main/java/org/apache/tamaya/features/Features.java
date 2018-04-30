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
package org.apache.tamaya.features;

import javax.naming.InitialContext;
import java.util.Objects;

/**
 * Simple Features singleton to check, which Tamaya modules are currently available.
 */
public final class Features {

    /** Private singleton constructor. */
    private Features(){}

    /**
     * Checks if <i>tamaya-events</i> is on the classpath.
     * @return true, if <i>tamaya-events</i> is on the classpath.
     */
    public static boolean eventsAvailable() {
        return checkClassIsLoadable("org.apache.tamaya.events.ConfigEventManager");
    }

    /**
     * Checks if <i>tamaya-formats</i> is on the classpath.
     * @return true, if <i>tamaya-formats</i> is on the classpath.
     */
    public static boolean formatsAvailable() {
        return checkClassIsLoadable("org.apache.tamaya.format.ConfigurationFormats");
    }

    /**
     * Checks if <i>tamaya-core</i> is on the classpath.
     * @return true, if <i>tamaya-core</i> is on the classpath.
     */
    public static boolean tamayaCoreAvailable() {
        return checkClassIsLoadable("org.apache.tamaya.core.internal.CoreConfiguration");
    }

    /**
     * Checks if <i>tamaya-injection</i> is on the classpath.
     * @return true, if <i>tamaya-injection</i> is on the classpath.
     */
    public static boolean injectionStandaloneAvailable() {
        return checkClassIsLoadable("org.apache.tamaya.inject.ConfigurationInjector");
    }

    /**
     * Checks if <i>tamaya-injection-cdi</i> or <i>tamaya-injection-ee</i> is on the classpath.
     * @return true, if <i>tamaya-injection-cdi</i> or <i>tamaya-injection-ee</i> is on the classpath.
     */
    public static boolean injectionCDIAvailable() {
        return checkClassIsLoadable("org.apache.tamaya.cdi.DefaultDynamicValue");
    }

    /**
     * Checks if <i>tamaya-mutableconfig</i> is on the classpath.
     * @return true, if <i>tamaya-mutableconfig</i> is on the classpath.
     */
    public static boolean mutableConfigAvailable() {
        return checkClassIsLoadable("org.apache.tamaya.mutableconfig.MutableConfigurationProvider");
    }

    /**
     * Checks if <i>tamaya-optional</i> is on the classpath.
     * @return true, if <i>tamaya-optional</i> is on the classpath.
     */
    public static boolean optionalAvailable() {
        return checkClassIsLoadable("org.apache.tamaya.optional.OptionalConfiguration");
    }

    /**
     * Checks if <i>tamaya-resolver</i> is on the classpath.
     * @return true, if <i>tamaya-resolver</i> is on the classpath.
     */
    public static boolean resolverAvailable() {
        return checkClassIsLoadable("org.apache.tamaya.resolver.Resolver");
    }

    /**
     * Checks if <i>tamaya-resources</i> is on the classpath.
     * @return true, if <i>tamaya-respurces</i> is on the classpath.
     */
    public static boolean resourcesAvailable() {
        return checkClassIsLoadable("org.apache.tamaya.resource.ConfigResources");
    }

    /**
     * Checks if <i>tamaya-spisupport</i> is on the classpath.
     * @return true, if <i>tamaya-spisupport</i> is on the classpath.
     */
    public static boolean spiSupportAvailable() {
        return checkClassIsLoadable("org.apache.tamaya.spisupport.PropertySourceComparator");
    }

    /**
     * Checks if <i>tamaya-filter</i> is on the classpath.
     * @return true, if <i>tamaya-filter</i> is on the classpath.
     */
    public static boolean filterSupportAvailable() {
        return checkClassIsLoadable("org.apache.tamaya.filter.ConfigurationFilter");
    }

    /**
     * Checks if <i>tamaya-spring</i> is on the classpath.
     * @return true, if <i>tamaya-spring</i> is on the classpath.
     */
    public static boolean springAvailable() {
        return checkClassIsLoadable("org.apache.tamaya.integration.spring.TamayaEnvironment");
    }

    /**
     * Checks if <i>tamaya-jndi</i> is on the classpath.
     * @return true, if <i>tamaya-jndi</i> is on the classpath.
     */
    public static boolean jndiAvailable() {
        return checkClassIsLoadable("org.apache.tamaya.jndi.JNDIPropertySource");
    }

    /**
     * Checks if <i>spring-core</i> is on the classpath.
     * @return true, if <i>spring-core</i> is on the classpath.
     */
    public static boolean extSpringCoreAvailable() {
        return checkClassIsLoadable("org.springframework.core.env.Environment");
    }

    /**
     * Checks if <i>OSGIe</i> is on the classpath.
     * @return true, if <i>OSGI</i> is on the classpath.
     */
    public static boolean extOSGIAvailable() {
        return checkClassIsLoadable("org.osgi.framework.BundleContext");
    }

    /**
     * Checks if JNDI is working.
     * @return true, if JNDI is working.
     */
    public static boolean extJndiAvailable() {
        try{
            new InitialContext();
            return true;
        }catch(Exception e){
            return false;
        }
    }

    /**
     * Checks if the given class canm be loaded, using {@code Class.forName(classname)}
     * using the current Thread Context ClassLoader.
     * @param classname the fully qualified classname.
     * @return true, if the given class canm be loaded.
     */
    public static boolean checkClassIsLoadable(String classname) {
        return checkClassIsLoadable(classname, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Checks if the given class canm be loaded, using {@code Class.forName(classname)}.
     * @param classname the fully qualified classname.
     * @param classLoader class loader from which the class must be loaded.
     * @return true, if the given class canm be loaded.
     */
    public static boolean checkClassIsLoadable(String classname, ClassLoader classLoader) {
        try{
            Class.forName(Objects.requireNonNull(classname), false, classLoader);
            return true;
        }catch(Throwable e){
            return false;
        }
    }
}
