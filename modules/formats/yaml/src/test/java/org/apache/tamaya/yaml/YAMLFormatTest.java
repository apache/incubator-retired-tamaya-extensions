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
package org.apache.tamaya.yaml;


import org.apache.tamaya.format.ConfigurationData;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class YAMLFormatTest {
    private final YAMLFormat format = new YAMLFormat();

    @Test
    public void testAcceptURL() throws MalformedURLException {
        assertTrue(format.accepts(new URL("http://127.0.0.1/anyfile.yaml")));
    }

    @Test
    public void testAcceptURL_BC1() throws MalformedURLException {
        assertFalse(format.accepts(new URL("http://127.0.0.1/anyfile.YAML")));
    }

    @Test(expected = NullPointerException.class)
    public void testAcceptURL_BC2() throws MalformedURLException {
        assertFalse(format.accepts(null));
    }

    @Test
    public void testAcceptURL_BC3() throws MalformedURLException {
        assertFalse(format.accepts(new URL("http://127.0.0.1/anyfile.docx")));
    }

    @Test
    public void testRead() throws IOException {
        URL configURL = getContactYaml();
        assertTrue(format.accepts(configURL));
        ConfigurationData data = loadConfigurationData(configURL);
        assertNotNull(data);
        for(Map.Entry<String,String> en:data.getDefaultProperties().entrySet()) {
            System.out.println(en.getKey() + " -> " + en.getValue());
        }
    }
    
    @Test
    public void testRead_nullValues() throws IOException {
    	URL configURL = getContactYaml();
        ConfigurationData data = loadConfigurationData(configURL);
        assertFalse(data.getDefaultProperties().containsKey("summary"));
    }

	private ConfigurationData loadConfigurationData(URL configURL) throws IOException {
		return format.readConfiguration(configURL.toString(), configURL.openStream());
	}

	private URL getContactYaml() {
		URL configURL = YAMLPropertySourceTest.class.getResource("/configs/valid/contact.yaml");
		return configURL;
	}

}