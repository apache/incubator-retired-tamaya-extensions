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

import org.apache.tamaya.ConfigException;
import org.apache.tamaya.spi.ServiceContextManager;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

/**
 * Interface to be implemented by modules. It supports loading of files or classpath resources either directly or by
 * defining an Ant-style resource pattern:
 * <ul>
 *     <li>'*' is a placeholder for any character (0..n)</li>
 *     <li>'**' is a placeholder for any number of subdirectories going down a directory structure recursively.</li>
 *     <li>'?' is a placeholder for exact one character</li>
 * </ul>
 * Given that the following expressions are valid expressions:
 * <pre>
 *     classpath:javax/annotations/*
 *     javax?/annotations&#47;**&#47;*.class
 *     org/apache/tamaya&#47;**&#47;tamayaconfig.properties
 *     file:C:/temp/*.txt
 *     file:C:\**\*.ini
 *     C:\Programs\**&#47;*.ini
 *     /user/home/A*b101_?.pid
 *     /var/logs&#47;**&#47;*.log
 * </pre>
 */
public interface ResourceResolver {

    /**
     * Resolves resource expressions to a createList of {@link URL}s. Hereby
     * the ordering of format matches the input of the resolved expressions. Nevertheless be aware that
     * there is no determined ordering of format located within a classloader.
     *
     * @param expressions the expressions to be resolved, not empty.
     * @return the corresponding collection of current {@link URL}s found, never
     * null.
     * .
     */
    Collection<URL> getResources(Collection<String> expressions);

    /**
     * Resolves resource expressions to a createList of {@link URL}s. Hereby
     * the ordering of format matches the input of the resolved expressions. Nevertheless be aware that
     * there is no determined ordering of format located within a classloader.
     *
     * @param expressions the expressions to be resolved, not empty.
     * @return the corresponding collection of current {@link URL}s found, never
     * null.
     * .
     */
    default Collection<URL> getResources(String... expressions){
        return getResources(Arrays.asList(expressions));
    }

    /**
     * Access the currently registered {@link ResourceLocator} instances.
     * @return the currently known {@link ResourceLocator} instances, never null.
     */
    Collection<ResourceLocator> getResourceLocators();

    /**
     * <p>Access the current ResourceResolver using the default classloader.</p>
     *
     * @throws ConfigException if no ResourceResolver is available (should not happen).
     *
     * @return the current ResourceResolver instance, never null.
     * @see ServiceContextManager#getDefaultClassLoader()
     */
    static ResourceResolver getInstance() throws ConfigException {
        return getInstance(ServiceContextManager.getDefaultClassLoader());
    }

    /**
     * <p>Access the current ResourceResolver.</p>
     *
     * @throws ConfigException if no ResourceResolver is available (should not happen).
     *
     * @return the current ResourceResolver instance, never null.
     */
    static ResourceResolver getInstance(ClassLoader classLoader) throws ConfigException {
        ResourceResolver resolver = ServiceContextManager.getServiceContext(classLoader)
                .getService(ResourceResolver.class);
        if (resolver == null) {
            throw new ConfigException("ResourceResolver not available.");
        }
        return resolver;
    }

}
