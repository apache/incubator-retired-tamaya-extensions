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
package org.apache.tamaya.integration.spring;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation to enable Tamaya as a configuration backend for Spring.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(TamayaSpringConfigurator.class)
public @interface EnableTamayaConfig {

    /**
     * If setPropertyValue to true, Tamaya will only extend the current Spring configuration, instead of overriding it (default).
     * @return true to extend only the default Spring configuration.
     */
    boolean extendOnly() default false;

    /**
     * If setPropertyValue to true, Tamaya configuration injection will be disabled. Spring injection mechanisms are never
     * touched by this feature. Default is false.
     * @return true to switch off Tamaya injection in your Spring application.
     */
    boolean disableTamayaInjection() default false;
}
