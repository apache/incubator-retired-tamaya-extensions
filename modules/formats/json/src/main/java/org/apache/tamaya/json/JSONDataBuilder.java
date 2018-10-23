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
import org.apache.tamaya.spi.PropertyValue;

/**
 * Visitor implementation to read a JSON asString input source.
 */
class JSONDataBuilder {

    private PropertyValue data = PropertyValue.create();
    private JsonValue root;

    JSONDataBuilder(String resource, JsonValue root) {
        data.setMeta("resource", resource);
        data.setMeta("format", "json");
        this.root = root;
    }

    private void addJsonObject(JsonObject jsonObject, PropertyValue parent, String objectKey){
        PropertyValue dataNode = objectKey==null?parent:parent.getOrCreateChild(objectKey);
        jsonObject.forEach((key,val) -> {
            switch(val.getValueType()) {
                case FALSE:
                    dataNode.addProperty(key, Boolean.FALSE.toString());
                    break;
                case TRUE:
                    dataNode.addProperty(key, Boolean.TRUE.toString());
                    break;
                case NUMBER:
                    dataNode.addProperty(key, val.toString());
                    break;
                case STRING:
                    dataNode.addProperty(key, ((JsonString) val).getString());
                    break;
                case NULL:
                    dataNode.addProperty(key, null);
                    break;
                case OBJECT:
                    addJsonObject((JsonObject)val, dataNode, key);
                    break;
                case ARRAY:
                    addArray((JsonArray)val, dataNode, key);
                    break;
                default:
                    throw new ConfigException("Internal failure while processing JSON document.");
            }
        });
    }

    private void addArray(JsonArray array, PropertyValue parent, String arrayKey) {
        array.forEach(val -> {
            PropertyValue dataNode = parent.createChild(arrayKey, true);
            switch(val.getValueType()) {
                case NULL:
                    break;
                case FALSE:
                    dataNode.setValue(Boolean.FALSE.toString());
                    break;
                case TRUE:
                    dataNode.setValue(Boolean.TRUE.toString());
                    break;
                case NUMBER:
                    dataNode.setValue(val.toString());
                    break;
                case STRING:
                    dataNode.setValue(((JsonString) val).getString());
                    break;
                case OBJECT:
                    addJsonObject((JsonObject)val, dataNode, null);
                    break;
                case ARRAY:
                    addArray((JsonArray)val, dataNode, "");
                    break;
                default:
                    throw new ConfigException("Internal failure while processing JSON document.");
            }
        });
    }

    public PropertyValue build() {
        if (root instanceof JsonObject) {
            addJsonObject((JsonObject)root, data, null);
        } else if (root instanceof JsonArray) {
            JsonArray array = (JsonArray)root;
            addArray(array, data, "");
        } else {
            throw new ConfigException("Unknown JsonType encountered: " + root.getClass().getName());
        }
        return data;
    }

}
