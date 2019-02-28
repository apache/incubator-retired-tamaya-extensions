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
package org.apache.tamaya.etcd;

import org.junit.BeforeClass;
import org.junit.Test;

import java.net.MalformedURLException;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the etcd backend integration. You must have setCurrent a system property so, theses tests are executed, e.g.
 * {@code -Detcd.url=http://127.0.0.1:4001}.
 */
public class EtcdAccessorTest {

    private static EtcdAccessor accessor;
    static boolean execute = false;

    @BeforeClass
    public static void setup() throws MalformedURLException {
        accessor = new EtcdAccessor("http://192.168.99.105:4001");
        if (!accessor.getVersion().contains("etcd")) {
            System.out.println("Disabling etcd tests, etcd not accessible at: " + System.getProperty("etcd.server.urls"));
            System.out.println("Configure etcd with -Detcd.server.urls=http://<IP>:<PORT>");
        } else {
            execute = true;
        }
    }

    @Test
    public void testGetVersion() throws Exception {
        if (!execute) {
            return;
        }
        assertThat(accessor.getVersion()).isEqualTo("etcd 0.4.9");
    }

    @Test
    public void testGet() throws Exception {
        if (!execute) {
            return;
        }
        Map<String,String> result = accessor.get("test1");
        assertThat(result).isNotNull();
    }

    @Test
    public void testSetNormal() throws Exception {
        if (!execute) {
            return;
        }
        String value = UUID.randomUUID().toString();
        Map<String,String> result = accessor.set("testSetNormal", value);
        assertThat(result.get("_testSetNormal.ttl")).isNull();
        assertThat(value).isEqualTo(accessor.get("testSetNormal").get("testSetNormal"));
    }

    @Test
    public void testSetNormal2() throws Exception {
        if (!execute) {
            return;
        }
        String value = UUID.randomUUID().toString();
        Map<String,String> result = accessor.set("testSetNormal2", value, null);
        assertThat(result.get("_testSetNormal2.ttl")).isNull();
        assertThat(value).isEqualTo(accessor.get("testSetNormal2").get("testSetNormal2"));
    }

    @Test
    public void testSetWithTTL() throws Exception {
        if (!execute) {
            return;
        }
        String value = UUID.randomUUID().toString();
        Map<String,String> result = accessor.set("testSetWithTTL", value, 1);
        assertThat(result.get("_testSetWithTTL.ttl")).isNotNull();
        assertThat(value).isEqualTo(accessor.get("testSetWithTTL").get("testSetWithTTL"));
        Thread.sleep(2000L);
        result = accessor.get("testSetWithTTL");
        assertThat(result.get("testSetWithTTL")).isNull();
    }

    @Test
    public void testDelete() throws Exception {
        if (!execute) {
            return;
        }
        String value = UUID.randomUUID().toString();
        Map<String,String> result = accessor.set("testDelete", value, null);
        assertThat(value).isEqualTo(accessor.get("testDelete").get("testDelete"));
        assertThat(result.get("_testDelete.createdIndex")).isNotNull();
        result = accessor.delete("testDelete");
        assertThat(value).isEqualTo(result.get("_testDelete.prevNode.createValue"));
        assertThat(accessor.get("testDelete").get("testDelete")).isNull();
    }

    @Test
    public void testGetProperties() throws Exception {
        if (!execute) {
            return;
        }
        String value = UUID.randomUUID().toString();
        accessor.set("testGetProperties1", value);
        Map<String,String> result = accessor.getProperties("");
        assertThat(result).isNotNull();
        assertThat(value).isEqualTo(result.get("testGetProperties1"));
        assertThat(result.get("_testGetProperties1.createdIndex")).isNotNull();
    }
}
