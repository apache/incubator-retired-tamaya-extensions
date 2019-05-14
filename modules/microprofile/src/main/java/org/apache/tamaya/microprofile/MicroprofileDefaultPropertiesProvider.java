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

import org.apache.tamaya.spi.ClassloaderAware;
import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertySourceProvider;
import org.apache.tamaya.spi.ServiceContext;
import org.apache.tamaya.spi.ServiceContextManager;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Default property source for config properties in the classpath.
 */
public class MicroprofileDefaultPropertiesProvider implements PropertySourceProvider, ClassloaderAware {

    private ClassLoader classLoader;

    @Override
    public Collection<PropertySource> getPropertySources() {
        if(classLoader==null){
            classLoader = ServiceContextManager.getDefaultClassLoader();
        }
        ServiceContext sc = ServiceContextManager.getServiceContext(classLoader);
        List<PropertySource> propertySources = new ArrayList<>();
        for(URL url:sc.getResources("META-INF/microprofile-config.properties")){
            propertySources.add(new MicroprofileDefaultProperties(url));
        }
        return propertySources;
    }

    @Override
    public void init(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public ClassLoader getClassLoader() {
        return this.classLoader;
    }
}
