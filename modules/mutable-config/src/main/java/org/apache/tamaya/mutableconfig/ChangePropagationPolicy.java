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
package org.apache.tamaya.mutableconfig;

import org.apache.tamaya.ConfigException;
import org.apache.tamaya.mutableconfig.spi.MutablePropertySource;
import org.apache.tamaya.spi.PropertySource;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Policy that defines how changes are applied to the available
 * {@link org.apache.tamaya.mutableconfig.spi.MutablePropertySource} instances, e.g.
 * <ul>
 *     <li><b>ALL: </b>Changes are propagated to all {@link org.apache.tamaya.mutableconfig.spi.MutablePropertySource}
 *     instances in order of significance. This means that a key added, updated or removed in each instance, if the key
 *     is writable/removable.</li>
 *     <li><b>SIGNIFICANT_ONLY: </b>A change (creation, update) is only applied, if
 * <ol>
 *     <li>the createValue is not provided by a more significant read-only property source.</li>
 *     <li>there is no more significant writable property source, which supports writing a g iven key.</li>
 * </ol>
 * In other words a added or updated createValue is written exactly once to the most significant
 * writable property source, which accepts a given key. Otherwise the change is discarded.</li>
 * <li><b>NONE: </b>Do not apply any changes.</li>
 * </ul>
 */
@FunctionalInterface
public interface ChangePropagationPolicy {

    /**
     * Method being called when a multiple key/createValue pairs are added or updated.
     * @param propertySources the property sources, including readable property sources of the current configuration,
     *                        never null.
     * @param configChange the configuration change, not null.
     */
    void applyChange(ConfigChangeRequest configChange, Collection<PropertySource> propertySources);



    /**
     * This propagation policy writes changes only once to the most significant property source, where a change is
     * applicable.
     * @param propertySourceNames the names of the mutable property sources to be considered for writing any changes to.
     * @return a corresponding {@link ChangePropagationPolicy} implementation, never null.
     */
    static ChangePropagationPolicy getApplySelectiveChangePolicy(final String... propertySourceNames){
        return new ChangePropagationPolicy() {

            private Set<String> sourceNames = new HashSet<>(Arrays.asList(propertySourceNames));

            @Override
            public void applyChange(ConfigChangeRequest change, Collection<PropertySource> propertySources) {
                for(PropertySource propertySource: propertySources){
                    if(propertySource instanceof MutablePropertySource){
                        if(this.sourceNames.contains(propertySource.getName())) {
                            MutablePropertySource target = (MutablePropertySource) propertySource;
                            try{
                                target.applyChange(change);
                            }catch(ConfigException e){
                                Logger.getLogger(ChangePropagationPolicy.class.getName())
                                        .warning("Failed to store changes '"+change+"' not applicable to "+target.getName()
                                        +"("+target.getClass().getName()+").");
                            }
                            break;
                        }
                    }
                }
            }
        };
    }

    /**
     * This propagation policy writes through all changes to all mutable property sources, where applicable.
     */
    ChangePropagationPolicy ALL_POLICY = new ChangePropagationPolicy() {
        @Override
        public void applyChange(ConfigChangeRequest change, Collection<PropertySource> propertySources) {
            for(PropertySource propertySource: propertySources){
                if(propertySource instanceof MutablePropertySource){
                    MutablePropertySource target = (MutablePropertySource)propertySource;
                    try{
                        target.applyChange(change);
                    }catch(ConfigException e){
                        Logger.getLogger(ChangePropagationPolicy.class.getName())
                                .warning("Failed to store changes '"+change+"' not applicable to "+target.getName()
                                +"("+target.getClass().getName()+").");
                    }
                }
            }
        }

    };

    /**
     * This propagation policy writes changes only once to the most significant property source, where a change is
     * applicable.
     */
    ChangePropagationPolicy MOST_SIGNIFICANT_ONLY_POLICY = new ChangePropagationPolicy() {
        @Override
        public void applyChange(ConfigChangeRequest change, Collection<PropertySource> propertySources) {
            for(PropertySource propertySource: propertySources){
                if(propertySource instanceof MutablePropertySource){
                    MutablePropertySource target = (MutablePropertySource)propertySource;
                    try{
                        target.applyChange(change);
                    }catch(ConfigException e){
                        Logger.getLogger(ChangePropagationPolicy.class.getName())
                                .warning("Failed to store changes '"+change+"' not applicable to "+target.getName()
                                +"("+target.getClass().getName()+").");
                    }
                    break;
                }
            }
        }

    };

    /**
     * This propagation policy writes changes only once to the most significant property source, where a change is
     * applicable.
     */
    ChangePropagationPolicy NONE_POLICY = new ChangePropagationPolicy() {
        @Override
        public void applyChange(ConfigChangeRequest change, Collection<PropertySource> propertySources) {
            Logger.getLogger(ChangePropagationPolicy.class.getName())
                    .warning("Cannot store changes '"+change+"': prohibited by change policy (read-only).");
        }
    };


}
