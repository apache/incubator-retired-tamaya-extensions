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
package org.apache.tamaya.resource.internal;

import org.junit.Ignore;

import java.net.URL;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This tests is using testing the classpath collector functionality, either by accessing/searching entries
 * from the java.annotation jar as well from the current (file-based classpath).
 */
public class ClasspathCollectorTest {

    @org.junit.Test
    public void testCollectAllClasses() throws Exception {
        ClasspathCollector cpc = new ClasspathCollector(Thread.currentThread().getContextClassLoader());
        Collection<URL> found = cpc.collectFiles("classpath:javax/annotation/*.class");
        assertThat(found).hasSize(8); // 7 ordinary, 1 inner class.
        Collection<URL> found2 = cpc.collectFiles("javax/annotation/*.class");
        assertThat(found).isEqualTo(found2);
    }

    @org.junit.Test
    public void testCollectAllInPackage() throws Exception {
        ClasspathCollector cpc = new ClasspathCollector(Thread.currentThread().getContextClassLoader());
        Collection<URL> found = cpc.collectFiles("classpath:javax/**/sql/*.class");
        assertThat(found).hasSize(2);
        Collection<URL> found2 = cpc.collectFiles("javax/**/sql/*.class");
        assertThat(found).isEqualTo(found2);
    }

    @org.junit.Test
    public void testCollectClassNames() throws Exception {
        ClasspathCollector cpc = new ClasspathCollector(Thread.currentThread().getContextClassLoader());
        Collection<URL> found = cpc.collectFiles("classpath:javax/annotation/**/R*.class");
        assertThat(found).hasSize(2);
        Collection<URL> found2 = cpc.collectFiles("javax/annotation/**/R*.class");
        assertThat(found).isEqualTo(found2);
    }

    @org.junit.Test
    public void testCollectWithExpression() throws Exception {
        ClasspathCollector cpc = new ClasspathCollector(Thread.currentThread().getContextClassLoader());
        Collection<URL> found = cpc.collectFiles("classpath:javax/annotation/R?so*.class");
        assertThat(found).hasSize(3);
        Collection<URL> found2 = cpc.collectFiles("javax/annotation/R?so*.class");
        assertThat(found).isEqualTo(found2);
    }

    @org.junit.Test
    public void testCollectResources() throws Exception {
        ClasspathCollector cpc = new ClasspathCollector(Thread.currentThread().getContextClassLoader());
        Collection<URL> found = cpc.collectFiles("classpath:META-INF/maven/org.apache.geronimo.specs/**/*");
        assertThat(found).hasSize(3);
        Collection<URL> found2 = cpc.collectFiles("META-INF/maven/org.apache.geronimo.specs/**/*");
        assertThat(found).isEqualTo(found2);
    }

    @org.junit.Test
    public void testCollectResourcesFromLocalFSPath() throws Exception {
        ClasspathCollector cpc = new ClasspathCollector(Thread.currentThread().getContextClassLoader());
        Collection<URL> found = cpc.collectFiles("classpath:resources_testroot/**/*.file");
        assertThat(found).hasSize(7);
        Collection<URL> found2 = cpc.collectFiles("resources_testroot/**/*.file");
        assertThat(found).isEqualTo(found2);
    }
}
