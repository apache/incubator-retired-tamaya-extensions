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
package org.apache.tamaya.events;

import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationSnapshot;
import org.apache.tamaya.spi.ConfigurationContext;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.omg.CORBA.Any;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

public class FrozenConfigurationTest {

    @Test
    public void idMustBeNotNull() {
        Configuration source = Mockito.mock(Configuration.class);

        Mockito.when(source.getContext()).thenReturn(ConfigurationContext.EMPTY);
        Mockito.when(source.getProperties()).thenReturn(Collections.emptyMap());

        FrozenConfiguration fc = FrozenConfiguration.of(source);

        assertThat(fc.getId()).isNotNull();
    }

    /*
     * All tests for equals() and hashCode() go here...
     */
    @Test
    public void twoFrozenAreDifferentIfTheyHaveADifferentIdAndFrozenAtTimestamp() {
        Map<String, String> properties = new HashMap<>();
        properties.put("key", "createValue");

        Configuration configuration = Mockito.mock(Configuration.class);
        Mockito.when(configuration.getContext()).thenReturn(ConfigurationContext.EMPTY);
        doReturn(properties).when(configuration).getProperties();

        FrozenConfiguration fcA = FrozenConfiguration.of(configuration);
        FrozenConfiguration fcB = FrozenConfiguration.of(configuration);

        assertThat(fcA.getId()).isNotEqualTo(fcB.getId());
        assertThat(fcA).isNotEqualTo(fcB);
    }

    /*
     * END OF ALL TESTS for equals() and hashCode()
     */
}