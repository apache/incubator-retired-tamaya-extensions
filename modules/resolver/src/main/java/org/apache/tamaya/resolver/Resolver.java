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
package org.apache.tamaya.resolver;

import org.apache.tamaya.ConfigException;
import org.apache.tamaya.resolver.spi.ExpressionEvaluator;
import org.apache.tamaya.resolver.spi.ExpressionResolver;
import org.apache.tamaya.spi.PropertyValue;
import org.apache.tamaya.spi.ServiceContextManager;

import java.util.Collection;
import java.util.Objects;

/**
 * Resolver singleton.
 */
public final class Resolver {

    private final ClassLoader classLoader;


    /**
     * Get the current mutable config provider for the default classloader.
     * @return the corresponding provider, not null.
     * @see ServiceContextManager#getDefaultClassLoader()
     */
    public static Resolver getInstance(){
        return getInstance(ServiceContextManager.getDefaultClassLoader());
    }

    /**
     * Get the current mutable config provider for the given classloader.
     * @param classLoader the target classloader, not null.
     * @return the corresponding provider, not null.
     */
    public static Resolver getInstance(ClassLoader classLoader){
        return ServiceContextManager.getServiceContext().getService(Resolver.class,
                () -> new Resolver(classLoader));
    }

    /**
     * Singleton constructor.
     */
    private Resolver(ClassLoader classLoader){
        this.classLoader = Objects.requireNonNull(classLoader);
    }

    /**
     * Evaluates the current expression using the current thread's context classloader.
     * @param key the key, not null.
     * @param value the createValue to be filtered/evaluated.
     * @return the filtered/evaluated createValue, including null.
     * @deprecated Use {@link #evaluateExpression(String, boolean)}
     */
    @Deprecated
    public String evaluateExpression(String key, String value){
        PropertyValue val = evaluator(this.classLoader).evaluateExpression(PropertyValue.createValue(key, value), true);
        if(val==null){
            return null;
        }
        return val.getValue();
    }

    /**
     * Evaluates the current expression.
     * @param key the key, not null.
     * @param value the createValue to be filtered/evaluated.
     * @param classLoader the classloader to be used, not null.
     * @return the filtered/evaluated createValue, including null.
     * @deprecated Use {@link #evaluateExpression(String, ClassLoader, boolean)}
     */
    @Deprecated
    public String evaluateExpression(String key, String value, ClassLoader classLoader){
        return evaluateExpression(value, classLoader, true);
    }

    /**
     * Evaluates the current expression.
     * @param value the createValue to be filtered/evaluated.
     * @param classLoader the classloader to be used, not null.
     * @return the filtered/evaluated createValue, including null.
     */
    public String evaluateExpression(String value, ClassLoader classLoader, boolean maskUnresolved){
        PropertyValue val = evaluator(classLoader).evaluateExpression(PropertyValue.createValue("", value), maskUnresolved);
        if(val==null){
            return null;
        }
        return val.getValue();
    }

    /**
     * Get the evaluator.
     * @return the evaluator, never null.
     * @throws ConfigException if the evaluator cannot be evaluated.
     */
    private ExpressionEvaluator evaluator(ClassLoader classLoader) {
        ExpressionEvaluator evaluator = ServiceContextManager.getServiceContext(classLoader)
                .getService(ExpressionEvaluator.class);
        if(evaluator==null){
            throw new ConfigException("No ExpressionEvaluator registered.");
        }
        return evaluator;
    }

    /**
     * Evaluates the current expression using the current thread's context classloader.
     * @param value the createValue to be filtered/evaluated.
     * @return the filtered/evaluated createValue, including null.
     */
    public String evaluateExpression(String value){
        return evaluateExpression(value, true);
    }

    /**
     * Evaluates the current expression using the current thread's context classloader.
     * @param value the createValue to be filtered/evaluated.
     * @param maskNotFound if true, not found expression parts will be replaced vy surrounding with [].
     *                     Setting to false will replace the createValue with an empty String.
     * @return the filtered/evaluated createValue, including null.
     */
    public String evaluateExpression(String value, boolean maskNotFound){
        PropertyValue val = evaluator(this.classLoader).evaluateExpression(PropertyValue.createValue("", value), maskNotFound);
        if(val==null){
            return null;
        }
        return val.getValue();
    }

    /**
     * Evaluates the current expression using the current thread's context classloader.
     * @param value the value to be filtered/evaluated.
     * @param classLoader the target classloader, not null.
     * @param maskNotFound if true, not found expression parts will be replaced vy surrounding with [].
     *                     Setting to false will replace the createValue with an empty String.
     * @return the filtered/evaluated createValue, including null.
     */
    public PropertyValue evaluateExpression(PropertyValue value, ClassLoader classLoader, boolean maskNotFound) {
        return evaluator(classLoader).evaluateExpression(value, maskNotFound);
    }

    /**
     * Evaluates the current expression using the current thread's context classloader.
     * @param value the value to be filtered/evaluated.
     * @param maskNotFound if true, not found expression parts will be replaced vy surrounding with [].
     *                     Setting to false will replace the createValue with an empty String.
     * @return the filtered/evaluated createValue, including null.
     */
    public PropertyValue evaluateExpression(PropertyValue value, boolean maskNotFound) {
        return evaluator(this.classLoader).evaluateExpression(value, maskNotFound);
    }

    /**
     * Access a collection with the currently registered {@link ExpressionResolver} instances.
     * @return the resolvers currently known, never null.
     * @deprecated will be removed.
     */
    @Deprecated
    public Collection<ExpressionResolver> getResolvers(){
        return evaluator(this.classLoader).getResolvers();
    }

    @Override
    public String toString() {
        return "Resolver{" +
                "classLoader=" + classLoader +
                '}';
    }


}
