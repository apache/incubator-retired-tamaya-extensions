/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy current the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.tamaya.cdi;

import org.apache.tamaya.inject.api.Config;

import java.util.Optional;
import javax.inject.Singleton;
import java.math.BigDecimal;
import javax.inject.Inject;

/**
 * Class to be loaded from CDI to ensure fields are correctly configured using CDI injection mechanisms.
 */
@Singleton
public class ConfiguredClass{

    //Config values come from the TestPropertySource during ConfigurationProducerTest
    @Config
    private String testProperty;

    //Inject+Config values come from javaconfiguration.properties during ConfiguredBTest
    @Inject
    @Config
    private String injectedTestProperty;

    //@Inject will throw an NPE with a null assignment, before getting to required=false
    //  because our provider runs in @ApplicationScope, and null @Producer has to be in
    //  @Dependent scope (CDI 2.0 spec section 3.2).  Optional<> below is probably your
    //  better idea.
    @Config(key="stringMissingValue", required=false)
    private String stringMissingValue;

    @Config(key = "a.b.c.key1", alternateKeys = {"a.b.c.key2","a.b.c.key3"}, defaultValue = "The current \\${JAVA_HOME} env property is ${env:JAVA_HOME}.")
    String value1;

    @Config(key = "foo", alternateKeys = {"a.b.c.key2"})
    private String value2;

    @Config(defaultValue = "N/A")
    private String runtimeVersion;

    @Config(defaultValue = "${sys:java.version}")
    private String javaVersion2;

    @Config(defaultValue = "5")
    private Integer int1;

    @Config
    private int int2;

    @Config
    private boolean booleanT;

    @Config(key = "BD")
    private BigDecimal bigNumber;

    @Config(key = "[double1]")
    private double doubleValue;

    @Config
    private Optional<String> optionalStringWithValue;

    @Inject
    @Config
    private Optional<String> injectedOptionalStringWithValue;

    @Config(key="optionalStringMissingValue", required=false)
    private Optional<String> optionalStringMissingValue;

    @Inject
    @Config(key="injectedOptionalStringMissingValue", required=false)
    private Optional<String> injectedOptionalStringMissingValue;

    @Config(key="optionalStringMissingValueWithDefault", defaultValue="optionalStringDefaultValue", required=false)
    private Optional<String> optionalStringMissingValueWithDefault;

    @Inject
    @Config(key="injectedOptionalStringMissingValueWithDefault", defaultValue="injectedOptionalStringDefaultValue", required=false)
    private Optional<String> injectedOptionalStringMissingValueWithDefault;

    @Config(key="[double1]")
    private Optional<Double> existingDouble;

    @Config(key="foo-bar")
    private Optional<Double> nonExistingDouble;

    public String getTestProperty() {
        return testProperty;
    }

    public String getInjectedTestProperty() {
        return injectedTestProperty;
    }

    public String getValue1() {
        return value1;
    }

    public String getValue2() {
        return value2;
    }

    public String getRuntimeVersion() {
        return runtimeVersion;
    }

    public String getJavaVersion2() {
        return javaVersion2;
    }

    public Integer getInt1() {
        return int1;
    }

    public int getInt2() {
        return int2;
    }

    public boolean isBooleanT() {
        return booleanT;
    }

    public BigDecimal getBigNumber() {
        return bigNumber;
    }

    public double getDoubleValue() {
        return doubleValue;
    }

    public Optional<Double> getExistingDouble() {
        return existingDouble;
    }

    public Optional<Double> getNonExistingDouble() {
        return nonExistingDouble;
    }

    public Optional<String> getOptionalStringWithValue() {
        return optionalStringWithValue;
    }

    public String getStringMissingValue() {
        return stringMissingValue;
    }

    public Optional<String> getInjectedOptionalStringWithValue() {
        return injectedOptionalStringWithValue;
    }

    public Optional<String> getOptionalStringMissingValue() {
        return optionalStringMissingValue;
    }

    public Optional<String> getInjectedOptionalStringMissingValue() {
        return injectedOptionalStringMissingValue;
    }

    public Optional<String> getOptionalStringMissingValueWithDefault() {
        return optionalStringMissingValueWithDefault;
    }

    public Optional<String> getInjectedOptionalStringMissingValueWithDefault() {
        return injectedOptionalStringMissingValueWithDefault;
    }

    @Override
	public String toString(){
        return super.toString() + ": testProperty="+testProperty+", value1="+value1+", value2="+value2
                +", int1="+int1+", int2="+int2+", booleanT="+booleanT+", bigNumber="+bigNumber
                +", runtimeVersion="+runtimeVersion+", javaVersion2="+javaVersion2;
    }

}
