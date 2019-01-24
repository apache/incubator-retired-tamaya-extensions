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

import org.apache.tamaya.Configuration;
import org.junit.Test;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class that test resolution of different values as configured within
 * {@link org.apache.tamaya.resolver.MyTestPropertySource} and on test resource path.
 */
public class ConfigResolutionTest {

    @Test
    public void test_Prefix_Resolution() {
        assertThat(Configuration.current().get("Before Text (prefixed)"))
            .isEqualTo("My Java version is " + System.getProperty("java.version"));
    }

    @Test
    public void test_Midfix_Resolution() {
        assertThat(Configuration.current().get("Before and After Text (prefixed)"))
            .isEqualTo("My Java version is " + System.getProperty("java.version") + ".");
    }

    @Test
    public void test_Prefix_Resolution_BadSyntax1() {
        assertThat(Configuration.current().get("Will fail1.")).isEqualTo("V$java.version");
    }

    @Test
    public void test_Prefix_Resolution_BadSyntax2() {
        assertThat(Configuration.current().get("Will fail2.")).isEqualTo("V$java.version}");
    }

    @Test
    public void test_Prefix_Resolution_BadSyntax31() {
        assertThat(Configuration.current().get("Will not fail3.")).isEqualTo("V${java.version");
    }

    @Test
    public void test_Prefix_Resolution_Escaped1() {
        assertThat(Configuration.current().get("Will not fail1.")).isEqualTo("V$\\{java.version");
    }

    @Test
    public void test_Prefix_Resolution_Escaped2() {
        assertThat(Configuration.current().get("Will not fail2.")).isEqualTo("V\\${java.version");
    }

    @Test
    public void test_Prefix_Resolution_EnvKeys() {
        assertThat(Configuration.current().get("env.keys"))
            .isEqualTo(System.getProperty("java.version") + " plus $java.version");
    }

    @Test
    public void test_Prefix_ExpressionOnly_Resolution() {
        assertThat(Configuration.current().get("Expression Only")).isEqualTo(System.getProperty("java.version"));
    }

    @Test
    public void testConfig_Refs() {
        assertThat(Configuration.current().get("config-ref"))
            .isEqualTo("Expression Only -> " + System.getProperty("java.version"));
        assertThat(Configuration.current().get("config-ref3"))
            .isEqualTo("Config Ref 3 -> Ref 2: Config Ref 2 -> Ref 1: Expression Only -> " + System.getProperty("java.version"));
        assertThat(Configuration.current().get("config-ref2"))
            .isEqualTo("Config Ref 2 -> Ref 1: Expression Only -> " + System.getProperty("java.version"));
    }

    @Test
    public void testClasspath_Refs() {
        String value = Configuration.current().get("cp-ref");
        assertThat(value).isNotNull().contains("This content comes from Testresource.txt!");
    }

    @Test
    public void testResource_Refs() {
        String value = Configuration.current().get("res-ref");
        assertThat(value).isNotNull().contains("This content comes from Testresource.txt!");
    }

    @Test
    public void testFile_Refs() {
        String value = Configuration.current().get("file-ref");
        assertThat(value).isNotNull().contains("This content comes from Testresource2.txt!");
    }

    @Test
    public void testFile_Refs_doNotAppendNewLineAtTheEnd() throws Exception {
        String value = Configuration.current().get("file3-ref");
        URI uri = getClass().getClassLoader().getResource("Testresource3.txt").toURI();
        byte[] byteContent = Files.readAllBytes(Paths.get(uri));
        String content = new String(byteContent, StandardCharsets.UTF_8);
        assertThat(content).isEqualTo(value);
    }

    @Test
    public void testURL_Refs() {
        String value = Configuration.current().get("url-ref");
        assertThat(value).isNotNull();
        assertThat(value.contains("doctype html") || "[http://www.google.com]".equals(value)).isTrue();
    }

    @Test
    public void testEscaping(){
        assertThat(Configuration.current().get("escaped")).isEqualTo(
                "Config Ref 3 -> Ref 2: \\${conf:config-ref2 will not be evaluated and will not contain\\t tabs \\n " +
                "newlines or \\r returns...YEP!");
    }

}
