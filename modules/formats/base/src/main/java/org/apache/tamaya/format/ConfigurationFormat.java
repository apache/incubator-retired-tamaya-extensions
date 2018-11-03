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
package org.apache.tamaya.format;

import org.apache.tamaya.spi.PropertyValue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * <p>Implementations current this class encapsulate the mechanism how to read a
 * resource including interpreting the format correctly (e.g. xml vs.
 * properties vs. ini). In most cases file only contains entries of the same priority, which would then
 * result in only one {@link org.apache.tamaya.spi.PropertySource}. Complex file formats, however, may contain entries
 * of different priorities. In this cases, each ordinal type found typically is returned as a separate section so the
 * consuming {@link org.apache.tamaya.spi.PropertySourceProvider} implementation can distribute the different part to
 * individual {@link org.apache.tamaya.spi.PropertySource}s.</p>
 *
 * <h3>Implementation Requirements</h3>
 * Implementations of this type must be
 * <ul>
 *     <li>thread-safe</li>
 * </ul>
 */
public interface ConfigurationFormat {

    /**
     * Get a unique name of the format. This name can be used to access the format.
     * @return the (unique) format's name, never null and not empty.
     */
    String getName();

    /**
     * Allows the format to examine the given resource, e.g. for a matching file ending. Only, if a format accepts an
     * URL, it will be tried for reading the configuration.
     * @param url the url to read the configuration data from (could be a file, a server location, a classpath
     *            resource or something else, not null.
     * @return true, if this format accepts the given URL for reading.
     */
    boolean accepts(URL url);


    /**
     * Reads a configuration from an URL, hereby parsing the given {@link java.io.InputStream}. Dependening on
     * the capabilities of the format the returned {@link PropertyValue} may contain
     * different levels of data:
     * <ul>
     *     <li>Only a <i>default</i> section is returned, since the configuration format does not support
     *     hierarchies, e.g. a root {@link PropertyValue} with a number of direct getList.</li>
     *     <li>Hierarchical formats such as INI, XML, YAML and JSON can have both createObject mapped childs as well as arrays/createList
     *     childs. With {@link PropertyValue#toMap()} a default mapping to a property based representation is
     *     available.</li>
     * </ul>
     *
     * Summarizing implementations common formats should always provide the data organized in a {@link PropertyValue}
     * tree for the given format.
     *
     * If the configuration format only contains entries of one ordinal type, normally only one single
     * instance of PropertySource is returned.
     * Nevertheless custom formats may contain different sections or parts,
     * where each part maps to a different target ordinal (eg defaults, domain config and app config). In the
     * ladder case multiple PropertySources can be returned, each one with its own ordinal and the corresponding
     * entries.
     * @see org.apache.tamaya.spi.PropertySource
     * @param inputStream the inputStream to read from, not null.
     * @param resource the resource id, not null.
     * @return the corresponding {@link PropertyValue} containing sections/properties read, never {@code null}.
     * @throws org.apache.tamaya.ConfigException if parsing of the input fails.
     * @throws IOException if reading the input fails.
     */
    ConfigurationData readConfiguration(String resource, InputStream inputStream) throws IOException;

}
