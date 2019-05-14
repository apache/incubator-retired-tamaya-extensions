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
package org.apache.tamaya.osgi;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Dictionary;
import java.util.Hashtable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by atsticks on 10.12.16.
 */
@RunWith(MockitoJUnitRunner.class)
public class TamayaConfigPluginTest extends  AbstractOSGITest{

    @Test
    public void pluginLoaded() throws Exception {
        assertThat(bundleContext.getService(bundleContext.getServiceReference(TamayaConfigPlugin.class))).isNotNull();
    }

    @Test
    public void testOperationMode() throws Exception {
        tamayaConfigPlugin.setDefaultPolicy(Policy.EXTEND);
        assertThat(Policy.EXTEND).isEqualTo(tamayaConfigPlugin.getDefaultPolicy());
        tamayaConfigPlugin.setDefaultPolicy(Policy.OVERRIDE);
    }

    @Test
    public void testAutoUpdate() throws Exception {
        boolean autoUpdate = tamayaConfigPlugin.isAutoUpdateEnabled();
        tamayaConfigPlugin.setAutoUpdateEnabled(!autoUpdate);
        assertThat(tamayaConfigPlugin.isAutoUpdateEnabled()).isEqualTo(!autoUpdate);
        tamayaConfigPlugin.setAutoUpdateEnabled(autoUpdate);
        assertThat(tamayaConfigPlugin.isAutoUpdateEnabled()).isEqualTo(autoUpdate);
    }

    @Test
    public void testDefaulEnabled() throws Exception {
        boolean enabled = tamayaConfigPlugin.isTamayaEnabledByDefault();
        tamayaConfigPlugin.setTamayaEnabledByDefault(!enabled);
        assertThat(tamayaConfigPlugin.isTamayaEnabledByDefault()).isEqualTo(!enabled);
        tamayaConfigPlugin.setTamayaEnabledByDefault(enabled);
        assertThat(tamayaConfigPlugin.isTamayaEnabledByDefault()).isEqualTo(enabled);
    }

    @Test
    public void testSetPluginConfig() throws Exception {
        Dictionary<String,Object> config = new Hashtable<>();
        ((TamayaConfigPlugin)tamayaConfigPlugin).setPluginConfig(config);
        assertThat(((TamayaConfigPlugin)tamayaConfigPlugin).getPluginConfig()).isEqualTo(config);
    }

    @Test
    public void testSetGetConfigValue() throws Exception {
        ((TamayaConfigPlugin)tamayaConfigPlugin).setConfigValue("bar", "foo");
        assertThat(((TamayaConfigPlugin)tamayaConfigPlugin).getConfigValue("bar")).isEqualTo("foo");
    }

    @Test
    public void getTMUpdateConfig() throws Exception {
        org.apache.tamaya.Configuration config = ((TamayaConfigPlugin)tamayaConfigPlugin).getTamayaConfiguration("java.");
        assertThat(config).isNotNull();
        assertThat(config.get("jlkjllj")).isNull();
        assertThat(System.getProperty("java.home")).isEqualTo(config.get("home"));
    }

    @Test
    public void getUpdateConfig() throws Exception {
        Dictionary<String, Object> config = tamayaConfigPlugin.updateConfig(TamayaConfigPlugin.COMPONENTID);
        assertThat(config).isNotNull();
        assertThat(System.getProperty("java.home")).isEqualTo(config.get("java.home"));
    }

    @Test
    public void getUpdateConfig_DryRun() throws Exception {
        Dictionary<String, Object> config = tamayaConfigPlugin.updateConfig(TamayaConfigPlugin.COMPONENTID, true);
        assertThat(config).isNotNull();
        assertThat(System.getProperty("java.home")).isEqualTo(config.get("java.home"));
    }

    @Test
    public void getUpdateConfig_Explicit_DryRun() throws Exception {
        Dictionary<String, Object> config = tamayaConfigPlugin.updateConfig(TamayaConfigPlugin.COMPONENTID, Policy.EXTEND, true, true);
        assertThat(config).isNotNull();
        assertThat(config.get("java.home")).isEqualTo(System.getProperty("java.home"));
    }

    @Test
    public void getPluginConfig() throws Exception {
        Dictionary<String, Object> config = ((TamayaConfigPlugin)tamayaConfigPlugin).getPluginConfig();
        assertThat(config).isNotNull();
        assertThat(super.getProperties(TamayaConfigPlugin.COMPONENTID)).isEqualTo(config);
    }

