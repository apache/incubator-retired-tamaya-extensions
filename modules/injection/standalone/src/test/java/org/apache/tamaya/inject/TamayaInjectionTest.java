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
package org.apache.tamaya.inject;

import annottext.AnnotatedConfigBean;
import annottext.AnnotatedConfigTemplate;
import annottext.InheritedAnnotatedConfigBean;
import annottext.NonAnnotatedConfigBean;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.spisupport.propertysource.MapPropertySource;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by Anatole on 12.01.2015.
 */
public class TamayaInjectionTest {

    @Test
    public void testInjectionNonAnnotatedClass(){
        assertThat(ConfigurationInjection.getConfigurationInjector()).isNotNull();
        NonAnnotatedConfigBean testInstance = new NonAnnotatedConfigBean();
        assertThat(testInstance.simple_value).isEqualTo("Should be overridden!");
        assertThat(testInstance.classFieldKey).isEqualTo("Foo");
        assertThat(testInstance.fieldKey).isNull();
        assertThat(testInstance.fullKey).isNull();
        assertThat(testInstance.test2).isEqualTo("This is not setCurrent.");
        ConfigurationInjection.getConfigurationInjector().configure(testInstance);
        assertThat(testInstance.simple_value).isEqualTo("aSimpleValue");
        assertThat(testInstance.classFieldKey).isEqualTo("Class-Field-Value");
        assertThat(testInstance.fieldKey).isEqualTo("Field-Value");
        assertThat(testInstance.fullKey).isEqualTo("Fullkey-Value");
        assertThat(testInstance.test2).isEqualTo("This is not setCurrent.");
    }

    @Test
    public void testInjectionClass(){
        assertThat(ConfigurationInjection.getConfigurationInjector()).isNotNull();
        AnnotatedConfigBean testInstance = new AnnotatedConfigBean();
        assertThat(testInstance.getHostName()).isNull();
        assertThat(testInstance.getAnotherValue()).isNull();
        assertThat(testInstance.myParameter).isNull();
        assertThat(testInstance.simpleValue).isNull();
        ConfigurationInjection.getConfigurationInjector().configure(testInstance);
        assertThat(testInstance.getHostName()).isEqualTo("tamaya01.incubator.apache.org");
        assertThat(testInstance.getAnotherValue()).isEqualTo("HALLO!");
        assertThat(testInstance.myParameter).isEqualTo("ET");
        assertThat(testInstance.simpleValue).isEqualTo("aSimpleValue");
        assertThat(testInstance.getDynamicValue()).isNotNull();
        assertThat(testInstance.getDynamicValue().isPresent()).isTrue();
        assertThat(testInstance.getDynamicValue().get()).isEqualTo("tamaya01.incubator.apache.org");
        assertThat(testInstance.getHostName()).isEqualTo(testInstance.getDynamicValue().get());
        assertThat(testInstance.javaVersion).isEqualTo(System.getProperty("java.version"));
    }

    @Test
    public void testInjectionInheritedClass(){
        assertThat(ConfigurationInjection.getConfigurationInjector()).isNotNull();
        InheritedAnnotatedConfigBean testInstance = new InheritedAnnotatedConfigBean();
        assertThat(testInstance.getHostName()).isNull();
        assertThat(testInstance.getAnotherValue()).isNull();
        assertThat(testInstance.myParameter).isNull();
        assertThat(testInstance.simpleValue).isNull();
        assertThat(testInstance.someMoreValue).isNull();
        assertThat(testInstance.notConfigured).isNull();
        ConfigurationInjection.getConfigurationInjector().configure(testInstance);
        assertThat(testInstance.getHostName()).isEqualTo("tamaya01.incubator.apache.org");
        assertThat(testInstance.getAnotherValue()).isEqualTo("HALLO!");
        assertThat(testInstance.myParameter).isEqualTo("ET");
        assertThat(testInstance.simpleValue).isEqualTo("aSimpleValue");
        assertThat(testInstance.getDynamicValue()).isNotNull();
        assertThat(testInstance.getDynamicValue().isPresent()).isTrue();
        assertThat(testInstance.getDynamicValue().get()).isEqualTo("tamaya01.incubator.apache.org");
        assertThat(testInstance.getHostName()).isEqualTo(testInstance.getDynamicValue().get());
        assertThat(testInstance.javaVersion).isEqualTo(System.getProperty("java.version"));
        assertThat(testInstance.someMoreValue).isEqualTo("s'more");
    }

    @Test
    public void testConfigTemplate(){
        assertThat(ConfigurationInjection.getConfigurationInjector()).isNotNull();
        AnnotatedConfigTemplate testInstance = ConfigurationInjection.getConfigurationInjector()
                .createTemplate(AnnotatedConfigTemplate.class);
        assertThat(testInstance.hostName()).isEqualTo("tamaya01.incubator.apache.org");
        assertThat(testInstance.myParameter()).isEqualTo("ET");
        assertThat(testInstance.simpleValue()).isEqualTo("aSimpleValue");
        assertThat(testInstance.getDynamicValue()).isNotNull();
        assertThat(testInstance.getDynamicValue().isPresent()).isTrue();
        assertThat(testInstance.getDynamicValue().get()).isEqualTo("tamaya01.incubator.apache.org");
        assertThat(testInstance.hostName()).isEqualTo(testInstance.getDynamicValue().get());
    }

    @Test
    public void testConfigTemplate_WithCustomConfig(){
        Map<String,String> properties = new HashMap<>();
        properties.put("env.stage", "custom-stage");
        properties.put("simple_value", "custom-value");
        properties.put("host.name", "custom-hostname");
        properties.put("anotherValue", "custom-HALLO!");
        properties.put("foo.bar.myprop", "custom-parameter");
        MapPropertySource ps = new MapPropertySource("test", properties);
        Configuration customConfig = ConfigurationProvider.getConfigurationBuilder()
                .addPropertySources(ps).build();
        assertThat(ConfigurationInjection.getConfigurationInjector()).isNotNull();
        AnnotatedConfigTemplate testInstance = ConfigurationInjection.getConfigurationInjector()
                .createTemplate(AnnotatedConfigTemplate.class, customConfig);
        assertThat(testInstance.hostName()).isEqualTo("custom-hostname");
        assertThat(testInstance.myParameter()).isEqualTo("custom-parameter");
        assertThat(testInstance.simpleValue()).isEqualTo("custom-value");
        assertThat(testInstance.getDynamicValue()).isNotNull();
        assertThat(testInstance.getDynamicValue().isPresent()).isTrue();
        assertThat(testInstance.getDynamicValue().get()).isEqualTo("custom-hostname");
        assertThat(testInstance.hostName()).isEqualTo(testInstance.getDynamicValue().get());
    }

}
