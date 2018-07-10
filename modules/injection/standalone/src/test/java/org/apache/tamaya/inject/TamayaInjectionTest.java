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
import org.apache.tamaya.base.configsource.MapConfigSource;
import org.apache.tamaya.resolver.internal.ConfigResolver;
import org.junit.Test;

import javax.config.Config;
import javax.config.ConfigProvider;
import javax.config.spi.ConfigProviderResolver;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Anatole on 12.01.2015.
 */
public class TamayaInjectionTest {

    @Test
    public void testInjectionNonAnnotatedClass(){
        assertNotNull(ConfigurationInjection.getConfigurationInjector());
        NonAnnotatedConfigBean testInstance = new NonAnnotatedConfigBean();
        assertEquals(testInstance.simple_value, "Should be overridden!");
        assertEquals(testInstance.classFieldKey, "Foo");
        assertEquals(testInstance.fieldKey, null);
        assertEquals(testInstance.fullKey, null);
        assertEquals(testInstance.test2, "This is not set.");
        ConfigurationInjection.getConfigurationInjector().configure(testInstance);
        assertEquals(testInstance.simple_value, "aSimpleValue");
        assertEquals(testInstance.classFieldKey, "Class-Field-Value");
        assertEquals(testInstance.fieldKey, "Field-Value");
        assertEquals(testInstance.fullKey, "Fullkey-Value");
        assertEquals(testInstance.test2, "This is not set.");
    }

    @Test
    public void testInjectionClass(){
        assertNotNull(ConfigurationInjection.getConfigurationInjector());
        AnnotatedConfigBean testInstance = new AnnotatedConfigBean();
        assertEquals(testInstance.getHostName(), null);
        assertEquals(testInstance.getAnotherValue(), null);
        assertEquals(testInstance.myParameter, null);
        assertEquals(testInstance.simpleValue, null);
        ConfigurationInjection.getConfigurationInjector().configure(testInstance);
        assertEquals(testInstance.getHostName(), "tamaya01.incubator.apache.org");
        assertEquals(testInstance.getAnotherValue(), "HALLO!");
        assertEquals(testInstance.myParameter, "ET");
        assertEquals(testInstance.simpleValue, "aSimpleValue");
        assertNotNull(testInstance.getDynamicValue());
        assertTrue(testInstance.getDynamicValue().isPresent());
        assertEquals(testInstance.getDynamicValue().get(), "tamaya01.incubator.apache.org");
        assertEquals(testInstance.getHostName(), testInstance.getDynamicValue().get());
        assertEquals(testInstance.javaVersion, System.getProperty("java.version"));
    }
    
    @Test
    public void testInjectionInheritedClass(){
        assertNotNull(ConfigurationInjection.getConfigurationInjector());
        InheritedAnnotatedConfigBean testInstance = new InheritedAnnotatedConfigBean();
        assertEquals(testInstance.getHostName(), null);
        assertEquals(testInstance.getAnotherValue(), null);
        assertEquals(testInstance.myParameter, null);
        assertEquals(testInstance.simpleValue, null);
        assertEquals(testInstance.someMoreValue, null);
        assertEquals(testInstance.notConfigured, null);
        ConfigurationInjection.getConfigurationInjector().configure(testInstance);
        assertEquals(testInstance.getHostName(), "tamaya01.incubator.apache.org");
        assertEquals(testInstance.getAnotherValue(), "HALLO!");
        assertEquals(testInstance.myParameter, "ET");
        assertEquals(testInstance.simpleValue, "aSimpleValue");
        assertNotNull(testInstance.getDynamicValue());
        assertTrue(testInstance.getDynamicValue().isPresent());
        assertEquals(testInstance.getDynamicValue().get(), "tamaya01.incubator.apache.org");
        assertEquals(testInstance.getHostName(), testInstance.getDynamicValue().get());
        assertEquals(testInstance.javaVersion, System.getProperty("java.version"));
        assertEquals(testInstance.someMoreValue, "s'more");
    }    

    @Test
    public void testConfigTemplate(){
        assertNotNull(ConfigurationInjection.getConfigurationInjector());
        AnnotatedConfigTemplate testInstance = ConfigurationInjection.getConfigurationInjector()
                .createTemplate(AnnotatedConfigTemplate.class);
        assertEquals(testInstance.hostName(), "tamaya01.incubator.apache.org");
        assertEquals(testInstance.myParameter(), "ET");
        assertEquals(testInstance.simpleValue(), "aSimpleValue");
        assertNotNull(testInstance.getDynamicValue());
        assertTrue(testInstance.getDynamicValue().isPresent());
        assertEquals(testInstance.getDynamicValue().get(), "tamaya01.incubator.apache.org");
        assertEquals(testInstance.hostName(), testInstance.getDynamicValue().get());
//        assertEquals(testInstance.simplestValue(), "HALLO!");
    }

    @Test
    public void testConfigTemplate_WithCustomConfig(){
        Map<String,String> properties = new HashMap<>();
        properties.put("env.stage", "custom-stage");
        properties.put("simple_value", "custom-value");
        properties.put("host.name", "custom-hostname");
        properties.put("anotherValue", "custom-HALLO!");
        properties.put("foo.bar.myprop", "custom-parameter");
        MapConfigSource ps = new MapConfigSource("test", properties);
        Config customConfig = ConfigProviderResolver.instance().getBuilder()
                .withSources(ps).build();
        assertNotNull(ConfigurationInjection.getConfigurationInjector());
        AnnotatedConfigTemplate testInstance = ConfigurationInjection.getConfigurationInjector()
                .createTemplate(AnnotatedConfigTemplate.class, customConfig);
        assertEquals(testInstance.hostName(), "custom-hostname");
        assertEquals(testInstance.myParameter(), "custom-parameter");
        assertEquals(testInstance.simpleValue(), "custom-value");
        assertNotNull(testInstance.getDynamicValue());
        assertTrue(testInstance.getDynamicValue().isPresent());
        assertEquals(testInstance.getDynamicValue().get(), "custom-hostname");
        assertEquals(testInstance.hostName(), testInstance.getDynamicValue().get());
    }

}
