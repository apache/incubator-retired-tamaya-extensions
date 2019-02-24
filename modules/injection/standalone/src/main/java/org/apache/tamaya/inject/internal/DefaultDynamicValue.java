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

import org.apache.tamaya.ConfigException;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.TypeLiteral;
import org.apache.tamaya.inject.api.DynamicValue;
import org.apache.tamaya.inject.spi.InjectionUtils;
import org.apache.tamaya.inject.api.LoadPolicy;
import org.apache.tamaya.inject.api.UpdatePolicy;
import org.apache.tamaya.inject.api.WithPropertyConverter;
import org.apache.tamaya.inject.spi.BaseDynamicValue;
import org.apache.tamaya.spi.PropertyConverter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * A accessor for a single configured createValue. This can be used to support values that may change during runtime,
 * reconfigured or final. Hereby external code (could be Tamaya configuration listeners or client code), can setCurrent a
 * new createValue. Depending on the {@link UpdatePolicy} the new createValue is immediately active or it requires an active commit
 * by client code. Similarly an instance also can ignore all later changes to the createValue.
 * <h3>Implementation Details</h3>
 * This class is
 * <ul>
 * <li>Serializable, when also the item stored is serializable</li>
 * <li>Thread safe</li>
 * </ul>
 *
 * @param <T> The type of the createValue.
 */
final class DefaultDynamicValue<T> extends BaseDynamicValue<T> {

    private static final long serialVersionUID = -2071172847144537443L;

    /**
     * The property converter to be applied, may be null. In the ladder case targetType is not null.
     */
    private final PropertyConverter<T> propertyConverter;

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
     * @param propertyConverter the optional converter to be used.
     */
    private DefaultDynamicValue(Object owner, String propertyName, Configuration configuration, TypeLiteral<T> targetType,
                                PropertyConverter<T> propertyConverter, List<String> keys, LoadPolicy loadPolicy,
                                UpdatePolicy updatePolicy) {
        super(owner, propertyName, targetType, keys, configuration);
        this.propertyConverter = propertyConverter;
        this.loadPolicy = Objects.requireNonNull(loadPolicy);
        setUpdatePolicy(updatePolicy);
        if (loadPolicy == LoadPolicy.INITIAL) {
            this.value = evaluateValue();
        }
    }

    public static DynamicValue<?> of(Object owner, Field annotatedField, Configuration configuration) {
        return of(owner, annotatedField, configuration, LoadPolicy.ALWAYS, UpdatePolicy.IMMEDIATE);
    }

    public static DynamicValue<?> of(Object owner, Field annotatedField, Configuration configuration, LoadPolicy loadPolicy) {
        return of(owner, annotatedField, configuration, loadPolicy, UpdatePolicy.IMMEDIATE);
    }

    public static DynamicValue<?> of(Object owner, Field annotatedField, Configuration configuration, UpdatePolicy updatePolicy) {
        return of(owner, annotatedField, configuration, LoadPolicy.ALWAYS, updatePolicy);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static DynamicValue<?> of(Object owner, Field annotatedField, Configuration configuration,
                                     LoadPolicy loadPolicy, UpdatePolicy updatePolicy) {
        // Check for adapter/filter
        Type targetType = annotatedField.getGenericType();
        if (targetType == null) {
            throw new ConfigException("Failed to evaluate target type for " + annotatedField.getDeclaringClass().getName()
                    + '.' + annotatedField.getName());
        }
        if (targetType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) targetType;
            Type[] types = pt.getActualTypeArguments();
            if (types.length != 1) {
                throw new ConfigException("Failed to evaluate target type for " + annotatedField.getDeclaringClass().getName()
                        + '.' + annotatedField.getName());
            }
            targetType = types[0];
        }
        PropertyConverter<?> propertyConverter = null;
        WithPropertyConverter annot = annotatedField.getAnnotation(WithPropertyConverter.class);
        if (annot != null) {
            try {
                propertyConverter = annot.value().getConstructor().newInstance();
            } catch (Exception e) {
                throw new ConfigException("Failed to instantiate annotated PropertyConverter on " +
                        annotatedField.getDeclaringClass().getName()
                        + '.' + annotatedField.getName(), e);
            }
        }
        List<String> keys = InjectionUtils.getKeys(annotatedField);
        return new DefaultDynamicValue(owner, annotatedField.getName(), configuration,
                TypeLiteral.of(targetType), propertyConverter, keys, loadPolicy, updatePolicy);
    }

    public static DynamicValue<?> of(Object owner, Method method, Configuration configuration) {
        return of(owner, method, configuration, LoadPolicy.ALWAYS, UpdatePolicy.IMMEDIATE);
    }

