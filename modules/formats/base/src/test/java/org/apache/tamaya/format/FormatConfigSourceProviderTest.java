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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

import javax.config.spi.ConfigSource;
import javax.config.spi.ConfigSourceProvider;

import static org.junit.Assert.*;

public class FormatConfigSourceProviderTest
        extends BaseFormatConfigSourceProvider {
    public FormatConfigSourceProviderTest() {
        super(ConfigurationFormats.getFormats(), "Test.ini", "Test.properties");
    }

    @Test
    public void getConfigSourcesTest() {
        ConfigSourceProvider provider = new FormatConfigSourceProviderTest();
        Iterable<ConfigSource> sources = provider.getConfigSources(null);

        Iterator iter = sources.iterator();
        assertTrue(iter.hasNext());
        iter.next();
        assertTrue(iter.hasNext());
        iter.next();
        assertFalse(iter.hasNext());
    }

    @Override
    protected Collection<ConfigSource> getConfigSources(ConfigurationData data) {
        ConfigSource ps = new MappedConfigurationDataConfigSource(data);
        ArrayList<ConfigSource> result = new ArrayList<ConfigSource>();
        result.add(ps);
        return result;
    }
}
