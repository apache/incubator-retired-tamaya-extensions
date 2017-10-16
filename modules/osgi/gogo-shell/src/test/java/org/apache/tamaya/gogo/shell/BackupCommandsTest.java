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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Created by atsti on 30.09.2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class BackupCommandsTest extends AbstractOSGITest{

    private BackupCommands commands;

    @Before
    public void setupCommands(){
        commands = new BackupCommands(bundleContext);
    }


    @Test
    public void testBackup_Create() throws Exception {
        commands.tm_backup_create("testBackup_Create1",false);
        verify(tamayaConfigPlugin).createBackup("testBackup_Create1");
        commands.tm_backup_create("testBackup_Create2",true);
        verify(tamayaConfigPlugin).createBackup("testBackup_Create2");
    }

    @Test
    public void testBackup_Delete() throws Exception {
        commands.tm_backup_delete("testBackup_Delete");
        verify(tamayaConfigPlugin).deleteBackup("testBackup_Delete");
    }

    @Test
    public void testBackup_Get() throws Exception {
        commands.tm_backup_get("testBackup_Get");
        verify(tamayaConfigPlugin).getBackup("testBackup_Get");
    }

    @Test
    public void testBackup_Restore() throws Exception {
        commands.tm_backup_restore("testBackup_Restore");
        verify(tamayaConfigPlugin).restoreBackup("testBackup_Restore");
    }

}