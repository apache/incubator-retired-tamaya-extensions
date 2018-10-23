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
import org.apache.tamaya.spi.ServiceContext;
import org.apache.tamaya.spi.ServiceContextManager;

import java.util.Collection;

/**
 * Resolver singleton.
 */
public final class Resolver {

    /**
     * Singleton constructor.
     */
    private Resolver(){}

    /**
     * Evaluates the current expression using the current thread's context classloader.
     * @param key the key, not null.
     * @param value the value to be filtered/evaluated.
     * @return the filtered/evaluated value, including null.
     */
    public static String evaluateExpression(String key, String value){
        return evaluator(Thread.currentThread().getContextClassLoader()).evaluateExpression(key, value, true);
    }

    /**
     * Evaluates the current expression.
     * @param key the key, not null.
     * @param value the value to be filtered/evaluated.
     * @param classLoader the classloader to be used, not null.
     * @return the filtered/evaluated value, including null.
     */
    public static String evaluateExpression(String key, String value, ClassLoader classLoader){
        return evaluator(classLoader).evaluateExpression(key, value, true);
    }

    /**
     * Get the evaluator.
     * @param classLoader the classloader to be used, not null.
     * @return the evaluator, never null.
     * @throws ConfigException if the evaluator cannot be evaluated.
     */
    private static ExpressionEvaluator evaluator(ClassLoader classLoader) {
        ExpressionEvaluator evaluator = ServiceContextManager.getServiceContext(classLoader)
                .getService(ExpressionEvaluator.class);
        if(evaluator==null){
            throw new ConfigException("No ExpressionEvaluator registered.");
        }
        return evaluator;
    }

    /**
     * Evaluates the current expression using the current thread's context classloader.
     * @param value the value to be filtered/evaluated.
     * @return the filtered/evaluated value, including null.
     */
    public static String evaluateExpression(String value){

        return evaluateExpression(value, true, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Evaluates the current expression.
     * @param value the value to be filtered/evaluated.
     * @param classLoader the classloader to be used, not null.
     * @return the filtered/evaluated value, including null.
     */
    public static String evaluateExpression(String value, ClassLoader classLoader){
        return evaluateExpression(value, true, classLoader);
    }

    /**
     * Evaluates the current expression using the current thread's context classloader.
     * @param value the value to be filtered/evaluated.
     * @param maskNotFound if true, not found expression parts will be replaced vy surrounding with [].
     *                     Setting to false will replace the value with an empty String.
     * @return the filtered/evaluated value, including null.
     */
    public static String evaluateExpression(String value, boolean maskNotFound){
        return evaluateExpression(value, maskNotFound, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Evaluates the current expression.
     * @param value the value to be filtered/evaluated.
     * @param maskNotFound if true, not found expression parts will be replaced vy surrounding with [].
     *                     Setting to false will replace the value with an empty String.
     * @param classLoader the classloader to be used, not null.
     * @return the filtered/evaluated value, including null.
     */
    public static String evaluateExpression(String value, boolean maskNotFound, ClassLoader classLoader){
        return evaluator(classLoader).evaluateExpression(null, value, maskNotFound);
    }

    /**
     * Access a collection with the currently registered {@link ExpressionResolver} instances.
     * @return the resolvers currently known, never null.
     * @deprecated will be removed.
     */
    @Deprecated
    public static Collection<ExpressionResolver> getResolvers(){
        return evaluator(Thread.currentThread().getContextClassLoader()).getResolvers();
    }
}
