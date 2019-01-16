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
package org.apache.tamaya.osgi;

import org.junit.Test;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Created by atsticks on 26.09.17.
 */
public class BackupsTest {

    private Dictionary<String,Object> createConfig(String pid){
        Hashtable<String,Object> config = new Hashtable<>();
        config.put("test.id", pid);
        return config;
    }
    @Test
    public void setGet() throws Exception {
        Dictionary<String,Object> cfg = createConfig("setCurrent");
        Backups.set("setCurrent", cfg);
        assertThat(Backups.get("setCurrent")).isEqualTo(cfg);
    }

    @Test
    public void remove() throws Exception {
        Dictionary<String,Object> cfg = createConfig("remove");
        Backups.set("remove", cfg);
        assertThat(Backups.get("remove")).isEqualTo(cfg);
        Backups.remove("remove");
        assertThat(Backups.get("remove")).isNull();
    }

    @Test
    public void removeAll() throws Exception {
        Dictionary<String,Object> cfg = createConfig("remove");
        Backups.set("remove", cfg);
        assertThat(Backups.get("remove")).isEqualTo(cfg);
        Backups.removeAll();
        assertThat(Backups.get("remove")).isNull();
    }

    @Test
    public void get1() throws Exception {
    }

    @Test
    public void getPids() throws Exception {
        Dictionary<String,Object> cfg = createConfig("getPids");
        Backups.set("getPids", cfg);
        Set<String> pids = Backups.getPids();
        assertThat(pids).isNotNull();
        assertThat(pids.contains("getPids")).isTrue();
        Backups.removeAll();
        pids = Backups.getPids();
        assertThat(pids).isNotNull();
        assertThat(pids.isEmpty()).isTrue();
    }

    @Test
    public void contains() throws Exception {
        Dictionary<String,Object> cfg = createConfig("contains");
        Backups.set("contains", cfg);
        assertThat(Backups.contains("contains")).isTrue();
        assertThat(Backups.contains("foo")).isFalse();
        Backups.removeAll();
        assertThat(Backups.contains("contains")).isFalse();
        assertThat(Backups.contains("foo")).isFalse();
    }

    @Test
    public void saveRestore() throws Exception {
        Dictionary<String,Object> store = new Hashtable<>();
        Dictionary<String,Object> cfg = createConfig("contains");
        Backups.set("saveRestore", cfg);
        Backups.save(store);
        Backups.removeAll();
        assertThat(Backups.contains("saveRestore")).isFalse();
        Backups.restore(store);
        assertThat(Backups.contains("saveRestore")).isTrue();
        assertThat(Backups.get("saveRestore")).isEqualTo(cfg);
    }

}
