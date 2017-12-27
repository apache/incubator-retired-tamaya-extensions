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
package org.apache.tamaya.functions;

import javax.config.ConfigProvider;
import javax.config.spi.ConfigSource;
import java.util.*;
import java.util.function.Function;

/**
 * Accessor that provides useful functions along with configuration.
 */
public final class ConfigSourceFunctions {
    /**
     * Implementation of an empty propertySource.
     */
    private static final ConfigSource EMPTY_PROPERTYSOURCE = new ConfigSource() {

        @Override
        public int getOrdinal() {
            return 0;
        }

        @Override
        public String getName() {
            return "<empty>";
        }

        @Override
        public String getValue(String key) {
            return null;
        }

        @Override
        public Map<String, String> getProperties() {
            return Collections.emptyMap();
        }

        @Override
        public String toString() {
            return "ConfigSource<empty>";
        }
    };


    private static final Function<String,Integer> DEFAULT_AREA_CALCULATOR =
                                            s -> s.lastIndexOf('.');

    /**
     * Private singleton constructor.
     */
    private ConfigSourceFunctions() {
    }

    /**
     * Calculates the current section key and compares it to the given key.
     *
     * @param key        the fully qualified entry key, not null
     * @param sectionKey the section key, not null
     * @param sectionCalculator function to calculate the split point of a key's section, e.g. {@code key.lastIndexOf('.')},
     *                          not null.
     * @param directChildrenOnly if true, only keys with the same area match. Otherwise also containing super-areas can
     *                           match.
     * @return true, if the entry is exact in this section
     */
    private static boolean isKeyInSection(String key, String sectionKey,
                                          Function<String,Integer> sectionCalculator, boolean directChildrenOnly) {
        Objects.requireNonNull(key, "Key must be given.");
        Objects.requireNonNull(sectionCalculator, "Section calculator must be given.");
        Objects.requireNonNull(sectionKey, "Section key must be given.");

        sectionKey = normalizeSectionKey(sectionKey);

        int lastIndex = sectionCalculator.apply(key);
        String curAreaKey = lastIndex > 0 ? key.substring(0, lastIndex) : "";
        if(directChildrenOnly) {
            return curAreaKey.equals(sectionKey);
        }else{
            return curAreaKey.startsWith(sectionKey);
        }
    }

    private static String normalizeKey(String key) {
        return normalizeKey(key, DEFAULT_AREA_CALCULATOR);
    }

    private static String normalizeKey(String key, Function<String,Integer> sectionCalculator) {
        if(key.isEmpty()){
            return key;
        }
        int index = sectionCalculator.apply(key.substring(0,1));
        if(index==0){
            return key.substring(1);
        }
        return key;
    }

    private static String normalizeSectionKey(String sectionKey) {
        return normalizeSectionKey(sectionKey, DEFAULT_AREA_CALCULATOR);
    }

    private static String normalizeSectionKey(String sectionKey, Function<String,Integer> areaCalculator) {
        // Ignore unneeded and trailing dot at the end of the section key
        if(sectionKey.isEmpty()){
            return sectionKey;
        }
        int lastIndex = areaCalculator.apply(sectionKey);
        int firstIndex = areaCalculator.apply(sectionKey.substring(0,1));

        String normalizedKey = lastIndex==(sectionKey.length()-1)
                   ? sectionKey.substring(0, sectionKey.length() - 1)
                   : sectionKey;

        normalizedKey = firstIndex==0 ? sectionKey.length() == 1 ? ""
                                                                              : normalizedKey.substring(1)
                                                   : normalizedKey;

        return normalizedKey;
    }

    /**
     * Checks if the given key is <i>directly</i> included in one of the given sections.
     *
     * @param key             the fully qualified entry key, not {@code null}
     * @param sectionKeys      the section keys, not {@code null}
     * @return true, if the entry is in one of the given sections
     */
    public static boolean isKeyInSection(String key, String... sectionKeys) {
        return isKeyInSection(key, true, DEFAULT_AREA_CALCULATOR, sectionKeys);
    }

    /**
     * Checks if the given key is included in one of the given sections.
     *
     * @param key             the fully qualified entry key, not {@code null}
     * @param sectionKeys      the section keys, not {@code null}
     * @param directChildrenOnly if true, then only keys match, which are a direct child of the given section.
     * @return true, if the entry is in one of the given sections
     */
    public static boolean isKeyInSection(String key, boolean directChildrenOnly, String... sectionKeys) {
        return isKeyInSection(key, directChildrenOnly, DEFAULT_AREA_CALCULATOR, sectionKeys);
    }

