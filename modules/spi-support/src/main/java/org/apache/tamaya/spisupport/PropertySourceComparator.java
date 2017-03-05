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
package org.apache.tamaya.spisupport;

import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertyValue;

import javax.annotation.Priority;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Comparator for ordering of PropertySources based on their ordinal method and class name.
 */
public class PropertySourceComparator implements Comparator<PropertySource>, Serializable {
    /** serial version UID. */
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(PropertySourceComparator.class.getName());

    private static final PropertySourceComparator INSTANCE = new PropertySourceComparator();

    private PropertySourceComparator(){}

    /**
     * Get the shared instance of the comparator.
     * @return the shared instance, never null.
     */
    public static PropertySourceComparator getInstance(){
        return INSTANCE;
    }


    /**
     * Order property source reversely, the most important come first.
     *
     * @param source1 the first PropertySource
     * @param source2 the second PropertySource
     * @return the comparison result.
     */
    private int comparePropertySources(PropertySource source1, PropertySource source2) {
        if (getOrdinal(source1) < getOrdinal(source2)) {
            return -1;
        } else if (getOrdinal(source1) > getOrdinal(source2)) {
            return 1;
        } else {
            return source1.getClass().getName().compareTo(source2.getClass().getName());
        }
    }

    public static int getOrdinal(PropertySource propertySource) {
//        PropertyValue ordinalValue = propertySource.get(PropertySource.TAMAYA_ORDINAL);
//        if(ordinalValue!=null){
//            try{
//                return Integer.parseInt(ordinalValue.getProperty().trim());
//            }catch(Exception e){
//                LOG.finest("Failed to parse ordinal from " + PropertySource.TAMAYA_ORDINAL +
//                        " in " + propertySource.getName()+": "+ordinalValue.getProperty());
//            }
//        }
//        try {
//            Method method = propertySource.getClass().getMethod("getOrdinal");
//            if(int.class.equals(method.getReturnType())){
//                try {
//                    return (int)method.invoke(propertySource);
//                } catch (Exception e) {
//                    LOG.log(Level.FINEST, "Error calling int getOrdinal() on " + propertySource.getName(), e);
//                }
//            }
//        } catch (NoSuchMethodException e) {
//            LOG.finest("No int getOrdinal() method found in " + propertySource.getName());
//        }
//        Priority prio = propertySource.getClass().getAnnotation(Priority.class);
//        if(prio!=null){
//            return prio.value();
//        }
        return propertySource.getOrdinal();
    }
    @Override
    public int compare(PropertySource source1, PropertySource source2) {
        return comparePropertySources(source1, source2);
    }
}
