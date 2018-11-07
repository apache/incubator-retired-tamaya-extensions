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
package org.apache.tamaya.consul;

import com.google.common.net.HostAndPort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton that reads and stores the current consul setup, especially the possible host:ports to be used.
 */
final class ConsulBackendConfig {

    private static final Logger LOG = Logger.getLogger(ConsulBackendConfig.class.getName());
    private static final String TAMAYA_CONSUL_SERVER_URLS = "tamaya.consul.server.urls";
    private static final String TAMAYA_CONSUL_DIRECTORY = "tamaya.consul.directory";
    private static final String TAMAYA_CONSUL_PREFIX = "tamaya.consul.prefix";


    private ConsulBackendConfig(){}

    public static String getConsulDirectory(){
        String val = System.getProperty(TAMAYA_CONSUL_DIRECTORY);
        if(val == null){
            val = System.getenv(TAMAYA_CONSUL_DIRECTORY);
        }
        if(val!=null){
            return val;
        }
        return "";
    }

    public static List<String> getServers(){
        String serverURLs = System.getProperty(TAMAYA_CONSUL_SERVER_URLS);
        if(serverURLs==null){
            serverURLs = System.getenv(TAMAYA_CONSUL_SERVER_URLS);
        }
        if(serverURLs==null){
            serverURLs = "http://127.0.0.1:4001";
        }
        List<String> servers = new ArrayList<>();
        for(String url:serverURLs.split("\\,")) {
            try{
                servers.add(url.trim());
                LOG.info("Using etcd endoint: " + url);
            } catch(Exception e){
                LOG.log(Level.SEVERE, "Error initializing etcd accessor for URL: " + url, e);
            }
        }
        return servers;
    }

    public static String getPrefix() {
        String val = System.getProperty(TAMAYA_CONSUL_PREFIX);
        if(val == null){
            val = System.getenv(TAMAYA_CONSUL_PREFIX);
        }
        if(val!=null){
            return val;
        }
        return "";
    }

}
