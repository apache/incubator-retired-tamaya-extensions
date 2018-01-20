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

import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.spi.ConfigurationContext;
import org.junit.Test;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import static org.mockito.Mockito.*;

public class ConfiguredVetoExtensionTest {

    ConfiguredVetoExtension extension = new ConfiguredVetoExtension();

    @Test
    public void willBeVetoedIfTypeHasBeenConfiguredAsConcreteClassName() {

        Configuration oldConfiguration = ConfigurationProvider.getConfiguration();

        try {

            ConfigurationContext context = mock(ConfigurationContext.class);
            Configuration configuration = mock(Configuration.class);

            when(configuration.getContext()).thenReturn(context);
            when(configuration.get("javax.enterprise.inject.vetoed")).thenReturn("org.apache.tamaya.cdi.extra.TestKlazz");

            ConfigurationProvider.setConfiguration(configuration);
            AnnotatedType<TestKlazz> annotatedType = mock(AnnotatedType.class);
            when(annotatedType.getJavaClass()).thenReturn(TestKlazz.class);

            ProcessAnnotatedType<TestKlazz> processAnnotatedType = mock(ProcessAnnotatedType.class);
            when(processAnnotatedType.getAnnotatedType()).thenReturn(annotatedType);

            extension.observesBean(processAnnotatedType);

            (processAnnotatedType).veto();
        } finally {
            ConfigurationProvider.setConfiguration(oldConfiguration);
        }
    }

    @Test
    public void willNotBeVetoedIfTypeHasNotBeenConfigured() {
        Configuration oldConfiguration = ConfigurationProvider.getConfiguration();

        try {

            ConfigurationContext context = mock(ConfigurationContext.class);
            Configuration configuration = mock(Configuration.class);

            when(configuration.getContext()).thenReturn(context);
            when(configuration.get("javax.enterprise.inject.vetoed")).thenReturn("org.apache.tamaya.cdi.extra.O");

            ConfigurationProvider.setConfiguration(configuration);
            AnnotatedType<TestKlazz> annotatedType = mock(AnnotatedType.class);
            when(annotatedType.getJavaClass()).thenReturn(TestKlazz.class);

            ProcessAnnotatedType<TestKlazz> processAnnotatedType = mock(ProcessAnnotatedType.class);
            when(processAnnotatedType.getAnnotatedType()).thenReturn(annotatedType);

            extension.observesBean(processAnnotatedType);

            verify(processAnnotatedType, never()).veto();
        } finally {
            ConfigurationProvider.setConfiguration(oldConfiguration);
        }
    }

    @Test
    public void handlesPropertyWithWhitespacesCorrectly() {
        String configuredValue = "  " + TestKlazz.class.getName() +
                                 ",\t" + TestKlazz2.class.getName();

        Configuration oldConfiguration = ConfigurationProvider.getConfiguration();

        try {
            ConfigurationContext context = mock(ConfigurationContext.class);
            Configuration configuration = mock(Configuration.class);

            when(configuration.getContext()).thenReturn(context);
            when(configuration.get("javax.enterprise.inject.vetoed")).thenReturn(configuredValue);

            ConfigurationProvider.setConfiguration(configuration);

            AnnotatedType<TestKlazz> annotatedType = mock(AnnotatedType.class);
            when(annotatedType.getJavaClass()).thenReturn(TestKlazz.class);

            ProcessAnnotatedType<TestKlazz> processAnnotatedType = mock(ProcessAnnotatedType.class);
            when(processAnnotatedType.getAnnotatedType()).thenReturn(annotatedType);

            extension.observesBean(processAnnotatedType);

            verify(processAnnotatedType).veto();
        } finally {
            ConfigurationProvider.setConfiguration(oldConfiguration);
        }
    }

    @Test
    public void useOfRegexPattersWorks() {
        String configuredValue = "  " + TestKlazz.class.getPackage().getName() +
                                 "\\..+";

        Configuration oldConfiguration = ConfigurationProvider.getConfiguration();

        try {
            ConfigurationContext context = mock(ConfigurationContext.class);
            Configuration configuration = mock(Configuration.class);

            when(configuration.getContext()).thenReturn(context);
            when(configuration.get("javax.enterprise.inject.vetoed")).thenReturn(configuredValue);

            ConfigurationProvider.setConfiguration(configuration);
            AnnotatedType<TestKlazz> annotatedType = mock(AnnotatedType.class);
            when(annotatedType.getJavaClass()).thenReturn(TestKlazz.class);

            ProcessAnnotatedType<TestKlazz> processAnnotatedType = mock(ProcessAnnotatedType.class);
            when(processAnnotatedType.getAnnotatedType()).thenReturn(annotatedType);

            extension.observesBean(processAnnotatedType);

            verify(processAnnotatedType).veto();
        } finally {
            ConfigurationProvider.setConfiguration(oldConfiguration);
        }
    }
}
