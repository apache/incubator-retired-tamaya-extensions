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
import org.junit.Test;
import org.mockito.Mockito;

import java.beans.PropertyChangeEvent;
import java.util.*;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;

public class ConfigurationChangeBuilderTest {

    @Test
    public void compareReturnAnEmptyListOfChangesForTwoEmptyConfigurations() {
        Configuration oc = Mockito.mock(Configuration.class, new MethodNotMockedAnswer());
        Configuration nc = Mockito.mock(Configuration.class, new MethodNotMockedAnswer());

        doReturn(emptyMap()).when(oc).getProperties();
        doReturn(emptyMap()).when(nc).getProperties();

        Collection<PropertyChangeEvent> diff = ConfigurationChangeBuilder.compare(oc, nc);

        assertThat(diff).isNotNull().isEmpty();
    }

    @Test
    public void compareReturnsAChangeEventIfThereIsANewKeyInTheNewVersionOfTheConfiguration() {
        Configuration oc = Mockito.mock(Configuration.class, new MethodNotMockedAnswer());
        Configuration nc = Mockito.mock(Configuration.class, new MethodNotMockedAnswer());

        doReturn(emptyMap()).when(oc).getProperties();
        doReturn(null).when(oc).get(eq("a"));

        Map<String, String> valuesNC = new HashMap<>();
        valuesNC.put("a", "19");

        doReturn(valuesNC).when(nc).getProperties();
        doReturn("19").when(nc).get(eq("a"));

        Collection<PropertyChangeEvent> diff = ConfigurationChangeBuilder.compare(oc, nc);

        assertThat(diff).isNotNull().isNotEmpty().hasSize(1);

        PropertyChangeEvent change = diff.iterator().next();

        assertThat(change).isNotNull();
        assertThat(change.getNewValue()).isEqualTo("19");
        assertThat(change.getOldValue()).isNull();
        assertThat(change.getPropertyName()).isEqualTo("a");
    }

    @Test
    public void compareReturnsAChangeEventIfAKeyHasBeenRemovedInTheNewVersionOfTheConfiguration() {
        Configuration oc = Mockito.mock(Configuration.class, new MethodNotMockedAnswer());
        Configuration nc = Mockito.mock(Configuration.class, new MethodNotMockedAnswer());

        Map<String, String> valuesOC = new HashMap<>();
        valuesOC.put("a", "19");

        doReturn(valuesOC).when(oc).getProperties();
        doReturn("19").when(oc).get(eq("a"));

        doReturn(emptyMap()).when(nc).getProperties();
        doReturn(null).when(nc).get(eq("a"));

        Collection<PropertyChangeEvent> diff = ConfigurationChangeBuilder.compare(oc, nc);

        assertThat(diff).isNotNull().isNotEmpty().hasSize(1);

        PropertyChangeEvent change = diff.iterator().next();

        assertThat(change).isNotNull();
        assertThat(change.getNewValue()).isNull();
        assertThat(change.getOldValue()).isEqualTo("19");
        assertThat(change.getPropertyName()).isEqualTo("a");
    }

    @Test
    public void compareReturnsAChangeEventIfValueOfExistingKeyHasChanged() {
        Configuration oc = Mockito.mock(Configuration.class, new MethodNotMockedAnswer());
        Configuration nc = Mockito.mock(Configuration.class, new MethodNotMockedAnswer());

        Map<String, String> valuesOC = new HashMap<>();
        valuesOC.put("a", "91");

        doReturn(valuesOC).when(oc).getProperties();
        doReturn("91").when(oc).get(eq("a"));
        doReturn("old configuration").when(oc).toString();

        Map<String, String> valuesNC = new HashMap<>();
        valuesNC.put("a", "19");

        doReturn(valuesNC).when(nc).getProperties();
        doReturn("19").when(nc).get(eq("a"));
        doReturn("new configuration").when(nc).toString();

        Collection<PropertyChangeEvent> diff = ConfigurationChangeBuilder.compare(oc, nc);

        assertThat(diff).isNotNull().isNotEmpty().hasSize(1);

        PropertyChangeEvent change = diff.iterator().next();

        assertThat(change).isNotNull();
        assertThat(change.getNewValue()).isEqualTo("19");
        assertThat(change.getOldValue()).isEqualTo("91");
        assertThat(change.getPropertyName()).isEqualTo("a");
    }

}
