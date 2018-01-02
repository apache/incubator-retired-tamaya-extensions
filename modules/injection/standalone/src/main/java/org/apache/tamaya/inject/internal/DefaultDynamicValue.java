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
import org.apache.tamaya.inject.api.LoadPolicy;
import org.apache.tamaya.inject.api.UpdatePolicy;
import org.apache.tamaya.inject.api.WithConverter;
import org.apache.tamaya.inject.spi.BaseDynamicValue;

import javax.config.Config;
import javax.config.spi.Converter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

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

    private static final long serialVersionUID = -2071172847144537443L;

    /**
     * Back reference to the base configuration instance. This reference is used reevaluate the given property and
     * compare the result with the previous value after a configuration change was triggered.
     */
    private final Config configuration;
    /**
     * The property converter to be applied, may be null. In the ladder case targetType is not null.
     */
    private final Converter<T> propertyConverter;

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
    private DefaultDynamicValue(Object owner, String propertyName, Config configuration, Type targetType,
                                Converter<T> propertyConverter, List<String> keys, LoadPolicy loadPolicy,
                                UpdatePolicy updatePolicy) {
        super(owner, propertyName, targetType, keys);
        this.configuration = Objects.requireNonNull(configuration);
        this.propertyConverter = propertyConverter;
        this.loadPolicy = Objects.requireNonNull(loadPolicy);
        setUpdatePolicy(updatePolicy);
        if(loadPolicy == LoadPolicy.INITIAL){
            this.value = evaluateValue();
        }
    }

    public static DynamicValue<?> of(Object owner, Field annotatedField, Config configuration) {
        return of(owner, annotatedField, configuration, LoadPolicy.ALWAYS, UpdatePolicy.IMMEDIATE);
    }

    public static DynamicValue<?> of(Object owner, Field annotatedField, Config configuration, LoadPolicy loadPolicy) {
        return of(owner, annotatedField, configuration, loadPolicy, UpdatePolicy.IMMEDIATE);
    }

    public static DynamicValue<?> of(Object owner, Field annotatedField, Config configuration, UpdatePolicy updatePolicy) {
        return of(owner, annotatedField, configuration, LoadPolicy.ALWAYS, updatePolicy);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static DynamicValue<?> of(Object owner, Field annotatedField, Config configuration, LoadPolicy loadPolicy, UpdatePolicy updatePolicy) {
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
                throw new IllegalArgumentException("Failed to instantiate annotated PropertyConverter on " +
                        annotatedField.getDeclaringClass().getName()
                        + '.' + annotatedField.getName(), e);
            }
        }
        List<String> keys = InjectionEvaluator.getKeys(annotatedField);
        return new DefaultDynamicValue(owner, annotatedField.getName(), configuration,
                targetType, propertyConverter, keys, loadPolicy, updatePolicy);
    }

    public static DynamicValue<?> of(Object owner, Method method, Config configuration) {
        return of(owner, method, configuration, LoadPolicy.ALWAYS, UpdatePolicy.IMMEDIATE);
    }

    public static DynamicValue<?> of(Object owner, Method method, Config configuration, UpdatePolicy updatePolicy) {
        return of(owner, method, configuration, LoadPolicy.ALWAYS, updatePolicy);
    }

    public static DynamicValue<?> of(Object owner, Method method, Config configuration, LoadPolicy loadPolicy) {
        return of(owner, method, configuration, loadPolicy, UpdatePolicy.IMMEDIATE);
    }

    @SuppressWarnings("unchecked")
	public static DynamicValue<?> of(Object owner, Method method, Config configuration, LoadPolicy loadPolicy, UpdatePolicy updatePolicy) {
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
                throw new IllegalArgumentException("Failed to instantiate annotated PropertyConverter on " +
                        method.getDeclaringClass().getName()
                        + '.' + method.getName(), e);
            }
        }
        return new DefaultDynamicValue(owner, method.getName(),
                configuration, targetType, propertyConverter, InjectionEvaluator.getKeys(method),
                loadPolicy, updatePolicy);
    }

    protected Converter getCustomConverter(){
        return this.propertyConverter;
    }

    @Override
    protected Config getConfiguration() {
        return configuration;
    }

    /**
     * If a value is present in this {@code DynamicValue}, returns the value,
     * otherwise throws {@code ConfigException}.
     *
     * @return the non-null value held by this {@code Optional}
     * @throws java.util.NoSuchElementException if there is no value present
     * @see DefaultDynamicValue#isPresent()
     */
    public T getValue() {
        return getOptionalValue()
                .orElseThrow(() -> new NoSuchElementException("No config value for: " + getKeys()));
    }

    /**
     * If a value is present in this {@code DynamicValue}, returns the value,
     * otherwise throws {@code ConfigException}.
     *
     * @return the non-null value held by this {@code Optional}
     * @throws java.util.NoSuchElementException if there is no value present
     * @see DefaultDynamicValue#isPresent()
     */
    public Optional<T> getOptionalValue() {
        T newLocalValue;
        if(loadPolicy!=LoadPolicy.INITIAL) {
            newLocalValue = evaluateValue();
            if (this.value == null) {
                this.value = newLocalValue;
            }
            if(!Objects.equals(this.value, newLocalValue)){
                switch (getUpdatePolicy()){
                    case IMMEDIATE:
                        commit();
                        break;
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
        return Optional.ofNullable(value);
    }

    /**
     * Method to check for and apply a new value. Depending on the {@link  UpdatePolicy}
     * the value is immediately or deferred visible (or it may even be ignored completely).
     *
     * @return true, if a new value has been detected. The value may not be visible depending on the current
     * {@link UpdatePolicy} in place.
     */
    public boolean updateValue() {
        if(this.value==null && this.newValue==null){
            this.value = evaluateValue();
            return false;
        }
        T newValue = evaluateValue();
        if (Objects.equals(newValue, this.value)) {
            return false;
        }
        switch (getUpdatePolicy()) {
            case LOG_ONLY:
                Logger.getLogger(getClass().getName()).info("Discard change on " + this + ", newValue=" + newValue);
                publishChangeEvent(value, newValue);
                this.newValue = null;
                break;
            case NEVER:
                this.newValue = null;
                break;
            case EXPLICIT:
                this.newValue = newValue;
                break;
            default:
                this.newValue = newValue;
                commit();
                break;
        }
        return true;
    }

    /**
     * Access a new value that has not yet been committed.
     *
     * @return the uncommitted new value, or null.
     */
    public T getNewValue() {
        @SuppressWarnings("unchecked")
		T nv = newValue==null?null:(T)newValue;
        return nv;
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
     */
    @SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        setUpdatePolicy((UpdatePolicy)ois.readObject());
        if (isPresent()) {
            this.value = (T) ois.readObject();
        }
        newValue = null;
    }

}
