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
package org.apache.tamaya.spi;


import java.util.List;

/**
 * Central SPI for programmatically dealing with the setup of the configuration system.
 * This includes adding and enlisting {@link org.apache.tamaya.spi.PropertySource}s,
 * managing {@link org.apache.tamaya.spi.PropertyConverter}s, ConfigFilters, etc.
 */
public interface ConfigurationContext {

    /**
     * This method can be used for programmatically adding {@link PropertySource}s.
     * It is not needed for normal 'usage' by end users, but only for Extension Developers!
     *
     * @param propertySourcesToAdd the PropertySources to add
     */
    void addPropertySources(PropertySource... propertySourcesToAdd);

    /**
     * This method returns the list of registered PropertySources ordered via their ordinal.
     * PropertySources with a lower ordinal come first. The PropertySource with the
     * highest ordinal comes last.
     * If two PropertySources have the same ordinal number they will get sorted
     * using their class name just to ensure the user at least gets the same ordering
     * after a JVM restart.
     *
     * @return sorted list of registered PropertySources
     */
    List<PropertySource> getPropertySources();

}
