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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertyValue;
import org.apache.tamaya.spisupport.propertysource.BasePropertySource;
import org.osgi.service.component.annotations.Component;

/**
 * Propertysource that accesses JNDI as source for configuration entries.
 */
@Component
public class JNDIPropertySource extends BasePropertySource {
    /** The logger used. */
    private static final Logger LOG = Logger.getLogger(JNDIPropertySource.class.getName());

    /**
     * Default ordinal to be used, as defined by {@link PropertySource#getOrdinal()} documentation.
     */
    private static final int DEFAULT_ORDINAL = 200;

    /** The root context, not null. */
    private Context context;
    /** The scanable property, default is {@code false}. */
    private boolean scannable = false;

    /**
     * Creates a new instance.
     * @param name the name of the property source, see {@link PropertySource#getName()}.
     * @param context the root context to be used, not null.
     */
    public JNDIPropertySource(String name, Context context){
        super(name);
        this.context = Objects.requireNonNull(context);
    }

    /**
     * Creates a new instance.
     * @param name the name of the property source, see {@link PropertySource#getName()}.
     * @throws NamingException if {@code new InitialContext()} throws an exception.
     */
    public JNDIPropertySource(String name) throws NamingException {
        super(name);
        this.context = new InitialContext();
    }

    /**
     * Creates a new instance, using {@code "jndi"} as property source name.
     * @throws NamingException if {@code new InitialContext()} throws an exception.
     */
    public JNDIPropertySource() throws NamingException {
        this("jndi");
        setDefaultOrdinal(DEFAULT_ORDINAL);
    }

    /**
     * If the property source is not scanable, an empty map is returned, otherwise
     * the current JNDI context is mapped to configuration map:
     * <ul>
     *   <li>For each leave entry one entry is created.</li>
     *   <li>The key is the fully path of parent contexts, separated by a '.'.</li>
     *   <li>The createValue is the createValue returned from {@code String.createValue(leaveObject)}.</li>
     * </ul>
     * @return a map representation of the JNDI tree.
     */
    @Override
    public Map<String, PropertyValue> getProperties() {
        if(scannable){
            try {
                return PropertyValue.map(toMap(this.context), getName());
            } catch (NamingException e) {
                LOG.log(Level.WARNING, "Error scanning JNDI tree.", e);
            }
        }
        return Collections.emptyMap();
    }

    @Override
    public boolean isScannable() {
        return scannable;
    }

    /**
     * If setCurrent to true, the property source will return a String representation of the JNDI
     * tree when calling {@code getProperties()}.
     * @see #getProperties()
     * @param val true, to activate scanable (default is false).
     */
    public void setScannable(boolean val){
        this.scannable = val;
    }

    @Override
    public PropertyValue get(String key) {
        try {
            key = key.replace('.', '/');
            Object o = context.lookup(key);
            return PropertyValue.of(key, o.toString(), getName());
        } catch (NamingException e) {
            LOG.log(Level.FINER, "Failed to lookup key in JNDI: " + key, e);
            return null;
        }
    }

    @Override
    protected String toStringValues() {
        return super.toStringValues() +
                "\n  context=" + context + '\'';
    }

    /**
     * Maps the given JNDI Context to a {@code Map<String,String>}:
     *  mapped to configuration map:
     * <ul>
     *   <li>For each leave entry one entry is created.</li>
     *   <li>The key is the fully path of parent contexts, separated by a '.'.</li>
     *   <li>The createValue is the createValue returned from {@code String.createValue(leaveObject)}.</li>
     * </ul>
     * @param ctx the JNDI context, not null.
     * @return the corresponding map, never null.
     * @throws NamingException If some JNDI issues occur.
     */
    public static Map<String,String> toMap(Context ctx) throws NamingException {
        String namespace = ctx instanceof InitialContext ? ctx.getNameInNamespace() : "";
        Map<String, String> map = new HashMap<>();
        NamingEnumeration<NameClassPair> list = ctx.list(namespace);
        while (list.hasMoreElements()) {
            NameClassPair next = list.next();
            String name = next.getName();
            String jndiPath = namespace + name;
            try {
                Object lookup = ctx.lookup(jndiPath);
                if (namespace.isEmpty()) {
                    if (lookup instanceof Context) {
                        Map<String, String> childMap = toMap((Context) lookup);
                        for (Map.Entry<String, String> en : childMap.entrySet()) {
                            map.put(name + "." + en.getKey(), en.getValue());
                        }
                    } else {
                        map.put(name, String.valueOf(lookup));
                    }
                }else{
                    if (lookup instanceof Context) {
                        Map<String, String> childMap = toMap((Context) lookup);
                        for (Map.Entry<String, String> en : childMap.entrySet()) {
                            map.put(namespace + "." + name + "." + en.getKey(), en.getValue());
                        }
                    } else {
                        map.put(namespace + "." + name, String.valueOf(lookup));
                    }
                }
            } catch (Exception t) {
                map.put(namespace + "." + name, "ERROR: " + t.getMessage());
            }
        }
        return map;
    }
}
