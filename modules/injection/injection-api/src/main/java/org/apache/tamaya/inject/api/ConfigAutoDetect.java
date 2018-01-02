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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adding this annotation tells the Tamaya injection system to inject all
 * fields found, also including fields not annotated with {@code @Config}.
 * The configuration keys to be used for resolution are basically determined
 * by the {@link Config} annotation(s). If missing the keys are evaluated in the
 * following order:
 * <ul>
 *     <li>packagename.simpleClassname.fieldName</li>
 *     <li>simpleClassname.fieldName</li>
 *     <li>fieldName</li>
 * </ul>
 * Fields not to be injected can be annotated with {@code @NoConfig} to exclude
 * them being elected for injection.
 * @see Config
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.TYPE })
public @interface ConfigAutoDetect {

    /**
     * Flag that tells the injection system if a {@link IllegalStateException} should
     * be thrown when a property cannot be resolved. Default is {@code false}.
     * @return {@code false} if no exception is thrown on unresolvable properties, {@code true} otherwise.
     */
    boolean failIfMissing() default false;
}
