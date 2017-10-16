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

import java.util.Hashtable;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
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
        String pid = UUID.randomUUID().toString();
        doReturn(true).when(tamayaConfigPlugin).createBackup(pid);
        String out = runTest(()-> {
            commands.tm_backup_create(pid,false);
            return null;
        });
        assertTrue(out.startsWith("Backup created, PID = " + pid));
        assertTrue(out.contains("Key"));
        assertTrue(out.contains("Value"));
        assertTrue(out.contains("-------------------------------------------------------------"));
        assertTrue(out.contains("java.home"));
        assertTrue(out.contains(System.getProperty("java.home")));
        verify(tamayaConfigPlugin).createBackup(pid);
        String pid2 = UUID.randomUUID().toString();
        doReturn(true).when(tamayaConfigPlugin).createBackup(pid2);
        doReturn(true).when(tamayaConfigPlugin).containsBackup(pid2);
        out = runTest(()-> {
            commands.tm_backup_create(pid2,true);
            return null;
        });
        assertTrue(out.startsWith("Backup created, PID = " + pid2));
        assertTrue(out.contains("java.home"));
        assertTrue(out.contains(System.getProperty("java.home")));
        verify(tamayaConfigPlugin).createBackup(pid2);
    }

    @Test
    public void testBackup_Delete() throws Exception {
        String out = runTest(()-> {
            commands.tm_backup_delete("testBackup_Delete");
            return null;
        });
        assertEquals("Backup deleted: testBackup_Delete".trim(), out.trim());
        verify(tamayaConfigPlugin).deleteBackup("testBackup_Delete");
    }

    @Test
    public void testBackup_Get() throws Exception {
        String out = runTest(()-> {
            commands.tm_backup_get("testBackup_Get");
            return null;
        });
        assertEquals("No backup found: testBackup_Get".trim(), out.trim());
        verify(tamayaConfigPlugin).getBackup("testBackup_Get");
        reset(tamayaConfigPlugin);
        doReturn(new Hashtable<>()).when(tamayaConfigPlugin).getBackup("testBackup_Get");
        out = runTest(()-> {
            commands.tm_backup_get("testBackup_Get");
            return null;
        });
        assertTrue(out.startsWith("PID: testBackup_Get\n"));
        verify(tamayaConfigPlugin).getBackup("testBackup_Get");
    }

    @Test
    public void testBackup_Restore() throws Exception {
        commands.tm_backup_restore("testBackup_Restore");
        verify(tamayaConfigPlugin).restoreBackup("testBackup_Restore");
    }

}