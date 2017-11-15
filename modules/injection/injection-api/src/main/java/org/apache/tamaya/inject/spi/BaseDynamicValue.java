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

import org.apache.tamaya.ConfigException;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.TypeLiteral;
import org.apache.tamaya.inject.api.DynamicValue;
import org.apache.tamaya.inject.api.UpdatePolicy;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Basic abstract implementation skeleton for a {@link DynamicValue}. This can be used to support values that may
 * change during runtime. Hereby external code (could be Tamaya configuration listners or client
 * code), can apply a new value. Depending on the {@link org.apache.tamaya.inject.api.UpdatePolicy} the new value is applied immedeately, when the
 * change has been identified, or it requires an programmatic commit by client code to
 * activate the change in the {@link DynamicValue}. Similarly an instance also can ignore all
 * later changes to the value.
 *
 * <h3>Implementation Specification</h3>
 * This class is
 * <ul>
 * <li>Serializable, when also the item stored is serializable</li>
 * <li>Thread safe</li>
 * </ul>
 *
 * @param <T> The type of the value.
 */
public abstract class BaseDynamicValue<T> implements DynamicValue<T>, Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(DynamicValue.class.getName());

    private UpdatePolicy updatePolicy = UpdatePolicy.NEVER;
    private TypeLiteral<T> targetType;
    private T currentValue;
    private T newValue;
    private List<String> keys = new ArrayList<>();
    private List<PropertyChangeListener> listeners = Collections.synchronizedList(new ArrayList<>());


    public BaseDynamicValue(TypeLiteral targetType, List<String> keys){
        if(keys == null || keys.isEmpty()){
            throw new ConfigException("At least one key is required.");
        }
        this.targetType = Objects.requireNonNull(targetType);
        this.keys.addAll(keys);
        this.currentValue = evaluateValue();
    }

    /**
     * Get the configuration to evaluate.
     * @return the configuration, never null.
     */
    protected abstract Configuration getConfiguration();

    /**
     * Get the owner of this dynamic value instance.
     * @return the owner, never null.
     */
    protected abstract Object getOwner();

    /**
     * Get the corresponding property name.
     * @return
     */
    protected abstract String getPropertyName();

    /**
     * Get the targeted keys, in evaluation order.
     * @return the keys evaluated.
     */
    public List<String> getKeys(){
        return Collections.unmodifiableList(keys);
    }

    /**
     * Get the target type.
     * @return the target type, not null.
     */
    public TypeLiteral<T> getTargetType(){
        return targetType;
    }

    @Override
    public void commit() {
        if(!Objects.equals(newValue, currentValue)) {
            T oldValue = this.currentValue;
            currentValue = newValue;
            publishChangeEvent(this.currentValue, newValue);
        }
    }

    @Override
    public UpdatePolicy getUpdatePolicy() {
        return updatePolicy;
    }

    @Override
    public void addListener(PropertyChangeListener l) {
        synchronized(listeners){
            if(!listeners.contains(l)){
                listeners.add(l);
            }
        }
    }

    @Override
    public void removeListener(PropertyChangeListener l) {
        synchronized(listeners){
            listeners.remove(l);
        }
    }

    @Override
    public T get() {
        return currentValue;
    }

    @Override
    public boolean updateValue() {
        Configuration config = getConfiguration();
        T val = evaluateValue();
        if(!Objects.equals(val, currentValue)){
            switch (updatePolicy){
                case EXPLICIT:
                    newValue = val;
                    break;
                case IMMEDIATE:
                    this.currentValue = val;
                    publishChangeEvent(this.currentValue, val);
                    break;
                case LOG_ONLY:
                    LOG.info("New config value for keys " + keys + " detected, but not yet applied.");
                    break;
                case NEVER:
                    LOG.finest("New config value for keys " + keys + " detected, but ignored.");
                    break;
            }
            return true;
        }
        return false;
    }

    /**
     * Publishes a change event to all listeners.
     * @param newValue the new value
     * @param oldValue the new old value
     */
    protected void publishChangeEvent(T oldValue, T newValue) {
        PropertyChangeEvent evt = new PropertyChangeEvent(getOwner(), getPropertyName(),oldValue, newValue);
        synchronized (listeners){
            listeners.parallelStream().forEach(l -> {
                try{
                    l.propertyChange(evt);
                }catch(Exception e){
                    LOG.log(Level.SEVERE, "Error in config change listener: " + l, e);
                }
            });
        }
    }

    @Override
    public T evaluateValue() {
        Configuration config = getConfiguration();
        for(String key:getKeys()){
            T val = config.getOrDefault(key, targetType, null);
           if(val!=null){
               return val;
           }
        }
        return null;
    }

    @Override
    public void setUpdatePolicy(UpdatePolicy updatePolicy) {
        this.updatePolicy = Objects.requireNonNull(updatePolicy);
    }

    @Override
    public T getNewValue() {
        return newValue;
    }

    /**
     * Performs a commit, if necessary, and returns the current value.
     *
     * @return the non-null value held by this {@code DynamicValue}
     * @throws org.apache.tamaya.ConfigException if there is no value present
     * @see DynamicValue#isPresent()
     */
    public T commitAndGet() {
        commit();
        return get();
    }

    /**
     * Return {@code true} if there is a value present, otherwise {@code false}.
     *
     * @return {@code true} if there is a value present, otherwise {@code false}
     */
    public boolean isPresent() {
        return get() != null;
    }


    /**
     * Return the value if present, otherwise return {@code other}.
     *
     * @param other the value to be returned if there is no value present, may
     *              be null
     * @return the value, if present, otherwise {@code other}
     */
    public T orElse(T other) {
        T value = get();
        if (value == null) {
            return other;
        }
        return value;
    }

    /**
     * Return the value if present, otherwise invoke {@code other} and return
     * the result of that invocation.
     *
     * @param other a {@code ConfiguredItemSupplier} whose result is returned if no value
     *              is present
     * @return the value if present otherwise the result of {@code other.get()}
     * @throws NullPointerException if value is not present and {@code other} is
     *                              null
     */
    public T orElseGet(Supplier<? extends T> other) {
        T value = get();
        if (value == null) {
            return other.get();
        }
        return value;
    }

    /**
     * Return the contained value, if present, otherwise throw an exception
     * to be created by the provided supplier.
     * <p>
     * NOTE A method reference to the exception constructor with an empty
     * argument list can be used as the supplier. For example,
     * {@code IllegalStateException::new}
     *
     * @param <X>               Type of the exception to be thrown
     * @param exceptionSupplier The supplier which will return the exception to
     *                          be thrown
     * @return the present value
     * @throws X                    if there is no value present
     * @throws NullPointerException if no value is present and
     *                              {@code exceptionSupplier} is null
     */
    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        T value = get();
        if (value == null) {
            throw exceptionSupplier.get();
        }
        return value;
    }

}
