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

import com.google.common.base.Optional;
import com.google.common.net.HostAndPort;
import com.orbitz.consul.Consul;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.model.kv.Value;
import org.apache.tamaya.mutableconfig.ConfigChangeRequest;
import org.apache.tamaya.mutableconfig.spi.MutablePropertySource;
import org.apache.tamaya.spi.PropertyValue;
import org.apache.tamaya.spisupport.propertysource.BasePropertySource;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Propertysource base class that is reading configuration from a configured consul endpoint.
 */
public abstract class AbstractConsulPropertySource extends BasePropertySource
implements MutablePropertySource{
    private static final Logger LOG = Logger.getLogger(AbstractConsulPropertySource.class.getName());

    private String prefix = "";

    private List<HostAndPort> consulBackends = new ArrayList<>();

    /** The config cache used. */
    private Map<String, PropertyValue> configMap = new ConcurrentHashMap<>();

    private AtomicLong timeoutDuration = new AtomicLong(TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES));

    private AtomicLong timeout = new AtomicLong();


    public AbstractConsulPropertySource(){
        this("consul");
    }

    public AbstractConsulPropertySource(String name){
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
    }

    /**
     * Gets the prefix that is added for looking up keys in consul. This allows to use a separate subnamespace in
     * consul for configuration.
     * @return the prefix, never null.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Sets the prefix that is added for looking up keys in consul. This allows to use a separate subnamespace in
     * consul for configuration.
     * @param prefix the prefix, not null.
     */
    public void setPrefix(String prefix) {
        this.prefix = Objects.requireNonNull(prefix);
    }

    /**
     * Set the consol server to connect to.
     * @param server the server list, not null.
     */
    public void setServer(List<String> server){
        if(!Objects.equals(getServer(), server)) {
            List<HostAndPort> consulBackends = new ArrayList<>();
            for (String s : server) {
                consulBackends.add(HostAndPort.fromString(s));
            }
            this.consulBackends = consulBackends;
            refresh();
        }

    }

    /**
     * Get a list of current servers.
     * @return the server list, not null.
     */
    public List<String> getServer() {
        return this.consulBackends.stream().map(s -> s.toString()).collect(Collectors.toList());
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
     * Clears the cached entries.
     */
    public void refresh(){
        this.configMap.clear();
    }

    @Override
    public PropertyValue get(String key) {
        checkRefresh();
        String reqKey = key;
        if(key.startsWith("[META]")){
            reqKey = key.substring("[META]".length());
            if(reqKey.endsWith(".createdIndex")){
                reqKey = reqKey.substring(0,reqKey.length()-".createdIndex".length());
            } else if(reqKey.endsWith(".modifiedIndex")){
                reqKey = reqKey.substring(0,reqKey.length()-".modifiedIndex".length());
            } else if(reqKey.endsWith(".ttl")){
                reqKey = reqKey.substring(0,reqKey.length()-".ttl".length());
            } else if(reqKey.endsWith(".expiration")){
                reqKey = reqKey.substring(0,reqKey.length()-".expiration".length());
            } else if(reqKey.endsWith(".source")){
                reqKey = reqKey.substring(0,reqKey.length()-".source".length());
            }
        }
        PropertyValue val = this.configMap.get(reqKey);
        if(val!=null){
            return val;
        }
        // check prefix, if key does not start with it, it is not part of our name space
        // if so, the prefix part must be removedProperties, so etcd can resolve without it
        for(HostAndPort hostAndPort: this.consulBackends){
            try{
                Consul consul = Consul.builder().withHostAndPort(hostAndPort).build();
                KeyValueClient kvClient = consul.keyValueClient();
                Optional<Value> valueOpt = kvClient.getValue(prefix + reqKey);
                if(!valueOpt.isPresent()) {
                    LOG.log(Level.FINE, "key not found in consul: " + prefix + reqKey);
                }else{
                    // No prefix mapping necessary here, since we only access/return the createValue...
                    Value value = valueOpt.get();
                    Map<String,String> props = new HashMap<>();
                    props.put("createIndex", String.valueOf(value.getCreateIndex()));
                    props.put("modifyIndex", String.valueOf(value.getModifyIndex()));
                    props.put("lockIndex", String.valueOf(value.getLockIndex()));
                    props.put("flags", String.valueOf(value.getFlags()));
                    props.put("source", getName());
                    val = PropertyValue.createValue(reqKey, value.getValue().get())
                        .setMeta(props);
                    break;
                }
            } catch(Exception e){
                LOG.log(Level.FINE, "etcd access failed on " + hostAndPort + ", trying next...", e);
            }
        }
        if(val!=null){
            this.configMap.put(reqKey, val);
        }
        return val;
    }

    @Override
    public Map<String, PropertyValue> getProperties() {
        checkRefresh();
        return Collections.unmodifiableMap(configMap);
    }

    @Override
    public void applyChange(ConfigChangeRequest configChange) {
        for(HostAndPort hostAndPort: this.consulBackends){
            try{
                Consul consul = Consul.builder().withHostAndPort(hostAndPort).build();
                KeyValueClient kvClient = consul.keyValueClient();

                for(String k: configChange.getRemovedProperties()){
                    try{
                        kvClient.deleteKey(k);
                    } catch(Exception e){
                        LOG.info("Failed to remove key from consul: " + k);
                    }
                }
                for(Map.Entry<String,String> en:configChange.getAddedProperties().entrySet()){
                    String key = en.getKey();
                    try{
                        kvClient.putValue(prefix + key,en.getValue());
                    }catch(Exception e) {
                        LOG.info("Failed to add key to consul: " + prefix + en.getKey() + "=" + en.getValue());
                    }
                }
                // success: stop here
                break;
            } catch(Exception e){
                LOG.log(Level.FINE, "consul access failed on " + hostAndPort + ", trying next...", e);
            }
        }
    }

    @Override
    protected String toStringValues() {
        return  super.toStringValues() +
                "  prefix=" + prefix + '\n' +
                "  cacheTimeout=" + timeout + '\n' +
                "  backends=" + this.consulBackends + '\n';
    }

}
