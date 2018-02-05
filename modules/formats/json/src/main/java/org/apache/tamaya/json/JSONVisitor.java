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

import java.util.*;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;


/**
 * Visitor implementation to read a JSON formatted input source.
 */
class JSONVisitor {
    private final JsonObject rootNode;
    private final Map<String, String> targetStore;

    JSONVisitor(JsonObject startNode, Map<String, String> target) {
        rootNode = startNode;
        targetStore = target;
    }

    public void run() {
        Deque<VisitingContext> stack = new ArrayDeque<>();

        stack.add(new VisitingContext(rootNode));
        boolean goOn = stack.peek().hasNext();

        if (goOn) {
            do {
                Map.Entry<String, JsonValue> current = stack.peek().nextElement();

                if (!(current.getValue() instanceof JsonStructure)) {
                    String key = stack.peek().getNSPrefix() + current.getKey();
                    String value;
                    JsonValue jsonValue = current.getValue();
                    switch(jsonValue.getValueType()) {
                        case NULL: value = null; break;
                        case FALSE: value = Boolean.FALSE.toString(); break;
                        case TRUE: value = Boolean.TRUE.toString(); break;
                        case NUMBER: value = jsonValue.toString(); break;
                        case STRING: value = ((JsonString) jsonValue).getString(); break;
                        default:
                            throw new IllegalStateException("Internal failure while processing JSON document.");
                    }
                    
                    targetStore.put(key, value);
                } else if (current.getValue() instanceof JsonObject) {
                    String key = stack.peek().getNSPrefix() + current.getKey();
                    JsonObject node = (JsonObject) current.getValue();
                    stack.push(new VisitingContext(node, key));
                } else if (current.getValue() instanceof JsonArray) {
                    String key = stack.peek().getNSPrefix() + current.getKey();
                    JsonArray array = (JsonArray) current.getValue();
                    stack.push(new VisitingContext(array, key));
                } else {
                    throw new IllegalStateException("Internal failure while processing JSON document.");
                }

                goOn = stack.peek().hasNext();

                while (!goOn && stack.size() > 0) {
                    stack.remove();
                    goOn = (stack.size() > 0) && stack.peek().hasNext();
                }
            } while (goOn);
        }
    }

    /**
     * Context for a sub context visited.
     */
    private static class VisitingContext {
        private final String namespace;
        private final JsonObject node;
        private final JsonArray array;
        private final Iterator<Map.Entry<String, JsonValue>> elements;

        public VisitingContext(JsonObject node) {
            this(node, "");
        }

        public VisitingContext(JsonObject rootNode, String currentNamespace) {
            namespace = currentNamespace;
            node = rootNode;
            array = null;
            elements = node.entrySet().iterator();
        }

        public VisitingContext(JsonArray array, String currentNamespace) {
            namespace = currentNamespace;
            this.array = array;
            this.node = null;
            Map<String,JsonValue> arrayMap = new HashMap<>();
            arrayMap.put("[array]", formatArray(array.iterator()));
            elements = arrayMap.entrySet().iterator();
        }

        private JsonValue formatArray(Iterator<JsonValue> iterator) {
            StringBuilder b = new StringBuilder();
            iterator.forEachRemaining(r -> b.append(r.toString().replace(",", "\\,")).append(','));
            if(b.length()>0){
                b.setLength(b.length()-1);
            }
            String elemsAsString = b.toString();
            return new JsonString(){

                @Override
                public ValueType getValueType() {
                    return ValueType.STRING;
                }

                @Override
                public String getString() {
                    return elemsAsString;
                }

                @Override
                public CharSequence getChars() {
                    return elemsAsString;
                }
            };
        }

        public Map.Entry<String, JsonValue> nextElement() {
            return elements.next();
        }


        public boolean hasNext() {
            return elements.hasNext();
        }

        public String getNSPrefix() {
            if(array!=null){
                return namespace;
            }
            return namespace.isEmpty() ? namespace : namespace + ".";
        }
    }
}
