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

package org.apache.tamaya.hazelcast;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.apache.tamaya.spi.PropertyValue;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by atsticks on 03.11.16.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HazelcaszPropertySourceTest {

    private static HazelcastInstance hz = HazelcastUtil.getHazelcastInstance();
    private HazelcastPropertySource hps = new HazelcastPropertySource();

    @BeforeClass
    public static void start() {
        IMap<Object, Object> map = hz.getMap("config3");
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("tamaya.ordinal", "2000");
        map.flush();
        IMap<Object, Object> map2 = hz.getMap("config4");
        map2.put("key1", "val1");
        map2.put("key2", "val2");
        map2.flush();
    }

    @org.junit.Test
    public void t01_testGetProperties(){
        hps.setMapReference("config3");
        Map<String, PropertyValue> values = hps.getProperties();
        assertNotNull(values);
        assertEquals(3, values.size());
        assertNotNull(values.get("k1"));
        assertNotNull(values.get("k2"));
        assertEquals("v1", values.get("k1").getValue());
        assertEquals("v2", values.get("k2").getValue());
        assertEquals(hps.getOrdinal(), 2000);

        hps.setMapReference("config4");
        Map<String, PropertyValue> values2 = hps.getProperties();
        assertNotNull(values2);
        assertEquals(2, values2.size());
        assertNotNull(values2.get("key1"));
        assertNotNull(values2.get("key2"));
        assertEquals("val1", values2.get("key1").getValue());
        assertEquals("val2", values2.get("key2").getValue());
        assertEquals(hps.getOrdinal(), 0);

        hps.setMapReference("bar");
        hps.setDefaultOrdinal(1500);
        Map<String, PropertyValue> values3 = hps.getProperties();
        assertNotNull(values3);
        assertEquals(0, values3.size());
        assertEquals(hps.getOrdinal(), 1500);
    }

    @org.junit.Test
    public void t02_testGetOrdinal(){
        hps.setMapReference("config3");
        hps.setDefaultOrdinal(1500);
        assertEquals(1500, hps.getDefaultOrdinal());
        assertEquals(hps.getOrdinal(), 2000);

        hps.setMapReference("config4");
        hps.setDefaultOrdinal(0);
        assertEquals(0, hps.getDefaultOrdinal());
        assertEquals(hps.getOrdinal(), 0);

        hps.setMapReference("bar");
        hps.setDefaultOrdinal(1500);
        assertEquals(1500, hps.getDefaultOrdinal());
        assertEquals(hps.getOrdinal(), 1500);
    }

    @org.junit.Test
    public void t03_tesGet(){
        hps.setMapReference("config3");
        PropertyValue val1 = hps.get("k1");
        assertNotNull(val1);
        assertEquals("v1", val1.getValue());
        PropertyValue val2 = hps.get("k2");
        assertNotNull(val2);
        assertEquals("v2", val2.getValue());

        hps.setMapReference("config4");
        val1 = hps.get("key1");
        assertNotNull(val1);
        assertEquals("val1", val1.getValue());
        val2 = hps.get("key2");
        assertNotNull(val2);
        assertEquals("val2", val2.getValue());

        hps.setMapReference("bar");
        val1 = hps.get("key1");
        assertNull(val1);
        val1 = hps.get("k1");
        assertNull(val1);
    }

    @org.junit.Test
    public void t03_tesGetMapReference(){
        hps.setMapReference("config3");
        assertEquals("config3", hps.getMapReference());

        hps.setMapReference("config4");
        assertEquals("config4", hps.getMapReference());
    }

    @org.junit.Test
    public void t03_tesGetSetName(){
        hps.setMapReference("config3");
        assertEquals("config3", hps.getMapReference());
        assertEquals("Hazelcast", hps.getName());

        hps.setMapReference("config4");
        hps.setName("bar");
        assertEquals("config4", hps.getMapReference());
        assertEquals("bar", hps.getName());
    }

    @org.junit.Test
    public void t04_testCache() throws InterruptedException {
        hps.setCacheTimeout(50L);
        assertTrue(hps.getValidUntil()>= System.currentTimeMillis());
        assertEquals(50L, hps.getCachePeriod());
        hps.setMapReference("config3");
        hps.setDefaultOrdinal(200);
        Map<String, PropertyValue> values = hps.getProperties();
        assertNotNull(values);
        assertEquals(3, values.size());
        assertNotNull(values.get("k1"));
        assertNotNull(values.get("k2"));
        assertEquals("v1", values.get("k1").getValue());
        assertEquals("v2", values.get("k2").getValue());
        assertEquals(hps.getOrdinal(), 2000);

        IMap<Object, Object> map = hz.getMap("config3");
        map.put("k3", "v3");
        map.remove("tamaya.ordinal");
        map.flush();

        // Read from cache
        assertEquals(50L, hps.getCachePeriod());
        assertTrue(hps.getValidUntil()>= System.currentTimeMillis());
        values = hps.getProperties();
        assertNotNull(values);
        assertEquals(3, values.size());
        assertNotNull(values.get("k1"));
        assertNotNull(values.get("k2"));
        assertEquals("v1", values.get("k1").getValue());
        assertEquals("v2", values.get("k2").getValue());
        assertEquals(hps.getOrdinal(), 2000);

        // Let cache timeout
        Thread.sleep(300L);

        // Read updated values
        values = hps.getProperties();
        assertNotNull(values);
        assertEquals(3, values.size());
        assertNotNull(values.get("k1"));
        assertNotNull(values.get("k2"));
        assertNotNull(values.get("k3"));
        assertEquals("v1", values.get("k1").getValue());
        assertEquals("v2", values.get("k2").getValue());
        assertEquals("v3", values.get("k3").getValue());
        assertEquals(hps.getOrdinal(), 200);
    }

    @AfterClass
    public static void end(){
        HazelcastUtil.shutdown();
    }
}
