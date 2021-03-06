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

import com.blazebit.persistence.BetweenBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.BuilderChainingException;
import com.blazebit.persistence.impl.SubqueryBuilderImpl;
import com.blazebit.persistence.impl.SubqueryBuilderListenerImpl;
import com.blazebit.persistence.impl.SubqueryInitiatorFactory;
import com.blazebit.persistence.impl.builder.expression.SuperExpressionSubqueryBuilderListener;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.expression.ParameterExpression;
import com.blazebit.persistence.impl.expression.SubqueryExpression;
import com.blazebit.persistence.impl.predicate.BetweenPredicate;

/**
 *
 * @author Moritz Becker
 */
public class BetweenBuilderImpl<T> extends SubqueryBuilderListenerImpl<T> implements BetweenBuilder<T>, LeftHandsideSubqueryPredicateBuilder{

    private final SubqueryInitiatorFactory subqueryInitFactory;
    private final ExpressionFactory expressionFactory;
    private final PredicateBuilderEndedListener listener;
    private final Expression left;
    private final boolean negated;
    private Expression start;
    private final T result;
    private BetweenPredicate predicate;
    private SubqueryInitiator<?> subqueryStartMarker;

    public BetweenBuilderImpl(T result, Expression left, Expression start, ExpressionFactory expressionFactory, PredicateBuilderEndedListener listener, SubqueryInitiatorFactory subqueryInitFactory) {
        this(result, left, start, expressionFactory, listener, subqueryInitFactory, false);
    }
    
    public BetweenBuilderImpl(T result, Expression left, Expression start, ExpressionFactory expressionFactory, PredicateBuilderEndedListener listener, SubqueryInitiatorFactory subqueryInitFactory, boolean negated) {
        this.result = result;
        this.left = left;
        this.start = start;
        this.expressionFactory = expressionFactory;
        this.listener = listener;
        this.subqueryInitFactory = subqueryInitFactory;
        this.negated = negated;
    }
    
    @Override
    public T and(Object end) {
        if (end == null) {
            throw new NullPointerException("end");
        }
        return chain(new BetweenPredicate(left, start, new ParameterExpression(end), negated));
    }

    @Override
    public T andExpression(String end) {
        return chain(new BetweenPredicate(left, start, expressionFactory.createArithmeticExpression(end), negated));
    }

    @Override
    public SubqueryInitiator<T> andSubqery() {
        verifySubqueryBuilderEnded();
        return startSubqueryInitiator(subqueryInitFactory.createSubqueryInitiator(result, this));
    }

    @Override
    public SubqueryInitiator<T> andSubqery(String subqueryAlias, String expression) {
        verifySubqueryBuilderEnded();
        SuperExpressionSubqueryBuilderListener superExpressionSubqueryListener = new SuperExpressionSubqueryBuilderListener(subqueryAlias, expressionFactory.createArithmeticExpression(expression)){

            @Override
            public void onBuilderEnded(SubqueryBuilderImpl builder) {
                super.onBuilderEnded(builder);
                predicate = new BetweenPredicate(left, start, superExpression, negated);
                listener.onBuilderEnded(BetweenBuilderImpl.this);
            }
            
        };
        return startSubqueryInitiator(subqueryInitFactory.createSubqueryInitiator(result, superExpressionSubqueryListener));
    }

    @Override
    public BetweenPredicate getPredicate() {
        return predicate;
    }

    @Override
    public void setLeftExpression(Expression start) {
        this.start = start;
    }

    @Override
    public void onBuilderEnded(SubqueryBuilderImpl<T> builder) {
        super.onBuilderEnded(builder);
        this.subqueryStartMarker = null;
        this.predicate = new BetweenPredicate(left, start, new SubqueryExpression(builder), negated);
        listener.onBuilderEnded(this);
    }

    @Override
    public void verifySubqueryBuilderEnded() {
        if(subqueryStartMarker != null){
            throw new BuilderChainingException("A builder was not ended properly.");
        }
        super.verifySubqueryBuilderEnded();
    }
    
    public <T> SubqueryInitiator<T> startSubqueryInitiator(SubqueryInitiator<T> subqueryInitiator){
        this.subqueryStartMarker = subqueryInitiator;
        return subqueryInitiator;
    }

    private T chain(BetweenPredicate predicate) {
        verifySubqueryBuilderEnded();
        this.predicate = predicate;
        listener.onBuilderEnded(this);
        return result;
    }
}
