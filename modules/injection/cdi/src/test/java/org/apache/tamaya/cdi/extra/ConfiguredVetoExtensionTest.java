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
package org.apache.tamaya.cdi.extra;

import org.junit.Test;


import javax.config.Config;
import javax.config.spi.ConfigProviderResolver;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import static org.mockito.Mockito.*;

public class ConfiguredVetoExtensionTest {
    ConfiguredVetoExtension extension = new ConfiguredVetoExtension();

    @Test
    public void willBeVetoedIfTypeHasBeenConfiguredAsConcreteClassName() {
        Config configuration = mock(Config.class);

        when(configuration.getValue("javax.enterprise.inject.vetoed", String.class))
                .thenReturn("org.apache.tamaya.cdi.extra.TestKlazz");

        ConfigProviderResolver.instance().registerConfig(configuration, getClass().getClassLoader());

        AnnotatedType<TestKlazz> annotatedType = mock(AnnotatedType.class);
        when(annotatedType.getJavaClass()).thenReturn(TestKlazz.class);

        ProcessAnnotatedType<TestKlazz> processAnnotatedType = mock(ProcessAnnotatedType.class);
        when(processAnnotatedType.getAnnotatedType()).thenReturn(annotatedType);

        extension.observesBean(processAnnotatedType);

        (processAnnotatedType).veto();
    }

    @Test
    public void willNotBeVetoedIfTypeHasNotBeenConfigured() {
        Config configuration = mock(Config.class);

        when(configuration.getValue("javax.enterprise.inject.vetoed", String.class))
                .thenReturn("org.apache.tamaya.cdi.extra.O");

        ConfigProviderResolver.instance().registerConfig(configuration, getClass().getClassLoader());

        AnnotatedType<TestKlazz> annotatedType = mock(AnnotatedType.class);
        when(annotatedType.getJavaClass()).thenReturn(TestKlazz.class);

        ProcessAnnotatedType<TestKlazz> processAnnotatedType = mock(ProcessAnnotatedType.class);
        when(processAnnotatedType.getAnnotatedType()).thenReturn(annotatedType);

        extension.observesBean(processAnnotatedType);

        verify(processAnnotatedType, never()).veto();
    }

    @Test
    public void handlesPropertyWithWhitespacesCorrectly() {
        String configuredValue = "  " + TestKlazz.class.getName() +
                                 ",\t" + TestKlazz2.class.getName();

        Config configuration = mock(Config.class);

        when(configuration.getValue("javax.enterprise.inject.vetoed", String.class)).thenReturn(configuredValue);

        ConfigProviderResolver.instance().registerConfig(configuration, getClass().getClassLoader());

        AnnotatedType<TestKlazz> annotatedType = mock(AnnotatedType.class);
        when(annotatedType.getJavaClass()).thenReturn(TestKlazz.class);

        ProcessAnnotatedType<TestKlazz> processAnnotatedType = mock(ProcessAnnotatedType.class);
        when(processAnnotatedType.getAnnotatedType()).thenReturn(annotatedType);

        extension.observesBean(processAnnotatedType);

        verify(processAnnotatedType).veto();
    }

    @Test
    public void useOfRegexPattersWorks() {
        String configuredValue = "  " + TestKlazz.class.getPackage().getName() +
                                 "\\..+";

        Config configuration = mock(Config.class);

        when(configuration.getValue("javax.enterprise.inject.vetoed", String.class))
                .thenReturn(configuredValue);

        ConfigProviderResolver.instance().registerConfig(configuration, getClass().getClassLoader());

        AnnotatedType<TestKlazz> annotatedType = mock(AnnotatedType.class);
        when(annotatedType.getJavaClass()).thenReturn(TestKlazz.class);

        ProcessAnnotatedType<TestKlazz> processAnnotatedType = mock(ProcessAnnotatedType.class);
        when(processAnnotatedType.getAnnotatedType()).thenReturn(annotatedType);

        extension.observesBean(processAnnotatedType);

        verify(processAnnotatedType).veto();
    }
}