    @Test
    public void getDefaultOperationMode() throws Exception {
        Policy om = tamayaConfigPlugin.getDefaultPolicy();
        assertThat(om).isNotNull();
        Dictionary<String,Object> pluginConfig = super.getProperties(TamayaConfigPlugin.COMPONENTID);
        pluginConfig.put(Policy.class.getSimpleName(), Policy.UPDATE_ONLY.toString());
        TamayaConfigPlugin plugin = new TamayaConfigPlugin(bundleContext);
        om = plugin.getDefaultPolicy();
        assertThat(om).isNotNull();
        assertThat(Policy.UPDATE_ONLY).isEqualTo(om);
        pluginConfig.put(Policy.class.getSimpleName(), Policy.OVERRIDE.toString());
        plugin = new TamayaConfigPlugin(bundleContext);
        om = plugin.getDefaultPolicy();
        assertThat(om).isNotNull();
        assertThat(Policy.OVERRIDE).isEqualTo(om);
    }

    @Test
    public void testConfiguration_Override() throws Exception {
        assertThat(cm).isNotNull();
        tamayaConfigPlugin.updateConfig("tamaya", Policy.OVERRIDE, true, false);
        org.osgi.service.cm.Configuration config = cm.getConfiguration("tamaya");
        assertThat(config).isNotNull();
        assertThat(config.getProperties()).isNotNull();
        assertThat(config.getProperties().isEmpty()).isFalse();
        assertThat(config.getProperties().size() > 4).isTrue();
        // Override should addPropertyValue additional values
        assertThat("success1").isEqualTo(config.getProperties().get("my.testProperty1"));
        assertThat("success2").isEqualTo(config.getProperties().get("my.testProperty2"));
        assertThat("success3").isEqualTo(config.getProperties().get("my.testProperty3"));
        assertThat("success4").isEqualTo(config.getProperties().get("my.testProperty4"));
        // Extend should also update any existing values...
        assertThat("Java2000").isEqualTo(config.getProperties().get("java.version"));
        tamayaConfigPlugin.restoreBackup("tamaya");
    }

    @Test
    public void testConfiguration_Override_ImplicitlyConfigured() throws Exception {
        assertThat(cm).isNotNull();
        org.osgi.service.cm.Configuration config = cm.getConfiguration("tamaya");
        Dictionary<String,Object> props = config.getProperties();
        props.put(TamayaConfigPlugin.TAMAYA_POLICY_PROP, "OVERRIDE");
        config.update(props);
        tamayaConfigPlugin.updateConfig("tamaya", Policy.UPDATE_ONLY, false, false);
        config = cm.getConfiguration("tamaya");
        assertThat(config).isNotNull();
        assertThat(config.getProperties()).isNotNull();
        assertThat(config.getProperties().isEmpty()).isFalse();
        assertThat(config.getProperties().size() > 4).isTrue();
        // Override should addPropertyValue additional values
        assertThat("success1").isEqualTo(config.getProperties().get("my.testProperty1"));
        assertThat("success2").isEqualTo(config.getProperties().get("my.testProperty2"));
        assertThat("success3").isEqualTo(config.getProperties().get("my.testProperty3"));
        assertThat("success4").isEqualTo(config.getProperties().get("my.testProperty4"));
        // Extend should also update any existing values...
        assertThat("Java2000").isEqualTo(config.getProperties().get("java.version"));
        tamayaConfigPlugin.restoreBackup("tamaya");
    }

    @Test
    public void testConfiguration_Extend() throws Exception {
        assertThat(cm).isNotNull();
        tamayaConfigPlugin.updateConfig("tamaya", Policy.EXTEND, true, false);
        org.osgi.service.cm.Configuration config = cm.getConfiguration("tamaya");
        assertThat(config).isNotNull();
        assertThat(config.getProperties()).isNotNull();
        assertThat(config.getProperties().isEmpty()).isFalse();
        assertThat(config.getProperties().size() > 4).isTrue();
        assertThat(config.getProperties().get("my.testProperty1")).isEqualTo("success1");
        assertThat(config.getProperties().get("my.testProperty2")).isEqualTo("success2");
        assertThat(config.getProperties().get("my.testProperty3")).isEqualTo("success3");
        assertThat(config.getProperties().get("my.testProperty4")).isEqualTo("success4");
        // Extend should not update any existing values...
        assertThat(config.getProperties().get("java.version")).isEqualTo(System.getProperty("java.version"));
        tamayaConfigPlugin.restoreBackup("tamaya");
    }

    @Test
    public void testConfiguration_Update_Only() throws Exception {
        assertThat(cm).isNotNull();
        tamayaConfigPlugin.updateConfig("tamaya", Policy.UPDATE_ONLY, true, false);
        org.osgi.service.cm.Configuration config = cm.getConfiguration("tamaya");
        assertThat(config).isNotNull();
        assertThat(config.getProperties()).isNotNull();
        assertThat(config.getProperties().isEmpty()).isFalse();
        assertThat(config.getProperties().size() > 4).isTrue();
        assertThat(config.getProperties().get("my.testProperty1")).isNull();
        assertThat(config.getProperties().get("my.testProperty2")).isNull();
        assertThat(config.getProperties().get("my.testProperty3")).isNull();
        assertThat(config.getProperties().get("my.testProperty4")).isNull();
        // Update only should update any existing values...
        assertThat(config.getProperties().get("java.version")).isEqualTo("Java2000");
        tamayaConfigPlugin.restoreBackup("tamaya");
    }

