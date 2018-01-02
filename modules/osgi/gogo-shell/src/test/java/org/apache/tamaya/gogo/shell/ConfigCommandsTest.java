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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
        assertTrue(out.startsWith("Config Sources"));
        assertTrue(out.contains(  "--------------"));
        assertTrue(out.contains("ID"));
        assertTrue(out.contains("Ordinal"));
        assertTrue(out.contains("Class"));
        assertTrue(out.contains("Size"));
        assertTrue(out.contains("environment-properties"));
        assertTrue(out.contains("system-properties"));

    }

    @Test
    public void testProperty_Default() throws Exception {
        String out = runTest(() -> {
            commands.tm_property("system-properties", "java.version", false);
            return null;
        });
        assertEquals(System.getProperty("java.version").trim(), out.trim());
    }

    @Test
    public void testProperty_Extended() throws Exception {
        String out = runTest(() -> {
            commands.tm_property("system-properties", "java.version", true);
            return null;
        });
        assertTrue(out.contains(System.getProperty("java.version")));
        assertTrue(out.contains("Config Source"));
        assertTrue(out.contains("Value"));
        assertTrue(out.contains("system-properties"));
    }

    @Test
    public void testPropertsource() throws Exception {
        String out = runTest(() -> {
            commands.tm_propertysource("system-properties");
            return null;
        });
        assertTrue(out.startsWith("Config Source"));
        assertTrue(out.contains("ID"));
        assertTrue(out.contains("system-properties"));
        assertTrue(out.contains("Ordinal"));
        assertTrue(out.contains("1000"));
        assertTrue(out.contains("Class"));
        assertTrue(out.contains("SystemConfigSource"));
        assertTrue(out.contains("Properties"));
        assertTrue(out.contains("Key"));
        assertTrue(out.contains("Value"));
        assertTrue(out.contains("Source"));
        assertTrue(out.contains("Meta"));
        assertTrue(out.contains("java.version"));
        assertTrue(out.contains(System.getProperty("java.version")));
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
        assertTrue(out.contains("Tamaya Configuration"));
        assertTrue(out.contains("Section"));
        assertTrue(out.contains("[testConfig]"));
        assertTrue(out.contains("Configuration"));
        out = runTest(() -> {
            commands.tm_config("java", "testConfig");
            return null;
        });
        assertTrue(out.contains("Tamaya Configuration"));
        assertTrue(out.contains("Section"));
        assertTrue(out.contains("[testConfig]"));
        assertTrue(out.contains("Filter"));
        assertTrue(out.contains("java"));
        assertTrue(out.contains("Configuration"));
        out = runTest(() -> {
            commands.tm_config("java", "");
            return null;
        });
        assertTrue(out.contains("Tamaya Configuration"));
        assertTrue(out.contains("Section"));
        assertTrue(out.contains("java"));
        assertTrue(out.contains("Configuration"));
        assertTrue(out.contains(".version"));
        assertTrue(out.contains(System.getProperty("java.version")));
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
        assertTrue(out.contains("Applied Configuration"));
        assertTrue(out.contains("PID"));
        assertTrue(out.contains("testApplyConfig"));
        assertTrue(out.contains("Policy"));
        assertTrue(out.contains("EXTEND"));
        assertTrue(out.contains("Dryrun"));
        assertTrue(out.contains("true"));
        assertTrue(out.contains("OSGI Configuration for PID"));
        assertTrue(out.contains("test"));
        assertTrue(out.contains("testVal"));
        verify(tamayaConfigPlugin).updateConfig("testApplyConfig", Policy.EXTEND, true, true);
        out = runTest(() -> {
            commands.tm_apply_config("testApplyConfig", Policy.OVERRIDE, false);
            return null;
        });
        assertTrue(out.contains("Applied Configuration"));
        assertTrue(out.contains("PID"));
        assertTrue(out.contains("testApplyConfig"));
        assertTrue(out.contains("Policy"));
        assertTrue(out.contains("OVERRIDE"));
        assertTrue(out.contains("Dryrun"));
        assertTrue(out.contains("false"));
        assertTrue(out.contains("OSGI Configuration for PID"));
        assertTrue(out.contains("test"));
        assertTrue(out.contains("testVal"));
        verify(tamayaConfigPlugin).updateConfig("testApplyConfig", Policy.OVERRIDE, true, false);
    }

}