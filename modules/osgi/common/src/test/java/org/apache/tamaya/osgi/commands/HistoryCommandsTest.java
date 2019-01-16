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
package org.apache.tamaya.osgi.commands;

import org.apache.tamaya.osgi.AbstractOSGITest;
import org.apache.tamaya.osgi.ConfigHistory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by atsti on 30.09.2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class HistoryCommandsTest extends AbstractOSGITest {

    @Test
    public void clearHistory() throws Exception {
        ConfigHistory.configured("clearHistory1", "test");
        ConfigHistory.configured("clearHistory2", "test");
        assertThat(ConfigHistory.getHistory("clearHistory1")).hasSize(1);
        assertThat(ConfigHistory.getHistory("clearHistory2")).hasSize(1);
        assertThat(ConfigHistory.getHistory("clearHistory3")).hasSize(0);
        String result = HistoryCommands.clearHistory(tamayaConfigPlugin, "clearHistory1");
        assertThat(result.contains("PID")).isTrue();
        assertThat(result.contains("clearHistory1")).isTrue();
        assertThat(ConfigHistory.getHistory("clearHistory1")).hasSize(0);
        assertThat(ConfigHistory.getHistory("clearHistory2")).hasSize(1);
        assertThat(ConfigHistory.getHistory("clearHistory3")).hasSize(0);
        ConfigHistory.configured("clearHistory1", "test");
        result = HistoryCommands.clearHistory(tamayaConfigPlugin, "*");
        assertThat(result.contains("PID")).isTrue();
        assertThat(result.contains("*")).isTrue();
        assertThat(ConfigHistory.getHistory("clearHistory1")).hasSize(0);
        assertThat(ConfigHistory.getHistory("clearHistory2")).hasSize(0);
        assertThat(ConfigHistory.getHistory("clearHistory3")).hasSize(0);

    }

    @Test
    public void getHistory() throws Exception {
        ConfigHistory.configured("getHistory", "test");
        ConfigHistory.configuring("getHistory", "test");
        ConfigHistory.propertySet("getHistory", "k1", "v1", null);
        ConfigHistory.propertySet("getHistory", "k2", null, "v2");
        String result = HistoryCommands.getHistory(tamayaConfigPlugin, "getHistory");
        assertThat(result).isNotNull();
        assertThat(result.contains("k1")).isTrue();
        assertThat(result.contains("v1")).isTrue();
        assertThat(result.contains("test")).isTrue();
        result = HistoryCommands.getHistory(tamayaConfigPlugin, "getHistory", ConfigHistory.TaskType.BEGIN.toString());
        assertThat(result).isNotNull();
        assertThat(result.contains("getHistory")).isTrue();
        assertThat(result.contains("test")).isTrue();
        assertThat(result.contains("k1")).isFalse();
        assertThat(result.contains("v2")).isFalse();
    }

    @Test
    public void getSetMaxHistorySize() throws Exception {
        String result = HistoryCommands.getMaxHistorySize(tamayaConfigPlugin);
        assertThat(result).isEqualTo(String.valueOf(tamayaConfigPlugin.getMaxHistorySize()));
        result = HistoryCommands.setMaxHistorySize(tamayaConfigPlugin, 111);
        assertThat(result).isEqualTo("tamaya-max-getHistory-getNumChilds=111");
        result = HistoryCommands.getMaxHistorySize(tamayaConfigPlugin);
        assertThat(result).isEqualTo("111");
    }

}
