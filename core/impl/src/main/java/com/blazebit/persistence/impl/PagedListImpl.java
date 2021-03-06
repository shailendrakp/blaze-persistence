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

import com.blazebit.persistence.KeySet;
import com.blazebit.persistence.PagedList;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @param <T> the type of elements in this list
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class PagedListImpl<T> extends ArrayList<T> implements PagedList<T> {

    private final KeySet keySet;
    private final long totalSize;
    private final int page;
    private final int totalPages;
    private final int firstResult;
    private final int maxResults;

    public PagedListImpl(KeySet keySet, long totalSize, int firstResult, int maxResults) {
        this.keySet = keySet;
        this.totalSize = totalSize;
        this.page = (int) Math.floor((firstResult == -1 ? 0 : firstResult) * 1d / maxResults) + 1;
        this.totalPages = (int) Math.ceil(totalSize * 1d / maxResults);
        this.firstResult = firstResult;
        this.maxResults = maxResults;
    }

    PagedListImpl(Collection<? extends T> collection, KeySet keySet, long totalSize, int firstResult, int maxResults) {
        super(collection);
        this.keySet = keySet;
        this.totalSize = totalSize;
        this.page = (int) Math.floor((firstResult == -1 ? 0 : firstResult) * 1d / maxResults) + 1;
        this.totalPages = (int) Math.ceil(totalSize * 1d / maxResults);
        this.firstResult = firstResult;
        this.maxResults = maxResults;
    }

    @Override
    public int getSize() {
        return size();
    }

    @Override
    public long getTotalSize() {
        return totalSize;
    }

    @Override
    public int getPage() {
        return page;
    }

    @Override
    public int getTotalPages() {
        return totalPages;
    }

    @Override
    public int getFirstResult() {
        return firstResult;
    }

    @Override
    public int getMaxResults() {
        return maxResults;
    }

    @Override
    public KeySet getKeySet() {
        return keySet;
    }

}
