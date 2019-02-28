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

import org.apache.tamaya.spi.PropertySource;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Set;

import static org.apache.tamaya.functions.PropertySourceFunctions.isKeyInSection;
import static org.apache.tamaya.functions.PropertySourceFunctions.isKeyInSections;
import static org.apache.tamaya.functions.PropertySourceFunctions.sections;
import static org.apache.tamaya.functions.PropertySourceFunctions.transitiveSections;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PropertySourceFunctionsTest {

    @Ignore
    @Test
    public void testAddMetaData() throws Exception {
        throw new RuntimeException("Not implement or look at me!");
    }

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
          .hasMessage("Section key must be given.");
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
     * Tests for isKeyInSections(String, String, String...)
     */

    @Test
    public void isKeyInSectionsStringStringStringVarargThrowsNPEIfKeyIsNull() {
        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                isKeyInSections(null, "a.b.", "a.b", "b.c");
            }
        }).isInstanceOf(NullPointerException.class)
          .hasMessage("Key must be given.");
    }

    @Test
    public void isKeyInSectionsStringStringStringVarargsThrowsNPEIfFirstSectionIsNotGiven() {
        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                isKeyInSections("key", null, "a.b");
            }
        }).isInstanceOf(NullPointerException.class)
          .hasMessage("At least one section key must be given.");
    }

    @Test
    public void isKeyInSectionsStringStringStringVarargshrowsNPEIfMoreSectionKeysIsNull() {
        // null should not cause any problems
        boolean result = isKeyInSections("key", "l.b", (String) null);

        assertThat(result).isFalse();
    }

    @Test
    public void isKeyInSectionsStringStringStringVaragrsSectioOfKeyIsAtEndOfVarargs() {
        String section = "abc.def.";
        String key = section + "key";

        // null should not cause any problems
        boolean result = isKeyInSections(key, "l.b", null, "abc", section);

        assertThat(result).describedAs("Key '%s' is in section '%s'.", key, section).isTrue();
    }

    /*
     * Tests for isKeyInSections(String, String[])
     */

    @Test
    public void isKeyInSectionsStringStringStringArrayCopesWithEmptyArrayForMoreSectionKeys() {
        String key = "a.b.key";
        String first = "a.b";

        boolean result = isKeyInSections(key, first);

        assertThat(result).describedAs("Key '%s' is in section '%s'.", key, first)
                          .isTrue();
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


    //----
    @Ignore
    @Test
    public void testIsKeyInSections() throws Exception {
        throw new RuntimeException("Not implement or look at me!");
    }

    @Ignore
    @Test
    public void testSections() throws Exception {
        throw new RuntimeException("Not implement or look at me!");
    }

    @Ignore
    @Test
    public void testTransitiveSections() throws Exception {
        throw new RuntimeException("Not implement or look at me!");
    }

    @Ignore
    @Test
    public void testSections1() throws Exception {
        throw new RuntimeException("Not implement or look at me!");
    }

    @Ignore
    @Test
    public void testTransitiveSections1() throws Exception {
        throw new RuntimeException("Not implement or look at me!");
    }

    @Ignore
    @Test
    public void testSectionsRecursive() throws Exception {
        throw new RuntimeException("Not implement or look at me!");
    }

    @Ignore
    @Test
    public void testSectionRecursive() throws Exception {
        throw new RuntimeException("Not implement or look at me!");
    }

    @Ignore
    @Test
    public void testStripSectionKeys() throws Exception {
        throw new RuntimeException("Not implement or look at me!");
    }

    @Ignore
    @Test
    public void testAddItems() throws Exception {
        throw new RuntimeException("Not implement or look at me!");
    }

    @Ignore
    @Test
    public void testAddItems1() throws Exception {
        throw new RuntimeException("Not implement or look at me!");
    }

    @Ignore
    @Test
    public void testReplaceItems() throws Exception {
        throw new RuntimeException("Not implement or look at me!");
    }

    @Ignore
    @Test
    public void testEmptyPropertySource() throws Exception {
        PropertySource ps = PropertySource.EMPTY;
        assertThat(ps).isNotNull();
        assertThat(ps.getProperties()).isNotNull().isEmpty();
        assertThat(ps.getName()).isEqualTo("<empty>");
    }
}
