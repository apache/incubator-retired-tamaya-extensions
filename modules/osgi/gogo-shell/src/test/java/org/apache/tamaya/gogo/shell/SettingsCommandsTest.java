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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

/**
 * Created by atsti on 30.09.2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class SettingsCommandsTest extends AbstractOSGITest{

    private SettingsCommands commands;

    @Before
    public void setupCommands(){
        commands = new SettingsCommands(bundleContext);
    }


    @Test
    public void testEnable() throws Exception {
        commands.tm_enable(true);
        verify(tamayaConfigPlugin).setTamayaEnabledByDefault(true);
        commands.tm_enable(false);
        verify(tamayaConfigPlugin).setTamayaEnabledByDefault(false);
    }

    @Test
    public void testEnabled() throws Exception {
        commands.tm_enabled();
        verify(tamayaConfigPlugin).isTamayaEnabledByDefault();
    }

    @Test
    public void testInfo() throws Exception {
        commands.tm_info();
        verify(tamayaConfigPlugin).isTamayaEnabledByDefault();
        verify(tamayaConfigPlugin).getDefaultPolicy();
    }

    @Test
    public void testPropagateUpdates() throws Exception {
        commands.tm_propagate_updates();
        verify(tamayaConfigPlugin).isAutoUpdateEnabled();
    }

    @Test
    public void testPropagateUpdatesSet() throws Exception {
        commands.tm_propagate_updates_set(true);
        verify(tamayaConfigPlugin).setAutoUpdateEnabled(true);
        commands.tm_propagate_updates_set(false);
        verify(tamayaConfigPlugin).setAutoUpdateEnabled(false);
    }

    @Test
    public void testPolicy() throws Exception {
        commands.tm_policy();
        verify(tamayaConfigPlugin).getDefaultPolicy();
    }

    @Test
    public void testPolicySet() throws Exception {
        commands.tm_policy_set(Policy.EXTEND);
        verify(tamayaConfigPlugin).setDefaultPolicy(Policy.EXTEND);
        commands.tm_policy_set(Policy.OVERRIDE);
        verify(tamayaConfigPlugin).setDefaultPolicy(Policy.OVERRIDE);
    }
}