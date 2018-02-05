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
package org.apache.tamaya.inject.internal;

import java.lang.reflect.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tamaya.base.ServiceContextManager;
import org.apache.tamaya.base.convert.ConversionContext;
import org.apache.tamaya.events.ConfigEventManager;
import org.apache.tamaya.events.spi.BaseConfigEvent;
import org.apache.tamaya.inject.spi.InjectionEvaluator;
import org.apache.tamaya.inject.api.WithConverter;
import org.apache.tamaya.inject.spi.ConfiguredType;
import org.apache.tamaya.resolver.spi.ExpressionEvaluator;
import org.apache.tamaya.base.ConfigContext;
import org.apache.tamaya.base.ConfigContextSupplier;

import javax.config.Config;
import javax.config.inject.ConfigProperty;
import javax.config.spi.Converter;


/**
 * Utility class containing several aspects used in this module.
 */
@SuppressWarnings("unchecked")
final class InjectionHelper {

    private static final Logger LOG = Logger.getLogger(InjectionHelper.class.getName());

    private static final boolean RESOLUTION_MODULE_LOADED = checkResolutionModuleLoaded();

    private static final boolean EVENTS_AVAILABLE = checkForEvents();

    private static boolean checkForEvents() {
        try{
            Class.forName("org.apache.tamaya.events.FrozenConfig");
            LOG.info("Detected tamaya-events is loaded, will trigger ConfigEvents...");
            return true;
        } catch(Exception e){
            LOG.info("Detected tamaya-events not found, will not trigger any ConfigEvents...");
            return false;
        }
    }

    private static boolean checkResolutionModuleLoaded() {
        try {
            Class.forName("org.apache.tamaya.resolver.internal.DefaultExpressionEvaluator");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private InjectionHelper() {
    }

    /**
     * Internally evaluated the current valid configuration keys based on the given annotations present.
     * @param method the method, not null.
     * @param config the config, not null.
     * @return the keys to be returned, or null.
     */
    public static <T> T getConfigValue(Method method, Class<T> type, javax.config.Config config) {
        return getConfigValue(method, type, null, config);
    }

    /**
     * Internally evaluated the current valid configuration keys based on the given annotations present.
     * @param field the field, not null.
     * @param config the config, not null.
     * @return the keys to be returned, or null.
     */
    public static <T> T getConfigValue(Field field, Class<T> type, javax.config.Config config) {
        return getConfigValue(field, type, null, config);
    }


    /**
     * Internally evaluated the current valid configuration keys based on the given annotations present.
     * @param member the member, not null.
     * @param retKey the array to return the key found, or null.
     * @return the keys to be returned, or null.
     */
    private static <T> T getConfigValue(AccessibleObject member, Class<T> targetType,
                                        String[] retKey,
                                        javax.config.Config config) {
        Objects.requireNonNull(targetType);
        targetType = unboxType(targetType);
        List<String> keys = InjectionEvaluator.getKeys(member);

        WithConverter converterAnnot = member.getAnnotation(WithConverter.class);
        if(converterAnnot!=null && !converterAnnot.value().getName().equals(WithConverter.class.getName())){
            return getCustomConvertedConfigValue(member, converterAnnot, targetType, keys, config);
        }

        Optional<T> result = null;
        for(String key:keys) {
            result = config.getOptionalValue(key, targetType);
            if (result.isPresent()) {
                if (retKey != null) {
                    retKey[0] = key;
                }
                return result.get();
            }
        }
        ConfigProperty prop = member.getAnnotation(ConfigProperty.class);
        if(prop!=null && !prop.defaultValue().equals(ConfigProperty.UNCONFIGURED_VALUE)){
            String textValue = prop.defaultValue();
            // How tp convert the default value in a portable way?
            if(config instanceof ConfigContextSupplier){
                ConfigContext ctx = ((ConfigContextSupplier)config).getConfigContext();
                for(Converter converter:ctx.getConverters(targetType)){
                    try{
                        Object o = converter.convert(textValue);
                        if(o!=null){
                            return (T)o;
                        }
                    }catch(Exception e){
                        LOG.log(Level.SEVERE, "Failed to convert using Converter on " +
                                converter.getClass().getName(), e);
                    }
                }
                if(String.class.equals(targetType) || CharSequence.class.equals(targetType)){
                    return (T)textValue;
                }
                throw new IllegalArgumentException("Non convertible value: " + textValue + ", target: " + targetType.getName());
            }
        }
        return null;
    }

    private static Class unboxType(Class targetType) {
        switch(targetType.getName()){
            case "byte":
                return Byte.class;
            case "char":
                return Character.class;
            case "boolean":
                return Boolean.class;
            case "int":
                return Integer.class;
            case "short":
                return Short.class;
            case "long":
                return Long.class;
            case "float":
                return Float.class;
            case "double":
                return Double.class;
            default:
                return targetType;

        }
    }

    public static <T> T getCustomConvertedConfigValue(AccessibleObject element, WithConverter converterAnnot,
                                   Class<T> targetType, List<String> keys, Config config) {
        // Check for adapter/filter
        Class<? extends Converter<T>> converterType = (Class<? extends Converter<T>>) converterAnnot.value();
        if (!converterType.getName().equals(WithConverter.class.getName())) {
            Converter<T> converter = null;
            try {
                converter = Converter.class.cast(converterType.newInstance());
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to instantiate converter: " + converterType.getName(), e);
            }
            for (String key : keys) {
                try {
                    ConversionContext ctx = new ConversionContext.Builder(key, targetType)
                            .setAnnotatedElement(element).build();
                    ConversionContext.setContext(ctx);
                    Optional<String> textValue = config.getOptionalValue(key, String.class);
                    if (textValue.isPresent()) {
                        T adaptedValue = converter.convert(textValue.get());
                        if (adaptedValue != null) {
                            return adaptedValue;
                        }
                    }
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "Failed to convert using explicit PropertyConverter on " + element, e);
                } finally {
                    ConversionContext.reset();
                }
            }
        }
        return null;
    }

    /**
     * Method that allows to statically check, if the resolver module is loaded. If the module is loaded
     * value expressions are automatically forwarded to the resolver module for resolution.
     *
     * @return true, if the resolver module is on the classpath.
     */
    public static boolean isResolutionModuleLoaded() {
        return RESOLUTION_MODULE_LOADED;
    }

    /**
     * Evaluates the given expression.
     *
     * @param expression the expression, not null.
     * @return the evaluated expression.
     */
    public static String evaluateValue(String expression) {
        if (!RESOLUTION_MODULE_LOADED) {
            return expression;
        }
        ExpressionEvaluator evaluator = ServiceContextManager.getServiceContext().getService(ExpressionEvaluator.class);
        if (evaluator != null) {
            return evaluator.evaluateExpression("<injection>", expression, true);
        }
        return expression;
    }

    /**
     * This method distributes the configuration event, if the Tamaya event module is accessible.
     * When Tamaya events are not available, the call simply returns.
     * @param event the event to be distributed, not null.
     */
    static void sendConfigurationEvent(ConfiguredType event) {
        if(EVENTS_AVAILABLE){
            ConfigEventManager.fireEvent(new BaseConfigEvent<ConfiguredType>(event, ConfiguredType.class) {});
        }
    }
}
