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
package org.apache.tamaya.filter;

import org.apache.tamaya.spi.Filter;
import org.osgi.service.component.annotations.Component;

import java.util.*;


/**
 * Hereby
 * <ul>
 *     <li><b>Single</b> filters are applied only when values are explicitly accessed. This is useful, e.g. for
 *     filtering passwords into clear text variants. Nevertheless metadata keys hidden on map level must be
 *     accessible (=not filtered) when accessed as single values.</li>
 *     <li><b>Map</b> filters are applied when values are filtered as part of a full properties access.
 *     Often filtering in these cases is more commonly applied, e.g. you dont want to show up all kind of metadata.
 *     </li>
 * </ul>
 *     For both variants individual filter rules can be applied here. All filters configured are managed on a
 *     thread-local level, so this class is typically used to temporarely filter out some values. Do not forget to
 *     restore its state, when not using a thread anymore (especially important in multi-threaded environments), not
 *     doing so will create nasty side effects of configuration not being visisble depending on the thread
 *     active.
 */
@Component
public final class ConfigurationFilter implements Filter{

    private static final ThreadLocal<Boolean> THREADED_METADATA_FILTERED = ThreadLocal.withInitial(() -> Boolean.TRUE);

    private static final ThreadLocal<List<Filter>> THREADED_FILTERS = ThreadLocal.withInitial(ArrayList::new);

    /**
     * Flag if metadata entries (starting with an '_') are filtered out on when accessing multiple properties, default
     * is {@code true}.
     * @return true, if metadata entries (starting with an '_') are to be filtered.
     */
    public static boolean isMetadataFiltered(){
        return THREADED_METADATA_FILTERED.get();
    }

    /**
     * Seactivates metadata filtering also on global map access for this thread.
     * @see #cleanupFilterContext()
     * @param filtered true,to enable metadata filtering (default).
     */
    public static void setMetadataFiltered(boolean filtered){
        THREADED_METADATA_FILTERED.set(filtered);
    }

    /**
     * Access the filtering configuration that is used on the current thread.
     *
     * @return the filtering config, never null.
     */
    public static List<Filter> getFilters(){
        return Collections.unmodifiableList(THREADED_FILTERS.get());
    }

    /**
     * Add a filter.
     * @param filter the filter.
     */
    public static void addFilter(Filter filter){
        if(!THREADED_FILTERS.get().contains(filter)) {
            THREADED_FILTERS.get().add(filter);
        }
    }

    /**
     * Adds a filter at given position.
     * @param pos the position.
     * @param filter the filter.
     */
    public static void addFilter(int pos, Filter filter){
        if(!THREADED_FILTERS.get().contains(filter)) {
            THREADED_FILTERS.get().add(pos, filter);
        }
    }

    /**
     * Removes a filter at a given position.
     * @param pos the position.
     * @return the filter removed, or null.
     */
    public static Filter removeFilter(int pos){
        return THREADED_FILTERS.get().remove(pos);
    }

    /**
     * Removes a filter.
     * @param filter the filter to be removed, not null.
     */
    public static void removeFilter(Filter filter) {
        THREADED_FILTERS.get().remove(filter);
    }

    /**
     * Clears all filters.
     */
    public static void clearFilters(){
        THREADED_FILTERS.get().clear();
    }

    /**
     * Set the filters.
     * @param filters the filters to be applied.
     */
    public static void setFilters(Filter... filters){
        setFilters(Arrays.asList(filters));
    }

    /**
     * Set the filters.
     * @param filters the filters to be applied.
     */
    public static void setFilters(Collection<Filter> filters) {
        THREADED_FILTERS.get().clear();
        THREADED_FILTERS.get().addAll(filters);
    }

    /**
     * Removes all programmable filters active on the current thread.
     */
    public static void cleanupFilterContext(){
        THREADED_FILTERS.get().clear();
        THREADED_METADATA_FILTERED.set(true);
    }

    @Override
    public String filterProperty(String key, String valueToBeFiltered) {
        for(Filter pred: THREADED_FILTERS.get()){
            valueToBeFiltered = pred.filterProperty(key, valueToBeFiltered);
        }
        return valueToBeFiltered;
    }
}
