/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tamaya.springexample;

import java.util.Date;
import java.util.Map;

import org.apache.tamaya.inject.api.Config;
import org.apache.tamaya.inject.api.DynamicValue;
import org.apache.tamaya.inject.api.UpdatePolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WelcomeController {

	@Value("${application.message:Hello World}")
	private String message = "Hello World";

	@Config(value = "background.color", required = false)
	private String backgroundColor = "#BBBBBB";

	@Config(value = "foreground.color", required = false, defaultValue = "#DDDDDD")
	private DynamicValue<String> foregroundColor;

	@GetMapping("/")
	public String welcome(Map<String, Object> model) {
		foregroundColor.setUpdatePolicy(UpdatePolicy.IMMEDIATE);
		model.put("time", new Date());
		model.put("message", this.message);
		model.put("background", this.backgroundColor);
		model.put("foreground", this.foregroundColor.get());
		return "welcome";
	}

}