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
package org.apache.tamaya.cdi;

import org.apache.tamaya.inject.api.DynamicValue;
import org.apache.tamaya.inject.api.LoadPolicy;
import org.apache.tamaya.inject.api.UpdatePolicy;
import org.apache.tamaya.inject.api.WithConverter;
import org.apache.tamaya.inject.spi.BaseDynamicValue;
import org.apache.tamaya.inject.spi.InjectionEvaluator;

import javax.config.Config;
import javax.config.spi.Converter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * A accessor for a single configured value. This can be used to support values that may change during runtime,
 * reconfigured or final. Hereby external code (could be Tamaya configuration listeners or client code), can set a
 * new value. Depending on the {@link UpdatePolicy} the new value is immediately active or it requires an active commit
 * by client code. Similarly an instance also can ignore all later changes to the value.
 * <h3>Implementation Details</h3>
 * This class is
 * <ul>
 * <li>Serializable, when also the item stored is serializable</li>
 * <li>Thread safe</li>
 * </ul>
 *
 * @param <T> The type of the value.
 */
final class DefaultDynamicValue<T> extends BaseDynamicValue<T> {

    /**
     * Back reference to the base configuration instance. This reference is used reevalaute the given property and
     * compare the result with the previous value after a configuration change was triggered.
     */
    private final Config configuration;

    /**
     * The property converter to be applied, may be null. In the ladder case targetType is not null.
     */
    private final Converter<T> customConverter;
    /**
     * Load policy.
     */
    private final LoadPolicy loadPolicy;

    /**
     * Constructor.
     *
     * @param propertyName      the name of the fields' property/method.
     * @param keys              the keys of the property, not null.
     * @param configuration     the configuration, not null.
     * @param targetType        the target type, not null.
     * @param customConverter the optional converter to be used.
     */
    private DefaultDynamicValue(Object owner, String propertyName, Config configuration, Type targetType,
                                Converter<T> customConverter, List<String> keys, LoadPolicy loadPolicy,
                                UpdatePolicy updatePolicy) {
        super(owner, propertyName, targetType, keys);
        this.configuration = Objects.requireNonNull(configuration);
        this.customConverter = customConverter;
        this.loadPolicy = Objects.requireNonNull(loadPolicy);
        setUpdatePolicy(updatePolicy);
        if(loadPolicy == LoadPolicy.INITIAL){
            updateValue();
        }
    }

    public static DynamicValue of(Object owner, Field annotatedField, Config configuration) {
        return of(owner, annotatedField, configuration, LoadPolicy.ALWAYS, UpdatePolicy.IMMEDIATE);
    }

    public static DynamicValue of(Object owner, Field annotatedField, Config configuration, LoadPolicy loadPolicy) {
        return of(owner, annotatedField, configuration, loadPolicy, UpdatePolicy.IMMEDIATE);
    }

    public static DynamicValue of(Object owner, Field annotatedField, Config configuration, UpdatePolicy updatePolicy) {
        return of(owner, annotatedField, configuration, LoadPolicy.ALWAYS, updatePolicy);
    }

    public static DynamicValue of(Object owner, Field annotatedField, Config configuration, LoadPolicy loadPolicy,
                                  UpdatePolicy updatePolicy) {
        // Check for adapter/filter
        Type targetType = annotatedField.getGenericType();
        if (targetType == null) {
            throw new IllegalArgumentException("Failed to evaluate target type for " + annotatedField.getDeclaringClass().getName()
                    + '.' + annotatedField.getName());
        }
        if (targetType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) targetType;
            Type[] types = pt.getActualTypeArguments();
            if (types.length != 1) {
                throw new IllegalArgumentException("Failed to evaluate target type for " + annotatedField.getDeclaringClass().getName()
                        + '.' + annotatedField.getName());
            }
            targetType = types[0];
        }
        Converter<?> propertyConverter = null;
        WithConverter annot = annotatedField.getAnnotation(WithConverter.class);
        if (annot != null) {
            try {
                propertyConverter = annot.value().newInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to instantiate annotated Converter on " +
                        annotatedField.getDeclaringClass().getName()
                        + '.' + annotatedField.getName(), e);
            }
        }
        List<String> keys = InjectionEvaluator.getKeys(annotatedField);
        return new DefaultDynamicValue(owner, annotatedField.getName(), configuration,
                targetType, propertyConverter, keys, loadPolicy, updatePolicy);
    }

    public static DynamicValue of(Object owner, Method method, Config configuration) {
        return of(owner, method, configuration, LoadPolicy.ALWAYS, UpdatePolicy.IMMEDIATE);
    }

    public static DynamicValue of(Object owner, Method method, Config configuration, UpdatePolicy updatePolicy) {
        return of(owner, method, configuration, LoadPolicy.ALWAYS, updatePolicy);
    }

    public static DynamicValue of(Object owner, Method method, Config configuration, LoadPolicy loadPolicy) {
        return of(owner, method, configuration, loadPolicy, UpdatePolicy.IMMEDIATE);
    }

    public static DynamicValue of(Object owner, Method method, Config configuration, LoadPolicy loadPolicy, UpdatePolicy updatePolicy) {
        // Check for adapter/filter
        Type targetType = method.getGenericReturnType();
        if (targetType == null) {
            throw new IllegalArgumentException("Failed to evaluate target type for " + method.getDeclaringClass()
                    .getName() + '.' + method.getName());
        }
        if (targetType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) targetType;
            Type[] types = pt.getActualTypeArguments();
            if (types.length != 1) {
                throw new IllegalArgumentException("Failed to evaluate target type for " + method.getDeclaringClass()
                        .getName() + '.' + method.getName());
            }
            targetType = types[0];
        }
        Converter<Object> propertyConverter = null;
        WithConverter annot = method.getAnnotation(WithConverter.class);
        if (annot != null) {
            try {
                propertyConverter = (Converter<Object>) annot.value().newInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to instantiate annotated Converter on " +
                        method.getDeclaringClass().getName()
                        + '.' + method.getName(), e);
            }
        }
        return new DefaultDynamicValue<>(owner, method.getName(),
                configuration, targetType, propertyConverter, InjectionEvaluator.getKeys(method),
                loadPolicy, updatePolicy);
    }


    @Override
    protected Config getConfiguration() {
        return configuration;
    }


    @Override
    protected Converter<T> getCustomConverter() {
        return customConverter;
    }

    @Override
    public String toString() {
        return "DefaultDynamicValue{" +
                "configuration=" + configuration +
                ", customConverter=" + customConverter +
                ", loadPolicy=" + loadPolicy +
                ", value=" + value +
                ", newValue=" + newValue +
                ", defaultValue=" + getDefaultValue() +
                ", discarded=" + discarded +
                ", keys=" + getKeys() +
                ", updatePolicy=" + getUpdatePolicy() +
                '}';
    }
}
