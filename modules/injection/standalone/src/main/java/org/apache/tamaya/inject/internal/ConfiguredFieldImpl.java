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

import org.apache.tamaya.inject.api.DynamicValue;
import org.apache.tamaya.inject.spi.InjectionEvaluator;
import org.apache.tamaya.inject.spi.ConfiguredField;

import javax.config.Config;
import javax.config.ConfigProvider;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Small class that contains and manages all information and access to a configured field and a concrete instance current
 * it (referenced by a weak reference). It also implements all aspects current keys filtering, converting and applying the
 * final keys by reflection.
 */
public class ConfiguredFieldImpl implements ConfiguredField{
    /**
     * The configured field instance.
     */
    protected final Field annotatedField;

    /**
     * Models a configured field and provides mechanisms for injection.
     *
     * @param field the field instance.
     */
    public ConfiguredFieldImpl(Field field) {
        Objects.requireNonNull(field);
        this.annotatedField = field;
    }


    /**
     * Evaluate the initial keys from the configuration and apply changes to the field.
     *
     * @param target the target instance.
     * @throws java.util.NoSuchElementException if evaluation or conversion failed.
     */
    public void configure(Object target, Config config) {
        if (this.annotatedField.getType() == DynamicValue.class) {
            applyDynamicValue(target);
        } else {
            applyValue(target, config, false);
        }
    }


    /**
     * This method instantiates and assigns a dynamic value.
     *
     * @param target the target instance, not null.
     * @throws NoSuchElementException if the configuration required could not be resolved or converted.
     */
    private void applyDynamicValue(Object target) {
        Objects.requireNonNull(target);
        try {
            AccessController.doPrivileged((PrivilegedExceptionAction<Object>) () -> {
                annotatedField.setAccessible(true);
                annotatedField.set(target,
                        DefaultDynamicValue.of(target, annotatedField, ConfigProvider.getConfig()));
                return annotatedField;
            });
        } catch (Exception e) {
            throw new NoSuchElementException("Failed to annotation configured field: " + this.annotatedField.getDeclaringClass()
                    .getName() + '.' + annotatedField.getName()+": " +  e);
        }
    }

    /**
     * This method applies a configuration to the field.
     *
     * @param target      the target instance, not null.
     * @param config The configuration to be used.
     * @param resolve     set to true, if expression resolution should be applied on the keys passed.
     * @throws NoSuchElementException if the configuration required could not be resolved or converted.
     */
    private void applyValue(Object target, Config config, boolean resolve) {
        Objects.requireNonNull(target);
        try {
            Class targetType = this.annotatedField.getType();
            Object configValue = InjectionHelper.getConfigValue(this.annotatedField, targetType, config);

            AccessController.doPrivileged((PrivilegedExceptionAction<Object>) () -> {
                annotatedField.setAccessible(true);
                if(configValue!=null) {
                    annotatedField.set(target, configValue);
                }
                return annotatedField;
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw new NoSuchElementException("Failed to evaluate annotated field: " + this.annotatedField.getDeclaringClass()
                    .getName() + '.' + annotatedField.getName()+": "+ e);
        }
    }

    /**
     * Get the field's type.
     * @return the field's type, not null.
     */
    @Override
    public Class<?> getType(){
        return this.annotatedField.getType();
    }

    /**
     * Access the applicable configuration keys for this field.
     * @return the configuration keys, never null.
     */
    @Override
    public Collection<String> getConfiguredKeys(){
        return InjectionEvaluator.getKeys(this.annotatedField);
    }

    @Override
    public String toString() {
        return "ConfiguredField[" + getSignature() + ']';
    }

    @Override
    public String getName() {
        return annotatedField.getName();
    }

    @Override
    public String getSignature() {
        return getName()+':'+getType().getName();
    }

    @Override
    public Field getAnnotatedField() {
        return annotatedField;
    }
}
