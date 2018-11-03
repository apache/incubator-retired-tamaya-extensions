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

import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertySourceProvider;
import org.junit.Test;
import static org.junit.Assert.*;

public class FormatPropertySourceProviderTest
        extends BaseFormatPropertySourceProvider {
    public FormatPropertySourceProviderTest() {
        super(ConfigurationFormats.getInstance().getFormats(), "Test.ini", "Test.properties");
    }

    @Test
    public void getPropertySourcesTest() {
        PropertySourceProvider provider = new FormatPropertySourceProviderTest();
        Collection<PropertySource> sources = provider.getPropertySources();
        
        assertEquals(2, sources.size());
    }

    @Override
    protected Collection<PropertySource> getPropertySources(ConfigurationData data) {
        PropertySource ps = new MappedConfigurationDataPropertySource(data);
        ArrayList<PropertySource> result = new ArrayList<PropertySource>();
        result.add(ps);
        return result;
    }
}
