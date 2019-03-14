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
package org.apache.tamaya.integration.spring;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by Anatole on 25.09.2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(name="tamaya-last", classes = {ConfiguredSpringBean.class, TamayaConfigExtendingTest.class})
@TestPropertySource(locations = "classpath:test.properties")
@EnableTamayaConfig(extendOnly = true)
public class TamayaConfigExtendingTest {

    @Autowired
    private ConfiguredSpringBean configuredBean;

    @Test
    public void assertBeanNotNull(){
        assertThat(configuredBean).isNotNull();
    }

    @Test
    public void assert_springApplication_Injected(){
        assertThat(configuredBean.getSpringApplication()).isEqualTo("test-app-name");
    }

    @Test
    public void assert_springApplication_Env(){
        assertThat(configuredBean.getEnv().getProperty("spring.application")).isEqualTo("test-app-name");
    }

}
