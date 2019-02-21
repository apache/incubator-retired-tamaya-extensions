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
package org.apache.tamaya.features;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests if feature are seen. Created by atsticks on 19.03.17.
 */
public class FeaturesTest {
    @Test
    public void eventsAvailable() throws Exception {
        assertThat(Features.eventsAvailable()).isTrue();
    }

    @Test
    public void formatsAvailable() throws Exception {
        assertThat(Features.formatsAvailable()).isTrue();
    }

    @Test
    public void tamayaCoreAvailable() throws Exception {
        assertThat(Features.tamayaCoreAvailable()).isTrue();
    }

    @Test
    public void injectionCDIAvailable() throws Exception {
        assertThat(Features.injectionCDIAvailable()).isTrue();
    }

    @Test
    public void injectionStandaloneAvailable() throws Exception {
        assertThat(Features.injectionStandaloneAvailable()).isTrue();
    }

    @Test
    public void mutableConfigAvailable() throws Exception {
        assertThat(Features.mutableConfigAvailable()).isTrue();
    }

    @Test
    public void optionalAvailable() throws Exception {
        assertThat(Features.optionalAvailable()).isTrue();
    }

    @Test
    public void resolverAvailable() throws Exception {
        assertThat(Features.resolverAvailable()).isTrue();
    }

    @Test
    public void resourcesAvailable() throws Exception {
        assertThat(Features.resourcesAvailable()).isTrue();
    }

    @Test
    public void spiSupportAvailable() throws Exception {
        assertThat(Features.spiSupportAvailable()).isTrue();
    }

    @Test
    public void filterSupportAvailable() throws Exception {
        assertThat(Features.filterSupportAvailable()).isTrue();
    }

    @Test
    public void springAvailable() throws Exception {
        assertThat(Features.springAvailable()).isTrue();
    }

    @Test
    public void jndiAvailable() throws Exception {
        assertThat(Features.jndiAvailable()).isTrue();
    }

    @Test
    public void extSpringCoreAvailable() throws Exception {
        assertThat(Features.extSpringCoreAvailable()).isTrue();
    }

    @Test
    public void extOSGIAvailable() throws Exception {
        assertThat(Features.extOSGIAvailable()).isTrue();
    }

    @Test
    public void extJndiAvailable() throws Exception {
        assertThat(Features.extJndiAvailable()).isTrue();
    }

    @Test
    public void checkClassIsLoadable() throws Exception {
        assertThat(Features.checkClassIsLoadable("java.lang.String")).isTrue();
        assertThat(Features.checkClassIsLoadable("foo.Bar")).isFalse();
    }

}
