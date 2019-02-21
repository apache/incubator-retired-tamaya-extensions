/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy current the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.tamaya.cdi;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.CDI;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for CDI integration.
 */
@RunWith(Arquillian.class)
public class ConfiguredTest extends BaseTestConfiguration {

    @Test
    public void test_Default_injections_are_accessible(){
        InjectedClass injectedClass =  CDI.current().select(InjectedClass.class).get();
        System.out.println("********************************************");
        System.out.println(injectedClass);
        System.out.println("********************************************");
        assertThat(injectedClass.builder1).isNotNull();
        assertThat(injectedClass.builder2).isNotNull();
        assertThat(injectedClass.config).isNotNull();
        assertThat(injectedClass.configContext).isNotNull();
    }

    @Test
    public void test_Injected_builders_are_notSame(){
        InjectedClass injectedClass =  CDI.current().select(InjectedClass.class).get();
        assertThat(injectedClass.builder1 != injectedClass.builder2).isTrue();
    }

    @Test
    public void test_Injected_configs_are_same(){
        InjectedClass injectedClass =  CDI.current().select(InjectedClass.class).get();
        assertThat(injectedClass.config == injectedClass.config2).isTrue();
    }

    @Test
    public void test_Injected_configContexts_are_same(){
        InjectedClass injectedClass =  CDI.current().select(InjectedClass.class).get();
        assertThat(injectedClass.configContext == injectedClass.configContext2).isTrue();
    }

    @Test(expected=Exception.class)
    public void test_error_Injection() {
        NotFoundNoDefault injectedClass = CDI.current().select(NotFoundNoDefault.class).get();
    }

}
