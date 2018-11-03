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

package org.apache.tamaya.mutableconfig;

import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by atsticks on 26.08.16.
 */
public class MutableConfigurationProviderTest {
    @Test
    public void createMutableConfiguration() throws Exception {
        assertNotNull(MutableConfigurationProvider.getInstance().createMutableConfiguration());
    }

    @Test
    public void createMutableConfiguration1() throws Exception {
        MutableConfiguration cfg = MutableConfigurationProvider.getInstance()
                .createMutableConfiguration(Configuration.current());
        assertNotNull(cfg);
        assertEquals(cfg.getChangePropagationPolicy(),
                MutableConfigurationProvider.getApplyMostSignificantOnlyChangePolicy());
    }

    @Test
    public void createMutableConfiguration2() throws Exception {
        ChangePropagationPolicy policy = MutableConfigurationProvider.getApplySelectiveChangePolicy("blabla");
        MutableConfiguration cfg = MutableConfigurationProvider.getInstance()
                .createMutableConfiguration(Configuration.current(),
                        policy);
        assertNotNull(cfg);
        assertEquals(cfg.getChangePropagationPolicy(), policy);
    }

    @Test
    public void createMutableConfiguration3() throws Exception {
        ChangePropagationPolicy policy = MutableConfigurationProvider.getApplySelectiveChangePolicy("gugus");
        MutableConfiguration cfg = MutableConfigurationProvider.getInstance()
                .createMutableConfiguration(policy);
        assertNotNull(cfg);
        assertEquals(cfg.getChangePropagationPolicy(), policy);
    }

    @Test
    public void getApplyAllChangePolicy() throws Exception {
        assertNotNull(MutableConfigurationProvider.getApplyAllChangePolicy());
    }

    @Test
    public void getApplyMostSignificantOnlyChangePolicy() throws Exception {
        assertNotNull(MutableConfigurationProvider.getApplyMostSignificantOnlyChangePolicy());
    }

    @Test
    public void getApplySelectiveChangePolicy() throws Exception {
        assertNotNull(MutableConfigurationProvider.getApplySelectiveChangePolicy());
    }

    @Test
    public void getApplyNonePolicy() throws Exception {
        assertNotNull(MutableConfigurationProvider.getApplyNonePolicy());
    }

}