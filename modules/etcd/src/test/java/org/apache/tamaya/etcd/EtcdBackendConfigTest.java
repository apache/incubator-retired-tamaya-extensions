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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EtcdBackendConfigTest {

    @Test
    public void testEtcdDirectoryProperty() throws Exception {
        final String directory = "./target/etcd-data";
        try {
            System.setProperty("tamaya.etcd.directory", directory);
            assertThat(EtcdBackendConfig.getEtcdDirectory()).isEqualTo(directory);
        } finally {
            System.clearProperty("tamaya.etcd.directory");
        }
    }

    @Test
    public void testEtcdTimeoutProperty() throws Exception {
        try {
            assertThat(EtcdBackendConfig.getEtcdTimeout()).isEqualTo(2000L);
            System.setProperty("tamaya.etcd.timeout", "5");
            assertThat(EtcdBackendConfig.getEtcdTimeout()).isEqualTo(5000L);
        } finally {
            System.clearProperty("tamaya.etcd.timeout");
        }
    }

    @Test
    public void testEtcdServerProperty() throws Exception {
        final String server = System.getProperty("etcd.server.urls");
        if (server == null){
            return;
        }
        try {
            System.clearProperty("tamaya.etcd.server");
            assertThat(EtcdBackendConfig.getServers()).contains("http://127.0.0.1:2379");  //the default
            System.setProperty("tamaya.etcd.server", server);
            assertThat(EtcdBackendConfig.getServers()).contains(server);
        } finally {
            System.clearProperty("tamaya.etcd.server");
        }
    }
}
