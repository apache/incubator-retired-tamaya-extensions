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

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.apache.tamaya.spi.PropertyValue;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by atsticks on 03.11.16.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AbstractHzPropertySourceTest {

    private static HazelcastInstance hz = HazelcastUtil.getHazelcastInstance();
    private AbstractHazelcastPropertySource hps = new AbstractHazelcastPropertySource() {
        @Override
        protected HazelcastInstance getHazelcastInstance() {
            return HazelcastUtil.getHazelcastInstance();
        }
    };

    @BeforeClass
    public static void start() {
        IMap<Object, Object> map = hz.getMap("config");
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("tamaya.ordinal", "2000");
        map.flush();
        IMap<Object, Object> map2 = hz.getMap("config2");
        map2.put("key1", "val1");
        map2.put("key2", "val2");
        map2.flush();
    }

    @Test
    public void t01_testGetProperties(){
        hps.setMapReference("config");
        Map<String, PropertyValue> values = hps.getProperties();
        assertThat(values).isNotNull().hasSize(3);
        assertThat(values.get("k1")).isNotNull();
        assertThat(values.get("k2")).isNotNull();
        assertThat("v1").isEqualTo(values.get("k1").getValue());
        assertThat("v2").isEqualTo(values.get("k2").getValue());
        assertThat(hps.getOrdinal()).isEqualTo(2000);

        hps.setMapReference("config2");
        Map<String, PropertyValue> values2 = hps.getProperties();
        assertThat(values2).isNotNull().hasSize(2);
        assertThat(values2.get("key1")).isNotNull();
        assertThat(values2.get("key2")).isNotNull();
        assertThat("val1").isEqualTo(values2.get("key1").getValue());
        assertThat("val2").isEqualTo(values2.get("key2").getValue());
        assertThat(hps.getOrdinal()).isEqualTo(0);

        hps.setMapReference("foo");
        hps.setDefaultOrdinal(1500);
        Map<String, PropertyValue> values3 = hps.getProperties();
        assertThat(values3).isNotNull();
        assertThat(values3).hasSize(0);
        assertThat(hps.getOrdinal()).isEqualTo(1500);
    }

    @Test
    public void t02_testGetOrdinal(){
        hps.setMapReference("config");
        hps.setDefaultOrdinal(1500);
        assertThat(1500).isEqualTo(hps.getDefaultOrdinal());
        assertThat(hps.getOrdinal()).isEqualTo(2000);

        hps.setMapReference("config2");
        hps.setDefaultOrdinal(0);
        assertThat(0).isEqualTo(hps.getDefaultOrdinal());
        assertThat(hps.getOrdinal()).isEqualTo(0);

        hps.setMapReference("foo");
        hps.setDefaultOrdinal(1500);
        assertThat(1500).isEqualTo(hps.getDefaultOrdinal());
        assertThat(hps.getOrdinal()).isEqualTo(1500);
    }

    @Test
    public void t03_tesGet(){
        hps.setMapReference("config");
        PropertyValue val1 = hps.get("k1");
        assertThat(val1).isNotNull();
        assertThat("v1").isEqualTo(val1.getValue());
        PropertyValue val2 = hps.get("k2");
        assertThat(val2).isNotNull();
        assertThat("v2").isEqualTo(val2.getValue());

        hps.setMapReference("config2");
        val1 = hps.get("key1");
        assertThat(val1).isNotNull();
        assertThat("val1").isEqualTo(val1.getValue());
        val2 = hps.get("key2");
        assertThat(val2).isNotNull();
        assertThat("val2").isEqualTo(val2.getValue());

        hps.setMapReference("foo");
        val1 = hps.get("key1");
        assertThat(val1).isNull();
        val1 = hps.get("k1");
        assertThat(val1).isNull();
    }

    @Test
    public void t03_tesGetMapReference(){
        hps.setMapReference("config");
        assertThat("config").isEqualTo(hps.getMapReference());

        hps.setMapReference("config2");
        assertThat("config2").isEqualTo(hps.getMapReference());
    }

    @Test
    public void t03_tesGetSetName(){
        hps.setMapReference("config");
        assertThat("config").isEqualTo(hps.getMapReference());
        assertThat("Hazelcast").isEqualTo(hps.getName());

        hps.setMapReference("config2");
        hps.setName("foo");
        assertThat("config2").isEqualTo(hps.getMapReference());
        assertThat("foo").isEqualTo(hps.getName());
    }

    @Test
    public void t04_testCache() throws InterruptedException {
        hps.setCacheTimeout(50L);
        assertThat(hps.getValidUntil() >= System.currentTimeMillis()).isTrue();
        assertThat(50L).isEqualTo(hps.getCachePeriod());
        hps.setMapReference("config");
        hps.setDefaultOrdinal(200);
        Map<String, PropertyValue> values = hps.getProperties();
        assertThat(values).isNotNull();
        assertThat(values).hasSize(3);
        assertThat(values.get("k1")).isNotNull();
        assertThat(values.get("k2")).isNotNull();
        assertThat("v1").isEqualTo(values.get("k1").getValue());
        assertThat("v2").isEqualTo(values.get("k2").getValue());
        assertThat(hps.getOrdinal()).isEqualTo(2000);

        IMap<Object, Object> map = hz.getMap("config");
        map.put("k3", "v3");
        map.remove("tamaya.ordinal");
        map.flush();

        // Read from cache
        assertThat(50L).isEqualTo(hps.getCachePeriod());
        assertThat(hps.getValidUntil() >= System.currentTimeMillis()).isTrue();
        values = hps.getProperties();
        assertThat(values).isNotNull();
        assertThat(values).hasSize(3);
        assertThat(values.get("k1")).isNotNull();
        assertThat(values.get("k2")).isNotNull();
        assertThat("v1").isEqualTo(values.get("k1").getValue());
        assertThat("v2").isEqualTo(values.get("k2").getValue());
        assertThat(hps.getOrdinal()).isEqualTo(2000);

        // Let cache timeout
        Thread.sleep(300L);

        // Read updated values
        values = hps.getProperties();
        assertThat(values).isNotNull();
        assertThat(values).hasSize(3);
        assertThat(values.get("k1")).isNotNull();
        assertThat(values.get("k2")).isNotNull();
        assertThat(values.get("k3")).isNotNull();
        assertThat("v1").isEqualTo(values.get("k1").getValue());
        assertThat("v2").isEqualTo(values.get("k2").getValue());
        assertThat("v3").isEqualTo(values.get("k3").getValue());
        assertThat(hps.getOrdinal()).isEqualTo(200);
    }

    @AfterClass
    public static void end(){
        HazelcastUtil.shutdown();
    }
}
