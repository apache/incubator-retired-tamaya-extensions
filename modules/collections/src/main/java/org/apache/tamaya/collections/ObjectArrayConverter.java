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
package org.apache.tamaya.collections;

import org.apache.tamaya.spi.ConversionContext;
import org.apache.tamaya.spi.PropertyConverter;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 *  PropertyConverter for gnerating ArrayList representation of a values.
 */
public class ObjectArrayConverter implements PropertyConverter<Object[]> {

    private static final Logger LOG = Logger.getLogger(ObjectArrayConverter.class.getName());

    /** The shared instance, used by other collection converters in this package.*/
    private static final ObjectArrayConverter INSTANCE = new ObjectArrayConverter();

    /**
     * Provide a shared instance, used by other collection converters in this package.
     * @return the shared instance, never null.
     */
    static ObjectArrayConverter getInstance(){
        return INSTANCE;
    }

    @Override
    public Object[] convert(String value, ConversionContext context) {
        Object array = CollectionConverter.convertArray(context);
        return (Object[])array;
    }

}
