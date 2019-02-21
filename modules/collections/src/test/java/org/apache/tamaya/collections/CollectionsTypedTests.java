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
public class CollectionsTypedTests {

    @Test
    public void testArrayListList_String(){
        Configuration config = Configuration.current();
        List<String> items = config.get("typed2.arraylist", new TypeLiteral<List<String>>(){});
        assertThat(items).isNotNull().isNotEmpty().hasSize(10).isInstanceOf(ArrayList.class);
        items = (List<String>) config.get("typed2.arraylist", List.class);
        assertThat(items).isNotNull().isNotEmpty().hasSize(10).isInstanceOf(ArrayList.class);
    }

    @Test
    public void testLinkedListList_String(){
        Configuration config = Configuration.current();
        List<String> items = config.get("typed2.linkedlist", new TypeLiteral<List<String>>(){});
        assertThat(items).isNotNull().isNotEmpty().hasSize(10).isInstanceOf(LinkedList.class);
        items = (List<String>) config.get("typed2.linkedlist", List.class);
        assertThat(items).isNotNull().isNotEmpty().hasSize(10).isInstanceOf(LinkedList.class);
    }


    @Test
    public void testHashSet_String(){
        Configuration config = Configuration.current();
        Set<String> items = config.get("typed2.hashset", new TypeLiteral<Set<String>>(){});
        assertThat(items).isNotNull().isNotEmpty().hasSize(10).isInstanceOf(HashSet.class);
        items = (Set<String>) config.get("typed2.hashset", Set.class);
        assertThat(items).isNotNull().isNotEmpty().hasSize(10).isInstanceOf(HashSet.class);
    }

    @Test
    public void testTreeSet_String(){
        Configuration config = Configuration.current();
        Set<String> items = config.get("typed2.treeset", new TypeLiteral<Set<String>>(){});
        assertThat(items).isNotNull().isNotEmpty().hasSize(10).isInstanceOf(TreeSet.class);
        items = (Set<String>) config.get("typed2.treeset", Set.class);
        assertThat(items).isNotNull().isNotEmpty().hasSize(10).isInstanceOf(TreeSet.class);
    }

    @Test
    public void testHashMap_String(){
        Configuration config = Configuration.current();
        Map<String,String> items = config.get("typed2.hashmap", new TypeLiteral<Map<String,String>>(){});
        assertThat(items).isNotNull().isNotEmpty().hasSize(4).isInstanceOf(HashMap.class)
            .containsEntry("1", "a")
            .containsEntry("2", "b")
            .containsEntry("3", "c")
            .containsEntry("4", " ");
        items = (Map<String,String>) config.get("typed2.hashmap", Map.class);
        assertThat(items).isNotNull().isNotEmpty().hasSize(4).isInstanceOf(HashMap.class)
            .containsEntry("1", "a")
            .containsEntry("2", "b")
            .containsEntry("3", "c")
            .containsEntry("4", " ");
    }

    @Test
    public void testTreeMap_String(){
        Configuration config = Configuration.current();
        Map<String,String> items = config.get("typed2.treemap", new TypeLiteral<Map<String,String>>(){});
        assertThat(items).isNotNull().isNotEmpty().hasSize(4).isInstanceOf(TreeMap.class)
            .containsEntry("1", "a")
            .containsEntry("2", "b")
            .containsEntry("3", "c")
            .containsEntry("4", " ");
        items = (Map<String,String>) config.get("typed2.treemap", Map.class);
        assertThat(items).isNotNull().isNotEmpty().hasSize(4).isInstanceOf(TreeMap.class)
            .containsEntry("1", "a")
            .containsEntry("2", "b")
            .containsEntry("3", "c")
            .containsEntry("4", " ");
    }

    @Test
    public void testCollection_HashSet(){
        Configuration config = Configuration.current();
        Collection<String> items = config.get("typed2.hashset", new TypeLiteral<Collection<String>>(){});
        assertThat(items).isNotNull().isNotEmpty().hasSize(10).isInstanceOf(HashSet.class);
        items = (Collection<String>) config.get("typed2.hashset", Collection.class);
        assertThat(items).isNotNull().isNotEmpty().hasSize(10).isInstanceOf(HashSet.class);
    }

    @Test
    public void testCollection_TreeSet(){
        Configuration config = Configuration.current();
        Collection<String> items = config.get("typed2.treeset", new TypeLiteral<Collection<String>>(){});
        assertThat(items).isNotNull().isNotEmpty().hasSize(10).isInstanceOf(TreeSet.class);
        items = (Collection<String>) config.get("typed2.treeset", Collection.class);
        assertThat(items).isNotNull().isNotEmpty().hasSize(10).isInstanceOf(TreeSet.class);
    }

    @Test
    public void testCollection_ArrayList(){
        Configuration config = Configuration.current();
        Collection<String> items = config.get("typed2.arraylist", new TypeLiteral<Collection<String>>(){});
        assertThat(items).isNotNull().isNotEmpty().hasSize(10).isInstanceOf(ArrayList.class);
        items = (Collection<String>) config.get("typed2.arraylist", Collection.class);
        assertThat(items).isNotNull().isNotEmpty().hasSize(10).isInstanceOf(ArrayList.class);
    }

    @Test
    public void testCollection_LinkedList(){
        Configuration config = Configuration.current();
        Collection<String> items = config.get("typed2.linkedlist", new TypeLiteral<Collection<String>>(){});
        assertThat(items).isNotNull().isNotEmpty().hasSize(10).isInstanceOf(LinkedList.class);
        items = (Collection<String>) config.get("typed2.linkedlist", Collection.class);
        assertThat(items).isNotNull().isNotEmpty().hasSize(10).isInstanceOf(LinkedList.class);
    }

}