    public static DynamicValue<?> of(Object owner, Method method, Configuration configuration, UpdatePolicy updatePolicy) {
        return of(owner, method, configuration, LoadPolicy.ALWAYS, updatePolicy);
    }

    public static DynamicValue<?> of(Object owner, Method method, Configuration configuration, LoadPolicy loadPolicy) {
        return of(owner, method, configuration, loadPolicy, UpdatePolicy.IMMEDIATE);
    }

    @SuppressWarnings("unchecked")
    public static DynamicValue<?> of(Object owner, Method method, Configuration configuration,
                                     LoadPolicy loadPolicy, UpdatePolicy updatePolicy) {
        // Check for adapter/filter
        Type targetType = method.getGenericReturnType();
        if (targetType == null) {
            throw new ConfigException("Failed to evaluate target type for " + method.getDeclaringClass()
                    .getName() + '.' + method.getName());
        }
        if (targetType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) targetType;
            Type[] types = pt.getActualTypeArguments();
            if (types.length != 1) {
                throw new ConfigException("Failed to evaluate target type for " + method.getDeclaringClass()
                        .getName() + '.' + method.getName());
            }
            targetType = types[0];
        }
        PropertyConverter<Object> propertyConverter = null;
        WithPropertyConverter annot = method.getAnnotation(WithPropertyConverter.class);
        if (annot != null) {
            try {
                propertyConverter = (PropertyConverter<Object>) annot.value().getConstructor().newInstance();
            } catch (Exception e) {
                throw new ConfigException("Failed to instantiate annotated PropertyConverter on " +
                        method.getDeclaringClass().getName()
                        + '.' + method.getName(), e);
            }
        }
        return new DefaultDynamicValue<>(owner, method.getName(),
                configuration, TypeLiteral.of(targetType), propertyConverter, InjectionUtils.getKeys(method),
                loadPolicy, updatePolicy);
    }

    protected PropertyConverter getCustomConverter() {
        return this.propertyConverter;
    }

    /**
     * If a createValue is present in this {@code DynamicValue}, returns the createValue,
     * otherwise throws {@code ConfigException}.
     *
     * @return the non-null createValue held by this {@code Optional}
     * @throws ConfigException if there is no createValue present
     * @see DefaultDynamicValue#isPresent()
     */
    public T get() {
        T newLocalValue;
        if (loadPolicy != LoadPolicy.INITIAL) {
            newLocalValue = evaluateValue();
            if (this.value == null) {
                this.value = newLocalValue;
            }
            if (!Objects.equals(this.value, newLocalValue)) {
                switch (getUpdatePolicy()) {
                    case IMMEDEATE:
                    case IMMEDIATE:
                        commit();
                        break;
                    case EXPLCIT:
                    case EXPLICIT:
                        this.newValue = newLocalValue;
                        break;
                    case LOG_ONLY:
                        publishChangeEvent(this.value, newLocalValue);
                        this.newValue = null;
                        break;
                    case NEVER:
                        this.newValue = null;
                        break;
                    default:
                        this.newValue = null;
                        break;
                }
            }
        }
        return value;
    }

    /**
     * Method to check for and apply a new createValue. Depending on the {@link  UpdatePolicy}
     * the createValue is immediately or deferred visible (or it may even be ignored completely).
     *
     * @return true, if a new createValue has been detected. The createValue may not be visible depending on the current
     * {@link UpdatePolicy} in place.
     */
    public boolean updateValue() {
        if (this.value == null && this.newValue == null) {
            this.value = evaluateValue();
            return false;
        }
        T newValue = evaluateValue();
        if (Objects.equals(newValue, this.value)) {
            return false;
        }
        switch (getUpdatePolicy()) {
            case LOG_ONLY:
                Logger.getLogger(getClass().getName()).info("Discard change on " + this + ", createValue=" + newValue);
                publishChangeEvent(value, newValue);
                this.newValue = null;
                break;
            case NEVER:
                this.newValue = null;
                break;
            case EXPLCIT:
            case IMMEDEATE:
            default:
                this.newValue = newValue;
                commit();
                break;
        }
        return true;
    }

    /**
     * Access a new createValue that has not yet been committed.
     *
     * @return the uncommitted new createValue, or null.
     */
    public T getNewValue() {
        return newValue;
    }

    /**
     * Serialization implementation that strips away the non serializable Optional part.
     *
     * @param oos the output stream
     * @throws IOException if serialization fails.
     */
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.writeObject(getUpdatePolicy());
        oos.writeObject(get());
    }

    /**
     * Reads an instance from the input stream.
     *
     * @param ois the object input stream
     * @throws IOException            if deserialization fails.
     * @throws ClassNotFoundException
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        setUpdatePolicy((UpdatePolicy) ois.readObject());
        if (isPresent()) {
            this.value = (T) ois.readObject();
        }
        newValue = null;
    }

}
