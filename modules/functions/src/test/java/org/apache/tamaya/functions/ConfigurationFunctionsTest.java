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
package org.apache.tamaya.functions;

import org.apache.tamaya.base.configsource.EnvironmentConfigSource;
import org.apache.tamaya.base.configsource.SystemConfigSource;
import org.junit.Test;

import javax.config.Config;
import javax.config.spi.ConfigBuilder;
import javax.config.spi.ConfigProviderResolver;
import java.io.PrintStream;

import static org.junit.Assert.*;

/**
 * Created by Anatole on 01.10.2015.
 */
public class ConfigurationFunctionsTest {

    @Test
    public void testFilter() throws Exception {
// TODO implement test
    }

    @Test
    public void testMap() throws Exception {
// TODO implement test
    }

    @Test
    public void testSection() throws Exception {
        // Tests with/without stripping of keys:
        // See https://issues.apache.org/jira/browse/TAMAYA-235
        testSection(false);
        testSection(true);
    }

    @Test
    public void testSection1() throws Exception {
// TODO implement test
    }

    @Test
    public void testIsKeyInSection() throws Exception {
// TODO implement test
    }

    @Test
    public void testIsKeyInSections() throws Exception {
// TODO implement test
    }

    @Test
    public void testSections() throws Exception {
// TODO implement test
    }

    @Test
    public void testTransitiveSections() throws Exception {
// TODO implement test
    }

    @Test
    public void testSections1() throws Exception {
// TODO implement test
    }

    @Test
    public void testTransitiveSections1() throws Exception {
// TODO implement test
    }

    @Test
    public void testSectionsRecursive() throws Exception {
// TODO implement test
    }

    @Test
    public void testCombine() throws Exception {
// TODO implement test
    }

    @Test
    public void testPropertySourceFrom() throws Exception {
// TODO implement test
    }

    @Test
    public void testSectionRecursive() throws Exception {
// TODO implement test
    }

    @Test
    public void testJsonInfo() throws Exception {
// TODO implement test
    }

    @Test
    public void testJsonInfo1() throws Exception {
// TODO implement test
    }

    @Test
    public void testXmlInfo() throws Exception {
// TODO implement test
    }

    @Test
    public void testXmlInfo1() throws Exception {
// TODO implement test
    }

    @Test
    public void testTextInfo() throws Exception {
// TODO implement test
    }

    @Test
    public void testTextInfo1() throws Exception {
// TODO implement test
    }

    @Test
    public void testAddItems() throws Exception {
// TODO implement test
    }

    @Test
    public void testEmptyConfiguration() throws Exception {
        Config ps = ConfigurationFunctions.emptyConfig();
        assertNotNull(ps);
        assertNotNull(ps.getPropertyNames());
        assertFalse(ps.getPropertyNames().iterator().hasNext());
    }


    private void testSection(boolean stripKeys){
        ConfigBuilder b = ConfigProviderResolver.instance().getBuilder()
                .withSources(new EnvironmentConfigSource(), new SystemConfigSource());
        Config cfg = ConfigurationFunctions.section("java.", stripKeys)
            .apply(b.build());
        System.out.println("*****************************************************");
        System.out.println("stripKeys: " + stripKeys);
        System.out.println("*****************************************************");
        dump(cfg.getPropertyNames(), cfg, System.out);
        System.out.println();
        System.out.println("Example Metadata:");
        System.out.println("\tjava.version         :  " + cfg.getValue("java.version", String.class));
        System.out.println("\tversion                 :  " + cfg.getValue("version", String.class));
    }

    private void dump(Iterable<String> keys, Config config, PrintStream stream) {
        stream.println("FULL DUMP:");
        for (String key : keys) {
            stream.println("\t" + key + " = " + config.getValue(key, String.class));
        }
    }
}