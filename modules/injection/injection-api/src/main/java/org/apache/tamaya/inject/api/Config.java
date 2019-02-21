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
 *     <li>The current possible property keys are evaluated by calling {@code cfg.current("a.b.ConfigureItem.aValue");},
 *     {@code cfg.current("ConfigureItem.aValue");}, {@code cfg.current("aValue");}</li>
 *     <li>if not successful, and since no @ConfigDefault annotation is present, the configured default createValue is used.
 *     <li>If no createValue could be evaluated a ({@link org.apache.tamaya.ConfigException} is thrown.</li>
 *     <li>On success, since no type conversion is involved, the createValue is injected.</li>
 * </ul>
 *
 * <h3>Explicit annotations</h3>
 * In the next example we explicitly define the configuration keys to be used:
 * <pre>
 * &amp;ConfigDefaultSections("section1")
 * public class ConfiguredItem {
 *
 *   &amp;Config(createValue = {"b", "[a.b.deprecated.keys]", "a"}, defaultValue = "myDefaultValue")
 *   private String aValue;
 * }
 * </pre>
 *
 * Within this example we evaluate multiple possible keys: {@code section1.b, a.b.deprecated.keys, section1.a}.
 * Evaluation is aborted if a key is resolved successfully. Hereby the ordering of the annotation values
 * define the ordering of resolution. If no createValue can be found, the configured default {@code myDefaultValue} is
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
 *   &amp;Config(createValue = {"b", "[a.b.deprecated.keys]", "a"}, defaultValue = "myDefaultValue")
 *   private String aValue;
 * }
 * </pre>
 *
 * Key resolution is similar to above, but now the default package names are used, resulting in
 * {@code a.b.ConfiguredItem.b, ConfiguredItem.b, a.b.deprecated.keys, a.b.ConfiguredItem.a, ConfiguredItem.a}
 * being evaluated.
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
public @interface Config {

    /** Value that is setCurrent by default as default, so it is possible to use empty Strings as default values. */
    String UNCONFIGURED_VALUE = "org.apache.tamaya.config.configproperty.unconfigureddvalue";

    /**
     * Defines the main configuration property key to be used. The final target property is evaluated based on
     * the {@link #keyResolver()} strategy, by default {@link KeyResolution#AUTO}.
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
     * @return the key resolution strategy, never null.
     */
    @Nonbinding
    Class<? extends KeyResolver> keyResolver() default KeyResolver.class;

    /**
     * Defines the alternate configuration property keys to be used, if no value could be evaluated using the main
     * {@link #key()}. All key values given are resolved using the {@link KeyResolution#ABSOLUTE} strategy.
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
