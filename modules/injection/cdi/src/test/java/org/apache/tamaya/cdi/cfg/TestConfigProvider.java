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
 */
package org.apache.tamaya.cdi.cfg;

import javax.config.spi.ConfigSource;
import javax.config.spi.ConfigSourceProvider;
import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Anatole on 29.09.2014.
 */
@ApplicationScoped
public class TestConfigProvider implements ConfigSourceProvider {

    private List<ConfigSource> configs = new ArrayList<>();

    public TestConfigProvider(){
        configs.add(new ProvidedConfigSource());
    }

    @Override
    public Collection<ConfigSource> getConfigSources(ClassLoader cl) {
        return configs;
    }
}
