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
package org.apache.tamaya.resolver.internal;

import org.apache.tamaya.resolver.spi.ExpressionEvaluator;
import org.apache.tamaya.resolver.spi.ExpressionResolver;
import org.apache.tamaya.spi.ServiceContextManager;

import javax.annotation.Priority;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default expression evaluator that manages several instances of {@link org.apache.tamaya.resolver.spi.ExpressionResolver}.
 * Each resolver is identified by a resolver id. Each expression passed has the form resolverId:resolverExpression, which
 * has the advantage that different resolvers can be active in parallel.
 */
@Priority(10000)
public class DefaultExpressionEvaluator implements ExpressionEvaluator {

    private static final Logger LOG = Logger.getLogger(DefaultExpressionEvaluator.class.getName());

    private List<ExpressionResolver> expressionResolvers;
    
    public DefaultExpressionEvaluator() {
    	
	}
    
    public DefaultExpressionEvaluator(List<ExpressionResolver> expressionResolvers) {
    	this.expressionResolvers = expressionResolvers;
    }

    /**
     * Comparator used (not needed with Java8).
     */
    private static final Comparator<ExpressionResolver> RESOLVER_COMPARATOR = new Comparator<ExpressionResolver>() {
        @Override
        public int compare(ExpressionResolver o1, ExpressionResolver o2) {
            return compareExpressionResolver(o1, o2);
        }
    };

    /**
     * Order ExpressionResolver reversely, the most important come first.
     *
     * @param res1 the first ExpressionResolver
     * @param res2 the second ExpressionResolver
     * @return the comparison result.
     */
    private static int compareExpressionResolver(ExpressionResolver res1, ExpressionResolver res2) {
        Priority prio1 = res1.getClass().getAnnotation(Priority.class);
        Priority prio2 = res2.getClass().getAnnotation(Priority.class);
        int ord1 = prio1 != null ? prio1.value() : 0;
        int ord2 = prio2 != null ? prio2.value() : 0;
        if (ord1 < ord2) {
            return -1;
        } else if (ord1 > ord2) {
            return 1;
        } else {
            return res1.getClass().getName().compareTo(res2.getClass().getName());
        }
    }

