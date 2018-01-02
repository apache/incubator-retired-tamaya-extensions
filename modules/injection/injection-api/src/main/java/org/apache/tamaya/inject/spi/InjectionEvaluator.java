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
package org.apache.tamaya.inject.spi;

import org.apache.tamaya.inject.api.ConfigDefaultSections;
import org.apache.tamaya.inject.api.ConfigFallbackKeys;

import javax.config.inject.ConfigProperty;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Utility class with several commonly used functions.
 */
public final class InjectionEvaluator {

    private InjectionEvaluator(){}


    /**
     * Collects all keys to be be accessed as defined by any annotations of type
     * {@link ConfigDefaultSections}, {@link javax.config.inject.ConfigProperty}.
     * @param member the (optionally) annotated member instance
     * @return the regarding key list to be accessed fomr the {@link javax.config.Config}.
     */
    public static List<String> getKeys(AccessibleObject member) {
        if(member instanceof Field){
            Field f = (Field)member;
            return getKeys(f, f.getDeclaringClass().getAnnotation(ConfigDefaultSections.class));
        } else if(member instanceof Method){
            Method m = (Method)member;
            return getKeys(m, m.getDeclaringClass().getAnnotation(ConfigDefaultSections.class));
        }
        return Collections.emptyList();
    }

    /**
     * Evaluates all absolute configuration keys based on the annotations found in a field/class.
     *
     * @param method method to analyze.
     * @return the list current keys in order how they should be processed/looked up.
     */
    private static List<String> getKeys(Method method, ConfigDefaultSections sectionAnnot) {
        return getKeys(method,
                sectionAnnot,
                method.getAnnotation(ConfigProperty.class),
                method.getAnnotation(ConfigFallbackKeys.class));
    }

    /**
     * Evaluates all absolute configuration keys based on the annotations found in a field/class.
     *
     * @param field field to analyze.
     * @return the list current keys in order how they should be processed/looked up.
     */
    private static List<String> getKeys(Field field, ConfigDefaultSections sectionAnnot) {
        return getKeys(field,
                sectionAnnot,
                field.getAnnotation(ConfigProperty.class),
                field.getAnnotation(ConfigFallbackKeys.class));
    }

    /**
     * Evaluates all absolute configuration keys based on the annotations found in a class.
     * 
     * @param member member to analyze.
     * @param sectionsAnnot         the (optional) annotation definining sections to be looked up.
     * @param propertyAnnotation the annotation on field/method level that may defined one or
     *                           several keys to be looked up (in absolute or relative form).
     * @return the list current keys in order how they should be processed/looked up.
     */
    private static List<String> getKeys(Member member,
                                        ConfigDefaultSections sectionsAnnot,
                                        ConfigProperty propertyAnnotation,
                                        ConfigFallbackKeys configFallbackKeys) {
        List<String> result = new ArrayList<>();
        List<String> memberKeys = new ArrayList<>();
        if(propertyAnnotation!=null && !propertyAnnotation.name().isEmpty()){
            memberKeys.add(propertyAnnotation.name());
        }
        if(configFallbackKeys !=null){
            memberKeys.addAll(Arrays.asList(configFallbackKeys.value()));
        }
        if (memberKeys.isEmpty()) {
            memberKeys.add(member.getName());
        }
        List<String> areaKeys = getSectionKeys(member, sectionsAnnot);
        for(String memberKey:memberKeys){
            if (memberKey.startsWith("[") && memberKey.endsWith("]")) {
                // absolute key, strip away brackets, take key as is
                result.add(memberKey.substring(1, memberKey.length()-1));
            }else{
                for(String areaKey:areaKeys) {
                    result.add(areaKey + '.' + memberKey);
                }
                result.add(memberKey);
            }
        }
        return result;
    }

    private static List<String> getSectionKeys(Member member, ConfigDefaultSections sectionsAnnot) {
        if(member instanceof Field){
            Field f = (Field)member;
            return getSectionKeys(f,sectionsAnnot);
        } else if(member instanceof Method){
            Method m = (Method)member;
            return getSectionKeys(m,sectionsAnnot);
        }
        return Collections.emptyList();
    }

    private static List<String> getSectionKeys(Method method, ConfigDefaultSections sectionAnnot) {
        List<String> areaKeys = new ArrayList<>();
        if (sectionAnnot != null && sectionAnnot.value().length>0) {
            // Remove original entry, since it will be replaced with prefixed entries
            areaKeys.addAll(Arrays.asList(sectionAnnot.value()));
        }else{
            areaKeys.add(method.getDeclaringClass().getName());
            areaKeys.add(method.getDeclaringClass().getSimpleName());
        }
        return areaKeys;
    }

    private static List<String> getSectionKeys(Field field, ConfigDefaultSections sectionAnnot) {
        List<String> areaKeys = new ArrayList<>();
        if (sectionAnnot != null && sectionAnnot.value().length>0) {
            // Remove original entry, since it will be replaced with prefixed entries
            areaKeys.addAll(Arrays.asList(sectionAnnot.value()));
        }else{
            areaKeys.add(field.getDeclaringClass().getName());
            areaKeys.add(field.getDeclaringClass().getSimpleName());
        }
        return areaKeys;
    }

}
