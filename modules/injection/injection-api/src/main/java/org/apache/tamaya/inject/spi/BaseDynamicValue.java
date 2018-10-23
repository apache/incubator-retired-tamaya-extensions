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
import org.apache.tamaya.spi.PropertyConverter;
import org.apache.tamaya.spi.ConversionContext;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Consumer;
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
public abstract class BaseDynamicValue<T> implements DynamicValue<T> {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(DynamicValue.class.getName());

    /** The value owner used for PropertyChangeEvents. */
    private Object owner;
    /**
     * The property name of the entry.
     */
    private String propertyName;

    /**
     * Policy that defines how new values are applied, be default it is applied initially once, but never updated
     * anymore.
     */
    private UpdatePolicy updatePolicy = UpdatePolicy.NEVER;
    /** The targe type. */
    private TypeLiteral<T> targetType;
    /**
     * The current value, never null.
     */
    protected transient T value;
    /** The last discarded value. */
    protected transient T discarded;
    /** Any new value, not yet applied. */
    protected transient T newValue;
    /** The configured default value, before type conversion. */
    private String defaultValue;
    /** The list of candidate keys to be used. */
    private List<String> keys = new ArrayList<>();
    /** The registered listeners. */
    private WeakList<PropertyChangeListener> listeners = new WeakList<>();

    /**
     * Creates a new instance.
     * @param owner the owner, not null.
     * @param propertyName the property name, not null.
     * @param targetType the target type.
     * @param keys the candidate keys.
     */
    public BaseDynamicValue(Object owner, String propertyName, TypeLiteral targetType, List<String> keys){
        if(keys == null || keys.isEmpty()){
            throw new ConfigException("At least one key is required.");
        }
        this.owner = owner;
        this.propertyName = Objects.requireNonNull(propertyName);
        this.targetType = Objects.requireNonNull(targetType);
        this.keys.addAll(keys);
    }

    /**
     * Get the default value, used if no value could be evaluated.
     * @return the default value, or null.
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Set the default value to be used.
     * @param defaultValue the default value.
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Get the configuration to evaluate.
     * @return the configuration, never null.
     */
    protected abstract Configuration getConfiguration();

    /**
     * Get the corresponding property name.
     * @return the property name.
     */
    protected String getPropertyName(){
        return propertyName;
    }

    /**
     * Get the owner of this dynamic value instance.
     * @return the owner, never null.
     */
    protected Object getOwner(){
        return owner;
    }

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
        if(!Objects.equals(newValue, value)) {
            T oldValue = this.value;
            value = newValue;
            discarded = null;
            publishChangeEvent(this.value, newValue);
            newValue = null;
        }
    }

    @Override
    public void discard() {
        if(newValue!=null){
            discarded = newValue;
        }
        newValue = null;
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
        updateValue();
        return value;
    }

    @Override
    public boolean updateValue() {
        Configuration config = getConfiguration();
        T val = evaluateValue();
        if(value == null){
            value = val;
            return true;
        }else if(discarded!=null && discarded.equals(val)){
            // the evaluated value has been discarded and will be flagged out.
            return false;
        }else{
            // Reset discarded state for a new value.
            discarded = null;
        }
        if(!Objects.equals(val, value)){
            switch (updatePolicy){
                case EXPLICIT:
                    newValue = val;
                    break;
                case IMMEDIATE:
                    this.value = val;
                    publishChangeEvent(this.value, val);
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
            listeners.forEach(l -> {
                try{
                    l.propertyChange(evt);
                }catch(Exception e){
                    LOG.log(Level.SEVERE, "Error in config change listener: " + l, e);
                }
            });
        }
    }

    /**
     * Allows to customize type conversion if needed, e.g. based on some annotations defined.
     * @return the custom converter, which replaces the default converters, ot null.
     */
    protected PropertyConverter<T> getCustomConverter(){
        return null;
    }

    @Override
    public T evaluateValue() {
        T value = null;
        List<PropertyConverter<T>> converters = new ArrayList<>();
        if (this.getCustomConverter() != null) {
            converters.add(this.getCustomConverter());
        }
        converters.addAll(getConfiguration().getContext().getPropertyConverters(targetType));

        for (String key : keys) {
            ConversionContext ctx = new ConversionContext.Builder(key, targetType).build();
            String stringVal = getConfiguration().getOrDefault(key, String.class, null);
            if(stringVal!=null) {
                if(String.class.equals(targetType.getType())){
                    value = (T)stringVal;
                }
                for(PropertyConverter<T> conv:converters){
                    try{
                        value = conv.convert(stringVal);
                        if(value!=null){
                            break;
                        }
                    }catch(Exception e){
                        LOG.warning("failed to convert: " + ctx);
                    }
                }
            }
        }
        if(value == null && defaultValue!=null){
            ConversionContext ctx = new ConversionContext.Builder("<defaultValue>", targetType).build();
            try {
                ConversionContext.set(ctx);
                for (PropertyConverter<T> conv : converters) {
                    try {
                        value = conv.convert(defaultValue);
                        if (value != null) {
                            break;
                        }
                    } catch (Exception e) {
                        LOG.warning("failed to convert: " + ctx);
                    }
                }
            }finally{
                ConversionContext.reset();
            }
        }
        return value;
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
    @Override
    public T commitAndGet() {
        commit();
        return get();
    }

    /**
     * Return {@code true} if there is a value present, otherwise {@code false}.
     *
     * @return {@code true} if there is a value present, otherwise {@code false}
     */
    @Override
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
    @Override
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
     * @return the value if present otherwise the result of {@code other.current()}
     * @throws NullPointerException if value is not present and {@code other} is
     *                              null
     */
    @Override
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
    @Override
    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        T value = get();
        if (value == null) {
            throw exceptionSupplier.get();
        }
        return value;
    }



    /**
     * Simple helper that allows keeping the listeners registered as weak references, hereby avoiding any
     * memory leaks.
     *
     * @param <I> the type
     */
    private class WeakList<I> {
        final List<WeakReference<I>> refs = new LinkedList<>();

        boolean contains(I item){
            for(WeakReference<I> t:refs){
                if(item.equals(t.get())){
                    return true;
                }
            }
            return false;
        }

        void forEach(Consumer<I> consumer){
            refs.parallelStream().forEach(ref -> {
                I t = ref.get();
                if(t!=null){
                    consumer.accept(t);
                }
            });
        }

        /**
         * Adds a new instance.
         *
         * @param t the new instance, not null.
         */
        void add(I t) {
            refs.add(new WeakReference<>(t));
        }

        /**
         * Removes a instance.
         *
         * @param t the instance to be removed.
         */
        void remove(I t) {
            synchronized (refs) {
                for (Iterator<WeakReference<I>> iterator = refs.iterator(); iterator.hasNext(); ) {
                    WeakReference<I> ref = iterator.next();
                    I instance = ref.get();
                    if (instance == null || instance == t) {
                        iterator.remove();
                        break;
                    }
                }
            }
        }


        /**
         * Access a list (copy) of the current instances that were not discarded by the GC.
         *
         * @return the list of accessible items.
         */
        public List<I> get() {
            synchronized (refs) {
                List<I> res = new ArrayList<>();
                for (Iterator<WeakReference<I>> iterator = refs.iterator(); iterator.hasNext(); ) {
                    WeakReference<I> ref = iterator.next();
                    I instance = ref.get();
                    if (instance == null) {
                        iterator.remove();
                    } else {
                        res.add(instance);
                    }
                }
                return res;
            }
        }
    }

}
