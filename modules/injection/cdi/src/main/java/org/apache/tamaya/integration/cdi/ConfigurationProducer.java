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
package org.apache.tamaya.integration.cdi;

import org.apache.tamaya.*;
import org.apache.tamaya.inject.api.*;
import org.apache.tamaya.spi.ConfigurationContext;
import org.apache.tamaya.spi.ConfigurationContextBuilder;
import org.apache.tamaya.spi.ConversionContext;
import org.apache.tamaya.spi.PropertyConverter;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import java.io.File;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Producer bean for configuration properties.
 */
@ApplicationScoped
public class ConfigurationProducer {

    private static final Logger LOGGER = Logger.getLogger(ConfigurationProducer.class.getName());

    private DynamicValue createDynamicValue(final InjectionPoint injectionPoint) {
        Member member = injectionPoint.getMember();
        if (member instanceof Field) {
            return DefaultDynamicValue.of((Field) member, ConfigurationProvider.getConfiguration());
        } else if (member instanceof Method) {
            return DefaultDynamicValue.of((Method) member, ConfigurationProvider.getConfiguration());
        }
        return null;
    }

    @Produces
    @Config
    public File resolveAndConvert_File(final InjectionPoint injectionPoint) {
        return (File)resolveAndConvert(injectionPoint);
    }

    @Produces
    @Config
    public BigDecimal resolveAndConvert_BigDecimal(final InjectionPoint injectionPoint) {
        return (BigDecimal)resolveAndConvert(injectionPoint);
    }

    @Produces
    @Config
    public BigInteger resolveAndConvert_BigInteger(final InjectionPoint injectionPoint) {
        return (BigInteger)resolveAndConvert(injectionPoint);
    }

    @Produces
    @Config
    public Number resolveAndConvert_Number(final InjectionPoint injectionPoint) {
        return (Number)resolveAndConvert(injectionPoint);
    }

    @Produces
    @Config
    public Boolean resolveAndConvert_Boolean(final InjectionPoint injectionPoint) {
        return (Boolean)resolveAndConvert(injectionPoint);
    }

    @Produces
    @Config
    public Byte resolveAndConvert_Byte(final InjectionPoint injectionPoint) {
        return (Byte)resolveAndConvert(injectionPoint);
    }

    @Produces
    @Config
    public Short resolveAndConvert_Short(final InjectionPoint injectionPoint) {
        return (Short)resolveAndConvert(injectionPoint);
    }

    @Produces
    @Config
    public Integer resolveAndConvert_Integer(final InjectionPoint injectionPoint) {
        return (Integer)resolveAndConvert(injectionPoint);
    }

    @Produces
    @Config
    public Long resolveAndConvert_Long(final InjectionPoint injectionPoint) {
        return (Long)resolveAndConvert(injectionPoint);
    }

    @Produces
    @Config
    public Float resolveAndConvert_Float(final InjectionPoint injectionPoint) {
        return (Float)resolveAndConvert(injectionPoint);
    }

    @Produces
    @Config
    public Double resolveAndConvert_Double(final InjectionPoint injectionPoint) {
        return (Double)resolveAndConvert(injectionPoint);
    }

    @Produces
    @Config
    public Date resolveAndConvert_Date(final InjectionPoint injectionPoint) {
        return (Date)resolveAndConvert(injectionPoint);
    }

    @Produces
    @Config
    public String resolveAndConvert_String(final InjectionPoint injectionPoint) {
        return (String)resolveAndConvert(injectionPoint);
    }

    @Produces
    @Config
    public GregorianCalendar resolveAndConvert_GregorianCalendar(final InjectionPoint injectionPoint) {
        return (GregorianCalendar)resolveAndConvert(injectionPoint);
    }

    @Produces
    @Config
    public Calendar resolveAndConvert_Calendar(final InjectionPoint injectionPoint) {
        return (Calendar)resolveAndConvert(injectionPoint);
    }

