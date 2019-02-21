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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ChangePropagationPolicyTest {

    @Test
    public void getApplyAllChangePolicy() throws Exception {
        assertThat(ChangePropagationPolicy.ALL_POLICY).isNotNull();
    }

    @Test
    public void getApplyMostSignificantOnlyChangePolicy() throws Exception {
        assertThat(ChangePropagationPolicy.MOST_SIGNIFICANT_ONLY_POLICY).isNotNull();
    }

    @Test
    public void getApplySelectiveChangePolicy() throws Exception {
        assertThat(ChangePropagationPolicy.getApplySelectiveChangePolicy("bla")).isNotNull();
    }

    @Test
    public void getApplyNonePolicy() throws Exception {
        assertThat(ChangePropagationPolicy.NONE_POLICY).isNotNull();
    }
}
