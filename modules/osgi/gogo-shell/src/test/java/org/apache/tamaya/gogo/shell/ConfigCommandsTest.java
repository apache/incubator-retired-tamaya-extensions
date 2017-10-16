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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
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
        commands.tm_propertysources();
    }

    @Test
    public void testProperty_Default() throws Exception {
        commands.tm_property("system-properties", "java.version", false);
    }

    @Test
    public void testProperty_Extended() throws Exception {
        commands.tm_property("system-properties", "java.version", true);
    }

    @Test
    public void testPropertsource() throws Exception {
        commands.tm_propertysource("system-properties");
    }

    @Test
    public void testConfig() throws Exception {
        Dictionary<String,Object> testConfig = new Hashtable<>();
        testConfig.put("test","testVal");
        doReturn(testConfig).when(tamayaConfigPlugin).getOSGIConfiguration(any(),any());
        commands.tm_config(null,"testConfig");
        commands.tm_config("java","testConfig");
    }

    @Test
    public void testApplyConfig() throws Exception {
        Dictionary<String,Object> testConfig = new Hashtable<>();
        testConfig.put("test","testVal");
        doReturn(testConfig).when(tamayaConfigPlugin).updateConfig(any(),any(), anyBoolean(), anyBoolean());
        commands.tm_apply_config("testApplyConfig", Policy.EXTEND, true);
        verify(tamayaConfigPlugin).updateConfig("testApplyConfig", Policy.EXTEND, true, true);
        commands.tm_apply_config("testApplyConfig", Policy.OVERRIDE, false);
        verify(tamayaConfigPlugin).updateConfig("testApplyConfig", Policy.OVERRIDE, true, false);
    }

}