    @Produces
    @Config
    public Object resolveAndConvert(final InjectionPoint injectionPoint) {
        if (DynamicValue.class.equals(injectionPoint.getAnnotated().getBaseType())) {
            return createDynamicValue(injectionPoint);
        }
        final Config annotation = injectionPoint.getAnnotated().getAnnotation(Config.class);
        final ConfigDefaultSections typeAnnot = injectionPoint.getAnnotated().getAnnotation(ConfigDefaultSections.class);
        final List<String> keys = TamayaCDIInjectionExtension.evaluateKeys(injectionPoint.getMember().getName(),
                annotation != null ? annotation.value() : null,
                typeAnnot != null ? typeAnnot.value() : null);

        final WithConfigOperator withOperatorAnnot = injectionPoint.getAnnotated().getAnnotation(WithConfigOperator.class);
        ConfigOperator operator = null;
        if (withOperatorAnnot != null) {
            operator = TamayaCDIInjectionExtension.CUSTOM_OPERATORS.get(withOperatorAnnot.value());
        }
        PropertyConverter customConverter = null;
        final WithPropertyConverter withConverterAnnot = injectionPoint.getAnnotated().getAnnotation(WithPropertyConverter.class);
        if (withConverterAnnot != null) {
            customConverter = TamayaCDIInjectionExtension.CUSTOM_CONVERTERS.get(withConverterAnnot.value());
        }

        // unless the extension is not installed, this should never happen because the extension
        // enforces the resolvability of the config
        Configuration config = ConfigurationProvider.getConfiguration();
        if (operator != null) {
            config = operator.operate(config);
        }
        final Class<?> toType = (Class<?>) injectionPoint.getAnnotated().getBaseType();
        String textValue = null;
        String defaultTextValue = annotation.defaultValue().isEmpty() ? "" : annotation.defaultValue();
        String keyFound = null;
        for (String key : keys) {
            textValue = config.get(key);
            if (textValue != null) {
                keyFound = key;
                break;
            }
        }
        ConversionContext.Builder builder = new ConversionContext.Builder(config,
                ConfigurationProvider.getConfiguration().getContext(), keyFound, TypeLiteral.of(toType));
        if (injectionPoint.getMember() instanceof AnnotatedElement) {
            builder.setAnnotatedElement((AnnotatedElement) injectionPoint.getMember());
        }
        ConversionContext conversionContext = builder.build();
        Object value = null;
        if (keyFound != null) {
            if (customConverter != null) {
                value = customConverter.convert(textValue, conversionContext);
            }
            if (value == null) {
                value = config.get(keyFound, toType);
            }
        } else if (defaultTextValue != null) {
            value = defaultTextValue;
            if (customConverter != null) {
                value = customConverter.convert((String)value, conversionContext);
            }
            if (value != null) {
                List<PropertyConverter<Object>> converters = ConfigurationProvider.getConfiguration().getContext()
                        .getPropertyConverters(TypeLiteral.of(toType));
                for (PropertyConverter<Object> converter : converters) {
                    try {
                        value = converter.convert(defaultTextValue, conversionContext);
                        if (value != null) {
                            LOGGER.log(Level.FINEST, "Parsed default value from '" + defaultTextValue + "' into " +
                                    injectionPoint);
                            break;
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.FINEST, "Failed to convert default value '" + defaultTextValue + "' for " +
                                injectionPoint, e);
                    }
                }
            }
        }
        if (value == null) {
            throw new ConfigException(String.format(
                    "Can't resolve any of the possible config keys: %s to the required target type: %s, supported formats: %s",
                    keys.toString(), toType.getName(), conversionContext.getSupportedFormats().toString()));
        }
        LOGGER.finest(String.format("Injecting %s for key %s in class %s", keyFound, value.toString(), injectionPoint.toString()));
        return value;
    }

    @Produces
    public Configuration getConfiguration(){
        return ConfigurationProvider.getConfiguration();
    }

    @Produces
    public ConfigurationContext getConfigurationContext(){
        return ConfigurationProvider.getConfiguration().getContext();
    }

    @Produces
    public ConfigurationContextBuilder getConfigurationContextBuilder(){
        return ConfigurationProvider.getConfigurationContextBuilder();
    }

}
