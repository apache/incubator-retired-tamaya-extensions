/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tamaya.cdi;

import org.apache.tamaya.base.convert.ConversionContext;
import org.apache.tamaya.base.convert.ConverterManager;
import org.apache.tamaya.functions.Supplier;
import org.apache.tamaya.inject.api.ConfigDefaultSections;
import org.apache.tamaya.inject.api.DynamicValue;
import org.apache.tamaya.inject.api.WithConverter;
import org.apache.tamaya.spi.ConfigContextSupplier;
import org.apache.tamaya.spi.TypeLiteral;

import javax.config.Config;
import javax.config.ConfigProvider;
import javax.config.inject.ConfigProperty;
import javax.config.spi.ConfigBuilder;
import javax.config.spi.ConfigProviderResolver;
import javax.config.spi.Converter;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Provider;
import java.lang.reflect.*;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Producer bean for configuration properties.
 */
@ApplicationScoped
public class ConfigProducer {

    private static final Logger LOGGER = Logger.getLogger(ConfigProducer.class.getName());

    private DynamicValue createDynamicValue(final InjectionPoint injectionPoint) {
        Member member = injectionPoint.getMember();
        if (member instanceof Field) {
            return DefaultDynamicValue.of(injectionPoint.getBean(), (Field) member, ConfigProvider.getConfig());
        } else if (member instanceof Method) {
            return DefaultDynamicValue.of(injectionPoint.getBean(), (Method) member, ConfigProvider.getConfig());
        }
        return null;
    }

    @Produces
    @ConfigProperty
    public Object resolveAndConvert(final InjectionPoint injectionPoint) {
        if (DynamicValue.class.equals(injectionPoint.getAnnotated().getBaseType())) {
            return createDynamicValue(injectionPoint);
        }
        final ConfigProperty annotation = injectionPoint.getAnnotated().getAnnotation(ConfigProperty.class);
        final ConfigDefaultSections typeAnnot = injectionPoint.getMember().getDeclaringClass().getAnnotation(ConfigDefaultSections.class);
        final List<String> keys = TamayaCDIInjectionExtension.evaluateKeys(injectionPoint.getMember().getName(),
                annotation != null ? new String[]{annotation.name()} : null,
                typeAnnot != null ? typeAnnot.value() : null);

        Converter customConverter = null;
        final WithConverter withConverterAnnot = injectionPoint.getAnnotated().getAnnotation(WithConverter.class);
        if (withConverterAnnot != null) {
            customConverter = TamayaCDIInjectionExtension.CUSTOM_CONVERTERS.get(withConverterAnnot.value());
        }
        String defaultTextValue = null;
        if(annotation!=null && !annotation.defaultValue().equals(ConfigProperty.UNCONFIGURED_VALUE)){
            defaultTextValue = annotation.defaultValue();
        }
        Config config = ConfigProvider.getConfig();
//        final WithConfigOperator withOperatorAnnot = injectionPoint.getAnnotated().getAnnotation(WithConfigOperator.class);
//        ConfigOperator operator = null;
//        if (withOperatorAnnot != null) {
//            operator = TamayaCDIInjectionExtension.CUSTOM_OPERATORS.get(withOperatorAnnot.value());
//        }
//        config = Objects.requireNonNull(operator.apply(config));

        Optional<String> textValue = Optional.empty();
        // Try to esolve using type, non present is possible, conversion issues are errors.
        String keyFound = null;
        for(String key:keys) {
            textValue = config.getOptionalValue(key, String.class);
            if(textValue.isPresent()) {
                keyFound = key;
                break;
            }
        }
        LOGGER.info("Converting config value found for " + injectionPoint );
        ConversionContext conversionContext = createConversionContext(keyFound, keys, config, injectionPoint);

        Object value = convertValue(textValue.orElse(defaultTextValue), conversionContext, customConverter);
        if (value == null) {
            throw new IllegalArgumentException(String.format(
                    "Can't resolve any of the possible config keys: %s to the required target type: %s, supported formats: %s",
                    keys, conversionContext.getTargetType(), conversionContext.getSupportedFormats().toString()));
        }
        LOGGER.finest(String.format("Injecting %s for key %s in class %s", keyFound, value.toString(), injectionPoint.toString()));
        if(TypeLiteral.of(injectionPoint.getAnnotated().getBaseType()).getRawType().equals(Optional.class)){
            return Optional.ofNullable(value);
        }
        return value;
    }

//    private Class getClass(Type baseType) {
//        if(baseType instanceof Class){
//            return Class.class.cast(baseType);
//        }else if(baseType instanceof ParameterizedType){
//            return getClass(((ParameterizedType)baseType).getRawType());
//        }else{
//            try {
//                return Class.forName(baseType.getTypeName());
//            } catch (ClassNotFoundException e) {
//                throw new IllegalArgumentException("Not a class tape: " + baseType.getTypeName());
//            }
//        }
//    }

    static ConversionContext createConversionContext(String key, List<String> keys, Config config, InjectionPoint injectionPoint) {
        final Type targetType = resolveTargetType(injectionPoint.getAnnotated().getBaseType());
        ConversionContext.Builder builder = new ConversionContext.Builder(config, key, targetType);
        if (injectionPoint.getMember() instanceof Field) {
            Field annotated = (Field)injectionPoint.getMember();
            if(annotated.isAnnotationPresent(ConfigProperty.class)) {
                builder.setAnnotatedElement(annotated);
            }
        }else if(injectionPoint.getMember() instanceof Method){
            Method method = (Method)injectionPoint.getMember();
            for(Type type:method.getParameterTypes()){
                if(type instanceof AnnotatedElement){
                    AnnotatedElement annotated = (AnnotatedElement)type;
                    if(annotated.isAnnotationPresent(ConfigProperty.class)) {
                        builder.setAnnotatedElement(annotated);
                    }
                }
            }
        }
        return builder.build();
    }

    private static <T> T convertValue(String textValue, ConversionContext conversionContext,
                               Converter<T> customConverter) {
        try {
            ConversionContext.setContext(conversionContext);
            if(customConverter!=null) {
                return customConverter.convert(textValue);
            }

            if(conversionContext.getConfiguration() instanceof ConfigContextSupplier){
                try {
                    return ConverterManager.defaultInstance().convertValue(textValue, conversionContext.getTargetType(),
                            ((ConfigContextSupplier) conversionContext.getConfiguration()).getConfigContext()
                                    .getConverters(conversionContext.getTargetType()));
                }catch(IllegalArgumentException e){
                    return null;
                }
            }
            return ConverterManager.defaultInstance().convertValue(textValue, conversionContext.getTargetType());
        }finally{
            ConversionContext.reset();
        }
    }

    private static Type resolveTargetType(Type targetType) {
        if(targetType instanceof ParameterizedType){
            ParameterizedType pt = (ParameterizedType)targetType;
            if(Provider.class.equals(pt.getRawType()) || Supplier.class.equals(pt.getRawType())
                    || Instance.class.equals(pt.getRawType())
                    || Optional.class.equals(pt.getRawType())){
               return pt.getActualTypeArguments()[0];
            }
        }
        return targetType;
    }


    @Produces
    public Config getConfig(){
        return ConfigProvider.getConfig();
    }

    @Produces
    public ConfigBuilder getConfigBuilder(){
        return ConfigProviderResolver.instance().getBuilder();
    }

}
