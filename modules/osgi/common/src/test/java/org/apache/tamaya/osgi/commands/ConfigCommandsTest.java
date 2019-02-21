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
package org.apache.tamaya.osgi.commands;

import org.apache.tamaya.osgi.AbstractOSGITest;
import org.apache.tamaya.osgi.Policy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by atsti on 30.09.2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigCommandsTest extends AbstractOSGITest{
    @Test
    public void getInfo() throws Exception {
        String result = ConfigCommands.getInfo(tamayaConfigPlugin);
        assertThat(result).isNotNull();
        System.out.println(result);
        assertThat(result.contains("Property Sources")).isTrue();
        assertThat(result.contains("Property Converter")).isTrue();
        assertThat(result.contains("Property Filter")).isTrue();
        assertThat(result.contains("ConfigurationContext")).isTrue();
        assertThat(result.contains("Configuration")).isTrue();
    }

    @Test
    public void readTamayaConfig() throws Exception {
        String result = ConfigCommands.readTamayaConfig("java", null);
        assertThat(result).isNotNull();
        System.out.println(result);
        assertThat(result.contains(".version")).isTrue();
        assertThat(result.contains("Section")).isTrue();
        assertThat(result.contains("java")).isTrue();
        result = ConfigCommands.readTamayaConfig("java", "version");
        assertThat(result).isNotNull();
        assertThat(result.contains(".version")).isFalse();
        assertThat(result.contains("Section")).isTrue();
        assertThat(result.contains("java")).isTrue();
        assertThat(result.contains("Filter")).isTrue();
        assertThat(result.contains("version")).isTrue();
        assertThat(result.contains("java.vendor")).isFalse();
        System.out.println("readTamayaConfig: " + result);
    }

    @Test
    public void readTamayaConfig4PID() throws Exception {
        String result = ConfigCommands.readTamayaConfig4PID("test", null);
        assertThat(result).isNotNull();
        System.out.println(result);
        assertThat(result.contains("Configuration")).isTrue();
        assertThat(result.contains("test")).isTrue();
    }

    @Test
    public void applyTamayaConfiguration() throws Exception {
        String result = ConfigCommands.applyTamayaConfiguration(tamayaConfigPlugin, "applyTamayaConfiguration", Policy.OVERRIDE.toString(), true);
        assertThat(result).isNotNull();
        System.out.println(result);
        assertThat(result.contains("OSGI Configuration for PID")).isTrue();
        assertThat(result.contains("applyTamayaConfiguration")).isTrue();
        assertThat(result.contains("OVERRIDE")).isTrue();
        assertThat(result.contains("Dryrun")).isTrue();
        assertThat(result.contains("true")).isTrue();
    }

    @Test
    public void readOSGIConfiguration() throws Exception {
        String result = ConfigCommands.readOSGIConfiguration(tamayaConfigPlugin, "readOSGIConfiguration", "java");
        assertThat(result).isNotNull();
        System.out.println(result);
        assertThat(result.contains("OSGI Configuration for PID")).isTrue();
        assertThat(result.contains("readOSGIConfiguration")).isTrue();
        assertThat(result.contains("java.home")).isTrue();
    }

    @Test
    public void getDefaultOpPolicy() throws Exception {
        Policy mode = tamayaConfigPlugin.getDefaultPolicy();
        String result = ConfigCommands.getDefaultOpPolicy(tamayaConfigPlugin);
        assertThat(result).isNotNull();
        assertThat(result.contains(mode.toString())).isTrue();
    }

    @Test
    public void setDefaultOpPolicy() throws Exception {
        String result = ConfigCommands.setDefaultOpPolicy(tamayaConfigPlugin, Policy.EXTEND.toString());
        assertThat(result).isNotNull();
        assertThat(result.contains("EXTEND")).isTrue();
        assertThat(tamayaConfigPlugin.getDefaultPolicy()).isEqualTo(Policy.EXTEND);
        result = ConfigCommands.setDefaultOpPolicy(tamayaConfigPlugin, Policy.UPDATE_ONLY.toString());
        assertThat(result).isNotNull();
        assertThat(result.contains("UPDATE_ONLY")).isTrue();
        assertThat(tamayaConfigPlugin.getDefaultPolicy()).isEqualTo(Policy.UPDATE_ONLY);
    }

    @Test
    public void getProperty() throws Exception {
        String result = ConfigCommands.getProperty("system-properties", "java.version", false);
        assertThat(result).isNotNull();
        System.out.println(result);
        assertThat(result).isEqualTo(System.getProperty("java.version"));
        result = ConfigCommands.getProperty("system-properties", "java.version", true);
        assertThat(result).isNotNull();
    }

    @Test
    public void getPropertySource() throws Exception {
        String result = ConfigCommands.getPropertySource("system-properties");
        assertThat(result).isNotNull();
        System.out.println(result);
        assertThat(result.contains("Property Source")).isTrue();
        assertThat(result.contains("ID")).isTrue();
        assertThat(result.contains("system-properties")).isTrue();
        assertThat(result.contains("Ordinal")).isTrue();
        assertThat(result.contains("java.version")).isTrue();
    }

    @Test
    public void getPropertySourceOverview() throws Exception {
        String result = ConfigCommands.getPropertySourceOverview();
        assertThat(result).isNotNull();
        System.out.println(result);
        assertThat(result.contains("Ordinal")).isTrue();
        assertThat(result.contains("Class")).isTrue();
        assertThat(result.contains("Ordinal")).isTrue();
        assertThat(result.contains("ID")).isTrue();
        assertThat(result.contains("Ordinal")).isTrue();
        assertThat(result.contains("system-properties")).isTrue();
        assertThat(result.contains("environment-properties")).isTrue();
        assertThat(result.contains("CLI")).isTrue();
    }

    @Test
    public void setDefaultEnabled() throws Exception {
        String result = ConfigCommands.setDefaultEnabled(tamayaConfigPlugin, true);
        assertThat(result).isNotNull();
        System.out.println(result);
        assertThat(result.contains(TamayaConfigService.TAMAYA_ENABLED_PROP+"=true")).isTrue();
        assertThat(tamayaConfigPlugin.isTamayaEnabledByDefault()).isTrue();
        result = ConfigCommands.setDefaultEnabled(tamayaConfigPlugin, false);
        assertThat(result).isNotNull();
        assertThat(result.contains(TamayaConfigService.TAMAYA_ENABLED_PROP+"=false")).isTrue();
        assertThat(tamayaConfigPlugin.isTamayaEnabledByDefault()).isFalse();
    }

    @Test
    public void getDefaultEnabled() throws Exception {
        tamayaConfigPlugin.setTamayaEnabledByDefault(true);
        String result = ConfigCommands.getDefaultEnabled(tamayaConfigPlugin);
        System.out.println(result);
        tamayaConfigPlugin.setTamayaEnabledByDefault(false);
        result = ConfigCommands.getDefaultEnabled(tamayaConfigPlugin);
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo("false");
    }

    @Test
    public void setAutoUpdateEnabled() throws Exception {
        String result = ConfigCommands.setAutoUpdateEnabled(tamayaConfigPlugin, true);
        assertThat(result).isNotNull();
        System.out.println(result);
        assertThat(result.contains("true")).isTrue();
        assertThat(result.contains(TamayaConfigService.TAMAYA_AUTO_UPDATE_ENABLED_PROP)).isTrue();
        assertThat(tamayaConfigPlugin.isAutoUpdateEnabled()).isTrue();
        result = ConfigCommands.setAutoUpdateEnabled(tamayaConfigPlugin, false);
        assertThat(result).isNotNull();
        assertThat(result.contains("false")).isTrue();
        assertThat(result.contains(TamayaConfigService.TAMAYA_AUTO_UPDATE_ENABLED_PROP)).isTrue();
        assertThat(tamayaConfigPlugin.isAutoUpdateEnabled()).isFalse();
    }

    @Test
    public void getAutoUpdateEnabled() throws Exception {
        tamayaConfigPlugin.setAutoUpdateEnabled(true);
        String result = ConfigCommands.getAutoUpdateEnabled(tamayaConfigPlugin);
        System.out.println(result);
        assertThat(result.contains("true")).isTrue();
        tamayaConfigPlugin.setAutoUpdateEnabled(false);
        result = ConfigCommands.getAutoUpdateEnabled(tamayaConfigPlugin);
        assertThat(result).isNotNull();
        assertThat(result.contains("false")).isTrue();
    }

}
