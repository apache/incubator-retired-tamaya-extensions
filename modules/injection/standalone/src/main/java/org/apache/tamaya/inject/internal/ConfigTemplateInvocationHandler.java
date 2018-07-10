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

import org.apache.tamaya.inject.api.DynamicValue;
import org.apache.tamaya.inject.spi.ConfiguredType;

import javax.config.Config;
import javax.config.ConfigProvider;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Invocation handler that handles request against a configuration template.
 */
public final class ConfigTemplateInvocationHandler implements InvocationHandler {

    /**
     * The configured type.
     */
    private final ConfiguredType type;
    /**
     * The target config, not null.
     */
    private final Config config;

    /**
     * Creates a new handler instance.
     *
     * @param type          the target type, not null.
     * @param config        the target config, not null.
     */
    public ConfigTemplateInvocationHandler(Class<?> type, Config config) {
        this.type = new ConfiguredTypeImpl(Objects.requireNonNull(type));
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Can only proxy interfaces as configuration templates.");
        }
        this.config = Objects.requireNonNull(config);
        InjectionHelper.sendConfigurationEvent(this.type);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("toString".equals(method.getName())) {
            return "Configured Proxy -> " + this.type.getType().getName();
        } else if ("hashCode".equals(method.getName())) {
            return Objects.hashCode(proxy);
        } else if ("equals".equals(method.getName())) {
            return Objects.equals(proxy, args[0]);
        } else if ("get".equals(method.getName())) {
            return config;
        }
        if (method.getReturnType() == DynamicValue.class) {
            return DefaultDynamicValue.of(proxy, method, config);
        }
        return InjectionHelper.getConfigValue(method, method.getReturnType(), config);
    }
}
