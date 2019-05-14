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


import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Annotation to define injection of a configured property or define the configuration data
 * backing a configuration template method. Hereby this annotation can be used in multiple
 * ways and combined with other annotations such as {@link WithConfigOperator}, {@link WithPropertyConverter}.
 *
 * <h3>Simplest variant</h3>
 * Below the most simple variant of a configured class is given:
 * <pre>
 * package a.b;
 *
 * public class ConfiguredItem {
 *   &amp;Config
 *   private String aValue;
 * }
 * </pre>
 * Configuration resolution is implemented as follows:
 * <ul>
 *     <li>The current valid Configuration is evaluated by calling {@code Configuration cfg = Configuration.current();}</li>
 *     <li>The current possible property keys are evaluated by calling {@link org.apache.tamaya.inject.spi.InjectionUtils#getKeys(Field)}
 *     or {@link org.apache.tamaya.inject.spi.InjectionUtils#getKeys(Method)} . Hereby the key resolution is delegated
 *     to an instance of {@link KeyResolver}, which can be defined on the configured class with the {@link ConfigSection}
 *     or (overriding) on the configured field/method with the {@link Config} annotation. The default key resolver
 *     is {@link org.apache.tamaya.inject.spi.AutoKeyResolver}.</li>
 *     <li>Each key evaluated is looked up in the configuration, until a configuration value has been found.</li>
 *     <li>if not successful, {@link Config#defaultValue()} is used, if present.</li>
 *     <li>If no value could be evaluated a ({@link org.apache.tamaya.ConfigException} is thrown, unless {@link Config#required()}
 *     is setPropertyValue to {@code true} (default is {@code false}).</li>
 *     <li>If necessary, the final <i>raw</i> value is converted into the required type to be injected, using a
 *     {@link org.apache.tamaya.spi.PropertyConverter}, then the value is injected.</li>
 * </ul>
 *
 * <h3>Explicit annotations</h3>
 * In the next example we explicitly define the configuration keys to be used:
 * <pre>
 * &amp;ConfigSection("section1")
 * public class ConfiguredItem {
 *
 *   &amp;Config(key = {"b"}, alternateKeys={"[a.b.deprecated.keys]", "a"}, defaultValue = "myDefaultValue")
 *   private String aValue;
 * }
 * </pre>
 *
 * Within this example we evaluate multiple possible keys: {@code section1.b, a.b.deprecated.keys, section1.a}.
 * Evaluation is aborted if a key is resolved successfully. Hereby the ordering of the annotation values
 * define the ordering of resolution. If no value can be found, the configured default {@code myDefaultValue} is
 * injected.
 *
 * <h3>Using explicit field annotation only</h3>
 * In the last example we explicitly define the configuration keys but omit the section part, letting the default
 * section names to be taken:
 * <pre>
 * package a.b;
 *
 * public class ConfiguredItem {
 *
 *   &amp;Config(key = {"b"}, alternateKeys={"[a.b.deprecated.keys]", "a"}, defaultValue = "myDefaultValue")
 *   private String aValue;
 * }
 * </pre>
 *
 * Key resolution is similar to above, but now the default section resolution allies, resulting in the keys
 * {@code ConfiguredItem.b, a.b.deprecated.keys, ConfiguredItem.a} being looked up.
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
public @interface Config {

    /** Value that is setCurrent by default as default, so it is possible to use empty Strings as default values. */
    String UNCONFIGURED_VALUE = "org.apache.tamaya.config.configproperty.unconfigureddvalue";

    /**
     * Defines the main configuration property key to be used. The final target property is evaluated based on
     * the {@link #keyResolver()} strategy, by default {@link org.apache.tamaya.inject.spi.AutoKeyResolver}.
     *
     * @return the main property key, not null. If empty, the field or property name (of a setter method) being injected
     * is used by default.
     */
    @Nonbinding
    String key() default "";

    /**
     *  Allows to customize the key resolution strategy how the {@link #key()} and {@link #alternateKeys()}values
     *  should be used to evaluate the final main target configuration keys. Hereby the default resolution
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
     * Note that on field or method injection without any {@link Config} annotation multiple main keys are generated, e.g.:
     * <ul>
     *     <li>a field named {@code a_b_property} evaluates to {@code a_b_property, a.b.property}}</li>
     *     <li>a field named {@code aProperty} evaluates to {@code aProperty, a.property}}</li>
     * </ul>
     *
     * @return the key resolution strategy, never null.
     */
    @Nonbinding
    Class<? extends KeyResolver> keyResolver() default KeyResolver.class;

    /**
     * Defines the alternate configuration property keys to be used, if no value could be evaluated using the main
     * {@link #key()}.
     *
     * @return the property keys, not null.
     */
    @Nonbinding
    String[] alternateKeys() default {};

    /**
     * The default createValue to be injected, if none of the configuration keys could be resolved. If no key has been
     * resolved and no default createValue is defined, it is, by default, handled as a deployment error. Depending on the
     * extension loaded default values can be fixed Strings or even themselves resolvable. For typed configuration of
     * type T entries that are not Strings the default createValue must be a valid input to a corresponding
     * {@link org.apache.tamaya.spi.PropertyConverter}.
     * 
     * @return default createValue used in case resolution fails.
     */
    @Nonbinding
    String defaultValue() default UNCONFIGURED_VALUE;

    /**
     * Flag that defines if a configuration property is required. If a required
     * property is missing, a {@link org.apache.tamaya.ConfigException} is raised.
     * Default is {@code true}.
     * @return the flag createValue.
     */
    @Nonbinding
    boolean required() default true;
}
