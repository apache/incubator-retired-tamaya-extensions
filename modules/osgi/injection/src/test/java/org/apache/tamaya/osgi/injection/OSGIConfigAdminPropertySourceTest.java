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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.junit.Assert.*;

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
        assertEquals("tamaya", propertySource.getPid());
    }

    @Test
    public void getLocation() throws Exception {
        assertNull(propertySource.getLocation());
    }

    @Test
    public void getDefaultOrdinal() throws Exception {
        assertEquals(0, propertySource.getDefaultOrdinal());
    }

    @Test
    public void getOrdinal() throws Exception {
        assertEquals(0, propertySource.getOrdinal());
    }

    @Test
    public void getValue() throws Exception {
        String val = propertySource.getValue("java.home");
        assertNotNull(val);
        assertEquals(val, System.getProperty("java.home"));
        val = propertySource.getValue("foo.bar");
        assertNull(val);
    }

    @Test
    public void getProperties() throws Exception {
        Map<String,String> props = propertySource.getProperties();
        assertNotNull(props);
        String val = props.get("java.home");
        assertEquals(val, System.getProperty("java.home"));
        val = props.get("foo.bar");
        assertNull(val);
    }

}