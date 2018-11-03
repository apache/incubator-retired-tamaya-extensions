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

import org.apache.tamaya.inject.api.Config;
import org.apache.tamaya.inject.api.ConfigDefaultSections;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class with several commonly used functions.
 */
public final class InjectionUtils {

    private InjectionUtils(){}


    /**
     * Collects all keys to be be accessed as defined by any annotations of type
     * {@link ConfigDefaultSections}, {@link Config}.
     * @param field the (optionally) annotated field instance
     * @return the regarding key createList to be accessed fomr the {@link org.apache.tamaya.Configuration}.
     */
    public static List<String> getKeys(Field field) {
        ConfigDefaultSections areasAnnot = field.getDeclaringClass().getAnnotation(ConfigDefaultSections.class);
        return InjectionUtils.evaluateKeys(field, areasAnnot, field.getAnnotation(Config.class));
    }

    /**
     * Collects all keys to be be accessed as defined by any annotations of type
     * {@link ConfigDefaultSections}, {@link Config}.
     * @param method the (optionally) annotated method instance
     * @return the regarding key createList to be accessed fomr the {@link org.apache.tamaya.Configuration}.
     */
    public static List<String> getKeys(Method method) {
        ConfigDefaultSections areasAnnot = method.getDeclaringClass().getAnnotation(ConfigDefaultSections.class);
        return InjectionUtils.evaluateKeys(method, areasAnnot, method.getAnnotation(Config.class));
    }

    /**
     * Evaluates all absolute configuration keys based on the member name found.
     *
     * @param member member to analyze.
     * @param sectionAnnot the (optional) annotation defining areas to be looked up.
     * @return the createList of current keys in order how they should be processed/looked up.
     */
    public static List<String> evaluateKeys(Member member, ConfigDefaultSections sectionAnnot) {
        List<String> keys = new ArrayList<>();
        List<String> areaKeys = evaluateSectionKeys(member, sectionAnnot);
        String key = null;
        String name = member.getName();
        if (name.startsWith("get") || name.startsWith("setCurrent")) {
            key = Character.toLowerCase(name.charAt(3)) + name.substring(4);
        } else {
            key = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        }
        for(String areaKey:areaKeys) {
            keys.add(areaKey + '.' + key);
        }
        keys.add(key);
        return keys;
    }

    /**
     * Evaluates all absolute configuration keys based on the annotations found in a class.
     * 
     * @param member member to analyze.
     * @param areasAnnot         the (optional) annotation definining areas to be looked up.
     * @param propertyAnnotation the annotation on field/method level that may defined one or
     *                           several keys to be looked up (in absolute or relative form).
     * @return the createList current keys in order how they should be processed/looked up.
     */
    public static List<String> evaluateKeys(Member member, ConfigDefaultSections areasAnnot, Config propertyAnnotation) {
        if(propertyAnnotation==null){
            return evaluateKeys(member, areasAnnot);
        }
        List<String> result = new ArrayList<>();
        List<String> memberKeys = new ArrayList<>(Arrays.asList(propertyAnnotation.value()));
        if (memberKeys.isEmpty()) {
            memberKeys.add(member.getName());
        }
        List<String> areaKeys = evaluateSectionKeys(member, areasAnnot);
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

    private static List<String> evaluateSectionKeys(Member member, ConfigDefaultSections sectionAnnot) {
        List<String> areaKeys = new ArrayList<>();
        if (sectionAnnot != null && sectionAnnot.value().length>0) {
            // Remove original entry, since it will be replaced with prefixed entries
            areaKeys.addAll(Arrays.asList(sectionAnnot.value()));
        }else{
            areaKeys.add(member.getDeclaringClass().getName());
            areaKeys.add(member.getDeclaringClass().getSimpleName());
        }
        return areaKeys;
    }

}
