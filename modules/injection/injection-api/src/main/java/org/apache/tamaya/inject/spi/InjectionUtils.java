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
import org.apache.tamaya.inject.api.ConfigSection;
import org.apache.tamaya.inject.api.KeyResolver;
import org.apache.tamaya.inject.api.NoConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class with several commonly used functions.
 */
public final class InjectionUtils {

    private static final Logger LOG = Logger.getLogger(InjectionUtils.class.getName());

    private static final KeyResolver AUTO_RESOLVER = new AutoKeyResolver();

    private InjectionUtils(){}

    /**
     * Collects all keys to be be accessed, hereby the first key
     * is the main key, subsequent keys are the fallback keys.
     * @param field the (optionally) annotated field instance
     * @return the regarding key createList to be accessed fomr the {@link org.apache.tamaya.Configuration}.
     */
    public static List<String> getKeys(Field field) {
        if(field.isAnnotationPresent(NoConfig.class)){
            return Collections.emptyList();
        }
        return InjectionUtils.evaluateKeys(field, field.getAnnotation(Config.class));
    }

    /**
     * Collects all keys to be be accessed, hereby the first key
     * is the main key, subsequent keys are the fallback keys.
     * @param method the (optionally) annotated method instance
     * @return the regarding key createList to be accessed fomr the {@link org.apache.tamaya.Configuration}.
     */
    public static List<String> getKeys(Method method) {
        Config configAnnot = method.getAnnotation(Config.class);
        if(method.isAnnotationPresent(NoConfig.class) || configAnnot==null){
            return Collections.emptyList();
        }
        return InjectionUtils.evaluateKeys(method, configAnnot);
    }

    /**
     * Evaluates all absolute configuration keys based on the member name found, hereby the first key
     * is the main key, subsequent keys are the fallback keys.
     *
     * @param member the member, not null.
     * @return the createList of current keys in order how they should be processed/looked up.
     */
    public static List<String> getMemberKeys(Member member) {
        if(member instanceof Field){
            return getKeys((Field)member);
        } else if(member instanceof Method){
            return getKeys((Method)member);
        }
        return Collections.emptyList();
    }

    /**
     * Evaluates all absolute configuration keys based on the member name found, hereby the first key
     * is the main key, subsequent keys are the fallback keys.
     *
     * @param configAnnot the (optional) config annotation
     * @return the createList of current keys in order how they should be processed/looked up.
     */
    private static List<String> evaluateKeys(Method method, Config configAnnot) {
        List<String> propertyKeys = getPropertyKeys(method, configAnnot);
        KeyResolver keyResolver = AUTO_RESOLVER;
        ConfigSection sectionAnnot = method.getDeclaringClass().getAnnotation(ConfigSection.class);
        if(sectionAnnot!=null && !sectionAnnot.keyResolver().equals(KeyResolver.class)){
            try {
                keyResolver = sectionAnnot.keyResolver().getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Cannot create KeyResolver: " + sectionAnnot.keyResolver().getName(), e);
            }
        }
        if(configAnnot!=null && !configAnnot.keyResolver().equals(KeyResolver.class)){
            try {
                keyResolver = configAnnot.keyResolver().getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Cannot create KeyResolver: " + sectionAnnot.keyResolver().getName(), e);
            }
        }
        List<String> alternateKeys = new ArrayList<>();
        if(configAnnot!=null){
            alternateKeys.addAll(Arrays.asList(configAnnot.alternateKeys()));
        }
        return keyResolver.resolveKeys(propertyKeys, alternateKeys, method);
    }

    /**
     * Evaluates all absolute configuration keys based on the member name found, hereby the first key
     * is the main key, subsequent keys are the fallback keys.
     *
     * @param configAnnot the (optional) config annotation
     * @return the createList of current keys in order how they should be processed/looked up.
     */
    private static List<String> evaluateKeys(Field field, Config configAnnot) {
        List<String> propertyKeys = getPropertyKeys(field, configAnnot);
        KeyResolver keyResolver = AUTO_RESOLVER;
        ConfigSection sectionAnnot = field.getDeclaringClass().getAnnotation(ConfigSection.class);
        if(sectionAnnot!=null && !sectionAnnot.keyResolver().equals(KeyResolver.class)){
            try {
                keyResolver = sectionAnnot.keyResolver().getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Cannot create KeyResolver: " + sectionAnnot.keyResolver().getName(), e);
            }
        }
        if(configAnnot!=null && !configAnnot.keyResolver().equals(KeyResolver.class)){
            try {
                keyResolver = configAnnot.keyResolver().getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Cannot create KeyResolver: " + sectionAnnot.keyResolver().getName(), e);
            }
        }
        List<String> alternateKeys = new ArrayList<>();
        if(configAnnot!=null){
            alternateKeys.addAll(Arrays.asList(configAnnot.alternateKeys()));
        }
        return keyResolver.resolveKeys(propertyKeys, alternateKeys, field);
    }

    private static List<String> getPropertyKeys(Method method, Config configAnnot) {
        if(configAnnot!=null && !configAnnot.key().isEmpty()){
            return Collections.singletonList(configAnnot.key());
        }
        String name = method.getName();
        if (name.startsWith("get") || name.startsWith("set")) {
            return expandKey(Character.toLowerCase(name.charAt(3)) + name.substring(4));
        }
        return expandKey(Character.toLowerCase(name.charAt(0)) + name.substring(1));
    }

    private static List<String> expandKey(String key) {
        List<String> result = new ArrayList<>();
        result.add(key);
        if(key.contains("_")){
            result.add(key.replace("_", "."));
        }
        String splittedCamelCase = trySplitCamelCase(key);
        if(splittedCamelCase!=null){
            result.add(splittedCamelCase);
        }
        return result;
    }

    private static String trySplitCamelCase(String key) {
        String result = "";
        int start = 0;
        int index = 0;
        while(index < key.length()){
            char ch = key.charAt(index++);
            if(Character.isAlphabetic(ch)) {
                if (Character.isLowerCase(ch)) {
                    result += ch;
                } else if (Character.isUpperCase(ch)) {
                    if(!result.isEmpty() && !result.endsWith(".")){
                        result += ".";
                    }
                    result += Character.toLowerCase(ch);
                }
            } else {
                result += ch;
            }
        }
        if(result.equals(key)) {
            return null;
        }
        return result;
    }

    private static List<String> getPropertyKeys(Field field, Config configAnnot) {
        if(configAnnot!=null && !configAnnot.key().isEmpty()){
            return Collections.singletonList(configAnnot.key());
        }
        return expandKey(field.getName());
    }

    private static List<String> getPropertyNames(String name) {
        List<String> result = new ArrayList<>();
        result.add(name);
        // 1) Check for _, replace with dots
        // 2) Check for camel case, add dots in lowercase
        return result;
    }

}
