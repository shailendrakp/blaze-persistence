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
package com.blazebit.persistence.view.impl.metamodel;

import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.SingularAttribute;
import java.lang.annotation.Annotation;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public abstract class AbstractParameterSingularAttribute<X, Y> extends AbstractParameterAttribute<X, Y> implements SingularAttribute<X, Y> {

    public AbstractParameterSingularAttribute(MappingConstructor<X> constructor, int index, Annotation mapping, Set<Class<?>> entityViews) {
        super(constructor, index, mapping, entityViews);
    }

    @Override
    public boolean isCollection() {
        return false;
    }
}
