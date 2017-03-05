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
package org.apache.tamaya.spisupport;

import org.apache.tamaya.spi.ConfigurationContext;
import org.apache.tamaya.spi.FilterContext;
import org.apache.tamaya.spi.PropertyFilter;
import org.apache.tamaya.spi.PropertyValue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the Configuration API. This class uses the current {@link ConfigurationContext} to evaluate the
 * chain of {@link org.apache.tamaya.spi.PropertySource} and {@link PropertyFilter}
 * instance to evaluate the current Configuration.
 */
public final class PropertyFilterManager {
    /**
     * The logger.
     */
    private static final Logger LOG = Logger.getLogger(PropertyFilterManager.class.getName());
    /**
     * The maximal number of filter cycles performed before aborting.
     */
    private static final int MAX_FILTER_LOOPS = 10;

    /**
     * Private singleton constructor.
     */
    private PropertyFilterManager(){}

    public static PropertyValue applyFilter(String key, PropertyValue unfilteredValue, ConfigurationContext configurationContext) {
        // Apply filters to values, prevent values filtered to null!
        for (int i = 0; i < MAX_FILTER_LOOPS; i++) {
            boolean changed = false;
            // Apply filters to values, prevent values filtered to null!
            for (PropertyFilter filter : configurationContext.getPropertyFilters()) {
                PropertyValue newValue = filter.filterProperty(unfilteredValue, new FilterContext(key, unfilteredValue));
                if (newValue != null && !newValue.equals(unfilteredValue)) {
                    changed = true;
                    if (LOG.isLoggable(Level.FINEST)) {
                        LOG.finest("Filter - " + key + ": " + unfilteredValue + " -> " + newValue + " by " + filter);
                    }
                } else if (unfilteredValue != null && !unfilteredValue.equals(newValue)) {
                    changed = true;
                    if (LOG.isLoggable(Level.FINEST)) {
                        LOG.finest("Filter - " + key + ": " + unfilteredValue + " -> " + newValue + " by " + filter);
                    }
                }
                unfilteredValue = newValue;
            }
            if (!changed) {
                LOG.finest("Finishing filter loop, no changes detected.");
                break;
            } else {
                if (i == (MAX_FILTER_LOOPS - 1)) {
                    if (LOG.isLoggable(Level.WARNING)) {
                        LOG.warning("Maximal filter loop count reached, aborting filter evaluation after cycles: " + i);
                    }
                } else {
                    LOG.finest("Repeating filter loop, changes detected.");
                }
            }
        }
        return unfilteredValue;
    }

    public static Map<String, PropertyValue> applyFilters(Map<String, PropertyValue> inputMap, ConfigurationContext configurationContext) {
        Map<String, PropertyValue> resultMap = new HashMap<>(inputMap);
        // Apply filters to values, prevent values filtered to null!
        for (int i = 0; i < MAX_FILTER_LOOPS; i++) {
            AtomicInteger changes = new AtomicInteger();
            for (PropertyFilter filter : configurationContext.getPropertyFilters()) {
                for (Map.Entry<String, PropertyValue> entry : inputMap.entrySet()) {
                    final String k = entry.getKey();
                    final PropertyValue v = entry.getValue();

                    PropertyValue newValue = filter.filterProperty(v, new FilterContext(k, inputMap));
                    if (newValue != null && !newValue.equals(v)) {
                        changes.incrementAndGet();
                        LOG.finest("Filter - " + k + ": " + v + " -> " + newValue + " by " + filter);
                    } else if (v != null && !v.equals(newValue)) {
                        changes.incrementAndGet();
                        LOG.finest("Filter - " + k + ": " + v + " -> " + newValue + " by " + filter);
                    }
                    // Remove null values
                    if (null != newValue) {
                        resultMap.put(k, newValue);
                    }else{
                        resultMap.remove(k);
                    }
                }
            }
            if (changes.get() == 0) {
                LOG.finest("Finishing filter loop, no changes detected.");
                break;
            } else {
                if (i == (MAX_FILTER_LOOPS - 1)) {
                    if (LOG.isLoggable(Level.WARNING)) {
                        LOG.warning("Maximal filter loop count reached, aborting filter evaluation after cycles: " + i);
                    }
                } else {
                    LOG.finest("Repeating filter loop, changes detected: " + changes.get());
                }
                changes.set(0);
            }
        }
        return resultMap;
    }

}
