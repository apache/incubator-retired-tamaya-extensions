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
package org.apache.tamaya.ext.examples.injection;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.config.Config;
import javax.config.ConfigProvider;
import java.math.BigInteger;

public class MinimalTest {

    private static Config config;

    @BeforeClass
    public static void before() throws InterruptedException {
        config = ConfigProvider.getConfig();
        Thread.sleep(100L);
    }

    @Test
    public void printMetaInfo() {
        System.out.println("****************************************************");
        System.out.println("Minimal Example");
        System.out.println("****************************************************");
        System.out.println();
        System.out.println("Example Metadata:");
        System.out.println("\tType        :  " + config.getValue("example.type", String.class));
        System.out.println("\tName        :  " + config.getValue("example.name", String.class));
        System.out.println("\tDescription :  " + config.getValue("example.description", String.class));
        System.out.println("\tVersion     :  " + config.getValue("example.version", String.class));
        System.out.println("\tAuthor      :  " + config.getValue("example.author", String.class));
        System.out.println();
    }

    @Test(expected = IllegalArgumentException.class)
    public void getNumberValueTooLong() {
        String value = config.getValue("example.number", String.class);
        System.err.println("**** example.number(String)=" + value);
        int number = config.getValue("example.number",int.class);
        System.out.println("----\n   example.number(int)=" + number);
    }

    @Test
    public void getNumberValueAsInt_BadCase() {
        String value = config.getValue("example.numberAsHex", String.class);
        int number = config.getValue("example.numberAsHex",int.class);
        print("example.numberAsHex", number);
    }

    @Test
    public void getNumberValueAsBigInteger() {
        String value = config.getValue("example.number", String.class);
        BigInteger number = config.getValue("example.number", BigInteger.class);
        print("example.number", number);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getNumberValueAsLongHex() {
        String value = config.getValue("example.numberAsLongHex", String.class);
        long number = config.getValue("example.numberAsLongHex",int.class);
        print("example.numberAsLongHex", number);
    }

    @Test
    public void getEnum() {
        String value = config.getValue("example.testEnum", String.class);
        TestEnum en = config.getValue("example.testEnum", TestEnum.class);
        print("example.testEnum", en);
    }

    protected void print(String key, Object value) {
        System.out.println("----\n" +
                "  " + key + "(String)=" + config.getValue(key, String.class)
                + "\n  " + key + "(" + value.getClass().getSimpleName() + ")=" + value);
    }
}
