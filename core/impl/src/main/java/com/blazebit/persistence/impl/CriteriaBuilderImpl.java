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

import com.blazebit.persistence.BaseQueryBuilder;
import com.blazebit.persistence.CaseWhenBuilder;
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.SelectObjectBuilder;
import com.blazebit.persistence.SimpleCaseWhenBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import java.util.Set;
import javax.persistence.EntityManager;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class CriteriaBuilderImpl<T> extends AbstractQueryBuilder<T, CriteriaBuilder<T>> implements CriteriaBuilder<T> {

    public CriteriaBuilderImpl(CriteriaBuilderFactoryImpl cbf, EntityManager em, Class<T> clazz, String alias, Set<String> registeredFunctions) {
        super(cbf, em, clazz, alias, registeredFunctions);
    }

    @Override
    public CriteriaBuilder<T> from(Class<?> clazz) {
        return (CriteriaBuilder<T>) super.from(clazz);
    }

    @Override
    public CriteriaBuilder<T> from(Class<?> clazz, String alias) {
        return (CriteriaBuilder<T>) super.from(clazz, alias);
    }
    
    @Override
    public CaseWhenBuilder<CriteriaBuilder<T>> selectCase() {
        return (CaseWhenBuilder<CriteriaBuilder<T>>) super.selectCase();
    }

    @Override
    public CaseWhenBuilder<CriteriaBuilder<T>> selectCase(String alias) {
        return (CaseWhenBuilder<CriteriaBuilder<T>>) super.selectCase(alias);
    }

    @Override
    public SimpleCaseWhenBuilder<CriteriaBuilder<T>> selectSimpleCase(String expression) {
        return (SimpleCaseWhenBuilder<CriteriaBuilder<T>>) super.selectSimpleCase(expression);
    }

    @Override
    public SimpleCaseWhenBuilder<CriteriaBuilder<T>> selectSimpleCase(String expression, String alias) {
        return (SimpleCaseWhenBuilder<CriteriaBuilder<T>>) super.selectSimpleCase(expression, alias);
    }

    @Override
    public <Y> SelectObjectBuilder<CriteriaBuilder<Y>> selectNew(Class<Y> clazz) {
        return (SelectObjectBuilder<CriteriaBuilder<Y>>) super.selectNew(clazz);
    }

    @Override
    public <Y> CriteriaBuilder<Y> selectNew(ObjectBuilder<Y> builder) {
        return (CriteriaBuilder<Y>) super.selectNew(builder);
    }

    @Override
    public CriteriaBuilder<T> select(String expression) {
        return (CriteriaBuilder<T>) super.select(expression);
    }

    @Override
    public CriteriaBuilder<T> select(String expression, String alias) {
        return (CriteriaBuilder<T>) super.select(expression, alias);
    }

    @Override
    public SubqueryInitiator<CriteriaBuilder<T>> selectSubquery() {
        return (SubqueryInitiator<CriteriaBuilder<T>>) super.selectSubquery();
    }

    @Override
    public SubqueryInitiator<CriteriaBuilder<T>> selectSubquery(String alias) {
        return (SubqueryInitiator<CriteriaBuilder<T>>) super.selectSubquery(alias);
    }

    @Override
    public SubqueryInitiator<CriteriaBuilder<T>> selectSubquery(String subqueryAlias, String expression) {
        return (SubqueryInitiator<CriteriaBuilder<T>>) super.selectSubquery(subqueryAlias, expression);
    }

    @Override
    public SubqueryInitiator<CriteriaBuilder<T>> selectSubquery(String subqueryAlias, String expression, String selectAlias) {
        return (SubqueryInitiator<CriteriaBuilder<T>>) super.selectSubquery(subqueryAlias, expression, selectAlias);
    }

}
