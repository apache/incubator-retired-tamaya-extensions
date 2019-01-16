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
package org.apache.tamaya.gogo.shell;

import org.apache.tamaya.osgi.Policy;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Dictionary;
import java.util.Hashtable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

/**
 * Created by atsti on 30.09.2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigCommandsTest extends AbstractOSGITest{

    private ConfigCommands commands;

    @Before
    public void setupCommands(){
        commands = new ConfigCommands(bundleContext);
    }


    @Test
    public void testPropertySources() throws Exception {
        String out = runTest(() -> {
            commands.tm_propertysources();
            return null;
        });
        assertThat(out.startsWith("Property Sources")).isTrue();
        assertThat(out.contains(  "----------------")).isTrue();
        assertThat(out.contains("ID")).isTrue();
        assertThat(out.contains("Ordinal")).isTrue();
        assertThat(out.contains("Class")).isTrue();
        assertThat(out.contains("Size")).isTrue();
        assertThat(out.contains("environment-properties")).isTrue();
        assertThat(out.contains("system-properties")).isTrue();
    }

    @Test
    public void testProperty_Default() throws Exception {
        String out = runTest(() -> {
            commands.tm_property("system-properties", "java.version", false);
            return null;
        });
        assertThat(System.getProperty("java.version").trim()).isEqualTo(out.trim());
    }

    @Test
    public void testProperty_Extended() throws Exception {
        String out = runTest(() -> {
            commands.tm_property("system-properties", "java.version", true);
            return null;
        });
        assertThat(out.contains(System.getProperty("java.version"))).isTrue();
        assertThat(out.contains("Property Source")).isTrue();
        assertThat(out.contains("Value")).isTrue();
        assertThat(out.contains("system-properties")).isTrue();
    }

    @Test
    public void testPropertsource() throws Exception {
        String out = runTest(() -> {
            commands.tm_propertysource("system-properties");
            return null;
        });
        assertThat(out.startsWith("Property Source")).isTrue();
        assertThat(out.contains("ID")).isTrue();
        assertThat(out.contains("system-properties")).isTrue();
        assertThat(out.contains("Ordinal")).isTrue();
        assertThat(out.contains("1000")).isTrue();
        assertThat(out.contains("Class")).isTrue();
        assertThat(out.contains("SystemPropertySource")).isTrue();
        assertThat(out.contains("Properties")).isTrue();
        assertThat(out.contains("Key")).isTrue();
        assertThat(out.contains("Value")).isTrue();
        assertThat(out.contains("Source")).isTrue();
        assertThat(out.contains("Meta")).isTrue();
        assertThat(out.contains("java.version")).isTrue();
        assertThat(out.contains(System.getProperty("java.version"))).isTrue();
    }

    @Test
    public void testConfig() throws Exception {
        Dictionary<String,Object> testConfig = new Hashtable<>();
        testConfig.put("test","testVal");
        doReturn(testConfig).when(tamayaConfigPlugin).getOSGIConfiguration(any(),any());
        String out = runTest(() -> {
            commands.tm_config(null, "testConfig");
            return null;
        });
        assertThat(out.contains("Tamaya Configuration")).isTrue();
        assertThat(out.contains("Section")).isTrue();
        assertThat(out.contains("[testConfig]")).isTrue();
        assertThat(out.contains("Configuration")).isTrue();
        out = runTest(() -> {
            commands.tm_config("java", "testConfig");
            return null;
        });
        assertThat(out.contains("Tamaya Configuration")).isTrue();
        assertThat(out.contains("Section")).isTrue();
        assertThat(out.contains("[testConfig]")).isTrue();
        assertThat(out.contains("Filter")).isTrue();
        assertThat(out.contains("java")).isTrue();
        assertThat(out.contains("Configuration")).isTrue();
        out = runTest(() -> {
            commands.tm_config("java", "");
            return null;
        });
        assertThat(out.contains("Tamaya Configuration")).isTrue();
        assertThat(out.contains("Section")).isTrue();
        assertThat(out.contains("java")).isTrue();
        assertThat(out.contains("Configuration")).isTrue();
        assertThat(out.contains(".version")).isTrue();
        assertThat(out.contains(System.getProperty("java.version"))).isTrue();
    }

    @Test
    public void testApplyConfig() throws Exception {
        Dictionary<String,Object> testConfig = new Hashtable<>();
        testConfig.put("test","testVal");
        doReturn(testConfig).when(tamayaConfigPlugin).updateConfig(any(),any(), anyBoolean(), anyBoolean());
        String out = runTest(() -> {
            commands.tm_apply_config("testApplyConfig", Policy.EXTEND, true);
            return null;
        });
        assertThat(out.contains("Applied Configuration")).isTrue();
        assertThat(out.contains("PID")).isTrue();
        assertThat(out.contains("testApplyConfig")).isTrue();
        assertThat(out.contains("Policy")).isTrue();
        assertThat(out.contains("EXTEND")).isTrue();
        assertThat(out.contains("Dryrun")).isTrue();
        assertThat(out.contains("true")).isTrue();
        assertThat(out.contains("OSGI Configuration for PID")).isTrue();
        assertThat(out.contains("test")).isTrue();
        assertThat(out.contains("testVal")).isTrue();
        verify(tamayaConfigPlugin).updateConfig("testApplyConfig", Policy.EXTEND, true, true);
        out = runTest(() -> {
            commands.tm_apply_config("testApplyConfig", Policy.OVERRIDE, false);
            return null;
        });
        assertThat(out.contains("Applied Configuration")).isTrue();
        assertThat(out.contains("PID")).isTrue();
        assertThat(out.contains("testApplyConfig")).isTrue();
        assertThat(out.contains("Policy")).isTrue();
        assertThat(out.contains("OVERRIDE")).isTrue();
        assertThat(out.contains("Dryrun")).isTrue();
        assertThat(out.contains("false")).isTrue();
        assertThat(out.contains("OSGI Configuration for PID")).isTrue();
        assertThat(out.contains("test")).isTrue();
        assertThat(out.contains("testVal")).isTrue();
        verify(tamayaConfigPlugin).updateConfig("testApplyConfig", Policy.OVERRIDE, true, false);
    }

}
