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

import javax.config.ConfigProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test class that test resolution of different values as configured within
 * {@link MyTestConfigSource} and on test resource path.
 */
public class ConfigResolutionTest {

    @Test
    public void test_Prefix_Resolution() {
        assertEquals(ConfigProvider.getConfig().getValue("Before Text (prefixed)", String.class), "My Java version is " + System.getProperty("java.version"));
    }

    @Test
    public void test_Midfix_Resolution() {
        assertEquals(ConfigProvider.getConfig().getValue("Before and After Text (prefixed)", String.class), "My Java version is " + System.getProperty("java.version") + ".");
    }

    @Test
    public void test_Prefix_Resolution_BadSyntax1() {
        assertEquals(ConfigProvider.getConfig().getValue("Will fail1.", String.class), "V$java.version");
    }

    @Test
    public void test_Prefix_Resolution_BadSyntax2() {
        assertEquals(ConfigProvider.getConfig().getValue("Will fail2.", String.class), "V$java.version}");
    }

    @Test
    public void test_Prefix_Resolution_BadSyntax31() {
        assertEquals(ConfigProvider.getConfig().getValue("Will not fail3.", String.class), "V${java.version");
    }

    @Test
    public void test_Prefix_Resolution_Escaped1() {
        assertEquals(ConfigProvider.getConfig().getValue("Will not fail1.", String.class), "V$\\{java.version");
    }

    @Test
    public void test_Prefix_Resolution_Escaped2() {
        assertEquals(ConfigProvider.getConfig().getValue("Will not fail2.", String.class), "V\\${java.version");
    }

    @Test
    public void test_Prefix_Resolution_EnvKeys() {
        assertEquals(ConfigProvider.getConfig().getValue("env.keys", String.class), System.getProperty("java.version") + " plus $java.version");
    }

    @Test
    public void test_Prefix_ExpressionOnly_Resolution() {
        assertEquals(ConfigProvider.getConfig().getValue("Expression Only", String.class), System.getProperty("java.version"));
    }

    @Test
    public void testConfig_Refs() {
        assertEquals(ConfigProvider.getConfig().getValue("config-ref", String.class), "Expression Only -> " + System.getProperty("java.version"));
        assertEquals(ConfigProvider.getConfig().getValue("config-ref3", String.class), "Config Ref 3 -> Ref 2: Config Ref 2 -> Ref 1: Expression Only -> " + System.getProperty("java.version"));
        assertEquals(ConfigProvider.getConfig().getValue("config-ref2", String.class), "Config Ref 2 -> Ref 1: Expression Only -> " + System.getProperty("java.version"));
    }

    @Test
    public void testClasspath_Refs() {
        String value = ConfigProvider.getConfig().getValue("cp-ref", String.class);
        assertNotNull(value);
        assertTrue(value.contains("This content comes from Testresource.txt!"));
    }

    @Test
    public void testResource_Refs() {
        String value = ConfigProvider.getConfig().getValue("res-ref", String.class);
        assertNotNull(value);
        assertTrue(value.contains("This content comes from Testresource.txt!"));
    }

    @Test
    public void testFile_Refs() {
        String value = ConfigProvider.getConfig().getValue("file-ref", String.class);
        assertNotNull(value);
        assertTrue(value.contains("This content comes from Testresource2.txt!"));
    }

    @Test
    public void testURL_Refs() {
        String value = ConfigProvider.getConfig().getValue("url-ref", String.class);
        assertNotNull(value);
        assertTrue(value.contains("doctype html") || "[http://www.google.com]".equals(value));
    }

    @Test
    public void testEscaping(){
        assertEquals(ConfigProvider.getConfig().getValue("escaped", String.class),
                "Config Ref 3 -> Ref 2: \\${conf:config-ref2 will not be evaluated and will not contain\\t tabs \\n " +
                "newlines or \\r returns...YEP!");
    }

}