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

import org.apache.tamaya.Configuration;
import org.apache.tamaya.inject.ConfigurationInjector;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;


/**
 * PostProcessor that performs injection of configured values using Tamaya {@link ConfigurationInjector}.
 */
public class SpringConfigInjectionPostProcessor implements BeanPostProcessor{

    private ObjectFactory<Configuration> configSupplier;

    public SpringConfigInjectionPostProcessor(ObjectFactory<Configuration> configSupplier){
        try{
            Configuration config = configSupplier.getObject();
            this.configSupplier = configSupplier;
        }catch(Exception e){
            this.configSupplier = Configuration::current;
        }
    }

    @Override
    public Object postProcessBeforeInitialization(Object o, String s) throws BeansException {
        ConfigurationInjector.getInstance().configure(o, this.configSupplier.getObject());
        return o;
    }

    @Override
    public Object postProcessAfterInitialization(Object o, String s) throws BeansException {
        return o;
    }
}
