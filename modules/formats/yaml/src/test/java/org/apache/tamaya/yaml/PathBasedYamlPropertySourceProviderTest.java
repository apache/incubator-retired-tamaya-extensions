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
package org.apache.tamaya.yaml;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PathBasedYamlPropertySourceProvider}.
 */
public class PathBasedYamlPropertySourceProviderTest {

    @Test
    public void getPropertySources() {
        PathBasedYamlPropertySourceProvider provider = new PathBasedYamlPropertySourceProvider(
                "configs/valid/*.yaml"
        );
        assertThat(provider.getPropertySources()).isNotNull();
        assertThat(provider.getPropertySources()).hasSize(4);
    }

    @Test
    public void getPropertySources_one() {
        PathBasedYamlPropertySourceProvider provider = new PathBasedYamlPropertySourceProvider(
                "configs/valid/conta*.yaml"
        );
        assertThat(provider.getPropertySources()).isNotNull();
        assertThat(provider.getPropertySources()).hasSize(1);
    }

    @Test
    public void getPropertySources_two() {
        PathBasedYamlPropertySourceProvider provider = new PathBasedYamlPropertySourceProvider(
                "configs/valid/test*.yaml"
        );
        assertThat(provider.getPropertySources()).isNotNull();
        assertThat(provider.getPropertySources()).hasSize(2);
    }

    @Test
    public void getPropertySources_none() {
        PathBasedYamlPropertySourceProvider provider = new PathBasedYamlPropertySourceProvider(
                "configs/valid/foo*.yaml", "configs/valid/*.yml"
        );
        assertThat(provider.getPropertySources()).isNotNull();
        assertThat(provider.getPropertySources()).hasSize(0);
    }
}