    /**
     * Checks if the given key is included in one of the given sections, using the given separator to identify sections.
     *
     * @param key             the fully qualified entry key, not {@code null}
     * @param sectionKeys     the section keys, not {@code null}
     * @param areaCalculator  the function to calculate the split point to identify the section of a key.
     * @param directChildrenOnly if true, then only keys match, which are a direct child of the given section.
     * @return true, if the entry is in one of the given sections
     */
    public static boolean isKeyInSection(String key, boolean directChildrenOnly,
                                         Function<String,Integer> areaCalculator, String... sectionKeys) {
        Objects.requireNonNull(key, "Key must be given.");
        Objects.requireNonNull(sectionKeys, "Section keys must be given.");

        for (String areaKey : sectionKeys) {
            if (areaKey == null) {
                continue;
            }
            if (isKeyInSection(key, areaKey, areaCalculator, directChildrenOnly)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return a query to evaluate the set with all fully qualified section names. This method should return the sections as accurate as possible,
     * but may not provide a complete set of sections that are finally accessible, especially when the underlying storage
     * does not support key iteration.
     *
     * @param properties properties to find sections in.
     * @return set with all sections, never {@code null}.
     */
    public static Set<String> sections(Map<String, String> properties) {
        return sections(properties, DEFAULT_AREA_CALCULATOR);
    }

    /**
     * Return a query to evaluate the set with all fully qualified section names. This method should return the sections as accurate as possible,
     * but may not provide a complete set of sections that are finally accessible, especially when the underlying storage
     * does not support key iteration.
     *
     * @param properties properties to find sections in.
     * @return set with all sections, never {@code null}.
     */
    public static Set<String> sections(Map<String, String> properties, Function<String,Integer> areaCalculator) {
        final Set<String> areas = new HashSet<>();
        for (String key : properties.keySet()) {
            String normalizedKey = normalizeKey(key, areaCalculator);

            int index = areaCalculator.apply(normalizedKey);
            if (index > 0) {
                areas.add(normalizedKey.substring(0, index));
            } else {
                areas.add("<root>");
            }
        }
        return areas;
    }

    /**
     * Return a query to evaluate the set with all fully qualified section names, containing the transitive closure also including all
     * subarea names, regardless if properties are accessible or not. This method should return the sections as accurate
     * as possible, but may not provide a complete set of sections that are finally accessible, especially when the
     * underlying storage does not support key iteration.
     *
     * @param properties properties to find transitive sections in.
     * @return s set with all transitive sections, never {@code null}.
     */
    public static Set<String> transitiveSections(Map<String, String> properties) {
        return transitiveSections(properties, DEFAULT_AREA_CALCULATOR);
    }

    /**
     * Return a query to evaluate the set with all fully qualified section names, containing the transitive closure also including all
     * subarea names, regardless if properties are accessible or not. This method should return the sections as accurate
     * as possible, but may not provide a complete set of sections that are finally accessible, especially when the
     * underlying storage does not support key iteration.
     * 
     * @param properties properties to find transitive sections in.
     * @return s set with all transitive sections, never {@code null}.
     */
    public static Set<String> transitiveSections(Map<String, String> properties, Function<String,Integer> areaCalculator) {
        final Set<String> transitiveAreas = new HashSet<>();
        for (String section : sections(properties, areaCalculator)) {
            section = normalizeSectionKey(section, areaCalculator);

            int index = areaCalculator.apply(section);
            if (index < 0 && section.isEmpty()) {
                transitiveAreas.add("<root>");
            } if (index < 0) {
                transitiveAreas.add(section);
            } else {
                while (index > 0) {
                    section = section.substring(0, index);
                    transitiveAreas.add(section);
                    index = section.lastIndexOf('.');
                }
            }
        }
        return transitiveAreas;
    }

    /**
     * Return a query to evaluate the set with all fully qualified section names, containing only the
     * sections that match the predicate and have properties attached. This method should return the sections as accurate as possible,
     * but may not provide a complete set of sections that are finally accessible, especially when the underlying storage
     * does not support key iteration.
     * 
     * @param properties properties to find sections in.
     * @param predicate A predicate to determine, which sections should be returned, not {@code null}.
     * @return s set with all sections, never {@code null}.
     */
    public static Set<String> sections(Map<String, String> properties, final Predicate<String> predicate) {
        Set<String> treeSet = new TreeSet<>();
        for (String area : sections(properties)) {
            if (predicate.test(area)) {
                treeSet.add(area);
            }
        }
        return treeSet;
    }

    /**
     * Return a query to evaluate the set with all fully qualified section names, containing the transitive closure also including all
     * subarea names, regardless if properties are accessible or not. This method should return the sections as accurate as possible,
     * but may not provide a complete set of sections that are finally accessible, especially when the underlying storage
     * does not support key iteration.
     *
     * @param properties properties to find transitive sections in.
     * @param predicate A predicate to determine, which sections should be returned, not {@code null}.
     * @return s set with all transitive sections, never {@code null}.
     */
    public static Set<String> transitiveSections(Map<String, String> properties, Predicate<String> predicate) {
        Set<String> treeSet = new TreeSet<>();
        for (String area : transitiveSections(properties)) {
            if (predicate.test(area)) {
                treeSet.add(area);
            }
        }
        return treeSet;
    }


    /**
     *Extracts the submap containing only entries with keys
     * that are contained in the given sections. Hereby
     * the section key is stripped away from the Map of the resulting keys.
     *
     * @param properties properties to find recursive sections in.
     * @param sectionKeys the section keys, not null
     * @return the section configuration, with the areaKey stripped away.
     */
    public static Map<String, String> sectionsRecursive(Map<String, String> properties, String... sectionKeys) {
        return sectionsRecursive(properties, true, sectionKeys);
    }

    /**
     * Extracts the submap containing only entries with keys
     * that are contained in the given section and it's subsections.
     *
     * @param properties properties to find sections in.
     * @param sectionKeys the section keys, not null
     * @param stripKeys   if set to true, the section key is stripped away fromMap the resulting key.
     * @return the section configuration, with the areaKey stripped away.
     */
    public static Map<String, String> sectionsRecursive(Map<String, String> properties, boolean stripKeys, String... sectionKeys) {
        Map<String, String> result = new HashMap<>(properties.size());
        for (Map.Entry<String, String> en : properties.entrySet()) {
            if (isKeyInSection(en.getKey(), false,DEFAULT_AREA_CALCULATOR, sectionKeys)) {
                if (stripKeys) {
                    result.put(stripSectionKeys(en.getKey(), sectionKeys), en.getValue());
                }else {
                    result.put(en.getKey(), en.getValue());
                }
            }
        }
        return result;
    }

    /**
     * Strips the section key of the given absolute key, if it is one of the areaKeys passed.
     *
     * @param key      the current key, not null.
     * @param areaKeys the areaKeys, not null.
     * @return the stripped key, or the original key (if no section was matching).
     */
    static String stripSectionKeys(String key, String... areaKeys) {
        for (String areaKey : areaKeys) {
            if (key.startsWith(areaKey + '.')) {
                return key.substring(areaKey.length() + 1);
            }
        }
        return key;
    }

    /**
     * Creates a ConfigOperator that adds the given items.
     *
     * @param propertySource source property source that is changed.
     * @param items    the items to be added/replaced.
     * @param override if true, all items existing are overridden by the new ones passed.
     * @return the ConfigOperator, never null.
     */
    public static ConfigSource addItems(ConfigSource propertySource, final Map<String, String> items, final boolean override) {
        return new EnrichedConfigSource(propertySource, items, override);
    }

    /**
     * Creates an operator that adds items to the instance (existing items will not be overridden).
     *
     * @param propertySource source property source that is changed.
     * @param items the items, not null.
     * @return the operator, never null.
     */
    public static ConfigSource addItems(ConfigSource propertySource, Map<String, String> items) {
        return addItems(propertySource, items, false);
    }

    /**
     * Creates an operator that replaces the given items.
     *
     * @param propertySource source property source that is changed.
     * @param items the items.
     * @return the operator for replacing the items.
     */
    public static ConfigSource replaceItems(ConfigSource propertySource, Map<String, String> items) {
        return addItems(propertySource, items, true);
    }

    /**
     * Accesses an empty PropertySource.
     *
     * @return an empty PropertySource, never null.
     */
    public static ConfigSource emptyConfigSource() {
        return EMPTY_PROPERTYSOURCE;
    }

    /**
     * Find all {@link ConfigSource} instances managed by the current
     * {@link javax.config.Config} that are assignable to the given type.
     *
     * @param expression the regular expression to match the source's name.
     * @return the list of all {@link ConfigSource} instances matching, never null.
     */
    public static Collection<? extends ConfigSource> findPropertySourcesByName(String expression) {
        List result = new ArrayList<>();
        for (ConfigSource src : ConfigProvider.getConfig().getConfigSources()) {
            if (src.getName().matches(expression)) {
                result.add(src);
            }
        }
        return result;
    }

    /**
     * Get a list of all {@link ConfigSource} instances managed by the current
     * {@link javax.config.Config} that are assignable to the given type.
     *
     * @param <T> the type of the property source instances requested 
     * @param type target type to filter for property sources. 
     * @return the list of all {@link ConfigSource} instances matching, never null.
     */
    public static <T> Collection<T> getPropertySources(Class<T> type) {
        List<T> result = new ArrayList<>();
        for (ConfigSource src : ConfigProvider.getConfig().getConfigSources()) {
            if (type.isAssignableFrom(src.getClass())) {
                result.add((T) src);
            }
        }
        return result;
    }

    /**
     * Get a list of all {@link ConfigSource} instances managed by the current
     * {@link javax.config.Config} that are assignable to the given type.
     *
     * @param <T> the type of the property source instances requested
     * @param type target type to filter for property sources. 
     * @return the list of all {@link ConfigSource} instances matching, never null.
     */
    public static <T> T getPropertySource(Class<T> type) {
        for (ConfigSource src : ConfigProvider.getConfig().getConfigSources()) {
            if (type.isAssignableFrom(src.getClass())) {
                return (T) src;
            }
        }
        return null;
    }

}
