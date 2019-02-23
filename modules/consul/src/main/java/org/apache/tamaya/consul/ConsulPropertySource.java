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

import java.util.*;
import java.util.logging.Logger;

/**
 * Propertysource that is reading configuration from a configured consul endpoint. Setting
 * {@code consul.prefix} as system property maps the consul based configuration
 * to this prefix namespace. Consul servers are configured as {@code consul.urls} system or environment property.
 */
public class ConsulPropertySource extends AbstractConsulPropertySource{
    private static final Logger LOG = Logger.getLogger(ConsulPropertySource.class.getName());


    public ConsulPropertySource(String prefix, List<String> backends){
        this();
        setPrefix(prefix);
        setServer(backends);
    }

    public ConsulPropertySource(List<String> backends){
        this();
        setServer(backends);
    }

    public ConsulPropertySource(){
        super();
        setDefaultOrdinal(1000);
        setPrefix(System.getProperty("tamaya.consul.prefix", ""));
    }

    public ConsulPropertySource(String... backends){
        this();
        setServer(Arrays.asList(backends));
    }


}
