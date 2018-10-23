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

import java.awt.*;
import java.util.Date;
import java.util.Map;

import org.apache.tamaya.Configuration;
import org.apache.tamaya.functions.ConfigurationFunctions;
import org.apache.tamaya.inject.api.Config;
import org.apache.tamaya.inject.api.DynamicValue;
import org.apache.tamaya.inject.api.UpdatePolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class WelcomeController {
	
	private static final String FOREGROUND_DEFAULT = "#DDDDDD"; 

	@Value("${application.message:Hello World}")
	private String message = "Hello World";

	@Config(value = "background.color", required = false)
	private String backgroundColor = "#BBBBBB";

	@Config(value = "foreground.color", required = false, defaultValue = FOREGROUND_DEFAULT)
	private DynamicValue<String> foregroundColor;

	@Config(value = "background.color", required = false)
	private Color bgColor;

	@GetMapping("/")
	public String welcome(Map<String, Object> model) {
		foregroundColor.setUpdatePolicy(UpdatePolicy.IMMEDIATE);
		model.put("time", new Date());
		model.put("message", this.message);
		model.put("background", this.backgroundColor);
		model.put("foreground", this.foregroundColor.get());
		return "welcome";
	}

	@GetMapping("/update")
	public String update(@RequestParam("foreground") String newForeground, Map<String, Object> model) {
		foregroundColor.setUpdatePolicy(UpdatePolicy.IMMEDIATE);
		if(newForeground!=null && !newForeground.isEmpty()){
			System.out.println("Setting new foreground: " + newForeground+"...");
			System.setProperty("foreground.color", newForeground);
		}
		model.put("time", new Date());
		model.put("message", this.message);
		model.put("background", this.backgroundColor);
		model.put("foreground", this.foregroundColor.get());
		return "welcome";
	}

    @GetMapping("/config")
    public String config(Map<String, Object> model) {
        Configuration config = Configuration.current();
        model.put("filter", "NO FILTER");
        model.put("config", config
                    .adapt(ConfigurationFunctions.textInfo()));
        return "config";
    }

    @GetMapping(value="/config/{path}")
	public String config(@PathVariable("path") String path, Map<String, Object> model) {
        Configuration config = Configuration.current();
        model.put("filter", path);
        model.put("config", config.map(ConfigurationFunctions.section(path))
					.adapt(ConfigurationFunctions.textInfo()));
		return "config";
	}

}