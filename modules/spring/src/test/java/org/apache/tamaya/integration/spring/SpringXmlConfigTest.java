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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by Anatole on 25.09.2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring-config.xml")
public class SpringXmlConfigTest {

    @Autowired
    private ConfiguredSpringBean configuredBean;

    @Test
    public void assertBeanNotNull(){
        assertThat(configuredBean).isNotNull();
    }

    @Test
    public void assert_JavaVersion_Injected(){
        assertThat(configuredBean.getJavaVersion()).isNotNull();
        assertThat(System.getProperty("java.version")).isEqualTo(configuredBean.getJavaVersion());
    }

    @Test
    public void assert_Number_Injected(){
        assertThat(configuredBean.getTestNumber()).isEqualTo(23);
    }

    @Test
    public void assert_SpringInjection(){
        assertThat(configuredBean.getSpringInjected()).isEqualTo("value11");
    }

    @Test
    public void assert_Number_From_Environment(){
        assertThat("value11").isEqualTo(configuredBean.getEnv().getProperty("myConfiguredValue"));
    }

}
