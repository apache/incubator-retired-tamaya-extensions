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

import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

import javax.enterprise.inject.spi.InjectionPoint;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

public class CDIConfiguredMethodTest {

    @Test
    public void returnsTheConfiguredKeys() {
        List<String> keys = asList("rate", "weight");
        InjectionPoint ip = Mockito.mock(InjectionPoint.class);

        CDIConfiguredMethod ccm = new CDIConfiguredMethod(ip, keys);

        assertThat(ccm.getConfiguredKeys(), Matchers.containsInAnyOrder("rate", "weight"));
    }

    @Test
    public void returnsTheNameOfTheGivenMethod() throws NoSuchMethodException {
        Method method = Klazz.class.getMethod("getValue");
        List<String> keys = asList("rate", "weight");
        InjectionPoint ip = Mockito.mock(InjectionPoint.class);

        when(ip.getMember()).thenReturn(method);

        CDIConfiguredMethod ccm = new CDIConfiguredMethod(ip, keys);

        assertThat(ccm.getName(), equalTo("getValue"));
    }

    @Test
    public void returnsCorrectSignatureOfTheGivenMethod1() throws Exception {
        String expectedSignature = "public org.apache.tamaya.cdi.OtherKlazz<java.lang.String> getValue()";

        Method method = Klazz.class.getMethod("getValue");
        List<String> keys = asList("rate", "weight");
        InjectionPoint ip = Mockito.mock(InjectionPoint.class);

        when(ip.getMember()).thenReturn(method);

        CDIConfiguredMethod ccm = new CDIConfiguredMethod(ip, keys);

        assertThat(ccm.getSignature(), equalTo(expectedSignature));
    }

    @Test
    public void returnsCorrectSignatureOfTheGivenMethod2() throws Exception {
        String expectedSignature = "public int getPrimitiveIntValue()";

        Method method = Klazz.class.getMethod("getPrimitiveIntValue");
        List<String> keys = asList("rate", "weight");
        InjectionPoint ip = Mockito.mock(InjectionPoint.class);

        when(ip.getMember()).thenReturn(method);

        CDIConfiguredMethod ccm = new CDIConfiguredMethod(ip, keys);

        assertThat(ccm.getSignature(), equalTo(expectedSignature));
    }

    @Test
    public void returnsCorrectSignatureOfTheGivenMethodReturnTypeIsVoid() throws Exception {
        String expectedSignature = "public void voidMethod()";

        Method method = Klazz.class.getMethod("voidMethod");
        List<String> keys = asList("rate", "weight");
        InjectionPoint ip = Mockito.mock(InjectionPoint.class);

        when(ip.getMember()).thenReturn(method);

        CDIConfiguredMethod ccm = new CDIConfiguredMethod(ip, keys);

        assertThat(ccm.getSignature(), equalTo(expectedSignature));
    }

    @Test
    public void returnsCorrectSignatureOfTheGivenMethodWithExceptions() throws Exception {
        String expectedSignature = "public void methodWithExceptions() " +
                                   "throws java.io.FileNotFoundException," +
                                   "java.nio.file.AccessDeniedException";

        Method method = Klazz.class.getMethod("methodWithExceptions");
        List<String> keys = asList("rate", "weight");
        InjectionPoint ip = Mockito.mock(InjectionPoint.class);

        when(ip.getMember()).thenReturn(method);

        CDIConfiguredMethod ccm = new CDIConfiguredMethod(ip, keys);

        assertThat(ccm.getSignature(), equalTo(expectedSignature));
    }

    @Test
    public void returnsCorrectSignatureOfTheGivenMethodWithOneException() throws Exception {
        String expectedSignature = "public void methodWithException() " +
                                   "throws java.io.FileNotFoundException";

        Method method = Klazz.class.getMethod("methodWithException");
        List<String> keys = asList("rate", "weight");
        InjectionPoint ip = Mockito.mock(InjectionPoint.class);

        when(ip.getMember()).thenReturn(method);

        CDIConfiguredMethod ccm = new CDIConfiguredMethod(ip, keys);

        assertThat(ccm.getSignature(), equalTo(expectedSignature));
    }

    @Test
    public void returnsCorrectSignatureOfTheGivenMethodWithOneParameter() throws Exception {
        String expectedSignature = "public void methodWithOneParameter(java.lang.String)";

        Method method = Klazz.class.getMethod("methodWithOneParameter", String.class);
        List<String> keys = asList("rate", "weight");
        InjectionPoint ip = Mockito.mock(InjectionPoint.class);

        when(ip.getMember()).thenReturn(method);

        CDIConfiguredMethod ccm = new CDIConfiguredMethod(ip, keys);

        assertThat(ccm.getSignature(), equalTo(expectedSignature));
    }

    @Test
    public void returnsCorrectSignatureOfTheGivenMethodWithTwoParameters() throws Exception {
        String expectedSignature = "public void methodWithTwoParameters(java.lang.String,java.io.File)";

        Method method = Klazz.class.getMethod("methodWithTwoParameters", String.class, File.class);
        List<String> keys = asList("rate", "weight");
        InjectionPoint ip = Mockito.mock(InjectionPoint.class);

        when(ip.getMember()).thenReturn(method);

        CDIConfiguredMethod ccm = new CDIConfiguredMethod(ip, keys);

        assertThat(ccm.getSignature(), equalTo(expectedSignature));
    }

    @Test
    public void returnsCorrectSignatureOfTheGivenMethodWithOneTypeParameter() throws Exception {
        String expectedSignature = "public <T> java.util.Collection<T> methodWithTypeParameter" +
                                   "(java.util.Collection<T>)";

        Method method = Klazz.class.getMethod("methodWithTypeParameter", Collection.class);

        List<String> keys = asList("rate", "weight");
        InjectionPoint ip = Mockito.mock(InjectionPoint.class);

        when(ip.getMember()).thenReturn(method);

        CDIConfiguredMethod ccm = new CDIConfiguredMethod(ip, keys);

        assertThat(ccm.getSignature(), equalTo(expectedSignature));
    }

}