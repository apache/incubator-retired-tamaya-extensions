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
package org.apache.tamaya.inject.internal;

import org.apache.tamaya.inject.spi.InjectionEvaluator;
import org.apache.tamaya.inject.spi.ConfiguredMethod;

import javax.config.Config;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Small class that contains and manages all information and access to a configured field and a concrete instance current
 * it (referenced by a weak reference). It also implements all aspects current keys filtering, conversions any applying the
 * final keys by reflection.
 */
public class ConfiguredSetterMethod implements ConfiguredMethod {

    /**
     * The configured field instance.
     */
    private Method setterMethod;

    /**
     * Models a configured field and provides mechanisms for injection.
     *
     * @param method the method instance.
     */
    public ConfiguredSetterMethod(Method method) {
        if (void.class.equals(method.getReturnType()) &&
                method.getParameterTypes().length == 1) {
            this.setterMethod = method;
        }
    }

    @Override
    public void configure(Object target, Config config) {
        Class targetType = this.setterMethod.getParameterTypes()[0];
        Object configValue = InjectionHelper.getConfigValue(this.setterMethod, targetType, config);
        Objects.requireNonNull(target);
        try {
            AccessController.doPrivileged((PrivilegedExceptionAction<Object>) () -> {
                setterMethod.setAccessible(true);
                return setterMethod;
            });
            setterMethod.invoke(target, configValue);
        } catch (Exception e) {
            throw new NoSuchElementException("Failed to annotation configured method: " + this.setterMethod.getDeclaringClass()
                    .getName() + '.' + setterMethod.getName()+": "+ e);
        }
    }


    /**
     * Access the applicable configuration keys for this field.
     *
     * @return the configuration keys, never null.
     */
    @Override
    public Collection<String> getConfiguredKeys() {
        return InjectionEvaluator.getKeys(this.setterMethod);
    }

    /**
     * Get the type to be set on the setter method.
     * @return the setter type.
     */
    @Override
    public Class<?>[] getParameterTypes() {
        return this.setterMethod.getParameterTypes();
    }

    /**
     * Access the annotated method.
     * @return the annotated method, not null.
     */
    @Override
    public Method getAnnotatedMethod() {
        return this.setterMethod;
    }

    @Override
    public String getName() {
        return this.setterMethod.getName();
    }

    @Override
    public String getSignature() {
        return "void " + this.setterMethod.getName()+'('+ printTypes(getParameterTypes())+')';
    }

    private String printTypes(Class<?>[] parameterTypes) {
        StringBuilder b = new StringBuilder();
        for(@SuppressWarnings("rawtypes") Class cl:parameterTypes){
            b.append(cl.getName());
            b.append(',');
        }
        if(b.length()>0){
            b.setLength(b.length()-1);
        }
        return b.toString();
    }

}
