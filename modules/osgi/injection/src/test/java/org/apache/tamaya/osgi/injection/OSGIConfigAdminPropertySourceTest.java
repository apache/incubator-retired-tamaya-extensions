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

import org.apache.tamaya.spi.PropertyValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by atsti on 03.10.2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class OSGIConfigAdminPropertySourceTest extends AbstractOSGITest{

    OSGIConfigAdminPropertySource propertySource;

    @Before
    public void init(){
        propertySource = new OSGIConfigAdminPropertySource(cm, "tamaya");
    }

    @Test
    public void getPID() throws Exception {
        assertThat("tamaya").isEqualTo(propertySource.getPid());
    }

    @Test
    public void getLocation() throws Exception {
        assertThat(propertySource.getLocation()).isNull();
    }

    @Test
    public void getDefaultOrdinal() throws Exception {
        assertThat(0).isEqualTo(propertySource.getDefaultOrdinal());
    }

    @Test
    public void getOrdinal() throws Exception {
        assertThat(0).isEqualTo(propertySource.getOrdinal());
    }

    @Test
    public void get() throws Exception {
        PropertyValue val = propertySource.get("java.home");
        assertThat(val).isNotNull();
        assertThat(val.getKey()).isEqualTo("java.home");
        assertThat(val.getValue()).isEqualTo(System.getProperty("java.home"));
        val = propertySource.get("foo.bar");
        assertThat(val).isNull();
    }

    @Test
    public void getProperties() throws Exception {
        Map<String,PropertyValue> props = propertySource.getProperties();
        assertThat(props).isNotNull();
        PropertyValue val = props.get("java.home");
        assertThat(val.getKey()).isEqualTo("java.home");
        assertThat(val.getValue()).isEqualTo(System.getProperty("java.home"));
        val = props.get("foo.bar");
        assertThat(val).isNull();
    }

}
