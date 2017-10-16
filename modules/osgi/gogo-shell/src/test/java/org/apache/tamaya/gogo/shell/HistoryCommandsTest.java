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
package org.apache.tamaya.gogo.shell;

import org.apache.tamaya.osgi.ConfigHistory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

/**
 * Created by atsti on 30.09.2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class HistoryCommandsTest extends AbstractOSGITest{

    private HistoryCommands commands;

    @Before
    public void setupCommands(){
        commands = new HistoryCommands(bundleContext);
    }


    @Test
    public void testDeleteHistory() throws Exception {
        commands.tm_history_delete("testDeleteHistory");
        verify(tamayaConfigPlugin).clearHistory("testDeleteHistory");
    }

    @Test
    public void testDeleteHistory_All() throws Exception {
        commands.tm_history_delete_all();
        verify(tamayaConfigPlugin).clearHistory();
    }

    @Test
    public void testHistory_Get() throws Exception {
        commands.tm_history_get("testHistory_Get", "");
        verify(tamayaConfigPlugin).getHistory("testHistory_Get");
        reset(tamayaConfigPlugin);
        commands.tm_history_get("testHistory_Get", "BEGIN,END");
        verify(tamayaConfigPlugin).getHistory("testHistory_Get");
    }

    @Test
    public void testHistoryMaxSize() throws Exception {
        commands.tm_history_maxsize();
        verify(tamayaConfigPlugin).getMaxHistorySize();
    }

    @Test
    public void testHistoryMaxSizeSet() throws Exception {
        commands.tm_history_maxsize_set(30);
        verify(tamayaConfigPlugin).setMaxHistorySize(30);
    }

}