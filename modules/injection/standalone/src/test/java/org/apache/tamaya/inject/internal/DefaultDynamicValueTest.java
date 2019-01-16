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
package org.apache.tamaya.inject.internal;

import org.apache.tamaya.inject.api.DynamicValue;
import org.apache.tamaya.inject.api.Config;
import org.apache.tamaya.inject.api.UpdatePolicy;
import org.apache.tamaya.spi.ConversionContext;
import org.apache.tamaya.spi.PropertyConverter;
import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertyValue;
import org.junit.Test;

import org.apache.tamaya.Configuration;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link org.apache.tamaya.inject.internal.DefaultDynamicValue}.
 */
public class DefaultDynamicValueTest {

    @Config("a")
    String myValue;

    @Config("a")
    String myValue2;

    @Config("a")
    void setterMethod(String value){

    }

    private PropertyChangeEvent event;

    private PropertyChangeListener consumer = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            event = evt;
        }
    };

    private Map<String,PropertyValue> properties = new HashMap<>();
    private Configuration config =
            Configuration.createConfigurationBuilder().addPropertySources(
            new PropertySource() {
                @Override
                public int getOrdinal() {
                    return 0;
                }

                @Override
                public String getName() {
                    return "test";
                }

                @Override
                public PropertyValue get(String key) {
                    return properties.get(key);
                }

                @Override
                public Map<String, PropertyValue> getProperties() {
                    return properties;
                }
            }
    ).build();

    @Test
    public void testOf_Field() throws Exception {
        DynamicValue val = DefaultDynamicValue.of(this, getClass().getDeclaredField("myValue"),
                Configuration.current());
        assertThat(val).isNotNull();
    }

    @Test
    public void testOf_Method() throws Exception {
        DynamicValue val = DefaultDynamicValue.of(this, getClass().getDeclaredMethod("setterMethod", String.class),
                config);
        assertThat(val).isNotNull();
    }

    @Test
    public void testCommitAndGet() throws Exception {
        properties.put("a",PropertyValue.of("a","aValue","test"));
        DynamicValue val = DefaultDynamicValue.of(this, getClass().getDeclaredField("myValue"),
                config);
        assertThat(val).isNotNull();
        assertThat("aValue").isEqualTo(val.evaluateValue());
    }

    @Test
    public void testCommitAndGets() throws Exception {
        properties.put("a",PropertyValue.of("a","aValue","test"));
        DynamicValue val = DefaultDynamicValue.of(this, getClass().getDeclaredField("myValue"),
                config);
        val.setUpdatePolicy(UpdatePolicy.EXPLICIT);
        assertThat(val).isNotNull();
        assertThat("aValue").isEqualTo(val.evaluateValue());
        // change config
        val.get();
        properties.put("a",PropertyValue.of("a","aValue2","test"));
        assertThat(val.updateValue()).isTrue();
        assertThat("aValue2").isEqualTo(val.commitAndGet());
    }

    @Test
    public void testCommit() throws Exception {
        properties.put("a",PropertyValue.of("a","aValue","test"));
        DynamicValue val = DefaultDynamicValue.of(this, getClass().getDeclaredField("myValue"),
                config);
        val.setUpdatePolicy(UpdatePolicy.EXPLICIT);
        assertThat(val).isNotNull();
        assertThat("aValue").isEqualTo(val.evaluateValue());
        // change config
        val.get();
        properties.put("a",PropertyValue.of("a","aValue2","test"));
        assertThat("aValue2").isEqualTo(val.evaluateValue());
        assertThat(val.updateValue()).isTrue();
        val.commit();
        assertThat("aValue2").isEqualTo(val.get());
    }

    @Test
    public void testGetSetUpdatePolicy() throws Exception {
        DynamicValue val = DefaultDynamicValue.of(this, getClass().getDeclaredField("myValue"),
                config);
        for(UpdatePolicy pol: UpdatePolicy.values()) {
            val.setUpdatePolicy(pol);
            assertThat(pol).isEqualTo(val.getUpdatePolicy());
        }
    }

    @Test
    public void testAddRemoveListener() throws Exception {
        properties.put("a",PropertyValue.of("a","aValue","test"));
        DynamicValue val = DefaultDynamicValue.of(this, getClass().getDeclaredField("myValue"),
                config);
        val.setUpdatePolicy(UpdatePolicy.IMMEDIATE);
        val.addListener(consumer);
        // change config
        val.get();
        properties.put("a",PropertyValue.of("a","aValue2","test"));
        val.get();
        assertThat(event).isNotNull();
        event = null;
        val.removeListener(consumer);
        properties.put("a",PropertyValue.of("a","aValue3","test"));
        val.updateValue();
        assertThat(event).isNull();
    }

    @Test
    public void testGet() throws Exception {
        properties.put("a",PropertyValue.of("a","aValue","test"));
        DynamicValue val = DefaultDynamicValue.of(this, getClass().getDeclaredField("myValue"),
                config);
        val.setUpdatePolicy(UpdatePolicy.IMMEDIATE);
        properties.put("a",PropertyValue.of("a","aValue2","test"));
        val.updateValue();
        assertThat("aValue2").isEqualTo(val.get());
    }

    @Test
    public void testUpdateValue() throws Exception {
        properties.put("a",PropertyValue.of("a","aValue","test"));
        DynamicValue val = DefaultDynamicValue.of(this, getClass().getDeclaredField("myValue"),
                config);
        val.setUpdatePolicy(UpdatePolicy.EXPLICIT);
        assertThat(val.get()).isNotNull();
        assertThat("aValue").isEqualTo(val.get());
        val.updateValue();
        assertThat("aValue").isEqualTo(val.get());
        val.setUpdatePolicy(UpdatePolicy.IMMEDIATE);
        val.updateValue();
        assertThat("aValue").isEqualTo(val.get());
    }

    @Test
    public void testEvaluateValue() throws Exception {
        properties.put("a",PropertyValue.of("a","aValue","test"));
        DynamicValue val = DefaultDynamicValue.of(this, getClass().getDeclaredField("myValue"),
                config);
        val.setUpdatePolicy(UpdatePolicy.EXPLICIT);
        assertThat(val.get()).isNotNull();
        assertThat("aValue").isEqualTo(val.evaluateValue());
        properties.put("a",PropertyValue.of("a","aValue2","test"));
        assertThat("aValue2").isEqualTo(val.evaluateValue());
    }

    @Test
    public void testGetNewValue() throws Exception {
        properties.put("a",PropertyValue.of("a","aValue","test"));
        DynamicValue val = DefaultDynamicValue.of(this, getClass().getDeclaredField("myValue"),
                config);
        val.setUpdatePolicy(UpdatePolicy.EXPLICIT);
        val.get();
        assertThat(val.getNewValue()).isNull();
        properties.put("a",PropertyValue.of("a","aValue2","test"));
        val.get();
        assertThat(val.getNewValue()).isNotNull();
        assertThat("aValue2").isEqualTo(val.getNewValue());
        val.commit();
        assertThat("aValue2").isEqualTo(val.get());
        assertThat(val.getNewValue()).isNull();
    }

    @Test
    public void testIfPresent() throws Exception {
        properties.put("a",PropertyValue.of("a","aValue","test"));
        DynamicValue val = DefaultDynamicValue.of(this, getClass().getDeclaredField("myValue"),
                config);
        val.setUpdatePolicy(UpdatePolicy.IMMEDIATE);
        assertThat(val.isPresent()).isTrue();
        properties.remove("a");
        val.updateValue();
        assertThat(val.isPresent()).isFalse();
    }

    @Test
    public void testOrElse() throws Exception {
        DynamicValue val = DefaultDynamicValue.of(this, getClass().getDeclaredField("myValue"),
                config);
        val.setUpdatePolicy(UpdatePolicy.IMMEDIATE);
        assertThat("bla").isEqualTo(val.orElse("bla"));
        properties.put("a",PropertyValue.of("a","aValue","test"));
        val.updateValue();
        assertThat("aValue").isEqualTo(val.orElse("bla"));
    }

