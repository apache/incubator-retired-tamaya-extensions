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

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IList;
import com.hazelcast.core.IMap;
import org.apache.tamaya.mutableconfig.ConfigChangeRequest;
import org.apache.tamaya.mutableconfig.spi.MutablePropertySource;
import org.apache.tamaya.spi.PropertyValue;
import org.apache.tamaya.spisupport.propertysource.BasePropertySource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Distributed Propertysource using a in-memory hazelcast cluster.
 * Created by atsticks on 03.11.16.
 *
 * Basically all kind of property entris can be stored. Additionally this property source allows
 * to pass additional getMeta-entries to control the TTL of the data in milliseconds. For illustration
 * the following mapProperties will store {@code my.entry} with a TLL of 20000 milliseconds (20 seconds) and
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
public abstract class AbstractHazelcastPropertySource extends BasePropertySource
implements MutablePropertySource{
    /** The logger. */
    private static final Logger LOG = Logger.getLogger(AbstractHazelcastPropertySource.class.getName());
    /** The Hazelcast config mapProperties used. */
    private Map<String, PropertyValue> configMap = new HashMap<>();
    /** The hazelcast mapProperties reference ID used, by default {@code tamaya.configuration}. */
    private String mapReference = "tamaya.configuration";
    /** Flag if this property source is read-only. */
    private boolean readOnly = false;

    private AtomicLong timeoutDuration = new AtomicLong(TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES));

    private AtomicLong timeout = new AtomicLong();

    /**
     * Creates a new instance, hereby using {@code "Hazelcast"} as property source name and
     * a default hazelcast backend created by calling {@link Hazelcast#newHazelcastInstance()}.
     */
    public AbstractHazelcastPropertySource(){
        super("Hazelcast");
    }

    /**
     * Creates a new instance, hereby using the given property source name and
     * a default hazelcast backend created by calling {@link Hazelcast#newHazelcastInstance()}.
     * @param name the property source name, not null.
     */
    public AbstractHazelcastPropertySource(String name){
        super(name);
    }

    /**
     * Get the current timeout, when a reload will be triggered on access.
     * @return the current timeout, or 0 if no data has been loaded at all.
     */
    public long getValidUntil(){
        return timeout.get();
    }

    /**
     * Get the current cache timeout.
     * @return the timeout duration after which data will be reloaded.
     */
    public long getCachePeriod(){
        return timeoutDuration.get();
    }

    /**
     * Set the duration after which the data cache will be reloaded.
     * @param millis the millis
     */
    public void setCacheTimeout(long millis){
        this.timeoutDuration.set(millis);
        this.refresh();
    }


    /**
     * Setting the read-only flag for this instance.
     * @param readOnly if true, the property source will not write back any changes to the
     *                 hazelcast backend.
     */
    public void setReadOnly(boolean readOnly){
        this.readOnly = readOnly;
    }

    /**
     * Flag to check if the property source is read-only.
     * @return true, if the instance is read-only.
     */
    public boolean isReadOnly(){
        return readOnly;
    }

    /**
     * Set the Hazelcast reference name for the Tamaya configuration Map.
     * @param mapReference the mapProperties reference to be used, not null.
     */
    public void setMapReference(String mapReference){
        if (!Objects.equals(mapReference, this.mapReference)) {
            this.mapReference = Objects.requireNonNull(mapReference);
            refresh();
        }
    }

    /**
     * Get the Hazelcast reference name for the Tamaya configuration Map.
     * @return the Hazelcast reference name for the Tamaya configuration Map, never null.
     */
    public String getMapReference(){
        return mapReference;
    }

    /**
     * Get access to the hazelcast instance used.
     * @return the hazelcast instance, not null.
     */
    protected abstract HazelcastInstance getHazelcastInstance();

    @Override
    public PropertyValue get(String key) {
        checkRefresh();
        return this.configMap.get(key);
    }

    @Override
    public Map<String, PropertyValue> getProperties() {
        checkRefresh();
        return this.configMap;
    }

    /**
     * Checks for a cache timeout and optionally reloads the data.
     */
    public void checkRefresh(){
        if(this.timeout.get() < System.currentTimeMillis()){
            refresh();
        }
    }

    /**
     * Reloads the configuration mapProperties from Hazelcast completely.
     */
    public void refresh() {
        IMap<String,String> config = getHazelcastInstance().getMap(mapReference);
        Map<String, PropertyValue> configMap = new HashMap<>();
        config.forEach((key, value) -> configMap.put(key,
                PropertyValue.createValue(key, value)
                        .setMeta("source", getName())
                        .setMeta("backend", "Hazelcast")
                        .setMeta("instance", getHazelcastInstance().getName())
                        .setMeta("mapReference", mapReference)
                        .immutable()));
        this.timeout.set(System.currentTimeMillis() + timeoutDuration.get());
        this.configMap = Collections.unmodifiableMap(configMap);
    }

    @Override
    public void applyChange(ConfigChangeRequest configChange) {
        if(readOnly){
            return;
        }
        IMap<String,String> config = getHazelcastInstance().getMap(mapReference);
        for(Map.Entry<String, String> en: configChange.getAddedProperties().entrySet()){
            String metaVal = configChange.getAddedProperties().get("_" + en.getKey()+".ttl");
            if(metaVal!=null){
                try {
                    long ms = Long.parseLong(metaVal);
                    config.put(en.getKey(), en.getValue(), ms, TimeUnit.MILLISECONDS);
                }catch(Exception e){
                    LOG.log(Level.WARNING, "Failed to parse TTL in millis: " + metaVal +
                            " for '"+ en.getKey()+"'", e);
                    config.put(en.getKey(), en.getValue());
                }
            }else {
                config.put(en.getKey(), en.getValue());
            }
        }
        for(String key: configChange.getRemovedProperties()){
            config.remove(key);
        }
        IList<String> taList = getHazelcastInstance().getList("[(META)tamaya.transactions]");
        taList.add(configChange.getTransactionID());
        config.put("[(META)tamaya.transaction].lastId", configChange.getTransactionID(), 1, TimeUnit.DAYS);
        config.put("[(META)tamaya.transaction].startedAt", String.valueOf(configChange.getStartedAt()), 1, TimeUnit.DAYS);
        config.flush();
        refresh();
    }

    @Override
    protected String toStringValues() {
        return super.toStringValues() +
                "\n  hazelcastInstance=" + getHazelcastInstance() +
                "\n  name='" + getName() + '\'' +
                "\n  mapReference='" + mapReference + '\'' +
                "\n  readOnly=" + readOnly + '\'';
    }

}
