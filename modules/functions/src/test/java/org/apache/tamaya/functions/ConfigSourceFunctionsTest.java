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

import org.apache.tamaya.base.configsource.SimpleConfigSource;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Test;

import javax.config.spi.ConfigSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.apache.tamaya.functions.ConfigSourceFunctions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.Assert.*;


public class ConfigSourceFunctionsTest {

    /*
     * Tests for isKeyInSection(String, String)
     */

    @Test
    public void isKeyInSectionThrowsNPEIfKeyIsNull() {
        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                isKeyInSection("a.b.c", null);
            }
        }).isInstanceOf(NullPointerException.class)
          .hasMessage("Section keys must be given.");
    }

    @Test
    public void isKeyInSectionThrowsNPEIfSectionKeyIsNull() {
        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                isKeyInSection(null, "a.b.c");
            }
        }).isInstanceOf(NullPointerException.class)
          .hasMessage("Key must be given.");
    }

    @Test
    public void isKeyInSectionForKeyInRootSection() {
        String key = "key";
        String sectionKey = "";

        boolean result = isKeyInSection(key, sectionKey);

        assertThat(result).describedAs("Key '%s' is in root section '%s'")
                          .isTrue();
    }

    @Test
    public void isKeyInSectionForKeyInExplicitRootSection() {
        String key = "key";
        String sectionKey = ".";

        boolean result = isKeyInSection(key, sectionKey);

        assertThat(result).describedAs("Key '%s' is in root section '%s'")
                          .isTrue();
    }

    @Test
    public void isKeyInSectionForKeyInSection() throws Exception {
        String key = "abc.def.g.h.key";
        String section = "abc.def.g.h";

        boolean result = isKeyInSection(key, section);

        assertThat(result).describedAs("Key %s is in section %s", key, section)
                          .isTrue();
    }

    @Test
    public void isKeyInSectionForKeyNotInSection() throws Exception {
        String key = "abc.def.g.h.i.key";
        String section = "abc.def.g.h";

        boolean result = isKeyInSection(key, section);

        assertThat(result).describedAs("Key %s is not in section %s", key, section)
                          .isFalse();
    }

    @Test
    public void isKeyInSectionIgnoresTrailingDotAtTheEndOfTheSection() throws Exception {
        String key = "abc.def.g.h.key";
        String section = "abc.def.g.h.";

        boolean result = isKeyInSection(key, section);

        assertThat(result).describedAs("Key %s is in section %s", key, section)
                          .isTrue();
    }


    /*
     * Tests for isKeyInSection(String, String, String...)
     */

    @Test
    public void isKeyInSectionsStringStringStringVarargThrowsNPEIfKeyIsNull() {
        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                ConfigSourceFunctions.isKeyInSection(null, "a.b.", "a.b", "b.c");
            }
        }).isInstanceOf(NullPointerException.class)
          .hasMessage("Key must be given.");
    }

    @Test
    public void isKeyInSectionStringStringStringVarargshrowsNPEIfMoreSectionKeysIsNull() {
        // null should not cause any problems
        boolean result = isKeyInSection("key", "l.b", (String) null);

        assertThat(result).isFalse();
    }

    @Test
    public void isKeyInSectionStringStringStringVaragrsSectioOfKeyIsAtEndOfVarargs() {
        String section = "abc.def.";
        String key = section + "key";

        // null should not cause any problems
        boolean result = ConfigSourceFunctions.isKeyInSection(key, "l.b", null, "abc", section);

        assertThat(result).describedAs("Key '%s' is in section '%s'.", key, section).isTrue();
    }


    /*
     * Tests for sections(Map<String, String>)
     */

    // null as parameter

    // empty as parameter

    // all keys in root section

    // some keys in packages

    @Test
    public void sectionsMapReturnsAllSectionsForGivenKeysInMap() {
        HashMap<String, String> kv = new HashMap<>();

        kv.put("abc.key", "v");
        kv.put("abc.def.key", "v");
        kv.put("a.key", "v");
        kv.put("b.key", "v");
        kv.put("key", "v");

        Set<String> result = sections(kv);

        assertThat(result).isNotNull()
                          .isNotEmpty()
                          .contains("abc", "abc.def", "a", "b", "<root>");
    }

    @Test
    public void sectionsMapTreatsLeadingDotAsOptional() {
        HashMap<String, String> kv = new HashMap<>();

        kv.put(".abc.key", "v");
        kv.put(".abc.def.key", "v");
        kv.put(".a.key", "v");
        kv.put(".b.key", "v");
        kv.put(".key", "v");

        Set<String> result = sections(kv);

        assertThat(result).isNotNull()
                          .isNotEmpty()
                          .contains("abc", "abc.def", "a", "b", "<root>");
    }

    /*
     * Tests for sections(Map<String, String> , Predicate<String>)
     */

    @Test
    public void sectionsMapPredicateFiltersAccordingToFilter() {
        HashMap<String, String> kv = new HashMap<>();

        kv.put(".abc.key", "v");
        kv.put(".abc.def.key", "v");
        kv.put(".a.key", "v");
        kv.put(".b.key", "v");
        kv.put(".key", "v");

        Set<String> result = sections(kv, new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return !s.startsWith("a");
            }
        });

        assertThat(result).isNotNull()
                          .isNotEmpty()
                          .contains("b", "<root>");
    }

    /*
     * Tests for transitiveSections(Map<String, String>)
     */

    @Test
    public void bla() {
        HashMap<String, String> kv = new HashMap<>();

        kv.put(".abc.key", "v");
        kv.put(".abc.def.key", "v");
        kv.put(".abc.def.ghi.key", "v");
        kv.put(".a.key", "v");
        kv.put(".b.key", "v");
        kv.put(".key", "v");

        Set<String> result = transitiveSections(kv);

        for (String s : result) {
            System.out.println(s);
        }


        assertThat(result).isNotNull()
                          .isNotEmpty()
                          .contains("abc", "abc.def", "a", "b", "<root>");


    }


    @Test
    public void testIsKeyInSections() throws Exception {
        SimpleConfigSource configSource = SimpleConfigSource.builder("test")
                .withProperty("a", "1")
                .withProperty("a.a.1", "1.1.1")
                .withProperty("a.a.2", "1.1.2")
                .withProperty("a.b", "1.2")
                .withProperty("b", "2")
                .build();

        assertTrue(ConfigSourceFunctions.isKeyInSection("a.1", "a", "b"));
        assertTrue(ConfigSourceFunctions.isKeyInSection("a.1", "b", "a"));
        assertFalse(ConfigSourceFunctions.isKeyInSection("a.1", ""));
        assertFalse(ConfigSourceFunctions.isKeyInSection("a.b.1", "a"));
        assertTrue(ConfigSourceFunctions.isKeyInSection("a.b.1", "a.b"));
        assertFalse(ConfigSourceFunctions.isKeyInSection("a.b.1", "", "a"));
        assertTrue(ConfigSourceFunctions.isKeyInSection("a.b.1", "", "a.b"));
        assertFalse(ConfigSourceFunctions.isKeyInSection("c.a",  "","c.a"));
        assertFalse(ConfigSourceFunctions.isKeyInSection("a.3", "b"));
    }


    @Test
    public void testTransitiveSections() throws Exception {
        Map<String,String> sections = new HashMap<>();
        sections.put("a.b.c.d.e", "1");
        sections.put("a.x.y", "2");
        sections.put("bb", "2");
        Set<String> transitiveSections = ConfigSourceFunctions.transitiveSections(sections);
        assertThat(transitiveSections)
                .isNotNull()
                .contains("a", "a.b", "a.b.c", "<root>");
    }


    @Test
    public void testSectionsRecursive() throws Exception {
        Map<String,String> sections = new HashMap<>();
        sections.put("a.b.c.d.e", "1");
        sections.put("a.x.y", "2");
        sections.put("b.b", "2");
        sections.put("bb", "2");
        Map<String,String> recursiveSections = ConfigSourceFunctions.sectionsRecursive(sections, true,"a.b");
        assertThat(recursiveSections)
                .isNotNull()
                .hasSize(1)
                .contains(entry("c.d.e", "1"));
        recursiveSections = ConfigSourceFunctions.sectionsRecursive(sections, true,"a");
        assertThat(recursiveSections)
                .isNotNull()
                .hasSize(2)
                .contains(entry("b.c.d.e", "1"), entry("x.y", "2"));
        recursiveSections = ConfigSourceFunctions.sectionsRecursive(sections, true,"b");
        assertThat(recursiveSections)
                .isNotNull()
                .hasSize(1)
                .contains(entry("b", "2"));
        recursiveSections = ConfigSourceFunctions.sectionsRecursive(sections, false,"a.b");
        assertThat(recursiveSections)
                .isNotNull()
                .hasSize(1)
                .contains(entry("a.b.c.d.e", "1"));
        recursiveSections = ConfigSourceFunctions.sectionsRecursive(sections, false,"a");
        assertThat(recursiveSections)
                .isNotNull()
                .hasSize(2)
                .contains(entry("a.b.c.d.e", "1"), entry("a.x.y", "2"));
        recursiveSections = ConfigSourceFunctions.sectionsRecursive(sections, false,"b");
        assertThat(recursiveSections)
                .isNotNull()
                .hasSize(1)
                .contains(entry("b.b", "2"));
        recursiveSections = ConfigSourceFunctions.sectionsRecursive(sections, true,"b");
        assertThat(recursiveSections)
                .isNotNull()
                .hasSize(1)
                .contains(entry("b", "2"));
    }

    @Test
    public void testStripSectionKeys() throws Exception {
        String result = ConfigSourceFunctions.stripSectionKeys("a.b.c", new String[]{ "a.b", "a"});
        assertThat(result)
                .isNotNull().isEqualTo("c");
        result = ConfigSourceFunctions.stripSectionKeys("a", new String[]{ "a"});
        assertThat(result)
                .isNotNull().isEqualTo("a");
        result = ConfigSourceFunctions.stripSectionKeys("foo.bar", new String[]{ ""});
        assertThat(result)
                .isNotNull().isEqualTo("foo.bar");
    }

    @Test
    public void testAddItems_Override_Default() throws Exception {
        ConfigSource cs = SimpleConfigSource.builder("test")
                .withProperty("a", "1")
                .withProperty("b", "2")
                .build();
        Map<String, String> additions = new HashMap<>();
        additions.put("b", "2-added");
        additions.put("c", "3");
        ConfigSource configSource = ConfigSourceFunctions.addItems(cs, additions);
        assertNotNull(configSource);
        assertThat(configSource.getPropertyNames())
                .isNotNull()
                .contains("a", "b", "c");
        assertThat(configSource.getProperties())
                .isNotNull()
                .contains(entry("a", "1"), entry("b", "2"), entry("c", "3"));
    }

    @Test
    public void testAddItemsOverride_Explicit() throws Exception {
        ConfigSource cs = SimpleConfigSource.builder("test")
                .withProperty("a", "1")
                .withProperty("b", "2")
                .build();
        Map<String, String> additions = new HashMap<>();
        additions.put("b", "2-added");
        additions.put("c", "3");
        ConfigSource configSource = ConfigSourceFunctions.addItems(cs, additions, true);
        assertNotNull(configSource);
        assertThat(configSource.getPropertyNames())
                .isNotNull()
                .contains("a", "b", "c");
        assertThat(configSource.getProperties())
                .isNotNull()
                .contains(entry("a", "1"), entry("b", "2-added"), entry("c", "3"));
    }

    @Test
    public void testAddItemsOverride_False() throws Exception {
        ConfigSource cs = SimpleConfigSource.builder("test")
                .withProperty("a", "1")
                .withProperty("b", "2")
                .build();
        Map<String, String> additions = new HashMap<>();
        additions.put("b", "2-added");
        additions.put("c", "3");
        ConfigSource configSource = ConfigSourceFunctions.addItems(cs, additions, false);
        assertNotNull(configSource);
        assertThat(configSource.getPropertyNames())
                .isNotNull()
                .contains("a", "b", "c");
        assertThat(configSource.getProperties())
                .isNotNull()
                .contains(entry("a", "1"), entry("b", "2"), entry("c", "3"));
    }

    @Test
    public void testReplaceItems() throws Exception {
        ConfigSource cs = SimpleConfigSource.builder("test")
                .withProperty("a", "1")
                .withProperty("b", "2")
                .build();
        Map<String, String> replacements = new HashMap<>();
        replacements.put("b", "2-added");
        replacements.put("c", "3");
        ConfigSource configSource = ConfigSourceFunctions.replaceItems(cs, replacements);
        assertNotNull(configSource);
        assertThat(configSource.getPropertyNames())
                .isNotNull()
                .contains("a", "b", "c");
        assertThat(configSource.getProperties())
                .isNotNull()
                .contains(entry("a", "1"), entry("b", "2-added"), entry("c", "3"));
    }

    @Test
    public void testEmptyPropertySource() throws Exception {
        ConfigSource ps = ConfigSourceFunctions.emptyConfigSource();
        assertThat(ps).isNotNull();
        assertNotNull(ps.getProperties());
        assertTrue(ps.getProperties().isEmpty());
        assertEquals(ps.getName(), "<empty>" );
    }
}