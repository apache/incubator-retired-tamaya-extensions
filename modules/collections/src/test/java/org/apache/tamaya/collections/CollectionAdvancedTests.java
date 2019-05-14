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

import java.util.Currency;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by atsticks on 16.02.16.
 */
public class CollectionAdvancedTests {

    /**
     * Tests if a custom separator works, Config is
     * <pre>
     *  sep-createList=a,b,c|d,e,f|g,h,i
     *  _sep-createList.collection-type=List
     *  _sep-createList.collection-separator=|
     * </pre>
     */
    @Test
    public void testCustomSeparator(){
        Configuration config = Configuration.current();
        List<String> items = config.get("sep-list", new TypeLiteral<List<String>>(){});
        assertThat(items).isNotNull().isNotEmpty().hasSize(3).containsExactly("a,b,c", "d,e,f", "g,h,i");
    }

    /**
     * Test typed content.
     * <pre>
     *  currency-createList=CHF,USD,YEN
     *  _currency-createList.collection-type=List
     * </pre>
     */
    @Test
    public void testTypedContent(){
        Configuration config = Configuration.current();
        List<Currency> items = config.get("currency-list", new TypeLiteral<List<Currency>>(){});
        assertThat(items).isNotNull().isNotEmpty().hasSize(3);
        assertThat("CHF").isEqualTo(items.get(0).getCurrencyCode());
        assertThat("USD").isEqualTo(items.get(1).getCurrencyCode());
        assertThat("USS").isEqualTo(items.get(2).getCurrencyCode());
    }

    /**
     * Tests if a custom parser works, Config is
     * <pre>
     *  parser-createList=a,b,c
     *  _parser-createList.collection-type=List
     *  _parser-createList.item-converter=org.apache.tamaya.collections.MyUpperCaseConverter
     * </pre>
     */
    @Test
    public void testCustomParser(){
        Configuration config = Configuration.current();
        List<String> items = config.get("parser-list", new TypeLiteral<List<String>>(){});
        assertThat(items).isNotNull().isNotEmpty().hasSize(3).containsExactly("(A)", "(B)", "(C)");
    }

    /**
     * Redefined mapProperties format parsing, Config is as follows:
     * <pre>
     *  redefined-mapProperties=0==none | 1==single | 2==any
     *  _redefined-mapProperties.mapProperties-entry-separator===
     *  _redefined-mapProperties.item-separator=|
     * </pre>
     */
    @Test
    public void testCustomMapParser(){
        Configuration config = Configuration.current();
        Map<String,String> items = config.get("redefined-map", Map.class);
        assertThat(items).isNotNull().isNotEmpty().hasSize(3);
        assertThat("none").isEqualTo(items.get("0"));
        assertThat("single").isEqualTo(items.get("1"));
        assertThat("any").isEqualTo(items.get("2"));
    }
}
