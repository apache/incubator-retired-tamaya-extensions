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
import java.util.Map;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JNDIPropertySourceTest{

    private InitialContext createFSContext() throws NamingException, MalformedURLException {
        Hashtable env = new Hashtable();
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
        assertFalse(ps.isScannable());
        Map<String,String> props = ps.getProperties();
        assertNotNull(props);
        assertTrue(props.isEmpty());
        ps.setScannable(true);
        assertTrue(ps.isScannable());
        props = ps.getProperties();
        assertNotNull(props);
        assertFalse(props.isEmpty());
        assertEquals(props.size(), 5);
        assertNotNull(props.get("c.c1.test5"));
        assertNotNull(props.get("c.test3"));
        assertNotNull(props.get("c.test4"));
        assertNotNull(props.get("b.test2"));
        assertNotNull(props.get("a.test1"));
    }

}