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

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Created by atsticks on 26.09.17.
 */
public class ConfigHistoryTest {
    @Test
    public void configuring() throws Exception {
        ConfigHistory en = ConfigHistory.configuring("configuring", "configuring_test");
        assertThat(en).isNotNull();
        assertThat("configuring").isEqualTo(en.getPid());
        assertThat(ConfigHistory.TaskType.BEGIN).isEqualTo(en.getType());
        assertThat("configuring_test").isEqualTo(en.getValue());
    }

    @Test
    public void configured() throws Exception {
        ConfigHistory en = ConfigHistory.configured("configured", "configured_test");
        assertThat(en).isNotNull();
        assertThat("configured").isEqualTo(en.getPid());
        assertThat(ConfigHistory.TaskType.END).isEqualTo(en.getType());
        assertThat("configured_test").isEqualTo(en.getValue());
    }

    @Test
    public void propertySet() throws Exception {
        ConfigHistory en = ConfigHistory.propertySet("propertySet", "propertySet.key", "new", "prev");
        assertThat(en).isNotNull();
        assertThat("propertySet").isEqualTo(en.getPid());
        assertThat(ConfigHistory.TaskType.PROPERTY).isEqualTo(en.getType());
        assertThat("propertySet.key").isEqualTo(en.getKey());
        assertThat("prev").isEqualTo(en.getPreviousValue());
        assertThat("new").isEqualTo(en.getValue());
    }

    @Test
    public void setGetMaxHistory() throws Exception {
        ConfigHistory.setMaxHistory(1000);
        assertThat(1000).isEqualTo(ConfigHistory.getMaxHistory());
    }

    @Test
    public void history() throws Exception {
        for(int i=0;i<100;i++){
            ConfigHistory.propertySet("getHistory", "getHistory"+i, "prev"+i, "new"+i);
        }
        List<ConfigHistory> hist = ConfigHistory.getHistory();
        assertThat(hist).isNotNull();
        assertThat(hist.size() >= 100).isTrue();
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
        assertThat(hist).isNotNull();
        assertThat(102).isEqualTo(hist.size());
        hist = ConfigHistory.getHistory("history2");
        assertThat(hist).isNotNull();
        assertThat(100).isEqualTo(hist.size());
        hist = ConfigHistory.getHistory(null);
        assertThat(hist).isNotNull();
        assertThat(hist.size() > 202).isTrue();
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
        assertThat(hist).isNotNull();
        assertThat(hist).hasSize(100);
        assertThat(ConfigHistory.getHistory("history4")).hasSize(100);
        ConfigHistory.clearHistory("history3");
        assertThat(ConfigHistory.getHistory("history3")).hasSize(0);
        assertThat(ConfigHistory.getHistory("history4")).hasSize(100);
        ConfigHistory.clearHistory(null);
        assertThat(ConfigHistory.getHistory().isEmpty()).isTrue();
        assertThat(ConfigHistory.getHistory("history4").isEmpty()).isTrue();
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
        assertThat(ConfigHistory.getHistory("save")).hasSize(10);
        Dictionary<String,Object> config = new Hashtable<>();
        ConfigHistory.save(config);
        assertThat(ConfigHistory.getHistory("save")).hasSize(10);
        ConfigHistory.clearHistory();
        assertThat(ConfigHistory.getHistory("save").isEmpty()).isTrue();
        ConfigHistory.restore(config);
        assertThat(ConfigHistory.getHistory("save")).hasSize(10);
    }

}
