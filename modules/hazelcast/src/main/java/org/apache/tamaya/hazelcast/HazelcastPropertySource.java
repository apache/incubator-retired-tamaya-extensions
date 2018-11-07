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
package org.apache.tamaya.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IList;
import com.hazelcast.core.IMap;
import org.apache.tamaya.mutableconfig.ConfigChangeRequest;
import org.apache.tamaya.mutableconfig.spi.MutablePropertySource;
import org.apache.tamaya.spi.PropertyValue;
import org.apache.tamaya.spisupport.propertysource.BasePropertySource;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Distributed Propertysource using a in-memory hazelcast cluster.
 * Created by atsticks on 03.11.16.
 *
 * Basically all kind of property entris can be stored. Additionally this property source allows
 * to pass additional getMeta-entries to control the TTL of the data in milliseconds. For illustration
 * the following map will store {@code my.entry} with a TLL of 20000 milliseconds (20 seconds) and
 * store {@code my.otherEntry} with infinite lifetime (as long as the cluster is alive):
 *
 * {@code
 *     my.entry=myvalue
 *     _my.entry.ttl=20000
 *     my.otherEntry=1234
 * }
 *
 * By default a new hazelcast instance is created, but it is also possible to reuse an existing
 * instance of pass a Hazelcast configuration instance.
 */
public class HazelcastPropertySource extends AbstractHazelcastPropertySource {

    /** The hazelcast API instance. */
    private HazelcastInstance hazelcastInstance;

    /**
     * Creates a new instance, hereby using {@code "Hazelcast"} as property source name and
     * a default hazelcast backend created by calling {@link Hazelcast#newHazelcastInstance()}.
     */
    public HazelcastPropertySource(){
        this.hazelcastInstance = Hazelcast.newHazelcastInstance();
    }

    /**
     * Creates a new instance, hereby using {@code "Hazelcast"} as property source name and the
     * given hazelcast instance.
     * @param hazelcastInstance the hazelcast instance, not null.
     */
    public HazelcastPropertySource(HazelcastInstance hazelcastInstance){
        this("Hazelcast", hazelcastInstance);
    }

    /**
     * Creates a new instance, hereby using the given property source name and
     * a default hazelcast backend created by calling {@link Hazelcast#newHazelcastInstance()}.
     * @param name the property source name, not null.
     */
    public HazelcastPropertySource(String name){
        super(name);
        this.hazelcastInstance = Hazelcast.newHazelcastInstance();
    }

    /**
     * Creates a new instance, hereby using the given property source name and
     * a creating a new hazelcast backend using the given Hazelcast {@link Config}.
     * @param config the hazelcast config, not null.
     * @param name the property source name, not null.
     */
    public HazelcastPropertySource(String name, Config config){
        super(name);
        this.hazelcastInstance = Hazelcast.newHazelcastInstance(config);
    }

    /**
     * Creates a new instance, hereby using the given property source name and the
     * hazelcast instance.
     * @param name the property source name, not null.
     * @param hazelcastInstance the hazelcast instance, not null.
     */
    public HazelcastPropertySource(String name, HazelcastInstance hazelcastInstance){
        super(name);
        this.hazelcastInstance = Objects.requireNonNull(hazelcastInstance);
    }

    /**
     * Get access to the hazelcast instance used.
     * @return the hazelcast instance, not null.
     */
    @Override
    public HazelcastInstance getHazelcastInstance() {
        return hazelcastInstance;
    }

}
