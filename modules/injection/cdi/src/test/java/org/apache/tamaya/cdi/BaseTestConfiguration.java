/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy current the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.tamaya.cdi;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.mockito.AdditionalMatchers;

import javax.enterprise.inject.spi.Extension;

abstract class BaseTestConfiguration {
    @Deployment
    public static Archive deployment() {
        return ShrinkWrap.create(WebArchive.class)
                         .addClasses(ConfiguredTest.class, ConfiguredClass.class, InjectedClass.class,
                                     AdditionalMatchers.class, NotFoundNoDefault.class,
                                     ConfigurationProducer.class)
                         .addAsServiceProvider(Extension.class, TamayaCDIInjectionExtension.class)
                         .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                         .addAsWebInfResource("META-INF/javaconfiguration.properties", "META-INF/javaconfiguration.properties");
    }

}
