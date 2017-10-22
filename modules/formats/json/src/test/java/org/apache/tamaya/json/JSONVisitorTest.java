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
package org.apache.tamaya.json;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.apache.tamaya.ConfigException;
import org.junit.Test;

public class JSONVisitorTest {

	@Test
	public void ensureJSONisParsedProperlyWithDifferentValueTypesFilteringOutEmptyValues() {
		JsonObject startNode = Json.createObjectBuilder().//
				add("key.sub", "value").//
				add("anotherKey", true).//
				add("notAnotherKey", false).//
				add("number", 4711).//
				add("null", JsonValue.NULL).//
				add("empty", JsonValue.EMPTY_JSON_OBJECT).//
				build();
		Map<String, String> targetStore = new HashMap<>();
		JSONVisitor visitor = new JSONVisitor(startNode, targetStore);
		assertThat(visitor).isNotNull();

		visitor.run();

		assertThat(targetStore).hasSize(5);
		assertThat(targetStore).containsKeys("key.sub", "anotherKey", "notAnotherKey", "number", "null");
		assertThat(targetStore).containsEntry("key.sub", "value");
		assertThat(targetStore).containsEntry("null", null);		
		assertThat(targetStore).containsEntry("anotherKey", "true");
		assertThat(targetStore).doesNotContainKey("empty");
	}

	@Test
	public void parsingWorksOnEmptyObject() {
		JsonObject startNode = Json.createObjectBuilder().build();

		Map<String, String> targetStore = new HashMap<>();
		JSONVisitor visitor = new JSONVisitor(startNode, targetStore);
		assertThat(visitor).isNotNull();

		visitor.run();
		assertThat(targetStore).isEmpty();
	}

	@Test(expected = ConfigException.class)
	public void arraysAreNotSupported() {
		JsonObject startNode = Json.createObjectBuilder().//
				add("arrayKey", Json.createArrayBuilder().build()).//
				build();
		Map<String, String> targetStore = new HashMap<>();
		JSONVisitor visitor = new JSONVisitor(startNode, targetStore);
		assertThat(visitor).isNotNull();
		visitor.run();
	}

}
