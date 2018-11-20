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
package org.apache.tamaya.resolver;

import org.apache.tamaya.spi.PropertyValue;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link org.apache.tamaya.resolver.Resolver}.
 */
public class ResolverTest {

    @Test
    public void testEvaluateExpression_withMask_NoKey() throws Exception {
        assertEquals(Resolver.getInstance().evaluateExpression("Version ${java.foo}", true),
                "Version ?{java.foo}");
    }

    @Test
    public void testEvaluateExpression_withMask_Classloader() throws Exception {
        assertEquals(Resolver.getInstance(Thread.currentThread().getContextClassLoader())
                .evaluateExpression("Version ${java.foo}", true),
                "Version ?{java.foo}");
    }


    @Test
    public void testEvaluateExpression() throws Exception {
        assertEquals(Resolver.getInstance().evaluateExpression("myKey", "Version ${java.version}"),
                "Version " + System.getProperty("java.version"));
    }

    @Test
    public void testEvaluateExpression_NoKey() throws Exception {
        assertEquals(Resolver.getInstance().evaluateExpression("Version ${java.version}"),
                "Version " + System.getProperty("java.version"));
    }

    @Test
    public void testEvaluateExpression_ClassLoader() throws Exception {
        assertEquals(Resolver.getInstance().evaluateExpression("myKey", "Version ${java.version}",
                Thread.currentThread().getContextClassLoader()),
                "Version " + System.getProperty("java.version"));
    }

    @Test
    public void testEvaluateExpression1_NoKey_ClassLoader() throws Exception {
        assertEquals(Resolver.getInstance().evaluateExpression("Version ${java.version}"),
                "Version " + System.getProperty("java.version"));
    }

    // TAMAYA-357
    @Test
    public void testEvaluateExpression_PropertyValue() throws Exception {
        String envKey = System.getenv().keySet().iterator().next();
        String expression = "Test ${java.version},${java.foo},${"+envKey+"}";
        PropertyValue val = PropertyValue.createValue("AnyKey", expression);
        PropertyValue evaluated = (Resolver.getInstance().evaluateExpression(val, true));
        assertNotNull(evaluated);
        assertNotNull(evaluated.getMeta("resolvers"));
        assertEquals("Test "+System.getProperty("java.version")+",?{java.foo},"+System.getenv(envKey),
                val.getValue());
        assertEquals("system-property, <unresolved>, environment-property, ", evaluated.getMeta("resolvers"));
    }

}