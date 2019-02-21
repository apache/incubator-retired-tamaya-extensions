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
 * This annotation allows to define the default section names used for evaluating the candidate
 * keys used for evaluating configuration values. The configuration keys
 * used are additionally determined by the {@link org.apache.tamaya.inject.api.Config}
 * annotation(s). This annotation allows
 * to define the configuration section that is prefixed to all <b>relative</b> configuration keys.
 * @see Config
 * @see ConfigAutoInject
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.TYPE })
public @interface ConfigSection {

    /**
     * Allows to declare an section name that is prepended to resolve relative configuration keys.
     * @return the section name to be used for key resolution.
     */
    String value() default "";

    /**
     *  Allows to customize the <i>default</i> key resolution strategy how the {@code key()} and {@code alternateKeys()}
     *  values should be used to evaluate the final main target configuration keys. Hereby the default resolution
     *  works as follows:
     * <ol>
     *    <li>The containing class <b>does not</b> have a {@link ConfigSection} annotation and the field/method does not have
     *     a {@link Config} annotation: the main key equals to
     *     {@code Owning.class.getSimpleName() + '.' + propertyKey}.</li>
     *    <li>The containing class <b>does not</b> have a {@link ConfigSection} annotation: the main key equals to
     *     {@code propertyKey}.</li>
     *    <li>The containing class <b>does</b> have a {@link ConfigSection} annotation: the main key equals to
     *     {@code areaAnnotation.getValue() + '.' + propertyKey}.</li>
     * </ol>
     *
     * @return the key resolution strategy, never null.
     */
    Class<? extends KeyResolver> keyResolver() default KeyResolver.class;

}
