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
package org.apache.tamaya.mutableconfig.propertysources;

import org.apache.tamaya.functions.Supplier;
import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spisupport.SimplePropertySource;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link LazyRefreshablePropertySource}. Created by atsticks on 11.11.16.
 */
public class LazyRefreshablePropertySourceTest {

    private SimplePropertySource simplePropertySource = new SimplePropertySource(
        getClass().getClassLoader().getResource("test.properties")
    );
    private SimplePropertySource simplePropertySource2 = new SimplePropertySource(
            getClass().getClassLoader().getResource("test2.properties")
    );
    private volatile boolean selectFirst;

    @Test
    public void of() throws Exception {
        assertNotNull(LazyRefreshablePropertySource.of(
                new Supplier<PropertySource>() {
                    @Override
                    public PropertySource get() {
                        return simplePropertySource;
                    }
                }
        ));
    }

    @Test
    public void of_WithDefaultOrdinal() throws Exception {
        assertNotNull(LazyRefreshablePropertySource.of(
                new Supplier<PropertySource>() {
                    @Override
                    public PropertySource get() {
                        return simplePropertySource;
                    }
                }, 100
        ));
    }

    @Test
    public void get() throws Exception {
        LazyRefreshablePropertySource ps = LazyRefreshablePropertySource.of(
                new Supplier<PropertySource>() {
                    @Override
                    public PropertySource get() {
                        return simplePropertySource;
                    }
                }
        );
        assertEquals(ps.get("test1").getValue(), "test1");
        assertEquals(ps.get("test2").getValue(), "test2");
        assertNull(ps.get("test3"));
    }

    @Test
    public void getName() throws Exception {
        LazyRefreshablePropertySource ps = LazyRefreshablePropertySource.of(
                new Supplier<PropertySource>() {
                    @Override
                    public PropertySource get() {
                        return simplePropertySource;
                    }
                }
        );
        assertEquals(ps.getName(), simplePropertySource.getName());
    }

    @Test
    public void getProperties() throws Exception {
        LazyRefreshablePropertySource ps = LazyRefreshablePropertySource.of(
                new Supplier<PropertySource>() {
                    @Override
                    public PropertySource get() {
                        return simplePropertySource;
                    }
                }
        );
        assertEquals(ps.getProperties(), simplePropertySource.getProperties());
    }

    @Test
    public void refresh() throws Exception {
        LazyRefreshablePropertySource ps1 = LazyRefreshablePropertySource.of(
                new Supplier<PropertySource>() {
                    @Override
                    public PropertySource get() {
                        try {
                            if (selectFirst) {
                                return simplePropertySource;
                            } else {
                                return simplePropertySource2;
                            }
                        }finally{
                            selectFirst = !selectFirst;
                        }
                    }
                }
        );
        // Simulate a refresh with the switching provider created above...
        ps1.setUpdateInterval(1L);
        if(ps1.get("test3")!=null){
            Thread.sleep(5L); //  NOSONAR
            assertEquals("test4", ps1.get("test4").getValue());
        }else{
            Thread.sleep(5L); //  NOSONAR
            assertNull("test3", ps1.get("test3"));
        }
    }

    @Test
    public void testEqualsAndHashCode() throws Exception {
        LazyRefreshablePropertySource ps1 = LazyRefreshablePropertySource.of(
                new Supplier<PropertySource>() {
                    @Override
                    public PropertySource get() {
                        return simplePropertySource;
                    }
                }
        );
        LazyRefreshablePropertySource ps2 = LazyRefreshablePropertySource.of(
                new Supplier<PropertySource>() {
                    @Override
                    public PropertySource get() {
                        return simplePropertySource;
                    }
                }
        );
        assertEquals(ps1, ps2);
        assertEquals(ps1.hashCode(), ps2.hashCode());
    }

    @Test
    public void testToString() throws Exception {

    }

}