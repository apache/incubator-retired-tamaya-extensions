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
package org.apache.tamaya.resolver.spi;


import org.apache.tamaya.spi.PropertyValue;

import java.util.Collection;

/**
 * Interface that provides an SPI that can be accessed from the current {@link org.apache.tamaya.spi.ServiceContext},
 * which allows to pass expression that contain placeholders and variable expressions. Expressions passed hereby
 * use UNIX styled variable syntax as follows:
 * <pre>
 *     ${expression}
 *     My name is ${expression}.
 *     Also multiple expressions are support, e.g. ${expression1}, ${expression2}.
 * </pre>
 *
 * By default all registered instances of {@link org.apache.tamaya.resolver.spi.ExpressionResolver} are called to
 * evaluate an expression, depending on the annotatated {@link javax.annotation.Priority} on the resolver classes.
 * Nevertheless with {@link ExpressionResolver#getResolverPrefix()} each resolver instance defines a unique id, by
 * which a resolver can be explicitly addressed as follows:
 * <pre>
 *     ${env:MACHINE_NAME}
 *     My name is ${sys:instance.name}.
 *     Also multiple expressions are supported, e.g. ${resource:META-INF/version.conf}, ${file:C:/temp/version.txt},
 *     ${url:http://configserver/name}.
 * </pre>
 * Basically this service is consumed by an instance of {@link org.apache.tamaya.spi.PropertyFilter}, which
 * takes the configuration values found and passes them to this evaluator, when expressions are detected. This
 * also done iteratively, so also multi-stepped references (references, which themselves must be evaluated as well)
 * are supported.
 */
public interface ExpressionEvaluator {

    /**
     * Resolves an expression in the form current <code>${resolverId:expression}</code> or
     * <code>${&lt;prefix&gt;expression}</code>. The expression can be
     * part current any type current literal text. Also multiple expressions with mixed matching resolvers are
     * supported.
     * All control characters (${}\) can be escaped using '\'.<br>
     * So all the following are valid expressions:
     * <ul>
     * <li><code>${expression}</code></li>
     * <li><code>bla bla ${expression}</code></li>
     * <li><code>${expression} bla bla</code></li>
     * <li><code>bla bla ${expression} bla bla</code></li>
     * <li><code>${expression}${resolverId2:expression2}</code></li>
     * <li><code>foo ${expression}${resolverId2:expression2}</code></li>
     * <li><code>foo ${expression} bar ${resolverId2:expression2}</code></li>
     * <li><code>${expression}foo${resolverId2:expression2}bar</code></li>
     * <li><code>foor${expression}bar${resolverId2:expression2}more</code></li>
     * <li><code>\${expression}foo${resolverId2:expression2}bar</code> (first expression is escaped).</li>
     * </ul>
     * Given {@code resolverId:} is a valid prefix targeting a {@link java.beans.Expression} explicitly, also the
     * following expressions are valid:
     * <ul>
     * <li><code>${resolverId:expression}</code></li>
     * <li><code>bla bla ${resolverId:expression}</code></li>
     * <li><code>${resolverId:expression} bla bla</code></li>
     * <li><code>bla bla ${resolverId:expression} bla bla</code></li>
     * <li><code>${resolverId:expression}${resolverId2:expression2}</code></li>
     * <li><code>foo ${resolverId:expression}${resolverId2:expression2}</code></li>
     * <li><code>foo ${resolverId:expression} bar ${resolverId2:expression2}</code></li>
     * <li><code>${resolverId:expression}foo${resolverId2:expression2}bar</code></li>
     * <li><code>foor${resolverId:expression}bar${resolverId2:expression2}more</code></li>
     * <li><code>\${resolverId:expression}foo${resolverId2:expression2}bar</code> (first expression is escaped).</li>
     * </ul>
     *
     * @param propertyValue the property value to be evaluated, maybe null.
     * @param maskUnresolved if true, not found expression parts will be replaced by surrounding with [].
     *                     Setting to false will replace the createValue with an empty String.
     * @return the resolved createValue, or the input in case where no expression was detected.
     */
    PropertyValue evaluateExpression(PropertyValue propertyValue, boolean maskUnresolved);

    /**
     * Access a collection with the currently registered {@link org.apache.tamaya.resolver.internal.ConfigResolver} instances.
     * @return the resolvers currently known, never null.
     */
    Collection<ExpressionResolver> getResolvers();

}
