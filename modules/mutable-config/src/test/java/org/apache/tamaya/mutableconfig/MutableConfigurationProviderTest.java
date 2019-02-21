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
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by atsticks on 26.08.16.
 */
public class MutableConfigurationProviderTest {
    @Test
    public void createMutableConfiguration() throws Exception {
        assertThat(MutableConfigurationProvider.getInstance().createMutableConfiguration()).isNotNull();
    }

    @Test
    public void createMutableConfiguration1() throws Exception {
        MutableConfiguration cfg = MutableConfigurationProvider.getInstance()
                .createMutableConfiguration(Configuration.current());
        assertThat(cfg).isNotNull();
        assertThat(cfg.getChangePropagationPolicy())
            .isEqualTo(ChangePropagationPolicy.MOST_SIGNIFICANT_ONLY_POLICY);
    }

    @Test
    public void createMutableConfiguration2() throws Exception {
        ChangePropagationPolicy policy = ChangePropagationPolicy.getApplySelectiveChangePolicy("blabla");
        MutableConfiguration cfg = MutableConfigurationProvider.getInstance()
                .createMutableConfiguration(Configuration.current(),
                        policy);
        assertThat(cfg).isNotNull();
        assertThat(cfg.getChangePropagationPolicy()).isEqualTo(policy);
    }

    @Test
    public void createMutableConfiguration3() throws Exception {
        ChangePropagationPolicy policy = ChangePropagationPolicy.getApplySelectiveChangePolicy("gugus");
        MutableConfiguration cfg = MutableConfigurationProvider.getInstance()
                .createMutableConfiguration(policy);
        assertThat(cfg).isNotNull();
        assertThat(cfg.getChangePropagationPolicy()).isEqualTo(policy);
    }

    @Test
    public void getApplyAllChangePolicy() throws Exception {
        assertThat(MutableConfigurationProvider.getApplyAllChangePolicy()).isNotNull();
    }

    @Test
    public void getApplyMostSignificantOnlyChangePolicy() throws Exception {
        assertThat(MutableConfigurationProvider.getApplyMostSignificantOnlyChangePolicy()).isNotNull();
    }

    @Test
    public void getApplySelectiveChangePolicy() throws Exception {
        assertThat(MutableConfigurationProvider.getApplySelectiveChangePolicy()).isNotNull();
    }

    @Test
    public void getApplyNonePolicy() throws Exception {
        assertThat(MutableConfigurationProvider.getApplyNonePolicy()).isNotNull();
    }

}
