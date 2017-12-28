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

import org.junit.Test;
import org.mockito.Mockito;

import javax.config.Config;
import java.beans.PropertyChangeEvent;
import java.util.*;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

public class ConfigChangeBuilderTest {

	@Test
	public void compareReturnAnEmptyListOfChangesForTwoEmptyConfigs() {
		Config oc = Mockito.mock(Config.class, new MethodNotMockedAnswer());
		Config nc = Mockito.mock(Config.class, new MethodNotMockedAnswer());

		doReturn(emptySet()).when(oc).getPropertyNames();
		doReturn(emptySet()).when(nc).getPropertyNames();

		Collection<PropertyChangeEvent> diff = ConfigChangeBuilder.compare(oc, nc);

		assertThat(diff).isNotNull().isEmpty();
	}

	@Test
	public void compareReturnsAChangeEventIfThereIsANewKeyInTheNewVersionOfTheConfig() {
		Config oc = Mockito.mock(Config.class, new MethodNotMockedAnswer());
		Config nc = Mockito.mock(Config.class, new MethodNotMockedAnswer());

		doReturn(emptySet()).when(oc).getPropertyNames();
		doThrow(new NoSuchElementException()).when(oc).getValue("a", String.class);
		doReturn(Optional.empty()).when(oc).getOptionalValue("a", String.class);

		Map<String, String> valuesNC = new HashMap<String, String>();
		valuesNC.put("a", "19");

		doReturn(valuesNC.keySet()).when(nc).getPropertyNames();
		doReturn(Optional.of("19")).when(nc).getOptionalValue("a", String.class);

		Collection<PropertyChangeEvent> diff = ConfigChangeBuilder.compare(oc, nc);

		assertThat(diff).isNotNull().isNotEmpty().hasSize(1);

		PropertyChangeEvent change = diff.iterator().next();

		assertThat(change).isNotNull();
		assertThat(change.getNewValue()).isEqualTo("19");
		assertThat(change.getOldValue()).isNull();
		assertThat(change.getPropertyName()).isEqualTo("a");
	}

	@Test
	public void compareReturnsAChangeEventIfAKeyHasBeenRemovedInTheNewVersionOfTheConfig() {
		Config oc = Mockito.mock(Config.class, new MethodNotMockedAnswer());
		Config nc = Mockito.mock(Config.class, new MethodNotMockedAnswer());

		Map<String, String> valuesOC = new HashMap<String, String>();
		valuesOC.put("a", "19");

		doReturn(valuesOC.keySet()).when(oc).getPropertyNames();
		doReturn("19").when(oc).getValue("a", String.class);

		doReturn(emptySet()).when(nc).getPropertyNames();
		doReturn(Optional.empty()).when(nc).getOptionalValue("a", String.class);

		Collection<PropertyChangeEvent> diff = ConfigChangeBuilder.compare(oc, nc);

		assertThat(diff).isNotNull().isNotEmpty().hasSize(1);

		PropertyChangeEvent change = diff.iterator().next();

		assertThat(change).isNotNull();
		assertThat(change.getNewValue()).isNull();
		assertThat(change.getOldValue()).isEqualTo("19");
		assertThat(change.getPropertyName()).isEqualTo("a");
	}

	@Test
	public void compareReturnsAChangeEventIfValueOfExistingKeyHasChanged() {
		Config oc = Mockito.mock(Config.class, new MethodNotMockedAnswer());
		Config nc = Mockito.mock(Config.class, new MethodNotMockedAnswer());

		Map<String, String> valuesOC = new HashMap<String, String>();
		valuesOC.put("a", "91");

		doReturn(valuesOC.keySet()).when(oc).getPropertyNames();
		doReturn(Optional.of("91")).when(oc).getOptionalValue("a", String.class);
		doReturn("91").when(oc).getValue("a",String.class);
		doReturn("old Config").when(oc).toString();

		Map<String, String> valuesNC = new HashMap<String, String>();
		valuesNC.put("a", "19");

		doReturn(valuesNC.keySet()).when(nc).getPropertyNames();
		doReturn(Optional.of("19")).when(nc).getOptionalValue("a",String.class);
		doReturn("19").when(nc).getValue("a",String.class);
		doReturn("new Config").when(nc).toString();

		Collection<PropertyChangeEvent> diff = ConfigChangeBuilder.compare(oc, nc);

		assertThat(diff).isNotNull().isNotEmpty().hasSize(1);

		PropertyChangeEvent change = diff.iterator().next();

		assertThat(change).isNotNull();
		assertThat(change.getNewValue()).isEqualTo("19");
		assertThat(change.getOldValue()).isEqualTo("91");
		assertThat(change.getPropertyName()).isEqualTo("a");
	}

}