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
package org.apache.tamaya.jndi;

import org.junit.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Hashtable;

import static org.assertj.core.api.Assertions.assertThat;

public class JNDIPropertySourceTest{

    private InitialContext createFSContext() throws NamingException, MalformedURLException {
        Hashtable<String, String> env = new Hashtable<>();
        env.put (Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.fscontext.RefFSContextFactory");
        return new InitialContext(env);
    }

    private Context getTestDirContext(InitialContext ctx) throws NamingException {
        return (Context)ctx.lookup(new File("./src/test/jndi-dir").getAbsolutePath());
    }

    @Test
    public void testCreateWithContext() throws NamingException, MalformedURLException {
        new JNDIPropertySource("jndi-test", createFSContext());
    }

    @Test
    public void testScanContext() throws NamingException, MalformedURLException {
        JNDIPropertySource ps = new JNDIPropertySource("jndi-test", getTestDirContext(createFSContext()));
        assertThat(ps.isScannable()).isFalse();
        assertThat(ps.getProperties()).isNotNull().isEmpty();
        ps.setScannable(true);
        assertThat(ps.isScannable()).isTrue();
        assertThat(ps.getProperties()).isNotNull().isNotEmpty().hasSize(5)
            .containsKeys("a.test1", "b.test2", "c.test3", "c.test4", "c.c1.test5");
    }

}
