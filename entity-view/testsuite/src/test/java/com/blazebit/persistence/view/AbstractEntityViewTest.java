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
package com.blazebit.persistence.view;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.view.entity.Document;
import com.blazebit.persistence.view.entity.Person;
import com.blazebit.persistence.view.entity.Version;
import com.blazebit.testsuite.base.AbstractPersistenceTest;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class AbstractEntityViewTest extends AbstractPersistenceTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
            Document.class,
            Version.class,
            Person.class
        };
    }
    
    protected <T> CriteriaBuilder<T> applySetting(EntityViewManager evm, Class<T> entityViewClass, CriteriaBuilder<?> criteriaBuilder) {
        EntityViewSetting<T, CriteriaBuilder<T>> setting = EntityViewSetting.create(entityViewClass);
        return evm.applySetting(setting, criteriaBuilder);
    }

}
