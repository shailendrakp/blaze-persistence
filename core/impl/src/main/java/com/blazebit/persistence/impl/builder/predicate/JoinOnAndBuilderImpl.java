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
package com.blazebit.persistence.impl.builder.predicate;

import com.blazebit.persistence.impl.builder.predicate.RestrictionBuilderImpl;
import com.blazebit.persistence.JoinOnAndBuilder;
import com.blazebit.persistence.JoinOnOrBuilder;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.impl.PredicateAndSubqueryBuilderEndedListener;
import com.blazebit.persistence.impl.SubqueryInitiatorFactory;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.predicate.AndPredicate;
import com.blazebit.persistence.impl.predicate.Predicate;
import com.blazebit.persistence.impl.predicate.PredicateBuilder;
import com.blazebit.persistence.impl.builder.predicate.PredicateBuilderEndedListener;

/**
 *
 * @author Moritz Becker
 * @since 1.0
 */
public class JoinOnAndBuilderImpl<T> extends PredicateAndSubqueryBuilderEndedListener<T> implements JoinOnAndBuilder<T>,
    PredicateBuilder {

    private final T result;
    private final PredicateBuilderEndedListener listener;
    private final AndPredicate predicate = new AndPredicate();
    private final ExpressionFactory expressionFactory;
    private final SubqueryInitiatorFactory subqueryInitFactory;

    public JoinOnAndBuilderImpl(T result, PredicateBuilderEndedListener listener, ExpressionFactory expressionFactory, SubqueryInitiatorFactory subqueryInitFactory) {
        this.result = result;
        this.listener = listener;
        this.expressionFactory = expressionFactory;
        this.subqueryInitFactory = subqueryInitFactory;
    }

    @Override
    public T endAnd() {
        verifyBuilderEnded();
        listener.onBuilderEnded(this);
        return result;
    }

    @Override
    public void onBuilderEnded(PredicateBuilder builder) {
        super.onBuilderEnded(builder);
        predicate.getChildren().add(builder.getPredicate());
    }

    @Override
    public JoinOnOrBuilder<JoinOnAndBuilder<T>> onOr() {
        return startBuilder(new JoinOnOrBuilderImpl<JoinOnAndBuilder<T>>(this, this, expressionFactory, subqueryInitFactory));
    }

    @Override
    public RestrictionBuilder<JoinOnAndBuilder<T>> on(String expression) {
        Expression leftExpression = expressionFactory.createSimpleExpression(expression);
        return startBuilder(new RestrictionBuilderImpl<JoinOnAndBuilder<T>>(this, this, leftExpression, subqueryInitFactory, expressionFactory));
    }

    @Override
    public Predicate getPredicate() {
        return predicate;
    }

}
