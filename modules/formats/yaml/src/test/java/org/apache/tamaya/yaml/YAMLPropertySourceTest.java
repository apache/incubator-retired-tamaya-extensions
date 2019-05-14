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
package org.apache.tamaya.yaml;

import org.junit.Test;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

public class YAMLPropertySourceTest {

    @Test
    public void testYamlWithOrdinal() throws Exception {
        URL configURL = YAMLPropertySourceTest.class.getResource("/configs/valid/test-with-prio.yaml");

        assertThat(configURL).isNotNull();

        YAMLPropertySource source = new YAMLPropertySource(configURL, 4);
        assertThat(source.getOrdinal()).isEqualTo(16784);
    }

    @Test
    public void testYamlDefaultOrdinal() throws Exception {
        URL configURL = YAMLPropertySourceTest.class.getResource("/configs/valid/test.yaml");

        assertThat(configURL).isNotNull();

        YAMLPropertySource source = new YAMLPropertySource(configURL, 4);
        assertThat(source.getOrdinal()).isEqualTo(4);
    }

    @Test
    public void testYamlCreateWithURL() throws Exception {
        URL configURL = YAMLPropertySourceTest.class.getResource("/configs/valid/test.yaml");

        assertThat(configURL).isNotNull();

        YAMLPropertySource source = new YAMLPropertySource(configURL);
        assertThat(source.getOrdinal()).isEqualTo(0);
    }
}
