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
package org.apache.tamaya.events;

import org.junit.Test;

import java.io.IOException;
import static org.junit.Assert.assertTrue;

/**
 * Test (currently manual) to test configuration changes.
 */
public class ObservedConfigTest {

    @Test(timeout=60000)
    public void testChangingConfig() throws IOException {
        ConfigEventManager.getInstance().setChangeMonitoringPeriod(100L);
        ConfigEventManager.getInstance().enableChangeMonitoring(true);
        while(MyConfigObserver.event==null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ConfigEvent<?> event = MyConfigObserver.event;
            if(event!=null) {
                assertTrue(event instanceof ConfigurationChange);
                ConfigurationChange cChange = (ConfigurationChange) event;
                if(cChange.isAdded("random.new")){
                    MyConfigObserver.event=null;
                }else {
                    assertTrue(cChange.isUpdated("random.new"));
                    break;
                }
            }
        }

    }

    public static final class MyConfigObserver implements ConfigEventListener{

        public static volatile ConfigEvent<?> event;

        @Override
        public void onConfigEvent(ConfigEvent<?> event) {
            MyConfigObserver.event = event;
        }
    }

}