    @Test
    public void testConfiguration_Override_Dryrun() throws Exception {
        assertThat(cm).isNotNull();
        Dictionary<String,Object> result = tamayaConfigPlugin.updateConfig("tamaya", Policy.OVERRIDE, true, true);
        assertThat(result).isNotNull();
        // Override should addPropertyValue additional values
        assertThat(result.get("my.testProperty1")).isEqualTo("success1");
        assertThat(result.get("my.testProperty2")).isEqualTo("success2");
        assertThat(result.get("my.testProperty3")).isEqualTo("success3");
        assertThat(result.get("my.testProperty4")).isEqualTo("success4");
        // Extend should also update any existing values...
        assertThat(result.get("java.version")).isEqualTo("Java2000");

        // DryRun: should not have been changged anything on OSGI level...
        org.osgi.service.cm.Configuration config = cm.getConfiguration("tamaya");
        assertThat(config).isNotNull();
        assertThat(config.getProperties()).isNotNull();
        assertThat(config.getProperties().isEmpty()).isFalse();
        assertThat(config.getProperties().size() > 4).isTrue();
        assertThat(config.getProperties().get("my.testProperty1")).isNull();
        assertThat(config.getProperties().get("my.testProperty2")).isNull();
        assertThat(config.getProperties().get("my.testProperty3")).isNull();
        assertThat(config.getProperties().get("my.testProperty4")).isNull();
        assertThat(config.getProperties().get("java.version")).isEqualTo(System.getProperty("java.version"));
    }

    @Test
    public void testConfiguration_Extend_Dryrun() throws Exception {
        assertThat(cm).isNotNull();
        Dictionary<String,Object> result = tamayaConfigPlugin.updateConfig("tamaya", Policy.EXTEND, true, true);
        assertThat(result).isNotNull();
        assertThat(result.get("my.testProperty1")).isEqualTo("success1");
        assertThat(result.get("my.testProperty2")).isEqualTo("success2");
        assertThat(result.get("my.testProperty3")).isEqualTo("success3");
        assertThat(result.get("my.testProperty4")).isEqualTo("success4");
        // Extend should not update any existing values...
        assertThat(result.get("java.version")).isEqualTo(System.getProperty("java.version"));

        // DryRun: should not have been changged anything on OSGI level...
        org.osgi.service.cm.Configuration config = cm.getConfiguration("tamaya");
        assertThat(config).isNotNull();
        assertThat(config.getProperties()).isNotNull();
        assertThat(config.getProperties().isEmpty()).isFalse();
        assertThat(config.getProperties().size() > 4).isTrue();
        assertThat(config.getProperties().get("my.testProperty1")).isNull();
        assertThat(config.getProperties().get("my.testProperty2")).isNull();
        assertThat(config.getProperties().get("my.testProperty3")).isNull();
        assertThat(config.getProperties().get("my.testProperty4")).isNull();
        assertThat(config.getProperties().get("java.version")).isEqualTo(System.getProperty("java.version"));
    }

    @Test
    public void testConfiguration_Update_Only_Dryrun() throws Exception {
        assertThat(cm).isNotNull();
        Dictionary<String,Object> result = tamayaConfigPlugin.updateConfig("tamaya", Policy.UPDATE_ONLY, true, true);
        assertThat(result).isNotNull();
        assertThat(result.size() > 4).isTrue();
        assertThat(result.get("my.testProperty1")).isNull();
        assertThat(result.get("my.testProperty2")).isNull();
        assertThat(result.get("my.testProperty3")).isNull();
        assertThat(result.get("my.testProperty4")).isNull();
        // Update only should update any existing values...
        assertThat(result.get("java.version")).isEqualTo("Java2000");

        // DryRun: should not have been changged anything on OSGI level...
        org.osgi.service.cm.Configuration config = cm.getConfiguration("tamaya");
        assertThat(config).isNotNull();
        assertThat(config.getProperties()).isNotNull();
        assertThat(config.getProperties().isEmpty()).isFalse();
        assertThat(config.getProperties().size() > 4).isTrue();
        assertThat(config.getProperties().get("my.testProperty1")).isNull();
        assertThat(config.getProperties().get("my.testProperty2")).isNull();
        assertThat(config.getProperties().get("my.testProperty3")).isNull();
        assertThat(config.getProperties().get("my.testProperty4")).isNull();
        assertThat(config.getProperties().get("java.version")).isEqualTo(System.getProperty("java.version"));
    }

}
