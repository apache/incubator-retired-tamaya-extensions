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
package org.apache.tamaya.osgi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton class to store OSGI configuration backups before changing the OSGI
 * Config with Tamaya settings. This allows to restore the configuration in
 * case of problems.
 */
final class Backups {

    private static final Logger LOG = Logger.getLogger(Backups.class.getName());
    public static final String TAMAYA_BACKUP = "tamaya.backup";
    private static Map<String, Hashtable<String,?>> initialConfigState = new ConcurrentHashMap<>();

    private Backups(){}

    /**
     * Sets the given backup for a PID.
     * @param pid the PID, not null.
     * @param config the config to store.
     */
    public static void set(String pid, Dictionary<String,?> config){
        initialConfigState.put(pid, toHashtable(config));
    }

    /**
     * Converts the dictionary to a hash table to enable serialization.
     * @param dictionary the config, not null.
     * @return the corresponding Hashtable
     */
    private static Hashtable<String, ?> toHashtable(Dictionary<String, ?> dictionary) {
        if (dictionary == null) {
            return null;
        }
        if(dictionary instanceof Hashtable){
            return (Hashtable<String,?>) dictionary;
        }
        Hashtable<String, Object> map = new Hashtable<>(dictionary.size());
        Enumeration<String> keys = dictionary.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            map.put(key, dictionary.get(key));
        }
        return map;
    }

    /**
     * Removes a backup.
     * @param pid the PID, not null.
     * @return the removed dictionary or null if unknown
     */
    public static Dictionary<String,?> remove(String pid){
        return initialConfigState.remove(pid);
    }

    /**
     * Removes all backups.
     */
    public static void removeAll(){
        initialConfigState.clear();
    }

    /**
     * Get a backup for a PID.
     * @param pid the PID, not null.
     * @return the backup found, or null.
     */
    public static Dictionary<String,?> get(String pid){
        return initialConfigState.get(pid);
    }

    /**
     * Get all current stored backups.
     * @return The backups stored, by PID.
     */
    public static Map<String,Dictionary<String,?>> get(){
        return new HashMap<>(initialConfigState);
    }

    /**
     * Get all currently known PIDs.
     * @return the PIDs, never null.
     */
    public static Set<String> getPids(){
        return initialConfigState.keySet();
    }

    /**
     * Checks if a backup exists for a given PID.
     * @param pid the pid, not null.
     * @return {@code true} if there is a backup for the given PID, {@code false} otherwise.
     */
    public static boolean contains(String pid){
        return initialConfigState.containsKey(pid);
    }

    /**
     * Saves the backups into the given config.
     * @param config the config, not null.
     */
    public static void save(Dictionary<String,Object> config){
        try{
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(initialConfigState);
            oos.flush();
            Base64.getEncoder().encode(bos.toByteArray());
            config.put(TAMAYA_BACKUP, Base64.getEncoder().encodeToString(bos.toByteArray()));
        }catch(Exception e){
            LOG.log(Level.SEVERE, "Failed to restore OSGI Backups.", e);
        }
    }

    /**
     * Restores the backups into the given config.
     * @param config the config, not null.
     */
    @SuppressWarnings("unchecked")
	public static void restore(Dictionary<String,Object> config){
        try{
            String serialized = (String)config.get(TAMAYA_BACKUP);
            if(serialized!=null) {
                ByteArrayInputStream bis = new ByteArrayInputStream(Base64.getDecoder().decode(serialized));
                ObjectInputStream ois = new ObjectInputStream(bis);
                initialConfigState = (Map<String, Hashtable<String,?>>) ois.readObject();
                ois.close();
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to store getConfig change history.", e);
        }
    }
}
