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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

import java.util.Hashtable;
import java.util.function.Supplier;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by atsti on 03.10.2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class ActivatorTest extends AbstractOSGITest{

    private TamayaOSGIInjector injector;
    private  Activator.TamayaConfigInjectionService injectionService;

    @Before
    public void init(){
        injector = new TamayaOSGIInjector(this.bundleContext);
        injectionService = new Activator.TamayaConfigInjectionService(
                injector
        );
    }

    @Test
    public void startStop() throws Exception {
        Activator activator = new Activator();
        activator.start(this.bundleContext);
        verify(bundleContext).registerService(eq(ConfigInjectionService.class), anyObject(), anyObject());
        activator.stop(this.bundleContext);
    }

    @Test
    public void isInjectionEnabled_Service(){
        ServiceReference reference = mock(ServiceReference.class);
        Bundle bundle = mock(Bundle.class);
        doReturn(bundle).when(reference).getBundle();
        doReturn(new Hashtable<>()).when(bundle).getHeaders();
        doReturn("true").when(reference).getProperty(ConfigInjectionService.TAMAYA_INJECTION_ENABLED_PROP);
        assertTrue(injectionService.isInjectionEnabled(reference));
        doReturn("yes").when(reference).getProperty(ConfigInjectionService.TAMAYA_INJECTION_ENABLED_PROP);
        assertTrue(injectionService.isInjectionEnabled(reference));
        doReturn("no").when(reference).getProperty(ConfigInjectionService.TAMAYA_INJECTION_ENABLED_PROP);
        assertFalse(injectionService.isInjectionEnabled(reference));
        doReturn(null).when(reference).getProperty(ConfigInjectionService.TAMAYA_INJECTION_ENABLED_PROP);
        assertFalse(injectionService.isInjectionEnabled(reference));
        doReturn("foo").when(reference).getProperty(ConfigInjectionService.TAMAYA_INJECTION_ENABLED_PROP);
        assertFalse(injectionService.isInjectionEnabled(reference));
        // service undefined, but bundle enabled
        doReturn(null).when(reference).getProperty(ConfigInjectionService.TAMAYA_INJECTION_ENABLED_PROP);
        doReturn(singleHashtable(ConfigInjectionService.TAMAYA_INJECTION_ENABLED_MANIFEST, "true")).when(bundle).getHeaders();
        assertTrue(injectionService.isInjectionEnabled(reference));
    }

    private Hashtable singleHashtable(String key, String value) {
        Hashtable t = new Hashtable();
        t.put(key, value);
        return t;
    }

    @Test
    public void isInjectionEnabled_Bundle() {
        Bundle bundle = mock(Bundle.class);
        doReturn(singleHashtable(ConfigInjectionService.TAMAYA_INJECTION_ENABLED_MANIFEST, "true")).when(bundle).getHeaders();
        assertTrue(injectionService.isInjectionEnabled(bundle));
        doReturn(singleHashtable(ConfigInjectionService.TAMAYA_INJECTION_ENABLED_MANIFEST, "YeS")).when(bundle).getHeaders();
        assertTrue(injectionService.isInjectionEnabled(bundle));
        doReturn(singleHashtable(ConfigInjectionService.TAMAYA_INJECTION_ENABLED_MANIFEST, "on")).when(bundle).getHeaders();
        assertTrue(injectionService.isInjectionEnabled(bundle));
        doReturn(singleHashtable(ConfigInjectionService.TAMAYA_INJECTION_ENABLED_MANIFEST, "TRUE")).when(bundle).getHeaders();
        assertTrue(injectionService.isInjectionEnabled(bundle));
        doReturn(singleHashtable(ConfigInjectionService.TAMAYA_INJECTION_ENABLED_MANIFEST, "no")).when(bundle).getHeaders();
        assertFalse(injectionService.isInjectionEnabled(bundle));
        doReturn(singleHashtable(ConfigInjectionService.TAMAYA_INJECTION_ENABLED_MANIFEST, "foo")).when(bundle).getHeaders();
        assertFalse(injectionService.isInjectionEnabled(bundle));
        doReturn(new Hashtable<>()).when(bundle).getHeaders();
        assertFalse(injectionService.isInjectionEnabled(bundle));
    }

    @Test
    public void configure() throws Exception {
        Example example = new Example();
        Example result = injectionService.configure("tamaya", null, example);
        assertNotNull(result);
        assertTrue(result==example);
        Example.checkExampleConfig(example);
    }

    @Test
    public void getConfiguredSupplier() throws Exception {
        Supplier<Example> supplier = injectionService.getConfiguredSupplier("tamaya", null, Example::new);
        assertNotNull(supplier);
        Example example = supplier.get();
        Example.checkExampleConfig(example);
    }

    @Test
    public void createTemplate() throws Exception {
        TemplateExample template = injectionService.createTemplate("tamaya", null, TemplateExample.class);
        Example.checkExampleConfig(template);
    }

}