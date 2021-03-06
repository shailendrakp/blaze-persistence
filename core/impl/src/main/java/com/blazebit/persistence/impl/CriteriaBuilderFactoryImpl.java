/*
 * Copyright 2014 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blazebit.persistence.impl;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.expression.ExpressionFactoryImpl;
import com.blazebit.persistence.impl.expression.SimpleCachingExpressionFactory;
import com.blazebit.persistence.spi.EntityManagerIntegrator;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.spi.QueryTransformer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.persistence.EntityManager;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class CriteriaBuilderFactoryImpl implements CriteriaBuilderFactory {

    private final List<QueryTransformer> queryTransformers;
    private final Map<String, Map<String, JpqlFunction>> functions;
    private final List<EntityManagerIntegrator> entityManagerIntegrators;
    private final ExpressionFactory expressionFactory;
    private final Map<String, Object> properties;

    public CriteriaBuilderFactoryImpl(CriteriaBuilderConfigurationImpl config) {
        this.queryTransformers = new ArrayList<QueryTransformer>(config.getQueryTransformers());
        this.functions = new HashMap<String, Map<String, JpqlFunction>>(config.getFunctions());
        this.entityManagerIntegrators = new ArrayList<EntityManagerIntegrator>(config.getEntityManagerIntegrators());
        this.expressionFactory = new SimpleCachingExpressionFactory(new ExpressionFactoryImpl());
        this.properties = copyProperties(config.getProperties());
    }

    public List<QueryTransformer> getQueryTransformers() {
        return queryTransformers;
    }
    
    public Map<String, Map<String, JpqlFunction>> getFunctions() {
        return functions;
    }

    public ExpressionFactory getExpressionFactory() {
        return expressionFactory;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public <T> CriteriaBuilder<T> create(EntityManager entityManager, Class<T> resultClass) {
        return create(entityManager, resultClass, resultClass.getSimpleName().toLowerCase());
    }
    
    @Override
    public <T> CriteriaBuilder<T> create(EntityManager entityManager, Class<T> resultClass, String alias) {
        Set<String> registeredFunctions = new HashSet<String>();
        EntityManager em = entityManager;
        for (int i = 0; i < entityManagerIntegrators.size(); i++) {
            EntityManagerIntegrator integrator = entityManagerIntegrators.get(i);
            em = integrator.registerFunctions(em, functions);
            registeredFunctions.addAll(integrator.getRegisteredFunctions(em));
        }
        
        CriteriaBuilderImpl<T> cb = new CriteriaBuilderImpl<T>(this, em, resultClass, alias, registeredFunctions);
        return cb;
    }

    private Map<String, Object> copyProperties(Properties properties) {
        Map<String, Object> newProperties = new HashMap<String, Object>();

        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            Object value = entry.getValue();
            newProperties.put(key, value);
        }

        return newProperties;
    }

}
