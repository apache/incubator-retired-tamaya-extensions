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
package org.apache.tamaya.collections;

import org.apache.tamaya.Configuration;
import org.apache.tamaya.TypeLiteral;
import org.junit.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Basic tests for Tamaya collection support. Relevant configs for this tests:
 * <pre>base.items=1,2,3,4,5,6,7,8,9,0
 * base.map=1:a, 2:b, 3:c, [4: ]
 * </pre>
 */
public class CollectionsBaseTests {

    @Test
    public void testList_String(){
        Configuration config = Configuration.current();
        List<String> items = config.get("base.items", new TypeLiteral<List<String>>(){});
        assertThat(items).isNotNull();
        assertThat(items.isEmpty()).isFalse();
        assertThat(items).hasSize(10);
        items = (List<String>) config.get("base.items", List.class);
        assertThat(items).isNotNull();
        assertThat(items.isEmpty()).isFalse();
        assertThat(items).hasSize(10);
    }

    @Test
    public void testArrayList_String(){
        Configuration config = Configuration.current();
        ArrayList<String> items = config.get("base.items", new TypeLiteral<ArrayList<String>>(){});
        assertThat(items).isNotNull();
        assertThat(items.isEmpty()).isFalse();
        assertThat(items).hasSize(10);
        items = (ArrayList<String>) config.get("base.items", ArrayList.class);
        assertThat(items).isNotNull();
        assertThat(items.isEmpty()).isFalse();
        assertThat(items).hasSize(10);
    }

    @Test
    public void testLinkedList_String(){
        Configuration config = Configuration.current();
        LinkedList<String> items = config.get("base.items", new TypeLiteral<LinkedList<String>>(){});
        assertThat(items).isNotNull();
        assertThat(items.isEmpty()).isFalse();
        assertThat(items).hasSize(10);
        items = (LinkedList<String>) config.get("base.items", LinkedList.class);
        assertThat(items).isNotNull();
        assertThat(items.isEmpty()).isFalse();
        assertThat(items).hasSize(10);
    }

    @Test
    public void testSet_String(){
        Configuration config = Configuration.current();
        Set<String> items = config.get("base.items", new TypeLiteral<Set<String>>(){});
        assertThat(items).isNotNull();
        assertThat(items.isEmpty()).isFalse();
        assertThat(items).hasSize(10);
        items = (Set<String>) config.get("base.items", Set.class);
        assertThat(items).isNotNull();
        assertThat(items.isEmpty()).isFalse();
        assertThat(items).hasSize(10);
    }

    @Test
    public void testSortedSet_String(){
        Configuration config = Configuration.current();
        Set<String> items = config.get("base.items", new TypeLiteral<SortedSet<String>>(){});
        assertThat(items).isNotNull();
        assertThat(items.isEmpty()).isFalse();
        assertThat(items).hasSize(10);
        items = (SortedSet<String>) config.get("base.items", SortedSet.class);
        assertThat(items).isNotNull();
        assertThat(items.isEmpty()).isFalse();
        assertThat(items).hasSize(10);
    }

    @Test
    public void testHashSet_String(){
        Configuration config = Configuration.current();
        Set<String> items = config.get("base.items", new TypeLiteral<HashSet<String>>(){});
        assertThat(items).isNotNull();
        assertThat(items.isEmpty()).isFalse();
        assertThat(items).hasSize(10);
        items = (HashSet<String>) config.get("base.items", HashSet.class);
        assertThat(items).isNotNull();
        assertThat(items.isEmpty()).isFalse();
        assertThat(items).hasSize(10);
    }

    @Test
    public void testTreeSet_String(){
        Configuration config = Configuration.current();
        TreeSet<String> items = config.get("base.items", new TypeLiteral<TreeSet<String>>(){});
        assertThat(items).isNotNull();
        assertThat(items.isEmpty()).isFalse();
        assertThat(items).hasSize(10);
        items = (TreeSet<String>) config.get("base.items", TreeSet.class);
        assertThat(items).isNotNull();
        assertThat(items.isEmpty()).isFalse();
        assertThat(items).hasSize(10);
    }

