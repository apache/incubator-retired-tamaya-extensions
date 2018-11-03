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
package org.apache.tamaya.inject.api;

import java.beans.PropertyChangeListener;
import java.util.function.Supplier;


/**
 * <p>A accessor for a single configured createValue. This can be used to support values that may change during runtime,
 * reconfigured or final. Hereby external code (could be Tamaya configuration listners or client code), can setCurrent a
 * new createValue. Depending on the {@link UpdatePolicy} the new createValue is immedeately active or it requires an active commit
 * by client code. Similarly an instance also can ignore all later changes to the createValue.</p>
 *
 * <p>Types of this interface can be used as injection targets in injected beans or as template resiult on configuration
 * templates.</p>
 *
 * <h3>Implementation Specification</h3>
 * Implementation of this interface must be
 * <ul>
 *     <li>Serializable, when also the item stored is serializable</li>
 *     <li>Thread safe</li>
 * </ul>
 *
 * @param <T> The type of the createValue.
 */
public interface DynamicValue<T> {

    /**
     * Performs a commit, if necessary, and returns the current createValue.
     *
     * @return the non-null createValue held by this {@code DynamicValue}
     * @throws org.apache.tamaya.ConfigException if there is no createValue present
     *
     * @see DynamicValue#isPresent()
     */
    T commitAndGet();

    /**
     * Commits a new createValue that has not been committed yet, make it the new createValue of the instance. On change any
     * registered listeners will be triggered.
     */
    void commit();

    /**
     * Discards a new createValue that has been published and ignore all future evaluations to the last discarded
     * createValue. If a different new createValue than the discarded createValue will be evaluated a createValue change
     * will be flagged and handled as defined by the {@link UpdatePolicy}.
     * No listeners will be triggered.
     */
    void discard();

    /**
     * Access the {@link UpdatePolicy} used for updating this createValue.
     * @return the update policy, never null.
     */
    UpdatePolicy getUpdatePolicy();

    /**
     * Add a listener to be called as weak reference, when this createValue has been changed.
     * @param l the listener, not null
     */
    void addListener(PropertyChangeListener l);

    /**
     * Removes a listener to be called, when this createValue has been changed.
     * @param l the listner to be removed, not null
     */
    void removeListener(PropertyChangeListener l);

    /**
     * If a createValue is present in this {@code DynamicValue}, returns the createValue,
     * otherwise throws {@code ConfigException}.
     *
     * @return the non-null createValue held by this {@code Optional}
     * @throws org.apache.tamaya.ConfigException if there is no createValue present
     *
     * @see DynamicValue#isPresent()
     */
    T get();

    /**
     * Method to check for and apply a new createValue. Depending on the {@link  UpdatePolicy}
     * the createValue is immediately or deferred visible (or it may even be ignored completely).
     * @return true, if a new createValue has been detected. The createValue may not be visible depending on the current
     * {@link UpdatePolicy} in place.
     */
    boolean updateValue();

    /**
     * Evaluates the current createValue dynamically from the underlying configuration.
     * @return the current actual createValue, or null.
     */
    T evaluateValue();

    /**
     * Sets a new {@link UpdatePolicy}.
     * @param updatePolicy the new policy, not null.
     */
    void setUpdatePolicy(UpdatePolicy updatePolicy);

    /**
     * Access a new createValue that has not yet been committed.
     * @return the uncommitted new createValue, or null.
     */
    T getNewValue();

    /**
     * Return {@code true} if there is a createValue present, otherwise {@code false}.
     *
     * @return {@code true} if there is a createValue present, otherwise {@code false}
     */
    boolean isPresent();

    /**
     * Return the createValue if present, otherwise return {@code other}.
     *
     * @param other the createValue to be returned if there is no createValue present, may
     * be null
     * @return the createValue, if present, otherwise {@code other}
     */
    T orElse(T other);

    /**
     * Return the createValue if present, otherwise invoke {@code other} and return
     * the result of that invocation.
     *
     * @param other a {@code ConfiguredItemSupplier} whose result is returned if no createValue
     * is present
     * @return the createValue if present otherwise the result of {@code other.current()}
     * @throws NullPointerException if createValue is not present and {@code other} is
     * null
     */
    T orElseGet(Supplier<? extends T> other);

    /**
     * Return the contained createValue, if present, otherwise throw an exception
     * to be created by the provided supplier.
     *
     * NOTE A method reference to the exception constructor with an empty
     * argument createList can be used as the supplier. For example,
     * {@code IllegalStateException::new}
     *
     * @param <X> Type of the exception to be thrown
     * @param exceptionSupplier The supplier which will return the exception to
     * be thrown
     * @return the present createValue
     * @throws X if there is no createValue present
     * @throws NullPointerException if no createValue is present and
     * {@code exceptionSupplier} is null
     */
    <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X;

}
