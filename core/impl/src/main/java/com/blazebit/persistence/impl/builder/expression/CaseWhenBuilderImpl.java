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
package com.blazebit.persistence.impl.builder.expression;

import com.blazebit.persistence.impl.SubqueryBuilderListener;
import com.blazebit.persistence.impl.SubqueryInitiatorFactory;
import com.blazebit.persistence.impl.SubqueryBuilderListenerImpl;
import com.blazebit.persistence.impl.builder.predicate.RestrictionBuilderImpl;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilder;
import com.blazebit.persistence.CaseWhenAndThenBuilder;
import com.blazebit.persistence.CaseWhenBuilder;
import com.blazebit.persistence.CaseWhenOrThenBuilder;
import com.blazebit.persistence.CaseWhenThenBuilder;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.builder.predicate.LeftHandsideSubqueryPredicateBuilderListener;
import com.blazebit.persistence.impl.PredicateAndExpressionBuilderEndedListener;
import com.blazebit.persistence.impl.builder.predicate.RightHandsideSubqueryPredicateBuilder;
import com.blazebit.persistence.impl.builder.predicate.SuperExpressionLeftHandsideSubqueryPredicateBuilder;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilderEndedListener;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.expression.GeneralCaseExpression;
import com.blazebit.persistence.impl.expression.WhenClauseExpression;
import com.blazebit.persistence.impl.predicate.ExistsPredicate;
import com.blazebit.persistence.impl.predicate.Predicate;
import com.blazebit.persistence.impl.predicate.PredicateBuilder;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class CaseWhenBuilderImpl<T> extends PredicateAndExpressionBuilderEndedListener implements CaseWhenBuilder<T>,
        CaseWhenThenBuilder<CaseWhenBuilder<T>>, ExpressionBuilder {

    private final T result;
    private final List<WhenClauseExpression> whenClauses;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    private final ExpressionFactory expressionFactory;

    private Predicate whenPredicate;
    private GeneralCaseExpression expression;
    
    private final SubqueryBuilderListenerImpl<?> leftSubqueryPredicateBuilderListener = new LeftHandsideSubqueryPredicateBuilderListener<RestrictionBuilderImpl<?>>();
    private final ExpressionBuilderEndedListener listener;

    public CaseWhenBuilderImpl(T result, ExpressionBuilderEndedListener listener, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory) {
        this.result = result;
        this.whenClauses = new ArrayList<WhenClauseExpression>();
        this.subqueryInitFactory = subqueryInitFactory;
        this.expressionFactory = expressionFactory;
        this.listener = listener;
    }

    @Override
    public RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>> when(String expression) {
        verifyBuilderEnded();
        Expression expr = expressionFactory.createSimpleExpression(expression);
        return startBuilder(new RestrictionBuilderImpl<CaseWhenThenBuilder<CaseWhenBuilder<T>>>(this, this, expr, subqueryInitFactory, expressionFactory));
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>>> whenSubquery() {
        verifyBuilderEnded();
        RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>> restrictionBuilder = startBuilder(
            new RestrictionBuilderImpl<CaseWhenThenBuilder<CaseWhenBuilder<T>>>(this, this, subqueryInitFactory, expressionFactory));
        return subqueryInitFactory.createSubqueryInitiator(restrictionBuilder,
                                                           (SubqueryBuilderListener<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>>>) leftSubqueryPredicateBuilderListener);
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>>> whenSubquery(String subqueryAlias, String expression) {
        verifyBuilderEnded();
        SuperExpressionLeftHandsideSubqueryPredicateBuilder<?> superExprLeftSubqueryPredicateBuilderListener = new SuperExpressionLeftHandsideSubqueryPredicateBuilder<RestrictionBuilderImpl<CaseWhenThenBuilder<CaseWhenBuilder<T>>>>(subqueryAlias, expressionFactory.createSimpleExpression(expression));
        RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>> restrictionBuilder = startBuilder(
            new RestrictionBuilderImpl<CaseWhenThenBuilder<CaseWhenBuilder<T>>>(this, this, subqueryInitFactory, expressionFactory));
        return subqueryInitFactory.createSubqueryInitiator(restrictionBuilder,
                                                           (SubqueryBuilderListener<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>>>) superExprLeftSubqueryPredicateBuilderListener);
    }

    @Override
    public SubqueryInitiator<CaseWhenThenBuilder<CaseWhenBuilder<T>>> whenExists() {
        verifyBuilderEnded();
        RightHandsideSubqueryPredicateBuilder rightHandside = startBuilder(new RightHandsideSubqueryPredicateBuilder(this, new ExistsPredicate()));
        return subqueryInitFactory.createSubqueryInitiator((CaseWhenThenBuilder<CaseWhenBuilder<T>>) this, rightHandside);
    }

    @Override
    public SubqueryInitiator<CaseWhenThenBuilder<CaseWhenBuilder<T>>> whenNotExists() {
        verifyBuilderEnded();
        RightHandsideSubqueryPredicateBuilder rightHandside = startBuilder(new RightHandsideSubqueryPredicateBuilder(this, new ExistsPredicate(true)));
        return subqueryInitFactory.createSubqueryInitiator((CaseWhenThenBuilder<CaseWhenBuilder<T>>) this, rightHandside);
    }

    @Override
    public CaseWhenBuilder<T> then(String expression) {
        whenClauses.add(new WhenClauseExpression(whenPredicate, expressionFactory.createScalarExpression(expression)));
        return this;
    }

    @Override
    public CaseWhenAndThenBuilder<CaseWhenBuilder<T>> whenAnd() {
        verifyBuilderEnded();
        return startBuilder(new CaseWhenAndThenBuilderImpl<CaseWhenBuilder<T>>(this, this, subqueryInitFactory, expressionFactory));
    }

    @Override
    public CaseWhenOrThenBuilder<CaseWhenBuilder<T>> whenOr() {
        verifyBuilderEnded();
        return startBuilder(new CaseWhenOrThenBuilderImpl<CaseWhenBuilder<T>>(this, this, subqueryInitFactory, expressionFactory));
    }

    @Override
    public T otherwise(String elseExpression) {
        verifyBuilderEnded();
        if(whenClauses.isEmpty()){
            throw new IllegalStateException("No when clauses specified");
        }
        expression = new GeneralCaseExpression(whenClauses, expressionFactory.createScalarExpression(elseExpression));
        listener.onBuilderEnded(this);
        return result;
    }

    @Override
    public void onBuilderEnded(PredicateBuilder o) {
        super.onBuilderEnded(o);
        this.whenPredicate = o.getPredicate();
    }    

    @Override
    public void onBuilderEnded(ExpressionBuilder builder) {
        super.onBuilderEnded(builder);
        whenClauses.add((WhenClauseExpression) builder.getExpression());
    }

    @Override
    public void verifyBuilderEnded() {
        super.verifyBuilderEnded();
        leftSubqueryPredicateBuilderListener.verifySubqueryBuilderEnded();
    }

    @Override
    public Expression getExpression() {
        return expression;
    }
    
}
