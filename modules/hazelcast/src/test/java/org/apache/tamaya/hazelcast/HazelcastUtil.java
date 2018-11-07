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
package org.apache.tamaya.hazelcast;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple test util o manage Hazelcast.
 */
public final class HazelcastUtil {

    private static HazelcastInstance hz;
    private static AtomicInteger accessCounter = new AtomicInteger();

    private HazelcastUtil(){}

    /**
     * Get a hazelcast instance.
     * @return a hazelcast instance, not null.
     */
    public static synchronized HazelcastInstance getHazelcastInstance(){
        accessCounter.incrementAndGet();
        if(hz==null){
            hz = Hazelcast.newHazelcastInstance();
        }
        return hz;
    }

    /**
     * Shut down hazelcast, if we are the last client alive.
     */
    public static synchronized void shutdown(){
        if(accessCounter.decrementAndGet()<0){
            if(hz!=null){
                hz.shutdown();
                hz = null;
            }
        }
    }
}
