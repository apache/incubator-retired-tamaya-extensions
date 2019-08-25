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
package org.apache.tamaya.resolver;

import org.apache.tamaya.resolver.internal.DefaultExpressionEvaluator;
import org.apache.tamaya.resolver.internal.ExpressionResolutionFilter;
import org.apache.tamaya.resolver.spi.ExpressionEvaluator;
import org.apache.tamaya.resolver.spi.ExpressionResolver;
import org.apache.tamaya.spi.PropertyValue;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Creates and test a manual filter setup using a custom resolver.
 */
public class ManualSetupTest {

    @Test
    public void testManualFilterSetup(){
        ExpressionEvaluator evaluator = new DefaultExpressionEvaluator(Arrays.asList(new CustomResolver()));
        ExpressionResolutionFilter reolverFilter = new ExpressionResolutionFilter(evaluator);
        PropertyValue filtered = reolverFilter.filterProperty(PropertyValue.createValue("foo", "${foo:cccabABabbaaaba}"), null);
        PropertyValue unfiltered = reolverFilter.filterProperty(PropertyValue.createValue("foo", "${url:cccabABabbaaaba}"), null);
        assertThat(filtered).isNotNull();
        assertThat(unfiltered).isNotNull();
        assertThat(filtered.getValue()).isEqualTo("CCCBBABBBBBBBBB");
        assertThat(unfiltered.getValue()).isEqualTo("?{url:cccabABabbaaaba}");
    }

    private static final class CustomResolver implements ExpressionResolver{

        @Override
        public String getResolverPrefix() {
            return "foo:";
        }

        @Override
        public String evaluate(String expression) {
            return expression.replaceAll("a", "b").toUpperCase();
        }
    }

}