    @Test
    public void testMap_String(){
        Configuration config = Configuration.current();
        Map<String,String> items = config.get("base.map", new TypeLiteral<Map<String,String>>(){});
        assertThat(items).isNotNull();
        assertThat(items.isEmpty()).isFalse();
        assertThat(items).hasSize(4);
        assertThat("a").isEqualTo(items.get("1"));
        assertThat("b").isEqualTo(items.get("2"));
        assertThat("c").isEqualTo(items.get("3"));
        assertThat(" ").isEqualTo(items.get("4"));
        items = (Map<String,String>) config.get("base.map", Map.class);
        assertThat(items).isNotNull();
        assertThat(items.isEmpty()).isFalse();
        assertThat(items).hasSize(4);
        assertThat("a").isEqualTo(items.get("1"));
        assertThat("b").isEqualTo(items.get("2"));
        assertThat("c").isEqualTo(items.get("3"));
        assertThat(" ").isEqualTo(items.get("4"));
    }

    @Test
    public void testHashMap_String(){
        Configuration config = Configuration.current();
        Map<String,String> items = config.get("base.map", new TypeLiteral<HashMap<String,String>>(){});
        assertThat(items).isNotNull();
        assertThat(items.isEmpty()).isFalse();
        assertThat(items).hasSize(4);
        assertThat("a").isEqualTo(items.get("1"));
        assertThat("b").isEqualTo(items.get("2"));
        assertThat("c").isEqualTo(items.get("3"));
        assertThat(" ").isEqualTo(items.get("4"));
        items = (HashMap<String,String>) config.get("base.map", HashMap.class);
        assertThat(items).isNotNull();
        assertThat(items.isEmpty()).isFalse();
        assertThat(items).hasSize(4);
        assertThat("a").isEqualTo(items.get("1"));
        assertThat("b").isEqualTo(items.get("2"));
        assertThat("c").isEqualTo(items.get("3"));
        assertThat(" ").isEqualTo(items.get("4"));
    }

    @Test
    public void testSortedMap_String(){
        Configuration config = Configuration.current();
        Map<String,String> items = config.get("base.map", new TypeLiteral<SortedMap<String,String>>(){});
        assertThat(items).isNotNull();
        assertThat(items.isEmpty()).isFalse();
        assertThat(items).hasSize(4);
        assertThat("a").isEqualTo(items.get("1"));
        assertThat("b").isEqualTo(items.get("2"));
        assertThat("c").isEqualTo(items.get("3"));
        assertThat(" ").isEqualTo(items.get("4"));
        items = (Map<String,String>) config.get("base.map", SortedMap.class);
        assertThat(items).isNotNull();
        assertThat(items.isEmpty()).isFalse();
        assertThat(items).hasSize(4);
        assertThat("a").isEqualTo(items.get("1"));
        assertThat("b").isEqualTo(items.get("2"));
        assertThat("c").isEqualTo(items.get("3"));
        assertThat(" ").isEqualTo(items.get("4"));
    }

    @Test
    public void testTreeMap_String(){
        Configuration config = Configuration.current();
        TreeMap<String,String> items = config.get("base.map", new TypeLiteral<TreeMap<String,String>>(){});
        assertThat(items).isNotNull();
        assertThat(items.isEmpty()).isFalse();
        assertThat(items).hasSize(4);
        assertThat("a").isEqualTo(items.get("1"));
        assertThat("b").isEqualTo(items.get("2"));
        assertThat("c").isEqualTo(items.get("3"));
        assertThat(" ").isEqualTo(items.get("4"));
        items =  config.get("base.map", TreeMap.class);
        assertThat(items).isNotNull();
        assertThat(items.isEmpty()).isFalse();
        assertThat(items).hasSize(4);
        assertThat("a").isEqualTo(items.get("1"));
        assertThat("b").isEqualTo(items.get("2"));
        assertThat("c").isEqualTo(items.get("3"));
        assertThat(" ").isEqualTo(items.get("4"));
    }

    @Test
    public void testCollection_String(){
        Configuration config = Configuration.current();
        Collection<String> items = config.get("base.items", new TypeLiteral<Collection<String>>(){});
        assertThat(items).isNotNull();
        assertThat(items.isEmpty()).isFalse();
        assertThat(items).hasSize(10);
        items = (Collection<String>) config.get("base.items", Collection.class);
        assertThat(items).isNotNull();
        assertThat(items.isEmpty()).isFalse();
        assertThat(items).hasSize(10);
    }
}
