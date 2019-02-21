///*
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// */
//package org.apache.tamaya.inject.api;
//
///**
// * Resolution strategy for the config key.
// */
//public enum KeyResolution {
//    /**
//     * The targeting key is evaluated as follows:
//     * <ol>
//     *     <li>The containing class <b>does not</b> have a {@link ConfigArea} annotation and the field/method does not have
//     *     a {@link Config} annotation: the main key equals to
//     *      *     {@code Owning.class.getSimpleName() + '.' + propertyKey}. This equals to {@link #RELATIVE_SIMPLE}.</li>
//     *     <li>The containing class <b>does not</b> have a {@link ConfigArea} annotation: the main key equals to
//     *     {@code propertyKey}. This equals to {@link #ABSOLUTE}.</li>
//     *     <li>The containing class <b>does</b> have a {@link ConfigArea} annotation: the main key equals to
//     *     {@code areaAnnotation.getValue() + '.' + propertyKey}.</li>
//     * </ol>
//     * This is the default key resolution strategy.
//     */
//    AUTO,
//
//    /**
//     * The targeting key is evaluated to {@code Owner.class.getSimpleName() + '.' + propertyKey}.
//     */
//    RELATIVE_SIMPLE,
//
//    /**
//     * The targeting key is evaluated to {@code Owner.class.getName() + '.' + propertyKey}.
//     */
//    RELATIVE_FQN,
//
//    /**
//     * The targeting key is evaluated using the evaluaed {@code propertyKey} only.
//     */
//    ABSOLUTE
//
//}
