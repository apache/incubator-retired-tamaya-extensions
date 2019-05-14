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
package org.apache.tamaya.hocon;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PathBasedHOCONPropertySourceProvider}.
 */
public class PathBasedHOCONPropertySourceProviderTest {

    @Test
    public void getPropertySources() {
        PathBasedHOCONPropertySourceProvider provider = new PathBasedHOCONPropertySourceProvider(
                "configs/valid/*.conf"
        );
        assertThat(provider.getPropertySources()).isNotNull();
        assertThat(provider.getPropertySources()).hasSize(7);
    }

    @Test
    public void getPropertySources_one() {
        PathBasedHOCONPropertySourceProvider provider = new PathBasedHOCONPropertySourceProvider(
                "configs/valid/cyril*.conf"
        );
        assertThat(provider.getPropertySources()).isNotNull();
        assertThat(provider.getPropertySources()).hasSize(1);
    }

    @Test
    public void getPropertySources_two() {
        PathBasedHOCONPropertySourceProvider provider = new PathBasedHOCONPropertySourceProvider(
                "configs/valid/simple-*.conf"
        );
        assertThat(provider.getPropertySources()).isNotNull();
        assertThat(provider.getPropertySources()).hasSize(3);
    }

    @Test
    public void getPropertySources_none() {
        PathBasedHOCONPropertySourceProvider provider = new PathBasedHOCONPropertySourceProvider(
                "configs/valid/foo*.conf", "configs/valid/*.CONF"
        );
        assertThat(provider.getPropertySources()).isNotNull();
        assertThat(provider.getPropertySources()).hasSize(0);
    }
}
