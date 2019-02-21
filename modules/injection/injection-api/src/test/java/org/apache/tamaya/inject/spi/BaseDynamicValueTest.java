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
import org.apache.tamaya.TypeLiteral;
import org.apache.tamaya.inject.api.UpdatePolicy;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class BaseDynamicValueTest {

    @Test
    public void create(){
        new MyDynamicValue(Configuration.current(),"a", "b");
    }

    @Test(expected = ConfigException.class)
    public void create_nokeys(){
        new MyDynamicValue(Configuration.current());
    }

    @Test
    public void commitAndGet() throws Exception {
        System.setProperty("commitAndGet", "yes");
        MyDynamicValue dv = new MyDynamicValue(Configuration.current(),"commitAndGet");
        System.setProperty("commitAndGet", "no");
        dv.setUpdatePolicy(UpdatePolicy.EXPLICIT);
        assertThat(dv.updateValue()).isTrue();
        assertThat(dv.commitAndGet()).isEqualTo("no");
        dv.setUpdatePolicy(UpdatePolicy.IMMEDIATE);
        System.setProperty("commitAndGet", "yes2");
        assertThat(dv.updateValue()).isTrue();
        assertThat(dv.get()).isEqualTo("yes2");
    }

    @Test
    public void isPresent() throws Exception {
        assertThat(new MyDynamicValue(Configuration.current(),"a", "b").isPresent()).isFalse();
        assertThat(new MyDynamicValue(Configuration.current(),"java.version").isPresent()).isTrue();
    }

    @Test
    public void orElse() throws Exception {
        assertThat(new MyDynamicValue(Configuration.current(),"a", "b").orElse("foo")).isEqualTo("foo");
    }

    @Test
    public void orElseGet() throws Exception {
        assertThat(new MyDynamicValue(Configuration.current(),"a", "b").orElseGet(() -> "foo")).isEqualTo("foo");
    }

    @Test(expected = NoSuchFieldException.class)
    public void orElseThrow() throws Throwable {
        new MyDynamicValue(Configuration.current(),"foo").orElseThrow(() -> new NoSuchFieldException("Test"));
    }

    private static final class MyDynamicValue extends BaseDynamicValue{

        public MyDynamicValue(Configuration config, String... keys){
            super(null, "test", TypeLiteral.of(String.class), Arrays.asList(keys), config);
        }

        @Override
        protected Configuration getConfiguration() {
            return Configuration.current();
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
