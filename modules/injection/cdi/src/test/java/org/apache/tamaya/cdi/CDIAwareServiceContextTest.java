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

import org.junit.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class CDIAwareServiceContextTest extends BaseTestConfiguration {
    private CDIAwareServiceContext context = new CDIAwareServiceContext();

    @Test
    public void getServiceReturnsNonCachedInstanceAtFirstCall() {
        assertThat(context.isCached(VertigoService.class)).isFalse();

        VertigoService service = context.getService(VertigoService.class);

        assertThat(service).isNotNull();
        assertThat(context.isCached(VertigoService.class)).isTrue();
    }

    @Test
    public void getServiceReturnsOnSecondCallCachedInstance() throws Exception {
        VertigoService service1 = context.getService(VertigoService.class);
        assertThat(context.isCached(VertigoService.class)).isTrue();

        VertigoService service2 = context.getService(VertigoService.class);

        assertThat(service2).isNotNull().isSameAs(service1);
    }

    @Test
    public void getServiceReturnsNullIfThereAreNoRegisteredSPIInstancesAvailable() throws Exception {
        DominoService service = context.getService(DominoService.class);

        assertThat(service).isNull();
    }

    @Test
    public void getServiceReturnsInstanceWithHighestPriority() throws Exception {
        TartanService service = context.getService(TartanService.class);

        assertThat(service).isNotNull();
        assertThat(service).isInstanceOf(TartanServiceTwoImpl.class);
    }
}