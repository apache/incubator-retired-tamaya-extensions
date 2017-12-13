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

import org.junit.Test;

import javax.config.Config;
import javax.config.ConfigProvider;

import static org.junit.Assert.*;

/**
 * Test class that test resolution of different values as configured within
 * {@link MyTestConfigSource} after applying {@link Resolver#makeResolvable(Config)} to a
 * non resolvable instance.
 */
public class ResolvableConfigTest {

    private NonResolvableConfig nonResolvableConfig = new NonResolvableConfig();
    private Config resolvableConfig = Resolver.makeResolvable(nonResolvableConfig);

    @Test
    public void test_Prefix_Resolution() {
        assertNotSame(nonResolvableConfig.getValue("Before Text (prefixed)", String.class), "My Java version is " + System.getProperty("java.version"));
        assertEquals(resolvableConfig.getValue("Before Text (prefixed)", String.class), "My Java version is " + System.getProperty("java.version"));
    }

    @Test
    public void test_Midfix_Resolution() {
        assertNotSame(nonResolvableConfig.getValue("Before and After Text (prefixed)", String.class), "My Java version is " + System.getProperty("java.version") + ".");
        assertEquals(resolvableConfig.getValue("Before and After Text (prefixed)", String.class), "My Java version is " + System.getProperty("java.version") + ".");
    }

    @Test
    public void test_Prefix_Resolution_BadSyntax1() {
        assertEquals(nonResolvableConfig.getValue("Will fail1.", String.class), "V$java.version");
        assertEquals(resolvableConfig.getValue("Will fail1.", String.class), "V$java.version");
    }

    @Test
    public void test_Prefix_Resolution_BadSyntax2() {
        assertEquals(resolvableConfig.getValue("Will fail2.", String.class), "V$java.version}");
        assertEquals(nonResolvableConfig.getValue("Will fail2.", String.class), "V$java.version}");
    }

    @Test
    public void test_Prefix_Resolution_BadSyntax31() {
        assertEquals(resolvableConfig.getValue("Will not fail3.", String.class), "V${java.version");
        assertEquals(nonResolvableConfig.getValue("Will not fail3.", String.class), "V${java.version");
    }

    @Test
    public void test_Prefix_Resolution_Escaped1() {
        assertEquals(resolvableConfig.getValue("Will not fail1.", String.class), "V$\\{java.version");
        assertEquals(nonResolvableConfig.getValue("Will not fail1.", String.class), "V$\\{java.version");
    }

    @Test
    public void test_Prefix_Resolution_Escaped2() {
        assertEquals(resolvableConfig.getValue("Will not fail2.", String.class), "V\\${java.version");
        assertEquals(nonResolvableConfig.getValue("Will not fail2.", String.class), "V\\${java.version");
    }

    @Test
    public void test_Prefix_Resolution_EnvKeys() {
        assertEquals(resolvableConfig.getValue("env.keys", String.class), System.getProperty("java.version") + " plus $java.version");
        assertNotSame(nonResolvableConfig.getValue("env.keys", String.class), System.getProperty("java.version") + " plus $java.version");
    }

    @Test
    public void test_Prefix_ExpressionOnly_Resolution() {
        assertEquals(resolvableConfig.getValue("Expression Only", String.class), System.getProperty("java.version"));
        assertNotSame(nonResolvableConfig.getValue("Expression Only", String.class), System.getProperty("java.version"));
    }

    @Test
    public void testConfig_Refs() {
        assertEquals(resolvableConfig.getValue("config-ref", String.class), "Expression Only -> " + System.getProperty("java.version"));
        assertEquals(resolvableConfig.getValue("config-ref3", String.class), "Config Ref 3 -> Ref 2: Config Ref 2 -> Ref 1: Expression Only -> " + System.getProperty("java.version"));
        assertEquals(resolvableConfig.getValue("config-ref2", String.class), "Config Ref 2 -> Ref 1: Expression Only -> " + System.getProperty("java.version"));

        assertNotSame(nonResolvableConfig.getValue("config-ref", String.class), "Expression Only -> " + System.getProperty("java.version"));
        assertNotSame(nonResolvableConfig.getValue("config-ref3", String.class), "Config Ref 3 -> Ref 2: Config Ref 2 -> Ref 1: Expression Only -> " + System.getProperty("java.version"));
        assertNotSame(nonResolvableConfig.getValue("config-ref2", String.class), "Config Ref 2 -> Ref 1: Expression Only -> " + System.getProperty("java.version"));
    }

    @Test
    public void testClasspath_Refs() {
        String value = resolvableConfig.getValue("cp-ref", String.class);
        assertNotNull(value);
        assertTrue(value.contains("This content comes from Testresource.txt!"));
        value = nonResolvableConfig.getValue("cp-ref", String.class);
        assertNotNull(value);
        assertEquals("${resource:Testresource.txt}", value);
    }

    @Test
    public void testResource_Refs() {
        String value = resolvableConfig.getValue("res-ref", String.class);
        assertNotNull(value);
        assertTrue(value.contains("This content comes from Testresource.txt!"));
        value = nonResolvableConfig.getValue("res-ref", String.class);
        assertNotNull(value);
        assertEquals("${resource:Test?es*ce.txt}", value);
    }

    @Test
    public void testFile_Refs() {
        String value = resolvableConfig.getValue("file-ref", String.class);
        assertNotNull(value);
        assertTrue(value.contains("This content comes from Testresource2.txt!"));
        value = nonResolvableConfig.getValue("file-ref", String.class);
        assertNotNull(value);
        assertTrue(value.contains("Testresource2.txt}"));
        assertTrue(value.contains("${file:"));
    }

    @Test
    public void testURL_Refs() {
        String value = resolvableConfig.getValue("url-ref", String.class);
        assertNotNull(value);
        assertTrue(value.contains("doctype html") || "[http://www.google.com]".equals(value));
        value = nonResolvableConfig.getValue("url-ref", String.class);
        assertNotNull(value);
        assertEquals("${url:http://www.google.com}", value);
    }

    @Test
    public void testEscaping(){
        assertEquals(resolvableConfig.getValue("escaped", String.class),
                "Config Ref 3 -> Ref 2: \\${conf:config-ref2 will not be evaluated and will not contain\\t tabs \\n " +
                "newlines or \\r returns...YEP!");
        assertEquals(nonResolvableConfig.getValue("escaped", String.class),
                "Config Ref 3 -> Ref 2: \\${conf:config-ref2 will not be evaluated and will not contain\\t tabs \\n " +
                        "newlines or \\r returns...YEP!");
    }

    @Test
    public void testGetPropertyNames(){
        assertEquals(resolvableConfig.getPropertyNames(), nonResolvableConfig.getPropertyNames());
    }

    @Test
    public void testGetConfigSources(){
        assertEquals(resolvableConfig.getConfigSources(), nonResolvableConfig.getConfigSources());
    }

}