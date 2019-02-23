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
package org.apache.tamaya.cdi;

import org.apache.tamaya.cdi.BaseTestConfiguration;
import org.apache.tamaya.cdi.extra.ConfiguredVetoExtension;
import org.apache.tamaya.cdi.extra.TestKlazz;
import org.apache.tamaya.cdi.extra.TestKlazz2;
import org.junit.Test;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import static org.mockito.Mockito.*;

public class ConfiguredVetoExtensionTest extends BaseTestConfiguration {

    ConfiguredVetoExtension extension = new ConfiguredVetoExtension();

    @Test
    public void willBeVetoedIfTypeHasBeenConfiguredAsConcreteClassName() {

        try {
            System.setProperty("javax.enterprise.inject.vetoed", "org.apache.tamaya.cdi.extra.TestKlazz");
            AnnotatedType<TestKlazz> annotatedType = mock(AnnotatedType.class);
            when(annotatedType.getJavaClass()).thenReturn(TestKlazz.class);

            ProcessAnnotatedType<TestKlazz> processAnnotatedType = mock(ProcessAnnotatedType.class);
            when(processAnnotatedType.getAnnotatedType()).thenReturn(annotatedType);

            extension.observesBean(processAnnotatedType);

            (processAnnotatedType).veto();
        } finally {
            System.setProperty("javax.enterprise.inject.vetoed","");
        }
    }

    @Test
    public void willNotBeVetoedIfTypeHasNotBeenConfigured() {

        try {

            System.setProperty("javax.enterprise.inject.vetoed", "org.apache.tamaya.cdi.extra.Oz");

            AnnotatedType<TestKlazz> annotatedType = mock(AnnotatedType.class);
            when(annotatedType.getJavaClass()).thenReturn(TestKlazz.class);

            ProcessAnnotatedType<TestKlazz> processAnnotatedType = mock(ProcessAnnotatedType.class);
            when(processAnnotatedType.getAnnotatedType()).thenReturn(annotatedType);

            extension.observesBean(processAnnotatedType);

            verify(processAnnotatedType, never()).veto();
        } finally {
            System.setProperty("javax.enterprise.inject.vetoed","");
        }
    }

    @Test
    public void handlesPropertyWithWhitespacesCorrectly() {
        String configuredValue = "  " + TestKlazz.class.getName() +
                                 ",\t" + TestKlazz2.class.getName();

        try {
            System.setProperty("javax.enterprise.inject.vetoed", configuredValue);

            AnnotatedType<TestKlazz> annotatedType = mock(AnnotatedType.class);
            when(annotatedType.getJavaClass()).thenReturn(TestKlazz.class);

            ProcessAnnotatedType<TestKlazz> processAnnotatedType = mock(ProcessAnnotatedType.class);
            when(processAnnotatedType.getAnnotatedType()).thenReturn(annotatedType);

            extension.observesBean(processAnnotatedType);

            verify(processAnnotatedType).veto();
        } finally {
            System.setProperty("javax.enterprise.inject.vetoed", "");
        }
    }

    @Test
    public void useOfRegexPattersWorks() {
        String configuredValue = "  " + TestKlazz.class.getPackage().getName() +
                                 "\\..+";

        try {
            System.setProperty("javax.enterprise.inject.vetoed", configuredValue);

            AnnotatedType<TestKlazz> annotatedType = mock(AnnotatedType.class);
            when(annotatedType.getJavaClass()).thenReturn(TestKlazz.class);

            ProcessAnnotatedType<TestKlazz> processAnnotatedType = mock(ProcessAnnotatedType.class);
            when(processAnnotatedType.getAnnotatedType()).thenReturn(annotatedType);

            extension.observesBean(processAnnotatedType);

            verify(processAnnotatedType).veto();
        } finally {
            System.setProperty("javax.enterprise.inject.vetoed", "");
        }
    }
}
