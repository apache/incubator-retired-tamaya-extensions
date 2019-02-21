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

import org.apache.tamaya.*;
import org.apache.tamaya.spi.*;
import org.apache.tamaya.spisupport.propertysource.BuildablePropertySource;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigBuilder;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.Converter;
import org.junit.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class MicroprofileAdapterTest {
    @Test
    public void toConfig() throws Exception {
        Configuration config = Configuration.current();
        Config mpConfig = MicroprofileAdapter.toConfig(config);
        assertThat(mpConfig).isNotNull();
        assertThat(config.getProperties().keySet()).isEqualTo(mpConfig.getPropertyNames());
    }

    @Test
    public void toConfigWithTamayaConfiguration() throws Exception {
        Configuration configuration = new MyConfiguration();
        MicroprofileConfig config = new MicroprofileConfig(configuration);
        TamayaConfiguration tamayaConfiguration = new TamayaConfiguration(config);

        Config result = MicroprofileAdapter.toConfig(tamayaConfiguration);

        assertThat(result).isNotNull()
                  .isInstanceOf(MicroprofileConfig.class)
                  .isSameAs(config);
    }

    @Test
    public void toConfiguration() throws Exception {
        Config mpConfig = ConfigProvider.getConfig();
        Configuration config = MicroprofileAdapter.toConfiguration(mpConfig);
        assertThat(config).isNotNull();
        assertThat(mpConfig.getPropertyNames()).isEqualTo(config.getProperties().keySet());
    }

    @Test
    public void toConfigurationWithNoneMicroprofileConfig() throws Exception {
        Config config = new MyConfig();
        Configuration result = MicroprofileAdapter.toConfiguration(config);

        assertThat(result).isNotNull().isInstanceOf(TamayaConfiguration.class);
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
        assertThat(configSources).isNotNull()
            .hasSize(tamayaSources.size());
        compare(testPropertySource, configSources.get(0));
    }

    private void compare(PropertySource tamayaSource, ConfigSource mpSource) {
        assertThat(mpSource.getName()).isEqualTo(tamayaSource.getName());
        assertThat(mpSource.getOrdinal()).isEqualTo(tamayaSource.getOrdinal());
        assertThat(mpSource.getProperties().keySet()).isEqualTo(tamayaSource.getProperties().keySet());
        for(String key:mpSource.getPropertyNames()){
            assertThat(mpSource.getValue(key)).isEqualTo(tamayaSource.get(key).getValue());
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
        assertThat(propertySources).isNotNull()
            .hasSize(configSources.size());
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
        assertThat(configSource).isNotNull();
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
        assertThat(configSource).isNotNull();
        compare(tamayaSource, configSource);
    }

    @Test
    public void toPropertyConverter() throws Exception {
        PropertyConverter<String> tamayaConverter = MicroprofileAdapter.toPropertyConverter(new UppercaseConverter());
        assertThat(tamayaConverter).isNotNull();
        assertThat("ABC").isEqualTo(tamayaConverter.convert("aBC", null));
    }

    @Test
    public void toConverter() throws Exception {
        Converter<String> mpConverter = MicroprofileAdapter.toConverter(new UppercasePropertyConverter());
        assertThat(mpConverter).isNotNull();
        assertThat("ABC").isEqualTo(mpConverter.convert("aBC"));
    }

    @Test
    public void toConfigBuilder() throws Exception {
        ConfigBuilder builder = MicroprofileAdapter.toConfigBuilder(Configuration.createConfigurationBuilder());
        assertThat(builder).isNotNull();
    }

    @Test
    public void toStringMap() throws Exception {
        Map<String,PropertyValue> props = new HashMap<>();
        props.put("a", PropertyValue.createValue("a","b").setMeta("source", "toStringMap"));
        Map<String, String> mpProps = MicroprofileAdapter.toStringMap(props);
        assertThat(mpProps).isNotNull();
        assertThat(props.keySet()).isEqualTo(mpProps.keySet());
        assertThat(mpProps.get("a")).isEqualTo("b");
    }

    @Test
    public void toPropertyValueMap() throws Exception {
        Map<String,String> props = new HashMap<>();
        props.put("a", "b");
        Map<String, PropertyValue> tamayaProps = MicroprofileAdapter.toPropertyValueMap(props, "toPropertyValueMap");
        assertThat(tamayaProps).isNotNull();
        assertThat(tamayaProps.keySet()).isEqualTo(props.keySet());
        assertThat(tamayaProps.get("a").getValue()).isEqualTo("b");
        assertThat("toPropertyValueMap").isEqualTo(tamayaProps.get("a").getMeta("source"));
    }

    static class MyConfig implements Config {
        @Override
        public <T> T getValue(String s, Class<T> aClass) {
            throw new RuntimeException("Not implemented yet!");
        }

        @Override
        public <T> Optional<T> getOptionalValue(String s, Class<T> aClass) {
            throw new RuntimeException("Not implemented yet!");
        }

        @Override
        public Iterable<String> getPropertyNames() {
            throw new RuntimeException("Not implemented yet!");
        }

        @Override
        public Iterable<ConfigSource> getConfigSources() {
            throw new RuntimeException("Not implemented yet!");
        }
    }

    static class MyConfiguration implements Configuration {
        @Override
        public String get(String key) {
            throw new RuntimeException("Not implemented yet!");
        }

        @Override
        public String getOrDefault(String key, String defaultValue) {
            throw new RuntimeException("Not implemented yet!");
        }

        @Override
        public <T> T getOrDefault(String key, Class<T> type, T defaultValue) {
            throw new RuntimeException("Not implemented yet!");
        }

        @Override
        public <T> T get(String key, Class<T> type) {
            throw new RuntimeException("Not implemented yet!");
        }

        @Override
        public <T> T get(String key, TypeLiteral<T> type) {
            throw new RuntimeException("Not implemented yet!");
        }

        @Override
        public <T> T getOrDefault(String key, TypeLiteral<T> type, T defaultValue) {
            throw new RuntimeException("Not implemented yet!");
        }

        @Override
        public Map<String, String> getProperties() {
            throw new RuntimeException("Not implemented yet!");
        }

        @Override
        public ConfigurationContext getContext() {
            throw new RuntimeException("Not implemented yet!");
        }

        @Override
        public ConfigurationSnapshot getSnapshot(Iterable<String> keys) {
            throw new RuntimeException("Not implemented yet!");
        }
    }

}
