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

import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertyValue;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Anatole on 04.01.2015.
 */
public class MyTestPropertySource implements PropertySource{

    private final Map<String,String> properties = new HashMap<>();

    public MyTestPropertySource(){
        properties.put("Expression Only", "${java.version}");
        properties.put("Expression Only (prefixed)", "${sys:java.version}");
        properties.put("Before Text", "My Java version is ${java.version}");
        properties.put("Before Text (prefixed)", "My Java version is ${sys:java.version}");
        properties.put("Before and After Text", "My Java version is ${java.version}.");
        properties.put("Before and After Text (prefixed)", "My Java version is ${sys:java.version}.");
        properties.put("Multi-expression", "Java version ${sys:java.version} and line.separator ${line.separator}.");

        properties.put("cp-ref", "${resource:Testresource.txt}");
        properties.put("file-ref", "${file:"+getFileRefAsString("Testresource2.txt")+"}");
        properties.put("file3-ref", "${file:"+getFileRefAsString("Testresource3.txt")+"}");
        properties.put("res-ref", "${resource:Test?es*ce.txt}");
        properties.put("url-ref", "${url:http://www.google.com}");
        properties.put("config-ref", "Expression Only -> ${conf:Expression Only}");
        properties.put("config-ref2", "Config Ref 2 -> Ref 1: ${conf:config-ref}");
        properties.put("config-ref3", "Config Ref 3 -> Ref 2: ${conf:config-ref2}");

        properties.put("Will fail1.", "V$java.version");
        properties.put("Will fail2.", "V$java.version}");
        properties.put("Will not fail3.", "V${java.version");
        properties.put("Will not fail1.", "V$\\{java.version");
        properties.put("Will not fail2.", "V\\${java.version");

        properties.put("env.keys", "${java.version} plus $java.version");

        properties.put("escaped", "Config Ref 3 -> Ref 2: \\${conf:config-ref2 will not be evaluated and will not contain\\t tabs \\n " +
                "newlines or \\r returns...YEP!");
    }

    private String getFileRefAsString(String filename) {
        try {
            URL res = getClass().getClassLoader().getResource(filename);
            if(res==null){
                return null;
            }
            return new File(res.toURI()).getAbsolutePath().replaceAll("\\\\","/");
        } catch (URISyntaxException e) {
            return "Failed to evaluate file: " + filename;
        }
    }

    @Override
    public int getOrdinal() {
        return 0;
    }

    @Override
    public String getName() {
        return "test";
    }

    @Override
    public PropertyValue get(String key) {
        return PropertyValue.createValue(key, properties.get(key)).setMeta("source", getName());
    }

    @Override
    public Map<String, PropertyValue> getProperties() {
        Map<String, PropertyValue> res = new HashMap<>();
        for(Map.Entry<String,String> en:properties.entrySet()){
            res.put(en.getKey(), PropertyValue.createValue(en.getKey(), en.getValue()));
        }
        return res;
    }

}
