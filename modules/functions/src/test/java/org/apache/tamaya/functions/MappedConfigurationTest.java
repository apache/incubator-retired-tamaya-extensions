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
package org.apache.tamaya.functions;

import org.junit.Test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class MappedConfigurationTest {
    /*
     * Tests for getOrDefault(String, String)
     */

    @Test(expected = NullPointerException.class)
    public void getOrDefaultWithTwoStringParametersThrowsNPEIfValueIsNull() throws Exception {
        MappedConfiguration mc = mock(MappedConfiguration.class);
        doReturn("z").when(mc).get(eq("a)"));
        doCallRealMethod().when(mc).getOrDefault(anyString(), anyString());

        mc.getOrDefault("a", (String)null);
    }

    @Test(expected = NullPointerException.class)
    public void getOrDefaultWithTwoStringParametersThrowsNPEIfKeyIsNull() throws Exception {
        MappedConfiguration mc = mock(MappedConfiguration.class);
        doCallRealMethod().when(mc).getOrDefault(anyString(), anyString());

        mc.getOrDefault((String)null, "z");
    }

}