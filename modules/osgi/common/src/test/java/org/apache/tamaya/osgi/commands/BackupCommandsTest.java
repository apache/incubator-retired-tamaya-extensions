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
package org.apache.tamaya.osgi.commands;

import org.apache.tamaya.osgi.AbstractOSGITest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Hashtable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by atsti on 30.09.2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class BackupCommandsTest extends AbstractOSGITest {
    @Test
    public void createBackup() throws Exception {
        String result = BackupCommands.createBackup(tamayaConfigPlugin, cm, "createBackup", false);
        assertThat(result).isNotNull();
        assertThat(result.contains("createBackup")).isTrue();
        assertThat(result.contains("Backup created")).isTrue();
        assertThat(tamayaConfigPlugin.containsBackup("createBackup")).isTrue();
        // A backup with the given name already exists, so it fails
        result = BackupCommands.createBackup(tamayaConfigPlugin, cm, "createBackup", false);
        assertThat(result).isNotNull();
        assertThat(result.contains("createBackup")).isTrue();
        assertThat(result.contains("Creating of backup failed")).isTrue();
        assertThat(result.contains("already existing")).isTrue();
        assertThat(tamayaConfigPlugin.containsBackup("createBackup")).isTrue();
        // any existing backups gets overridden
        result = BackupCommands.createBackup(tamayaConfigPlugin, cm, "createBackup", true);
        assertThat(result).isNotNull();
        assertThat(result.contains("createBackup")).isTrue();
        assertThat(result.contains("Backup created")).isTrue();
        assertThat(tamayaConfigPlugin.containsBackup("createBackup")).isTrue();
    }

    @Test
    public void deleteBackup() throws Exception {
        BackupCommands.createBackup(tamayaConfigPlugin, cm, "deleteBackup", false);
        assertThat(tamayaConfigPlugin.containsBackup("deleteBackup")).isTrue();
        String result = BackupCommands.deleteBackup(tamayaConfigPlugin, "deleteBackup");
        assertThat(result).isNotNull();
        assertThat(result.contains("deleteBackup")).isTrue();
        assertThat(result.contains("Backup deleted")).isTrue();
        assertThat(tamayaConfigPlugin.containsBackup("deleteBackup")).isFalse();
    }

    @Test
    public void restoreBackup() throws Exception {
        BackupCommands.createBackup(tamayaConfigPlugin, cm, "restoreBackup", false);
        assertThat(tamayaConfigPlugin.containsBackup("restoreBackup")).isTrue();
        String result = BackupCommands.restoreBackup(tamayaConfigPlugin, "restoreBackup");
        assertThat(result).isNotNull();
        assertThat(result.contains("restoreBackup")).isTrue();
        assertThat(result.contains("Backup restored")).isTrue();
        BackupCommands.deleteBackup(tamayaConfigPlugin, "restoreBackup");
        assertThat(tamayaConfigPlugin.containsBackup("restoreBackup")).isFalse();
        result = BackupCommands.restoreBackup(tamayaConfigPlugin, "restoreBackup");
        assertThat(result.contains("Backup restore failed")).isTrue();
        assertThat(result.contains("no backup found")).isTrue();
    }

    @Test
    public void listBackup() throws Exception {
        BackupCommands.createBackup(tamayaConfigPlugin, cm, "listBackup", false);
        String result = BackupCommands.listBackup(tamayaConfigPlugin, "listBackup");
        result.concat("listBackup");
        result.contains("pid");
    }

    @Test
    public void printProps() throws Exception {
        Hashtable<String,Object> props = new Hashtable<>();
        props.put("k1", "v1");
        props.put("k2", "v2");
        String result = BackupCommands.printProps(props);
        assertThat(result.contains("k1")).isTrue();
        assertThat(result.contains("k2")).isTrue();
        assertThat(result.contains("v1")).isTrue();
        assertThat(result.contains("v2")).isTrue();
    }

}
