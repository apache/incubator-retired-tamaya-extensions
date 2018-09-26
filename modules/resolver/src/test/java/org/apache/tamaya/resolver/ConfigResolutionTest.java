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

import org.apache.tamaya.ConfigurationProvider;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Test class that test resolution of different values as configured within
 * {@link org.apache.tamaya.resolver.MyTestPropertySource} and on test resource path.
 */
public class ConfigResolutionTest {

    @Test
    public void test_Prefix_Resolution() {
        assertEquals(ConfigurationProvider.getConfiguration().get("Before Text (prefixed)"), "My Java version is " + System.getProperty("java.version"));
    }

    @Test
    public void test_Midfix_Resolution() {
        assertEquals(ConfigurationProvider.getConfiguration().get("Before and After Text (prefixed)"), "My Java version is " + System.getProperty("java.version") + ".");
    }

    @Test
    public void test_Prefix_Resolution_BadSyntax1() {
        assertEquals(ConfigurationProvider.getConfiguration().get("Will fail1."), "V$java.version");
    }

    @Test
    public void test_Prefix_Resolution_BadSyntax2() {
        assertEquals(ConfigurationProvider.getConfiguration().get("Will fail2."), "V$java.version}");
    }

    @Test
    public void test_Prefix_Resolution_BadSyntax31() {
        assertEquals(ConfigurationProvider.getConfiguration().get("Will not fail3."), "V${java.version");
    }

    @Test
    public void test_Prefix_Resolution_Escaped1() {
        assertEquals(ConfigurationProvider.getConfiguration().get("Will not fail1."), "V$\\{java.version");
    }

    @Test
    public void test_Prefix_Resolution_Escaped2() {
        assertEquals(ConfigurationProvider.getConfiguration().get("Will not fail2."), "V\\${java.version");
    }

    @Test
    public void test_Prefix_Resolution_EnvKeys() {
        assertEquals(ConfigurationProvider.getConfiguration().get("env.keys"), System.getProperty("java.version") + " plus $java.version");
    }

    @Test
    public void test_Prefix_ExpressionOnly_Resolution() {
        assertEquals(ConfigurationProvider.getConfiguration().get("Expression Only"), System.getProperty("java.version"));
    }

    @Test
    public void testConfig_Refs() {
        assertEquals(ConfigurationProvider.getConfiguration().get("config-ref"), "Expression Only -> " + System.getProperty("java.version"));
        assertEquals(ConfigurationProvider.getConfiguration().get("config-ref3"), "Config Ref 3 -> Ref 2: Config Ref 2 -> Ref 1: Expression Only -> " + System.getProperty("java.version"));
        assertEquals(ConfigurationProvider.getConfiguration().get("config-ref2"), "Config Ref 2 -> Ref 1: Expression Only -> " + System.getProperty("java.version"));
    }

    @Test
    public void testClasspath_Refs() {
        String value = ConfigurationProvider.getConfiguration().get("cp-ref");
        assertNotNull(value);
        assertTrue(value.contains("This content comes from Testresource.txt!"));
    }

    @Test
    public void testResource_Refs() {
        String value = ConfigurationProvider.getConfiguration().get("res-ref");
        assertNotNull(value);
        assertTrue(value.contains("This content comes from Testresource.txt!"));
    }

    @Test
    public void testFile_Refs() {
        String value = ConfigurationProvider.getConfiguration().get("file-ref");
        assertNotNull(value);
        assertTrue(value.contains("This content comes from Testresource2.txt!"));
    }
    
    @Test
    public void testFile_Refs_doNotAppendNewLineAtTheEnd() throws Exception {
        String value = ConfigurationProvider.getConfiguration().get("file3-ref");
        
        URI uri = getClass().getClassLoader().getResource("Testresource3.txt").toURI();
        byte[] byteContent = Files.readAllBytes(Paths.get(uri));
        String content = new String(byteContent, StandardCharsets.UTF_8);
        
        assertEquals(content, value);
    }

    @Test
    public void testURL_Refs() {
        String value = ConfigurationProvider.getConfiguration().get("url-ref");
        assertNotNull(value);
        assertTrue(value.contains("doctype html") || "[http://www.google.com]".equals(value));
    }

    @Test
    public void testEscaping(){
        assertEquals(ConfigurationProvider.getConfiguration().get("escaped"),
                "Config Ref 3 -> Ref 2: \\${conf:config-ref2 will not be evaluated and will not contain\\t tabs \\n " +
                "newlines or \\r returns...YEP!");
    }

}