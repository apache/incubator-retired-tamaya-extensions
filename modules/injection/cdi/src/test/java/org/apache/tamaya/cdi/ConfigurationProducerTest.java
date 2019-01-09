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

import java.io.File;
import java.util.Optional;

import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.tamaya.inject.api.Config;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Arquillian.class)
public class ConfigurationProducerTest {

    @Deployment
    public static Archive<?> deployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(ConfiguredClass.class, InjectedClass.class,
                        TamayaCDIInjectionExtension.class, TamayaCDIAccessor.class,
                        org.apache.tamaya.cdi.ConfigurationProducer.class)
                .addAsServiceProvider(Extension.class, TamayaCDIInjectionExtension.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebInfResource("META-INF/javaconfiguration.properties", "META-INF/javaconfiguration.properties");
    }

    @Inject
    private AllTypes allTypes;

    @Test
    public void defaultValues() {
        assertThat(allTypes).isNotNull();
        assertThat("defaultString").isEqualTo(allTypes.getDefaultString());
        assertThat(new File("./")).isEqualTo(allTypes.getDefaultFile());
        assertThat(allTypes.getDefaultBoolean()).isTrue();
        assertThat(45).isEqualTo((int) allTypes.getDefaultInteger());
    }

    @Test
    public void actualPropertyValues() {
        assertThat(allTypes).isNotNull();
        assertThat("hello").isEqualTo(allTypes.getString());
        assertThat(new File("./conf")).isEqualTo(allTypes.getFile());
        assertThat(allTypes.getaBoolean()).isTrue();
        assertThat(123).isEqualTo((int) allTypes.getInteger());
    }

    @Test
    public void optionalStringFieldIsSet() {
        assertThat(allTypes).isNotNull();
        assertThat(allTypes.optionalString).isNotNull();
        assertThat(allTypes.optionalString.isPresent()).isTrue();
        assertThat("hello").isEqualTo(allTypes.optionalString.get());
    }

    @Test
    public void optionalIntegerFieldIsSet() {
        assertThat(allTypes).isNotNull();
        assertThat(allTypes.optionalInteger).isNotNull();
        assertThat(allTypes.optionalInteger.isPresent()).isTrue();
        assertThat(123).isEqualTo(allTypes.optionalInteger.get().longValue());
    }

    @Test
    public void providerStringFieldIsSet() {
        assertThat(allTypes).isNotNull();
        assertThat(allTypes.providerString).isNotNull();
        assertThat("hello").isEqualTo(allTypes.providerString.get());
        assertThat("hello").isEqualTo(allTypes.providerString.get());
    }

    @Test
    public void providerIntegerFieldIsSet() {
        assertThat(allTypes).isNotNull();
        assertThat(allTypes.providerInteger).isNotNull();
        assertThat(123).isEqualTo(allTypes.providerInteger.get().longValue());
        assertThat(123).isEqualTo(allTypes.providerInteger.get().longValue());
    }

    static class AllTypes {

        private String stringAsMethodParam;
        private Integer integerAsMethodParam;
        private Optional<String> optionalStringAsMethodParam;
        private Optional<Integer> optionalIntegerAsMethodParam;
        private Provider<String> providerStringAsMethodParam;
        private Provider<Integer> providerIntegerAsMethodParam;

        @Inject
        @Config(value = "string.value", defaultValue = "defaultString")
        private String string;

        @Inject
        @Config(value = "string.value", defaultValue = "defaultString")
        private Optional<String> optionalString;

        @Inject
        @Config(value = "string.value", defaultValue = "defaultString")
        private Provider<String> providerString;

        @Inject
        @Config(value = "defaultString.value", defaultValue = "defaultString")
        private String defaultString;

        @Inject
        @Config(value = "file.value", defaultValue = "./")
        private File file;

        @Inject
        @Config(value = "defaultFile.value", defaultValue = "./")
        private File defaultFile;

        @Inject
        @Config(value = "boolean.value", defaultValue = "true")
        private Boolean aBoolean;

        @Inject
        @Config(value = "defaultBoolean.value", defaultValue = "true")
        private Boolean defaultBoolean;

        @Inject
        @Config(value = "integer.value", defaultValue = "45")
        private Integer integer;

        @Inject
        @Config(value = "defaultInteger.value", defaultValue = "45")
        private Integer defaultInteger;

        @Inject
        @Config(value = "integer.value", defaultValue = "45")
        private Optional<Integer> optionalInteger;

        @Inject
        @Config(value = "integer.value", defaultValue = "45")
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
        public void setStringAsMethodParam(@Config(value = "string.value", defaultValue = "defaultString") String stringAsMethodParam) {
            this.stringAsMethodParam = stringAsMethodParam;
        }

        @Inject
        public void setIntegerAsMethodParam(@Config(value = "integer.value", defaultValue = "45")Integer integerAsMethodParam) {
            this.integerAsMethodParam = integerAsMethodParam;
        }

        @Inject
        public void setOptionalStringAsMethodParam(@Config(value = "string.value", defaultValue = "defaultString") Optional<String> optionalStringAsMethodParam) {
            this.optionalStringAsMethodParam = optionalStringAsMethodParam;
        }

        @Inject
        public void setOptionalIntegerAsMethodParam(@Config(value = "integer.value", defaultValue = "45") Optional<Integer> optionalIntegerAsMethodParam) {
            this.optionalIntegerAsMethodParam = optionalIntegerAsMethodParam;
        }

        @Inject
        public void setProviderStringAsMethodParam(@Config(value = "string.value", defaultValue = "defaultString") Provider<String> providerStringAsMethodParam) {
            this.providerStringAsMethodParam = providerStringAsMethodParam;
        }

        @Inject
        public void setProviderIntegerAsMethodParam(@Config(value = "integer.value", defaultValue = "45") Provider<Integer> providerIntegerAsMethodParam) {
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
