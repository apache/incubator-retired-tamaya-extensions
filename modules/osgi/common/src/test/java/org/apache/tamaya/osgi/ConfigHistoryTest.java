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
import java.util.List;

import static org.junit.Assert.*;


/**
 * Created by atsticks on 26.09.17.
 */
public class ConfigHistoryTest {
    @Test
    public void configuring() throws Exception {
        ConfigHistory en = ConfigHistory.configuring("configuring", "configuring_test");
        assertNotNull(en);
        assertEquals("configuring", en.getPid());
        assertEquals(ConfigHistory.TaskType.BEGIN, en.getType());
        assertEquals("configuring_test", en.getValue());
    }

    @Test
    public void configured() throws Exception {
        ConfigHistory en = ConfigHistory.configured("configured", "configured_test");
        assertNotNull(en);
        assertEquals("configured", en.getPid());
        assertEquals(ConfigHistory.TaskType.END, en.getType());
        assertEquals("configured_test", en.getValue());
    }

    @Test
    public void propertySet() throws Exception {
        ConfigHistory en = ConfigHistory.propertySet("propertySet", "propertySet.key", "new", "prev");
        assertNotNull(en);
        assertEquals("propertySet", en.getPid());
        assertEquals(ConfigHistory.TaskType.PROPERTY, en.getType());
        assertEquals("propertySet.key", en.getKey());
        assertEquals("prev", en.getPreviousValue());
        assertEquals("new", en.getValue());
    }

    @Test
    public void setGetMaxHistory() throws Exception {
        ConfigHistory.setMaxHistory(1000);
        assertEquals(1000, ConfigHistory.getMaxHistory());
    }

    @Test
    public void history() throws Exception {
        for(int i=0;i<100;i++){
            ConfigHistory.propertySet("getHistory", "getHistory"+i, "prev"+i, "new"+i);
        }
        List<ConfigHistory> hist = ConfigHistory.getHistory();
        assertNotNull(hist);
        assertTrue(hist.size()>=100);
    }

    @Test
    public void history_pid() throws Exception {
        ConfigHistory.configuring("history1", "history_pid");
        for(int i=0;i<100;i++){
            ConfigHistory.propertySet("history1", "getHistory"+i, "prev"+i, "new"+i);
        }
        ConfigHistory.configured("history1", "history_pid");
        for(int i=0;i<100;i++){
            ConfigHistory.propertySet("history2", "getHistory"+i, "prev"+i, "new"+i);
        }
        List<ConfigHistory> hist = ConfigHistory.getHistory("history1");
        assertNotNull(hist);
        assertEquals(102, hist.size());
        hist = ConfigHistory.getHistory("history2");
        assertNotNull(hist);
        assertEquals(100, hist.size());
        hist = ConfigHistory.getHistory(null);
        assertNotNull(hist);
        assertTrue(hist.size()>202);
    }

    @Test
    public void clearHistory() throws Exception {
        for(int i=0;i<100;i++){
            ConfigHistory.propertySet("history3", "getHistory"+i, "prev"+i, "new"+i);
        }
        for(int i=0;i<100;i++){
            ConfigHistory.propertySet("history4", "getHistory"+i, "prev"+i, "new"+i);
        }
        List<ConfigHistory> hist = ConfigHistory.getHistory("history3");
        assertNotNull(hist);
        assertEquals(100, hist.size());
        assertEquals(100, ConfigHistory.getHistory("history4").size());
        ConfigHistory.clearHistory("history3");
        assertEquals(0, ConfigHistory.getHistory("history3").size());
        assertEquals(100, ConfigHistory.getHistory("history4").size());
        ConfigHistory.clearHistory(null);
        assertTrue(ConfigHistory.getHistory().isEmpty());
        assertTrue(ConfigHistory.getHistory("history4").isEmpty());
    }


    @Test
    public void setPreviousValue() throws Exception {
    	// TODO
    }

    @Test
    public void getValue() throws Exception {
    	// TODO
    }

    @Test
    public void getKey() throws Exception {
    	// TODO
    }

    @Test
    public void saveRestore() throws Exception {
        for(int i=0;i<10;i++){
            ConfigHistory.propertySet("save", "getHistory"+i, "prev"+i, "new"+i);
        }
        assertEquals(10, ConfigHistory.getHistory("save").size());
        Dictionary<String,Object> config = new Hashtable<>();
        ConfigHistory.save(config);
        assertEquals(10, ConfigHistory.getHistory("save").size());
        ConfigHistory.clearHistory();
        assertTrue(ConfigHistory.getHistory("save").isEmpty());
        ConfigHistory.restore(config);
        assertEquals(10, ConfigHistory.getHistory("save").size());
    }

}