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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.resolver.internal.DefaultExpressionEvaluator;
import org.apache.tamaya.resolver.internal.ExpressionResolutionFilter;
import org.apache.tamaya.resolver.spi.ExpressionEvaluator;
import org.apache.tamaya.resolver.spi.ExpressionResolver;
import org.junit.Test;

public class CustomExpressionResolverConfigurationTest {

	@Test
	public void withCustomDecryptResolver() {
		Configuration configuration = createConfiguration(new DecryptResolver());
		
		String sysPropKey = "mySystemProperty";
		try {
			System.setProperty(sysPropKey, "${decrypt:1234}");
			assertEquals("1234.decrypted", configuration.get("mySystemProperty"));
		} finally {
			System.clearProperty(sysPropKey);
		}
	}
	
	@Test
	public void withNoResolver() {
		Configuration configuration = createConfiguration();
		
		String sysPropKey = "mySystemProperty";
		try {
			System.setProperty(sysPropKey, "${decrypt:1234}");
			assertEquals("?{decrypt:1234}", configuration.get("mySystemProperty"));
		} finally {
			System.clearProperty(sysPropKey);
		}
	}

	private Configuration createConfiguration(ExpressionResolver... resolvers) {
		ExpressionEvaluator evaluator = new DefaultExpressionEvaluator(new ArrayList<>(Arrays.asList(resolvers)));
		ExpressionResolutionFilter filter = new ExpressionResolutionFilter(evaluator);
		
		Configuration configuration = ConfigurationProvider.getConfigurationBuilder()
			.addDefaultPropertySources()
			.addPropertyFilters(filter)
			.build();
		return configuration;
	}
	
	private static class DecryptResolver implements ExpressionResolver {

		@Override
		public String getResolverPrefix() {
			return "decrypt:";
		}

		@Override
		public String evaluate(String expression) {
			return expression + ".decrypted";
		}
	}
	
}