// TODO reenable with Java 8 support.
//    @Test
//    public void testOrElseGet() throws Exception {
//        DynamicValue val = DefaultDynamicValue.of(getClass().getDeclaredField("myValue"),
//                config);
//        val.setUpdatePolicy(UpdatePolicy.IMMEDEATE);
//        assertEquals("bla", val.orElseGet(new ConfiguredItemSupplier() {
//            @Override
//            public Object current() {
//                return "bla";
//            }
//        }));
//        properties.put("a", "aValue");
//        val.updateValue();
//        assertEquals("aValue", val.orElseGet(new ConfiguredItemSupplier() {
//            @Override
//            public Object current() {
//                return "bla";
//            }
//        }));
//    }
//
//    @Test(expected = ConfigException.class)
//    public void testOrElseThrow() throws Throwable {
//        DynamicValue val = DefaultDynamicValue.of(getClass().getDeclaredField("myValue"),
//                config);
//        val.setUpdatePolicy(UpdatePolicy.EXPLCIT);
//        val.current();
//        properties.put("a", "aValue");
//        assertEquals("aValue", val.orElseThrow(new ConfiguredItemSupplier() {
//            @Override
//            public ConfigException current() {
//                return new ConfigException("bla");
//            }
//        }));
//        properties.remove("a");
//        val.updateValue();
//        assertEquals("aValue", val.orElseThrow(new ConfiguredItemSupplier() {
//            @Override
//            public ConfigException current() {
//                return new ConfigException("bla");
//            }
//        }));
//    }

    private static final class DoublicatingConverter implements PropertyConverter<String>{

        @Override
        public String convert(String value, ConversionContext ctx) {
            return value + value;
        }
    }
}
