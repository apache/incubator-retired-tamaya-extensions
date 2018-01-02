/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tamaya.cdi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Optional;

import javax.config.inject.ConfigProperty;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;
import javax.inject.Provider;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ConfigurationProducerTest {

    @Deployment
    public static Archive<?> deployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(ConfiguredClass.class, InjectedClass.class,
                        TamayaCDIInjectionExtension.class, TamayaCDIAccessor.class,
                        org.apache.tamaya.cdi.ConfigProducer.class)
                .addAsServiceProvider(Extension.class, TamayaCDIInjectionExtension.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebInfResource("META-INF/javaconfig.properties", "META-INF/javaconfig.properties");
    }

    @Inject
    private AllTypes allTypes;

    @Test
    public void defaultValues() {
        assertNotNull(allTypes);
        assertEquals("defaultString", allTypes.getDefaultString());
        assertEquals(new File("./"), allTypes.getDefaultFile());
//        assertEquals(new Duration("2 hours and 54 minutes"), allTypes.getDefaultDuration());
        assertEquals(true, allTypes.getDefaultBoolean());
        assertEquals(45, (int) allTypes.getDefaultInteger());
    }

    @Test
    public void actualPropertyValues() {
        assertNotNull(allTypes);
        assertEquals("hello", allTypes.getString());
        assertEquals(new File("./conf"), allTypes.getFile());
//        assertEquals(new Duration("10 minutes and 57 seconds"), allTypes.getDuration());
        assertEquals(true, allTypes.getaBoolean());
        assertEquals(123, (int) allTypes.getInteger());
    }

    @Test
    public void optionalStringFieldIsSet() {
        assertNotNull(allTypes);
        assertNotNull(allTypes.optionalString);
        assertTrue(allTypes.optionalString.isPresent());
        assertEquals("hello", allTypes.optionalString.get());
    }

    @Test
    public void optionalIntegerFieldIsSet() {
        assertNotNull(allTypes);
        assertNotNull(allTypes.optionalInteger);
        assertTrue(allTypes.optionalInteger.isPresent());
        assertEquals(123, allTypes.optionalInteger.get().longValue());
    }

    @Test
    public void providerStringFieldIsSet() {
        assertNotNull(allTypes);
        assertNotNull(allTypes.providerString);
        assertEquals("hello", allTypes.providerString.get());
        assertEquals("hello", allTypes.providerString.get());
    }

    @Test
    public void providerIntegerFieldIsSet() {
        assertNotNull(allTypes);
        assertNotNull(allTypes.providerInteger);
        assertEquals(123, allTypes.providerInteger.get().longValue());
        assertEquals(123, allTypes.providerInteger.get().longValue());
    }

    static class AllTypes {

        private String stringAsMethodParam;
        private Integer integerAsMethodParam;
        private Optional<String> optionalStringAsMethodParam;
        private Optional<Integer> optionalIntegerAsMethodParam;
        private Provider<String> providerStringAsMethodParam;
        private Provider<Integer> providerIntegerAsMethodParam;

        @Inject
        @ConfigProperty(name = "string.value", defaultValue = "defaultString")
        private String string;

        @Inject
        @ConfigProperty(name = "string.value", defaultValue = "defaultString")
        private Optional<String> optionalString;

        @Inject
        @ConfigProperty(name = "string.value", defaultValue = "defaultString")
        private Provider<String> providerString;

        @Inject
        @ConfigProperty(name = "defaultString.value", defaultValue = "defaultString")
        private String defaultString;

        @Inject
        @ConfigProperty(name = "file.value", defaultValue = "./")
        private File file;

        @Inject
        @ConfigProperty(name = "defaultFile.value", defaultValue = "./")
        private File defaultFile;

        @Inject
        @ConfigProperty(name = "boolean.value", defaultValue = "true")
        private Boolean aBoolean;

        @Inject
        @ConfigProperty(name = "defaultBoolean.value", defaultValue = "true")
        private Boolean defaultBoolean;

        @Inject
        @ConfigProperty(name = "integer.value", defaultValue = "45")
        private Integer integer;

        @Inject
        @ConfigProperty(name = "defaultInteger.value", defaultValue = "45")
        private Integer defaultInteger;

        @Inject
        @ConfigProperty(name = "integer.value", defaultValue = "45")
        private Optional<Integer> optionalInteger;

        @Inject
        @ConfigProperty(name = "integer.value", defaultValue = "45")
        private Provider<Integer> providerInteger;

        public String getString() {
            return string;
        }

        public File getFile() {
            return file;
        }

        public Boolean getaBoolean() {
            return aBoolean;
        }

        public Integer getInteger() {
            return integer;
        }

        public String getDefaultString() {
            return defaultString;
        }

        public File getDefaultFile() {
            return defaultFile;
        }

        public Boolean getDefaultBoolean() {
            return defaultBoolean;
        }

        public Integer getDefaultInteger() {
            return defaultInteger;
        }


        @Inject
        public void setStringAsMethodParam(@ConfigProperty(name = "string.value", defaultValue = "defaultString") String stringAsMethodParam) {
            this.stringAsMethodParam = stringAsMethodParam;
        }

        @Inject
        public void setIntegerAsMethodParam(@ConfigProperty(name = "integer.value", defaultValue = "45")Integer integerAsMethodParam) {
            this.integerAsMethodParam = integerAsMethodParam;
        }

        @Inject
        public void setOptionalStringAsMethodParam(@ConfigProperty(name = "string.value", defaultValue = "defaultString") Optional<String> optionalStringAsMethodParam) {
            this.optionalStringAsMethodParam = optionalStringAsMethodParam;
        }

        @Inject
        public void setOptionalIntegerAsMethodParam(@ConfigProperty(name = "integer.value", defaultValue = "45") Optional<Integer> optionalIntegerAsMethodParam) {
            this.optionalIntegerAsMethodParam = optionalIntegerAsMethodParam;
        }

        @Inject
        public void setProviderStringAsMethodParam(@ConfigProperty(name = "string.value", defaultValue = "defaultString") Provider<String> providerStringAsMethodParam) {
            this.providerStringAsMethodParam = providerStringAsMethodParam;
        }

        @Inject
        public void setProviderIntegerAsMethodParam(@ConfigProperty(name = "integer.value", defaultValue = "45") Provider<Integer> providerIntegerAsMethodParam) {
            this.providerIntegerAsMethodParam = providerIntegerAsMethodParam;
        }

        @Override
		public String toString() {
			return "AllTypes [stringAsMethodParam=" + stringAsMethodParam + ", integerAsMethodParam="
					+ integerAsMethodParam + ", optionalStringAsMethodParam=" + optionalStringAsMethodParam
					+ ", optionalIntegerAsMethodParam=" + optionalIntegerAsMethodParam
					+ ", providerStringAsMethodParam=" + providerStringAsMethodParam + ", providerIntegerAsMethodParam="
					+ providerIntegerAsMethodParam + ", string=" + string + ", optionalString=" + optionalString
					+ ", providerString=" + providerString + ", defaultString=" + defaultString + ", file=" + file
					+ ", defaultFile=" + defaultFile + ", aBoolean=" + aBoolean + ", defaultBoolean=" + defaultBoolean
					+ ", integer=" + integer + ", defaultInteger=" + defaultInteger + ", optionalInteger="
					+ optionalInteger + ", providerInteger=" + providerInteger + "]";
		}
    }
}
