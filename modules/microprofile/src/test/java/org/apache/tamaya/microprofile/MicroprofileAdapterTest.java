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
package org.apache.tamaya.microprofile;

import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.spi.PropertyConverter;
import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertyValue;
import org.apache.tamaya.spisupport.BuildablePropertySource;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigBuilder;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.Converter;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class MicroprofileAdapterTest {
    @Test
    public void toConfig() throws Exception {
        Configuration config = ConfigurationProvider.getConfiguration();
        Config mpConfig = MicroprofileAdapter.toConfig(config);
        assertNotNull(mpConfig);
        assertEquals(config.getProperties().keySet(), mpConfig.getPropertyNames());
    }

    @Test
    public void toConfiguration() throws Exception {
        Config mpConfig = ConfigProvider.getConfig();
        Configuration config = MicroprofileAdapter.toConfiguration(mpConfig);
        assertNotNull(config);
        assertEquals(mpConfig.getPropertyNames(), config.getProperties().keySet());
    }

    @Test
    public void toConfigSources() throws Exception {
        BuildablePropertySource testPropertySource = BuildablePropertySource.builder()
                .withSource("toConfigSources")
                .withSimpleProperty("string0", "value0")
                .withSimpleProperty("int0", "0")
                .build();
        List<PropertySource> tamayaSources = new ArrayList<>();
        tamayaSources.add(testPropertySource);
        List<ConfigSource> configSources = MicroprofileAdapter.toConfigSources(tamayaSources);
        assertNotNull(configSources);
        assertEquals(tamayaSources.size(), configSources.size());
        compare(testPropertySource, configSources.get(0));
    }

    private void compare(PropertySource tamayaSource, ConfigSource mpSource) {
        assertEquals(mpSource.getName(),tamayaSource.getName());
        assertEquals(mpSource.getOrdinal(), tamayaSource.getOrdinal());
        assertEquals(mpSource.getProperties().keySet(), tamayaSource.getProperties().keySet());
        for(String key:mpSource.getPropertyNames()){
            assertEquals(mpSource.getValue(key), tamayaSource.get(key).getValue());
        }
    }

    @Test
    public void toPropertySources() throws Exception {
        BuildableConfigSource configSource = BuildableConfigSource.builder()
                .withSource("toConfigSources")
                .withProperty("string0", "value0")
                .withProperty("int0", "0")
                .build();
        List<ConfigSource> configSources = new ArrayList<>();
        configSources.add(configSource);
        List<PropertySource> propertySources = MicroprofileAdapter.toPropertySources(configSources);
        assertNotNull(propertySources);
        assertEquals(propertySources.size(), configSources.size());
        compare(propertySources.get(0), configSource);
    }

    @Test
    public void toConfigSource() throws Exception {
        BuildablePropertySource tamayaSource = BuildablePropertySource.builder()
                .withSource("toConfigSource")
                .withSimpleProperty("string0", "value0")
                .withSimpleProperty("int0", "0")
                .build();
        ConfigSource configSource = MicroprofileAdapter.toConfigSource(tamayaSource);
        assertNotNull(configSource);
        compare(tamayaSource, configSource);
    }

    @Test
    public void toPropertySource() throws Exception {
        BuildableConfigSource configSource = BuildableConfigSource.builder()
                .withSource("toConfigSource")
                .withProperty("string0", "value0")
                .withProperty("int0", "0")
                .build();
        PropertySource tamayaSource = MicroprofileAdapter.toPropertySource(configSource);
        assertNotNull(configSource);
        compare(tamayaSource, configSource);
    }

    @Test
    public void toPropertyConverter() throws Exception {
        PropertyConverter<String> tamayaConverter = MicroprofileAdapter.toPropertyConverter(new UppercaseConverter());
        assertNotNull(tamayaConverter);
        assertEquals("ABC", tamayaConverter.convert("aBC", null));
    }

    @Test
    public void toConverter() throws Exception {
        Converter<String> mpConverter = MicroprofileAdapter.toConverter(new UppercasePropertyConverter());
        assertNotNull(mpConverter);
        assertEquals("ABC", mpConverter.convert("aBC"));
    }

    @Test
    public void toConfigBuilder() throws Exception {
        ConfigBuilder builder = MicroprofileAdapter.toConfigBuilder(ConfigurationProvider.getConfigurationContextBuilder());
        assertNotNull(builder);
    }

    @Test
    public void toStringMap() throws Exception {
        Map<String,PropertyValue> props = new HashMap<>();
        props.put("a", PropertyValue.of("a","b", "toStringMap"));
        Map<String, String> mpProps = MicroprofileAdapter.toStringMap(props);
        assertNotNull(mpProps);
        assertEquals(props.keySet(), mpProps.keySet());
        assertEquals(mpProps.get("a"), "b");
    }

    @Test
    public void toPropertyValueMap() throws Exception {
        Map<String,String> props = new HashMap<>();
        props.put("a", "b");
        Map<String, PropertyValue> tamayaProps = MicroprofileAdapter.toPropertyValueMap(props, "toPropertyValueMap");
        assertNotNull(tamayaProps);
        assertEquals(tamayaProps.keySet(), props.keySet());
        assertEquals(tamayaProps.get("a").getValue(), "b");
        assertEquals("toPropertyValueMap", tamayaProps.get("a").getSource());
    }

}