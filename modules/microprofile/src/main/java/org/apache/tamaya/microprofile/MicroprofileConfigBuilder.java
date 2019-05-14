/*
 * Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.tamaya.microprofile;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.ServiceLoader;

import org.apache.tamaya.Configuration;
import org.apache.tamaya.TypeLiteral;
import org.apache.tamaya.spi.ConfigurationBuilder;
import org.apache.tamaya.spi.PropertyConverter;
import org.apache.tamaya.spi.ServiceContextManager;
import org.apache.tamaya.spisupport.PriorityServiceComparator;
import org.apache.tamaya.spisupport.PropertySourceComparator;
import org.apache.tamaya.spisupport.propertysource.EnvironmentPropertySource;
import org.apache.tamaya.spisupport.propertysource.SystemPropertySource;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigBuilder;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;
import org.eclipse.microprofile.config.spi.Converter;

import javax.annotation.Priority;

/**
 * Created by atsticks on 23.03.17.
 */
final class MicroprofileConfigBuilder implements ConfigBuilder {

    private ConfigurationBuilder configurationBuilder;

    MicroprofileConfigBuilder(ConfigurationBuilder configurationBuilder) {
        for(ConfigSource cs:ServiceLoader.load(ConfigSource.class)){
            System.out.println(cs.getName());
        }
        this.configurationBuilder = Objects.requireNonNull(configurationBuilder);
        configurationBuilder.addDefaultPropertyConverters();
    }

    public ConfigurationBuilder getConfigurationBuilder() {
        return configurationBuilder;
    }

    /**
     * Add the default configuration sources appearing on the builder's classpath
     * including:
     * <ol>
     * <li>System properties</li>
     * <li>Environment properties</li>
     * <li>/META-INF/microprofile-config.properties</li>
     * </ol>
     *
     * @return the ConfigBuilder with the default configuration sources
     */
    @Override
    public ConfigBuilder addDefaultSources() {
        configurationBuilder.addPropertySources(
                new SystemPropertySource(400), //
                new EnvironmentPropertySource(300) //
        );
        MicroprofileDefaultPropertiesProvider provider = new MicroprofileDefaultPropertiesProvider();
        provider.init(this.configurationBuilder.getClassLoader());
        configurationBuilder.addPropertySources(
                provider.getPropertySources()
        );
        configurationBuilder.sortPropertySources(PropertySourceComparator.getInstance());
        return this;
    }

    /**
     * Add ConfigSources registered using the ServiceLoader.
     *
     * @return the ConfigBuilder with the added configuration sources
     */
    @Override
    public ConfigBuilder addDiscoveredSources() {
        for (ConfigSource configSource : ServiceContextManager.getServiceContext(
                configurationBuilder.getClassLoader()).getServices(ConfigSource.class)) {
            configurationBuilder.addPropertySources(MicroprofileAdapter.toPropertySource(configSource));
        }

        for (ConfigSourceProvider configSourceProvider : ServiceContextManager.getServiceContext(
                configurationBuilder.getClassLoader()
        ).getServices(ConfigSourceProvider.class)) {
            configurationBuilder.addPropertySources(MicroprofileAdapter.toPropertySources(configSourceProvider.getConfigSources(
                    Thread.currentThread().getContextClassLoader()
            )));
        }
        configurationBuilder.sortPropertySources(PropertySourceComparator.getInstance());
        return this;
    }

    /**
     * Add Converters registered using the ServiceLoader.
     *
     * @return the ConfigBuilder with the added configuration converters
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public ConfigBuilder addDiscoveredConverters() {
        for (Converter<?> converter : ServiceContextManager.getServiceContext(
                configurationBuilder.getClassLoader()
        ).getServices(Converter.class)) {
            TypeLiteral targetType = TypeLiteral.of(
                    TypeLiteral.getGenericInterfaceTypeParameters(converter.getClass(), Converter.class)[0]);

            configurationBuilder.addPropertyConverters(targetType,
                    MicroprofileAdapter.toPropertyConverter(converter));
        }
        return this;
    }

    @Override
    public ConfigBuilder forClassLoader(ClassLoader loader) {
        this.configurationBuilder.setClassLoader(loader);
        return this;
    }

    @Override
    public ConfigBuilder withSources(ConfigSource... sources) {
        for (ConfigSource source : sources) {
            configurationBuilder.addPropertySources(MicroprofileAdapter.toPropertySource(source));
        }
        return this;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public ConfigBuilder withConverters(Converter<?>... converters) {
        for (Converter<?> converter : converters) {
            TypeLiteral lit = TypeLiteral.of(converter.getClass());
            Type[] types = TypeLiteral.getGenericInterfaceTypeParameters(converter.getClass(), Converter.class);
            if(types.length==0){
                throw new IllegalArgumentException("Cannot evaluate converter target type for " + converter);
            }
            TypeLiteral target = TypeLiteral.of(types[0]);
            configurationBuilder.addPropertyConverters(target,
                    MicroprofileAdapter.toPropertyConverter(converter));
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> ConfigBuilder withConverter(Class<T> type, int priority, Converter<T> converter) {
        configurationBuilder.addPropertyConverters(TypeLiteral.of(type), MicroprofileAdapter.toPropertyConverter(priority, converter));
        return this;
    }

    @Override
    public Config build() {
        configurationBuilder.sortPropertyConverter(MicroProfileAwareConverterComparator.INSTANCE);
        Configuration config = getConfigurationBuilder().build();
        Configuration.setCurrent(config);
        return MicroprofileAdapter.toConfig(config);
    }

    @Override
    public String toString() {
        return "TamayaConfigBuilder{" +
                "delegate=" + configurationBuilder +
                '}';
    }

    /**
     * This comparator will extract any internally adapted Microprofile {@link Converter} to
     * evaluate the corrent {@link javax.annotation.Priority} and use this information for comparison.
     */
    private static final class MicroProfileAwareConverterComparator implements Comparator<PropertyConverter>{

        static final MicroProfileAwareConverterComparator INSTANCE = new MicroProfileAwareConverterComparator();

        @Override
        public int compare(PropertyConverter o1, PropertyConverter o2) {
            int priority1 = PriorityServiceComparator.getPriority(o1) + 99; // Microprofile default
            int priority2 = PriorityServiceComparator.getPriority(o2) + 99; // Microprofile default
            if(o1 instanceof TamayaPropertyConverter){
                priority1 = ((TamayaPropertyConverter)o1).getPriority();
            }
            if(o2 instanceof TamayaPropertyConverter){
                priority2 = ((TamayaPropertyConverter)o2).getPriority();
            }
            return priority2 - priority1;
        }
    }

}
