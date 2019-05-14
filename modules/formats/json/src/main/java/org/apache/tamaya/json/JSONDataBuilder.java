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

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.apache.tamaya.ConfigException;
import org.apache.tamaya.spi.ListValue;
import org.apache.tamaya.spi.ObjectValue;
import org.apache.tamaya.spi.PropertyValue;

import java.util.Objects;

/**
 * Visitor implementation to read a JSON toString input source.
 */
class JSONDataBuilder {

    private String resource;
    private JsonValue root;

    JSONDataBuilder(String resource, JsonValue root) {
        this.resource = Objects.requireNonNull(resource);
        this.root = root;
    }

    private void addJsonObject(JsonObject jsonObject, ObjectValue dataNode){
        jsonObject.forEach((key,val) -> {
            switch(val.getValueType()) {
                case FALSE:
                    dataNode.setValue(key, Boolean.FALSE.toString());
                    break;
                case TRUE:
                    dataNode.setValue(key, Boolean.TRUE.toString());
                    break;
                case NUMBER:
                    dataNode.setValue(key, val.toString());
                    break;
                case STRING:
                    dataNode.setValue(key, ((JsonString) val).getString());
                    break;
                case NULL:
                    dataNode.setValue(key, null);
                    break;
                case OBJECT:
                    ObjectValue oval = dataNode.addObject(key);
                    addJsonObject((JsonObject)val, oval);
                    break;
                case ARRAY:
                    ListValue aval = dataNode.addList(key);
                    addArray((JsonArray)val, aval);
                    break;
                default:
                    throw new ConfigException("Internal failure while processing JSON document.");
            }
        });
    }

    private void addArray(JsonArray array, ListValue dataNode) {
        array.forEach(val -> {
            switch(val.getValueType()) {
                case NULL:
                    break;
                case FALSE:
                    dataNode.addValue(Boolean.FALSE.toString());
                    break;
                case TRUE:
                    dataNode.addValue(Boolean.TRUE.toString());
                    break;
                case NUMBER:
                    dataNode.addValue(val.toString());
                    break;
                case STRING:
                    dataNode.addValue(((JsonString) val).getString());
                    break;
                case OBJECT:
                    ObjectValue oval = dataNode.addObject();
                    addJsonObject((JsonObject)val, oval);
                    break;
                case ARRAY:
                    ListValue aval = dataNode.addList();
                    addArray((JsonArray)val, aval);
                    break;
                default:
                    throw new ConfigException("Internal failure while processing JSON document.");
            }
        });
    }

    public PropertyValue build() {
        PropertyValue data;
        if (root instanceof JsonObject) {
            data = PropertyValue.createObject("");
            addJsonObject((JsonObject)root, (ObjectValue) data);
        } else if (root instanceof JsonArray) {
            JsonArray array = (JsonArray)root;
            data = PropertyValue.createList("");
            addArray(array, (ListValue) data);
        } else {
            throw new ConfigException("Unknown JsonType encountered: " + root.getClass().getName());
        }
        data.setMeta("resource", resource);
        data.setMeta("format", "json");
        return data;
    }

}
