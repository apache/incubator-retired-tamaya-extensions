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
package org.apache.tamaya.inject.spi;

import org.apache.tamaya.inject.api.KeyResolver;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Key resolver, which evaluates the keys to {@code propertyKey} only.
 */
public final class AbsoluteKeyResolver implements KeyResolver {
    
    @Override
    public List<String> resolveKeys(List<String> propertyKeys, List<String> alternateKeys, Member member) {
        List<String> result = new ArrayList<>();
        String sectionName = getSectionName(member);
        for(String key:propertyKeys) {
            result.add(evaluateKey(sectionName, key));
        }
        for(String fk:alternateKeys){
            result.add(evaluateKey(sectionName, fk));
        }
        return result;
    }

    private String evaluateKey(String sectionName, String key) {
        key = key.trim();
        if(key.startsWith("[") && key.endsWith("]")) {
            return key.substring(1, key.length()-1);
        }else if(sectionName!=null) {
            return sectionName + "." + key;
        }
        return key;
    }

    private String getSectionName(Member member) {
        return null;
    }
}
