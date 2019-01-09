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
package org.apache.tamaya.osgi.injection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by atsti on 03.10.2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class OSGIConfigurationInjectorTest extends AbstractOSGITest{

    @Test
    public void getPid() throws Exception {
        OSGIConfigurationInjector injector = new OSGIConfigurationInjector(cm, "getPid");
        assertThat("getPid").isEqualTo(injector.getPid());
    }

    @Test
    public void getLocation() throws Exception {
        OSGIConfigurationInjector injector = new OSGIConfigurationInjector(cm, "getLocation", "/test");
        assertThat("/test").isEqualTo(injector.getLocation());
    }

    @Test
    public void configure() throws Exception {
        OSGIConfigurationInjector injector = new OSGIConfigurationInjector(cm, "OSGIConfigurationInjectorTest");
        Example example = new Example();
        Example result = injector.configure(example);
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(example);
        Example.checkExampleConfig(example);
    }

    @Test
    public void getConfiguredSupplier() throws Exception {
        OSGIConfigurationInjector injector = new OSGIConfigurationInjector(cm, "OSGIConfigurationInjectorTest");
        Supplier<Example> supplier = injector.getConfiguredSupplier(Example::new);
        assertThat(supplier).isNotNull();
        Example example = supplier.get();
        Example.checkExampleConfig(example);
    }

    @Test
    public void createTemplate() throws Exception {
        OSGIConfigurationInjector injector = new OSGIConfigurationInjector(cm, "OSGIConfigurationInjectorTest");
        TemplateExample template = injector.createTemplate(TemplateExample.class);
        Example.checkExampleConfig(template);
    }


}
