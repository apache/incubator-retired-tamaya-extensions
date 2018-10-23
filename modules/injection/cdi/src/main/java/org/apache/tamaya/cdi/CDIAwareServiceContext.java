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
package org.apache.tamaya.cdi;

import org.apache.tamaya.ConfigException;
import org.apache.tamaya.spi.ServiceContext;

import javax.annotation.Priority;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * <p>This class implements a {@link ServiceContext}, which basically provides a similar loading mechanism as used
 * by the {@link ServiceLoader}. Whereas the {@link ServiceLoader} only loads configurations
 * and instances from one classloader, this loader manages configs found and the related instances for each
 * classloader along the classloader hierarchies individually. It ensures instances are loaded on the classloader
 * level, where they first are visible. Additionally it ensures the same configuration resource (and its
 * declared services) are loaded multiple times, when going up the classloader hierarchy.</p>
 *
 * <p>Finally classloaders are not stored by reference by this class, to ensure they still can be garbage collected.
 * Refer also the inherited getParent class for further details.</p>
 *
 * <p>This class uses an ordinal of {@code 10}, so it overrides any default {@link ServiceContext} implementations
 * provided with the Tamaya core modules.</p>
 */
public class CDIAwareServiceContext implements ServiceContext {

    private static final Logger LOG = Logger.getLogger(CDIAwareServiceContext.class.getName());
    /**
     * Singletons.
     */
    private final Map<Class<?>, Object> singletons = new ConcurrentHashMap<>();

    private ServiceContext defaultServiceContext = new ServiceLoaderServiceContext();


    @Override
    public <T> T getService(Class<T> serviceType) {
        Object cached = singletons.get(serviceType);

        if (cached == null) {
            Collection<T> services = getServices(serviceType);
            if (services.isEmpty()) {
                cached = null;
            } else {
                cached = getServiceWithHighestPriority(services, serviceType);
            }
            if (cached != null) {
                singletons.put(serviceType, cached);
            }
        }
        return serviceType.cast(cached);
    }

    @Override
    public <T> T create(Class<T> serviceType) {
        T serv = getService(serviceType);
        if(serv!=null){
            try {
                return (T)serv.getClass().newInstance();
            } catch (Exception e) {
                Logger.getLogger(getClass().getName())
                        .log(Level.SEVERE, "Failed to create new instance of: " +serviceType.getName(), e);
            }
        }
        return null;
    }

    /**
     * Loads and registers services.
     *
     * @param <T>         the concrete type.
     * @param serviceType The service type.
     * @return the items found, never {@code null}.
     */
    @Override
    public <T> List<T> getServices(final Class<T> serviceType) {
        List<T> found = defaultServiceContext.getServices(serviceType);
        try {
            BeanManager beanManager = TamayaCDIAccessor.getBeanManager();
            Instance<T> cdiInstances = null;
            if(beanManager!=null) {
                Set<Bean<?>> instanceBeans = beanManager.getBeans(Instance.class);
                Bean<?> bean = instanceBeans.iterator().next();
                cdiInstances = (Instance<T>) beanManager.getReference(bean, Instance.class,
                        beanManager.createCreationalContext(bean));
            }
            if(cdiInstances!=null){
                for(T t:cdiInstances.select(serviceType)){
                    found.add(t);
                }
            }
        }catch(Exception e){
            LOG.log(Level.SEVERE, "Failed to access BeanManager.", e);
        }
        return found;
    }

    @Override
    public Enumeration<URL> getResources(String resource) throws IOException {
        return defaultServiceContext.getResources(resource);
    }

    @Override
    public URL getResource(String resource) {
        return defaultServiceContext.getResource(resource);
    }

    /**
     * Checks the given instance for a @Priority annotation. If present the annotation's value s evaluated. If no such
     * annotation is present, a default priority is returned (1);
     * @param o the instance, not null.
     * @return a priority, by default 1.
     */
    public static int getPriority(Object o){
        int prio = 1; //X TODO discuss default priority
        Priority priority = o.getClass().getAnnotation(Priority.class);
        if (priority != null) {
            prio = priority.value();
        }
        return prio;
    }

    /**
     * @param services to scan
     * @param <T>      type of the service
     *
     * @return the service with the highest {@link Priority#value()}
     *
     * @throws ConfigException if there are multiple service implementations with the maximum priority
     */
    private <T> T getServiceWithHighestPriority(Collection<T> services, Class<T> serviceType) {

        // we do not need the priority stuff if the list contains only one element
        if (services.size() == 1) {
            return services.iterator().next();
        }

        Integer highestPriority = null;
        int highestPriorityServiceCount = 0;
        T highestService = null;

        for (T service : services) {
            int prio = getPriority(service);
            if (highestPriority == null || highestPriority < prio) {
                highestService = service;
                highestPriorityServiceCount = 1;
                highestPriority = prio;
            } else if (highestPriority == prio) {
                highestPriorityServiceCount++;
            }
        }

        if (highestPriorityServiceCount > 1) {
            throw new ConfigException(MessageFormat.format("Found {0} implementations for Service {1} with Priority {2}: {3}",
                    highestPriorityServiceCount,
                    serviceType.getName(),
                    highestPriority,
                    services));
        }

        return highestService;
    }

    @Override
    public ClassLoader getClassLoader() {
        return defaultServiceContext.getClassLoader();
    }

    @Override
    public void init(ClassLoader classLoader) {
        this.defaultServiceContext.init(Objects.requireNonNull(classLoader));
    }

    /**
     * Returns ordinal of 20, overriding defaults as well as the inherited (internally used) CLAwareServiceContext
     * instance.
     * @return ordinal of 20.
     */
    @Override
    public int ordinal() {
        return 20;
    }

    /**
     * <p>Checks the internal used cache contains already instances
     * of the given type.</p>
     *
     * @param clazz type to be checked if it is already in the internal cache.
     * @return {@code true} if there are cached instances of the requested type,
     *         otherwise {@code false}.
     */
    protected boolean isCached(Class<?> clazz) {
        return singletons.containsKey(clazz);
    }
}