    /**
     * Resolves an expression in the form current <code>${resolverId:expression}</code> or
     * <code>${&lt;prefix&gt;expression}</code>. The expression can be
     * part current any type current literal text. Also multiple expressions with mixed matching resolvers are
     * supported.
     * All control characters (${}\) can be escaped using '\'.<br>
     * So all the following are valid expressions:
     * <ul>
     * <li><code>${expression}</code></li>
     * <li><code>bla bla ${expression}</code></li>
     * <li><code>${expression} bla bla</code></li>
     * <li><code>bla bla ${expression} bla bla</code></li>
     * <li><code>${expression}${resolverId2:expression2}</code></li>
     * <li><code>foo ${expression}${resolverId2:expression2}</code></li>
     * <li><code>foo ${expression} bar ${resolverId2:expression2}</code></li>
     * <li><code>${expression}foo${resolverId2:expression2}bar</code></li>
     * <li><code>foor${expression}bar${resolverId2:expression2}more</code></li>
     * <li><code>\${expression}foo${resolverId2:expression2}bar</code> (first expression is escaped).</li>
     * </ul>
     * Given {@code resolverId:} is a valid prefix targeting a {@link java.beans.Expression} explicitly, also the
     * following expressions are valid:
     * <ul>
     * <li><code>${resolverId:expression}</code></li>
     * <li><code>bla bla ${resolverId:expression}</code></li>
     * <li><code>${resolverId:expression} bla bla</code></li>
     * <li><code>bla bla ${resolverId:expression} bla bla</code></li>
     * <li><code>${resolverId:expression}${resolverId2:expression2}</code></li>
     * <li><code>foo ${resolverId:expression}${resolverId2:expression2}</code></li>
     * <li><code>foo ${resolverId:expression} bar ${resolverId2:expression2}</code></li>
     * <li><code>${resolverId:expression}foo${resolverId2:expression2}bar</code></li>
     * <li><code>foor${resolverId:expression}bar${resolverId2:expression2}more</code></li>
     * <li><code>\${resolverId:expression}foo${resolverId2:expression2}bar</code> (first expression is escaped).</li>
     * </ul>
     *
     * @param key the key to be filtered
     * @param value value to be analyzed for expressions
     * @param maskUnresolved if true, not found expression parts will be replaced by surrounding with [].
     *                     Setting to false will replace the value with an empty String.
     * @return the resolved value, or the input in case where no expression was detected.
     */
    @Override
    public String evaluateExpression(String key, String value, boolean maskUnresolved){
        if(value ==null){
            return null;
        }
        StringTokenizer tokenizer = new StringTokenizer(value, "${}", true);
        StringBuilder resolvedValue = new StringBuilder();
        StringBuilder current = new StringBuilder();
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
                switch (token) {
                    case "$":
                        String nextToken = tokenizer.hasMoreTokens()?tokenizer.nextToken():"";
                        if (!"{".equals(nextToken)) {
                            current.append(token);
                            current.append(nextToken);
                            break;
                        }
                        if(value.indexOf('}')<=0){
                            current.append(token);
                            current.append(nextToken);
                            break;
                        }
                        String subExpression = parseSubExpression(tokenizer, value);
                        String res = evaluateInternal(subExpression, maskUnresolved);
                        if(res!=null) {
                            current.append(res);
                        }
                        break;
                    default:
                        current.append(token);
            }
        }
        if (current.length() > 0) {
            resolvedValue.append(current);
        }
        return resolvedValue.toString();
    }

    @Override
    public Collection<ExpressionResolver> getResolvers() {
    	if (expressionResolvers != null) {
    		return expressionResolvers;
    	}
    	
        List<ExpressionResolver> resolvers = new ArrayList<>();
        for (ExpressionResolver resolver : ServiceContextManager.getServiceContext().getServices(ExpressionResolver.class)) {
            resolvers.add(resolver);
        }
        Collections.sort(resolvers, RESOLVER_COMPARATOR);
        return resolvers;
    }

    /**
     * Parses subexpression from tokenizer, hereby counting all open and closed brackets, but ignoring any
     * meta characters.
     * @param tokenizer the current tokenizer instance
     * @param valueToBeFiltered subexpression to be filtered for
     * @return the parsed sub expression
     */
    private String parseSubExpression(StringTokenizer tokenizer, String valueToBeFiltered) {
        StringBuilder expression = new StringBuilder();
        boolean escaped = false;
        while(tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            switch (token) {
                case "\\":
                    if(!escaped) {
                        escaped = true;

                    } else {
                        expression.append(token);
                        escaped = false;
                    }
                    break;
                case "{":
                    if(!escaped) {
                        LOG.warning("Ignoring not escaped '{' in : " + valueToBeFiltered);
                    }
                    expression.append(token);
                    escaped = false;
                    break;
                case "$":
                    if(!escaped) {
                        LOG.warning("Ignoring not escaped '$' in : " + valueToBeFiltered);
                    }
                    expression.append(token);
                    escaped = false;
                    break;
                case "}":
                    if(escaped) {
                        expression.append(token);
                        escaped = false;
                    } else{
                        return expression.toString();
                    }
                    break;
                default:
                    expression.append(token);
                    escaped = false;
                    break;
            }
        }
        LOG.warning("Invalid expression syntax in: " + valueToBeFiltered + ", expression does not close!");
            return valueToBeFiltered;
    }

    /**
     * Evaluates the expression parsed, hereby checking for prefixes and trying otherwise all available resolvers,
     * based on priority.
     * @param unresolvedExpression the parsed, but unresolved expression
     * @param maskUnresolved if true, not found expression parts will be replaced by surrounding with [].
     *                     Setting to false will replace the value with an empty String.
     * @return the resolved expression, or null.
     */
    private String evaluateInternal(String unresolvedExpression, boolean maskUnresolved) {
        String value = null;
        // 1 check for explicit prefix
        Collection<ExpressionResolver> resolvers = getResolvers();
        for(ExpressionResolver resolver:resolvers){
            if(unresolvedExpression.startsWith(resolver.getResolverPrefix())){
                value = resolver.evaluate(unresolvedExpression.substring(resolver.getResolverPrefix().length()));
                break;
            }
        }
        if(value==null){
            for(ExpressionResolver resolver:resolvers){
                try{
                    value = resolver.evaluate(unresolvedExpression);
                    if(value!=null){
                        return value;
                    }
                }catch(Exception e){
                    LOG.log(Level.FINEST, "Error during expression resolution from " + resolver, e);
                }
            }
        }
        if(value==null){
            LOG.log(Level.WARNING, "Unresolvable expression encountered " + unresolvedExpression);
            if(maskUnresolved){
                value = "?{" + unresolvedExpression + '}';
            }
        }
        return value;
    }


}
