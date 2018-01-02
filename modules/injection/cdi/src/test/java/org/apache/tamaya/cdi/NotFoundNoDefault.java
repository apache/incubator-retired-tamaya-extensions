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

import javax.config.inject.ConfigProperty;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import java.io.File;
import java.time.Duration;


@Alternative
public class NotFoundNoDefault {

        @Inject
        @ConfigProperty(name="string.bla")
        private String string;

        @Inject
        @ConfigProperty(name="file.bla")
        private File file;

        @Inject
        @ConfigProperty(name="duration.bla")
        private Duration duration;

        @Inject
        @ConfigProperty(name="boolean.bla")
        private Boolean aBoolean;

        @Inject
        @ConfigProperty(name="integer.bla")
        private Integer integer;

        public String getString() {
            return string;
        }

        public File getFile() {
            return file;
        }

        public Duration getDuration() {
            return duration;
        }

        public Boolean getaBoolean() {
            return aBoolean;
        }

        public Integer getInteger() {
            return integer;
        }

        @Override
        public String toString() {
            return "NotFoundNoDefault{" +
                    "string='" + string + '\'' +
                    ", file=" + file +
                    ", duration=" + duration +
                    ", aBoolean=" + aBoolean +
                    ", integer=" + integer +
                    '}';
        }

    }