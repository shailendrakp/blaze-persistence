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

import com.blazebit.annotation.AnnotationUtils;
import com.blazebit.lang.StringUtils;
import com.blazebit.persistence.view.AttributeFilter;
import com.blazebit.persistence.view.AttributeFilters;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.MappingSubquery;
import com.blazebit.persistence.view.metamodel.AttributeFilterMapping;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.blazebit.reflection.ReflectionUtils;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public abstract class AbstractMethodAttribute<X, Y> extends AbstractAttribute<X, Y> implements MethodAttribute<X, Y> {

    private final String name;
    private final Method javaMethod;
    private final Map<String, AttributeFilterMapping> filterMappings;

    protected AbstractMethodAttribute(ViewType<X> viewType, Method method, Annotation mapping, Set<Class<?>> entityViews) {
        super(viewType,
              (Class<Y>) ReflectionUtils.getResolvedMethodReturnType(viewType.getJavaType(), method),
              mapping,
              entityViews,
              "for the attribute '" + StringUtils.firstToLower(method.getName().substring(3)) + "' of the class '" + viewType.getJavaType().getName() + "'!");
        this.name = StringUtils.firstToLower(method.getName().substring(3));
        this.javaMethod = method;
        this.filterMappings = new HashMap<String, AttributeFilterMapping>();
        
        AttributeFilter filterMapping = AnnotationUtils.findAnnotation(method, AttributeFilter.class);
        AttributeFilters filtersMapping = AnnotationUtils.findAnnotation(method, AttributeFilters.class);
        
        if (filterMapping != null) {
            if (filtersMapping != null) {
                throw new IllegalArgumentException("Illegal occurrences of @Filter and @Filters on the attribute '" + name + "' of the class '" + viewType.getJavaType().getName() + "'!");
            }
            
            addFilterMapping(filterMapping);
        } else if (filtersMapping != null) {
            for (AttributeFilter f : filtersMapping.value()) {
                addFilterMapping(f);
            }
        }

        if (this.mapping != null && this.mapping.isEmpty()) {
            throw new IllegalArgumentException("Illegal empty mapping for the attribute '" + name + "' of the class '" + viewType.getJavaType().getName() + "'!");
        }
    }

    private void addFilterMapping(AttributeFilter filterMapping) {
        String filterName = filterMapping.name();
        
        if (filterName.isEmpty()) {
            filterName = name;
            
            if (filterMappings.containsKey(filterName)) {
                throw new IllegalArgumentException("Illegal duplicate filter name mapping '" + filterName + "' at attribute '" + name + "' of the class '" + getDeclaringType().getJavaType().getName() + "'!");
            }
        }
        
        AttributeFilterMapping attributeFilterMapping = new AttributeFilterMappingImpl(this, filterName, filterMapping.value());
        filterMappings.put(attributeFilterMapping.getName(), attributeFilterMapping);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Method getJavaMethod() {
        return javaMethod;
    }

    @Override
    public AttributeFilterMapping getFilter(String filterName) {
        return filterMappings.get(filterName);
    }

    @Override
    public Set<AttributeFilterMapping> getFilters() {
        return new HashSet<AttributeFilterMapping>(filterMappings.values());
    }
    
    public Map<String, AttributeFilterMapping> getFilterMappings() {
        return filterMappings;
    }

    public static String validate(ViewType<?> viewType, Method m) {
        // Concrete methods are not mapped
        if (!Modifier.isAbstract(m.getModifiers()) || m.isBridge()) {
            return null;
        }

        String attributeName;

        // We only support bean style getters
        if (ReflectionUtils.isSetter(m)) {
            attributeName = StringUtils.firstToLower(m.getName().substring(3));
            Method getter = ReflectionUtils.getGetter(viewType.getJavaType(), attributeName);

            if (getter == null) {
                throw new RuntimeException("The setter '" + m.getName() + "' from the entity view '" + viewType.getJavaType().getName() + "' has no corresponding getter!");
            }

            if (m.getParameterTypes()[0] != getter.getReturnType()) {
                throw new IllegalArgumentException("The setter '" + m.getName() + "' of the class '" + viewType.getJavaType().getName()
                    + "' must accept an argument of the same type as it's corresponding getter returns!");
            }

            if (getter.getReturnType().isPrimitive()) {
                throw new IllegalArgumentException("Primitive type not allowed for the setter '" + m.getName() + "' of the class '" + viewType.getJavaType().getName() + "'!");
            }

            return null;
        } else if (!ReflectionUtils.isGetter(m)) {
            throw new IllegalArgumentException("The given method '" + m.getName() + "' from the entity view '" + viewType.getJavaType().getName()
                + "' is no bean style getter or setter!");
        } else {
            int index = m.getName().startsWith("is") ? 2 : 3;
            attributeName = StringUtils.firstToLower(m.getName().substring(index));
            Method setter = ReflectionUtils.getSetter(viewType.getJavaType(), attributeName);

            if (setter != null && setter.getParameterTypes()[0] != m.getReturnType()) {
                throw new IllegalArgumentException("The getter '" + m.getName() + "' of the class '" + viewType.getJavaType().getName()
                    + "' must have the same return type as it's corresponding setter accepts!");
            }
            if (m.getReturnType().isPrimitive()) {
                throw new IllegalArgumentException("Primitive type not allowed for the getter '" + m.getName() + "' of the class '" + viewType.getJavaType().getName() + "'!");
            }
        }

        if (m.getExceptionTypes().length > 0) {
            throw new IllegalArgumentException("The given method '" + m.getName() + "' from the entity view '" + viewType.getJavaType().getName() + "' must not throw an exception!");
        }

        return attributeName;
    }

    public static Annotation getMapping(ViewType<?> viewType, Method m) {
        Class<?> entityClass = viewType.getEntityClass();
        Mapping mapping = AnnotationUtils.findAnnotation(m, Mapping.class);

        if (mapping == null) {
            IdMapping idMapping = AnnotationUtils.findAnnotation(m, IdMapping.class);

            if (idMapping != null) {
                if (idMapping.value().isEmpty()) {
                    throw new IllegalArgumentException("Illegal empty id mapping for the getter '" + m.getName() + "' in the entity view'" + viewType.getJavaType().getName()
                        + "'!");
                }

                return idMapping;
            }
            
            MappingParameter mappingParameter = AnnotationUtils.findAnnotation(m, MappingParameter.class);

            if (mappingParameter != null) {
                if (mappingParameter.value().isEmpty()) {
                    throw new IllegalArgumentException("Illegal empty mapping parameter for the getter '" + m.getName() + "' in the entity view'" + viewType.getJavaType().getName()
                        + "'!");
                }

                return mappingParameter;
            }

            MappingSubquery mappingSubquery = AnnotationUtils.findAnnotation(m, MappingSubquery.class);

            if (mappingSubquery != null) {
                return mappingSubquery;
            }

            // Implicit mapping
            String attributeName = StringUtils.firstToLower(m.getName().substring(3));

            // First check if a the same method exists in the entity class
            boolean entityAttributeExists = ReflectionUtils.getMethod(entityClass, m.getName()) != null;
            // If not, check if a field with the given attribute name exists in the entity class
            entityAttributeExists = entityAttributeExists || ReflectionUtils.getField(entityClass, attributeName) != null;

            if (!entityAttributeExists) {
                throw new IllegalArgumentException("The entity class '" + entityClass.getName() + "' has no attribute '" + attributeName
                    + "' that was implicitly mapped in entity view '" + viewType.getName() + "' in class '" + m.getDeclaringClass().getName() + "'");
            }

            mapping = new MappingLiteral(attributeName);
        }

        if (mapping.value().isEmpty()) {
            throw new IllegalArgumentException("Illegal empty mapping for the getter '" + m.getName() + "' in the entity view'" + viewType.getJavaType().getName() + "'!");
        }

        return mapping;
    }
}
