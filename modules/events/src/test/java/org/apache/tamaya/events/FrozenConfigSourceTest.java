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
package org.apache.tamaya.events;

import org.apache.tamaya.base.configsource.ConfigSourceComparator;
import org.apache.tamaya.base.configsource.SystemConfigSource;
import org.junit.Test;

import javax.config.spi.ConfigSource;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests for {@link FrozenConfigSource}.
 */
public class FrozenConfigSourceTest {

    private static final ConfigSource myPS = new SystemConfigSource();

    @Test
    public void testOf() throws Exception {
        ConfigSource ps = FrozenConfigSource.of(myPS);
        assertNotNull(ps);
    }

    @Test
    public void testGetName() throws Exception {
        ConfigSource ps = FrozenConfigSource.of(myPS);
        String name = ps.getName();
        assertNotNull(name);
        assertEquals(name, ps.getName());
    }

    @Test
    public void testGetOrdinal() throws Exception {
        ConfigSource ps = FrozenConfigSource.of(myPS);
        assertEquals(ConfigSourceComparator.getOrdinal(myPS),
                ConfigSourceComparator.getOrdinal(ps));
    }

    @Test
    public void testGet() throws Exception {
        ConfigSource ps = FrozenConfigSource.of(myPS);
        assertNotNull(ps);
        for (Map.Entry<String, String> e : myPS.getProperties().entrySet()) {
            assertEquals(ps.getValue(e.getKey()), e.getValue());
        }
    }

    @Test
    public void testGetProperties() throws Exception {
        ConfigSource ps = FrozenConfigSource.of(myPS);
        assertNotNull(ps);
        assertNotNull(ps.getProperties());
        assertFalse(ps.getProperties().isEmpty());
    }

    @Test
    public void testEquals() throws Exception {
        ConfigSource ps1 = FrozenConfigSource.of(myPS);
        ConfigSource ps2 = FrozenConfigSource.of(myPS);
        assertEquals(ps1.getName(), ps2.getName());
        assertEquals(ps1.getProperties().size(), ps2.getProperties().size());
    }

    @Test
    public void testHashCode() throws Exception {
        boolean alwaysDifferent = true;
        for(int i=0;i<10;i++){
            ConfigSource ps1 = FrozenConfigSource.of(myPS);
            ConfigSource ps2 = FrozenConfigSource.of(myPS);
            // sometimes not same, because frozenAt in ms maybe different
            if(ps1.hashCode()==ps2.hashCode()){
                alwaysDifferent=false;
                break;
            }
        }
        if(alwaysDifferent){
            fail("HashCode should be same if frozenAt is in the same ms...");
        }
    }

    @Test
    public void testToString() throws Exception {
        ConfigSource ps = FrozenConfigSource.of(myPS);
        String toString = ps.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("FrozenPropertySource"));
        assertTrue(toString.contains(myPS.getName()));
    }
}