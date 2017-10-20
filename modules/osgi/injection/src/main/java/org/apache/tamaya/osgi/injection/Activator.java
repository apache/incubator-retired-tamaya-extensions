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
package org.apache.tamaya.osgi.injection;

import org.osgi.framework.*;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Activator that registers the Tamaya commands for the Felix Gogo console used
 * in Apache Felix and Equinox.
 */
public class Activator implements BundleActivator {

    private static final Logger LOG = Logger.getLogger(Activator.class.getName());

    private TamayaOSGIInjector injector;
    private ServiceRegistration<ConfigInjectionService> serviceReg;

    @Override
    public void start(BundleContext context) throws Exception {
        LOG.finest("Registering Tamaya OSGI Config injector...");
        injector = new TamayaOSGIInjector(context);
        Dictionary<String, Object> props = new Hashtable<>();
        serviceReg = context.registerService(
                ConfigInjectionService.class,
                new TamayaConfigInjectionService(injector), props);
        injector.start();
        LOG.finest("Registered Tamaya OSGI Config injector.");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        LOG.finest("Unregistering Tamaya Config injector...");
        if (serviceReg != null) {
            serviceReg.unregister();
        }
        injector.stop();
    }

    static final class TamayaConfigInjectionService implements ConfigInjectionService{

        private TamayaOSGIInjector injector;

        TamayaConfigInjectionService(TamayaOSGIInjector injector){
            this.injector = Objects.requireNonNull(injector);
        }

        @Override
        public boolean isInjectionEnabled(ServiceReference reference) {
            return injector.isInjectionEnabled(reference);
        }

        @Override
        public boolean isInjectionEnabled(Bundle bundle) {
            return injector.isInjectionEnabled(bundle);
        }

        @Override
        public <T> T configure(String pid, String location, T instance) {
            return injector.configure(pid, location, instance);
        }

        @Override
        public <T> Supplier<T> getConfiguredSupplier(String pid, String location, Supplier<T> supplier) {
            return injector.getConfiguredSupplier(pid, location, supplier);
        }

        @Override
        public <T> T createTemplate(String pid, String location, Class<T> templateType) {
            return injector.createTemplate(pid, location, templateType);
        }
    }

}
