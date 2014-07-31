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

import com.blazebit.persistence.view.Filter;
import com.blazebit.persistence.view.SubqueryProvider;
import com.blazebit.persistence.view.metamodel.SubqueryAttribute;
import com.blazebit.persistence.view.metamodel.ViewType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 *
 * @author cpbec
 */
public class MethodSubquerySingularAttributeImpl<X, Y> extends AbstractMethodSingularAttribute<X, Y> implements SubqueryAttribute<X, Y> {

    public MethodSubquerySingularAttributeImpl(ViewType<X> viewType, Method method, Annotation mapping) {
        super(viewType, method, mapping);
    }
 
}
