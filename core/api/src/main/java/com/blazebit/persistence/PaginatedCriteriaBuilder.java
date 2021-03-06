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
 * A builder for paginated criteria queries.
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.0
 */
public interface PaginatedCriteriaBuilder<T> extends QueryBuilder<T, PaginatedCriteriaBuilder<T>> {

    /**
     * Returns the query string that selects the count of elements.
     *
     * @return The query string
     */
    public String getPageCountQueryString();

    /**
     * Returns the query string that selects the id of the elements.
     *
     * @return The query string
     */
    public String getPageIdQueryString();

    /*
     * Covariant overrides
     */
    @Override
    public PaginatedCriteriaBuilder<T> from(Class<?> entityClass);

    @Override
    public PaginatedCriteriaBuilder<T> from(Class<?> entityClass, String alias);
    
    @Override
    public PagedList<T> getResultList();

    @Override
    public SimpleCaseWhenBuilder<PaginatedCriteriaBuilder<T>> selectSimpleCase(String expression);

    @Override
    public SimpleCaseWhenBuilder<PaginatedCriteriaBuilder<T>> selectSimpleCase(String expression, String alias);

    @Override
    public CaseWhenBuilder<PaginatedCriteriaBuilder<T>> selectCase();

    @Override
    public CaseWhenBuilder<PaginatedCriteriaBuilder<T>> selectCase(String alias);

    @Override
    public <Y> SelectObjectBuilder<PaginatedCriteriaBuilder<Y>> selectNew(Class<Y> clazz);

    @Override
    public <Y> PaginatedCriteriaBuilder<Y> selectNew(ObjectBuilder<Y> builder);

    @Override
    public PaginatedCriteriaBuilder<T> select(String expression);

    @Override
    public PaginatedCriteriaBuilder<T> select(String expression, String alias);

    @Override
    public SubqueryInitiator<PaginatedCriteriaBuilder<T>> selectSubquery();

    @Override
    public SubqueryInitiator<PaginatedCriteriaBuilder<T>> selectSubquery(String alias);

    @Override
    public SubqueryInitiator<PaginatedCriteriaBuilder<T>> selectSubquery(String subqueryAlias, String expression, String selectAlias);

    @Override
    public SubqueryInitiator<PaginatedCriteriaBuilder<T>> selectSubquery(String subqueryAlias, String expression);

}
