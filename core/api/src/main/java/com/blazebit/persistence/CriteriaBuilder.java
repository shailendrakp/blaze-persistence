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
package com.blazebit.persistence;

/**
 * A builder for criteria queries. This is the entry point for building queries.
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.0
 */
public interface CriteriaBuilder<T> extends QueryBuilder<T, CriteriaBuilder<T>>, GroupByBuilder<T, CriteriaBuilder<T>>,
    DistinctBuilder<T, CriteriaBuilder<T>> {

    /*
     * Covariant overrides.
     */
    @Override
    public CriteriaBuilder<T> from(Class<?> entityClass);

    @Override
    public CriteriaBuilder<T> from(Class<?> entityClass, String alias);
    
    @Override
    public SimpleCaseWhenStarterBuilder<CriteriaBuilder<T>> selectSimpleCase(String expression);

    @Override
    public SimpleCaseWhenStarterBuilder<CriteriaBuilder<T>> selectSimpleCase(String expression, String alias);

    @Override
    public CaseWhenStarterBuilder<CriteriaBuilder<T>> selectCase();

    @Override
    public CaseWhenStarterBuilder<CriteriaBuilder<T>> selectCase(String alias);

    @Override
    public <Y> SelectObjectBuilder<CriteriaBuilder<Y>> selectNew(Class<Y> clazz);

    @Override
    public <Y> CriteriaBuilder<Y> selectNew(ObjectBuilder<Y> builder);

    @Override
    public CriteriaBuilder<T> select(String expression);

    @Override
    public CriteriaBuilder<T> select(String expression, String alias);

    @Override
    public SubqueryInitiator<CriteriaBuilder<T>> selectSubquery();

    @Override
    public SubqueryInitiator<CriteriaBuilder<T>> selectSubquery(String alias);

    @Override
    public SubqueryInitiator<CriteriaBuilder<T>> selectSubquery(String subqueryAlias, String expression, String selectAlias);

    @Override
    public SubqueryInitiator<CriteriaBuilder<T>> selectSubquery(String subqueryAlias, String expression);
}
