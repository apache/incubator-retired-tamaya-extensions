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
package org.apache.tamaya.mutableconfig;

import org.apache.tamaya.mutableconfig.internal.WritablePropertiesSource;
import org.apache.tamaya.mutableconfig.internal.WritableXmlPropertiesSource;
import org.junit.Test;

import javax.config.ConfigProvider;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Tests for {@link MutableConfig}.
 */
public class MutableConfigTest {

    /**
     * Test create change request.
     *
     * @throws Exception the exception
     */
    @Test
    public void testCreateMutableConfiguration() throws Exception {
        File f = File.createTempFile("ConfigChangeRequest",".properties");
        MutableConfig cfg1 = MutableConfigProvider.createMutableConfig(
                ConfigProvider.getConfig(),
                MutableConfigProvider.getApplyAllChangePolicy());
        assertNotNull(cfg1);
        assertNotNull(cfg1.getConfigChangeRequest());
        MutableConfig cfg2 = MutableConfigProvider.createMutableConfig(
                ConfigProvider.getConfig());
        assertNotNull(cfg2);
        assertNotNull(cfg2.getConfigChangeRequest());
        assertTrue(cfg1!=cfg2);
        assertTrue(cfg1.getConfigChangeRequest()!=cfg2.getConfigChangeRequest());
    }

    /**
     * Test null create change request.
     *
     * @throws Exception the exception
     */
    @Test(expected=NullPointerException.class)
    public void testNullCreateMutableConfiguration1() throws Exception {
        MutableConfigProvider.createMutableConfig(
                (javax.config.Config) null);
    }

    /**
     * Test null create change request.
     *
     * @throws Exception the exception
     */
    @Test(expected=NullPointerException.class)
    public void testNullCreateMutableConfiguration2() throws Exception {
        MutableConfigProvider.createMutableConfig(
                (ChangePropagationPolicy) null);
    }

    /**
     * Test read write properties with rollback.
     *
     * @throws IOException the io exception
     */
    @Test
    public void testReadWriteProperties_WithCancel() throws IOException {
        WritablePropertiesSource.target.delete();
        MutableConfig mutConfig = MutableConfigProvider.createMutableConfig(
                ConfigProvider.getConfig()
        );
        mutConfig.put("key1", "value1");
        Map<String,String> cm = new HashMap<>();
        cm.put("key2", "value2");
        cm.put("key3", "value3");
    }

    /**
     * Test read write properties with commit.
     *
     * @throws IOException the io exception
     */
    @Test
    public void testReadWriteProperties_WithCommit() throws IOException {
        WritablePropertiesSource.target.delete();
        MutableConfig mutConfig = MutableConfigProvider.createMutableConfig(
                ConfigProvider.getConfig()
        );
        mutConfig.put("key1", "value1");
        Map<String,String> cm = new HashMap<>();
        cm.put("key2", "value2");
        cm.put("key3", "value3");
        mutConfig.putAll(cm);
        mutConfig.store();
        assertTrue(WritablePropertiesSource.target.exists());
        MutableConfig mmutConfig2 = MutableConfigProvider.createMutableConfig(
                ConfigProvider.getConfig()
        );
        mmutConfig2.remove("foo");
        mmutConfig2.remove("key3");
        mmutConfig2.put("key1", "value1.2");
        mmutConfig2.put("key4", "value4");
        mmutConfig2.store();
        Properties props = new Properties();
        props.load(WritablePropertiesSource.target.toURL().openStream());
        assertEquals(3, props.size());
        assertEquals("value1.2", props.getProperty("key1"));
        assertEquals("value2", props.getProperty("key2"));
        assertEquals("value4", props.getProperty("key4"));
    }

    /**
     * Test read write xml properties with commit.
     *
     * @throws IOException the io exception
     */
    @Test
    public void testReadWriteXmlProperties_WithCommit() throws IOException {
        WritableXmlPropertiesSource.target.delete();
        MutableConfig cfg = MutableConfigProvider.createMutableConfig(
                ConfigProvider.getConfig(), MutableConfigProvider.getApplyAllChangePolicy());
        cfg.put("key1", "value1");
        Map<String,String> cm = new HashMap<>();
        cm.put("key2", "value2");
        cm.put("key3", "value3");
        cfg.putAll(cm);
        cfg.store();
        assertTrue(WritableXmlPropertiesSource.target.exists());
        MutableConfig cfg2 = MutableConfigProvider.createMutableConfig(
                ConfigProvider.getConfig());
        assertTrue(cfg != cfg2);
        cfg2.remove("foo");
        cfg2.remove("key3");
        cfg2.put("key1", "value1.2");
        cfg2.put("key4", "value4");
        cfg2.store();
        Properties props = new Properties();
        props.loadFromXML( WritableXmlPropertiesSource.target.toURL().openStream());
        assertEquals(3, props.size());
        assertEquals("value1", props.getProperty("key1"));
        assertEquals("value2", props.getProperty("key2"));
    }

    /**
     * Test read write xml properties with commit.
     *
     * @throws IOException the io exception
     */
    @Test
    public void testWriteWithNoChangePolicy() throws IOException {
        WritableXmlPropertiesSource.target.delete();
        MutableConfig cfg = MutableConfigProvider.createMutableConfig(
                ConfigProvider.getConfig(),
                MutableConfigProvider.getApplyNonePolicy());
        cfg.put("key1", "value1");
        Map<String,String> cm = new HashMap<>();
        cm.put("key2", "value2");
        cm.put("key3", "value3");
        cfg.putAll(cm);
        cfg.store();
        assertFalse(WritableXmlPropertiesSource.target.exists());
    }


}