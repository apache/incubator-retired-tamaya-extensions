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
package org.apache.tamaya.inject.api;

import java.lang.reflect.Member;
import java.util.List;

/**
 * Implementations of this class can be configured to delegate the resolution of configuration lookup keys.
 * The default resolution in place is
 */
@FunctionalInterface
public interface KeyResolver {

  /**
   * Evaluates the effective configuration keys in order of significance.
   * @param propertyKeys the main property keys, never null. The first key is the most significant property key, whereas
   *                     subsequent keys may be derived, e.g. by replacing '_' or camel case with '.' etc.
   * @param fallbackKeys the configured fallback keys.
   * @param member the annotated {@link java.lang.reflect.Field} or {@link java.lang.reflect.Method}.
   * @return the absolute keys to be evaluated in order of precedence (most significant first).
   */
  List<String> resolveKeys(List<String> propertyKeys, List<String> fallbackKeys, Member member);

}