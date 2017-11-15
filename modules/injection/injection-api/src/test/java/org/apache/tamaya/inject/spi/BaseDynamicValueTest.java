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
package org.apache.tamaya.inject.spi;

import org.apache.tamaya.ConfigException;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.TypeLiteral;
import org.apache.tamaya.inject.api.UpdatePolicy;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class BaseDynamicValueTest {

    @Test
    public void create(){
        new MyDynamicValue("a", "b");
    }

    @Test(expected = ConfigException.class)
    public void create_nokeys(){
        new MyDynamicValue();
    }

    @Test
    public void commitAndGet() throws Exception {
        System.setProperty("commitAndGet", "yes");
        MyDynamicValue dv = new MyDynamicValue("commitAndGet");
        System.setProperty("commitAndGet", "no");
        dv.setUpdatePolicy(UpdatePolicy.EXPLICIT);
        assertTrue(dv.updateValue());
        assertEquals(dv.commitAndGet(), "no");
        dv.setUpdatePolicy(UpdatePolicy.IMMEDIATE);
        System.setProperty("commitAndGet", "yes2");
        assertTrue(dv.updateValue());
        assertEquals(dv.get(), "yes2");
    }

    @Test
    public void isPresent() throws Exception {
        assertFalse(new MyDynamicValue("a", "b").isPresent());
        assertTrue(new MyDynamicValue("java.version").isPresent());
    }

    @Test
    public void orElse() throws Exception {
        assertEquals(new MyDynamicValue("a", "b").orElse("foo"), "foo");
    }

    @Test
    public void orElseGet() throws Exception {
        assertEquals(new MyDynamicValue("a", "b").orElseGet(() -> "foo"), "foo");
    }

    @Test(expected = NoSuchFieldException.class)
    public void orElseThrow() throws Throwable {
        new MyDynamicValue("foo").orElseThrow(() -> new NoSuchFieldException("Test"));
    }

    private static final class MyDynamicValue extends BaseDynamicValue{

        public MyDynamicValue(String... keys){
            super(TypeLiteral.of(String.class), Arrays.asList(keys));
        }

        @Override
        protected Configuration getConfiguration() {
            return ConfigurationProvider.getConfiguration();
        }

        @Override
        protected Object getOwner() {
            return this;
        }

        @Override
        protected String getPropertyName() {
            return "this";
        }
    }

}