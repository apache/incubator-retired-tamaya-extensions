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
import static org.junit.Assert.assertEquals;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.apache.tamaya.spi.ObjectValue;
import org.apache.tamaya.spi.PropertyValue;
import org.junit.Test;

public class JSONVisitorTest {

	@Test
	public void ensureJSONisParsedProperlyWithDifferentValueTypesFilteringOutEmptyValues() {
		JsonObject startNode = Json.createObjectBuilder().//
				add("key.sub", "createValue").//
				add("anotherKey", true).//
				add("notAnotherKey", false).//
				add("number", 4711).//
				add("null", JsonValue.NULL).//
				add("empty", JsonValue.EMPTY_JSON_OBJECT).//
				build();
		JSONDataBuilder visitor = new JSONDataBuilder("Test:ensureJSONisParsedProperlyWithDifferentValueTypesFilteringOutEmptyValues", startNode);

		PropertyValue data = visitor.build();
		assertThat(data).isNotNull();

		ObjectValue ov = data.toObjectValue();
		assertThat(ov.getValues().size() == 6);
		assertEquals(data.getSize(), 6);
		assertThat(data.toMap()).containsKeys("key.sub", "anotherKey", "notAnotherKey", "number", "null");
		assertThat(data.toMap()).containsEntry("key.sub", "createValue");
		assertThat(data.toMap()).containsEntry("null", null);
		assertThat(data.toMap()).containsEntry("anotherKey", "true");
		assertThat(data.toMap()).doesNotContainEntry("empty", null);
	}

	@Test
	public void parsingWorksOnEmptyObject() {
		JsonObject startNode = Json.createObjectBuilder().build();

		Map<String, String> targetStore = new HashMap<>();
		JSONDataBuilder visitor = new JSONDataBuilder("Test:parsingWorksOnEmptyObject", startNode);
		PropertyValue data = visitor.build();
		assertThat(data).isNotNull();
		assertThat(data.isLeaf());
	}

	@Test
	public void arrayInObject() {
		JsonObject startNode = Json.createObjectBuilder().//
				add("arrayKey", Json.createArrayBuilder().build()).//
				build();
		JSONDataBuilder visitor = new JSONDataBuilder("Test:array", startNode);
		PropertyValue data = visitor.build();
		assertThat(data).isNotNull();
		System.out.println(data.asString());
	}

	@Test
	public void array() {
		JsonArray startNode = Json.createArrayBuilder().//
				add(Json.createObjectBuilder().add("k1", 1).add("k2", 2).build()).//
				add(Json.createObjectBuilder().add("k1", 1).add("k2", 2).build()).//
				add(Json.createArrayBuilder().add(Json.createObjectBuilder().add("k31", "v31").add("k32", false).build()).build()).//
				build();
		JSONDataBuilder visitor = new JSONDataBuilder("Test:array", startNode);
		PropertyValue data = visitor.build();
		assertThat(data).isNotNull();
		System.out.println(data.asString());
	}

}
