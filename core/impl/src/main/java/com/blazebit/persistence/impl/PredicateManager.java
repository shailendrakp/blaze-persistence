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

import com.blazebit.persistence.impl.builder.predicate.CaseExpressionBuilderListener;
import com.blazebit.persistence.CaseWhenStarterBuilder;
import com.blazebit.persistence.impl.builder.predicate.RootPredicate;
import com.blazebit.persistence.impl.builder.predicate.SuperExpressionLeftHandsideSubqueryPredicateBuilder;
import com.blazebit.persistence.impl.builder.predicate.RightHandsideSubqueryPredicateBuilder;
import com.blazebit.persistence.impl.builder.predicate.LeftHandsideSubqueryPredicateBuilderListener;
import com.blazebit.persistence.impl.builder.predicate.RestrictionBuilderImpl;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SimpleCaseWhenStarterBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.builder.expression.CaseWhenBuilderImpl;
import com.blazebit.persistence.impl.builder.expression.SimpleCaseWhenBuilderImpl;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.predicate.BetweenPredicate;
import com.blazebit.persistence.impl.predicate.EqPredicate;
import com.blazebit.persistence.impl.predicate.ExistsPredicate;
import com.blazebit.persistence.impl.predicate.GePredicate;
import com.blazebit.persistence.impl.predicate.GtPredicate;
import com.blazebit.persistence.impl.predicate.InPredicate;
import com.blazebit.persistence.impl.predicate.LePredicate;
import com.blazebit.persistence.impl.predicate.LikePredicate;
import com.blazebit.persistence.impl.predicate.LtPredicate;
import com.blazebit.persistence.impl.predicate.NotPredicate;
import com.blazebit.persistence.impl.predicate.Predicate;
import com.blazebit.persistence.impl.expression.VisitorAdapter;
import com.blazebit.persistence.impl.predicate.AndPredicate;
import com.blazebit.persistence.impl.predicate.IsEmptyPredicate;
import com.blazebit.persistence.impl.predicate.IsNullPredicate;
import com.blazebit.persistence.impl.predicate.MemberOfPredicate;
import com.blazebit.persistence.impl.predicate.OrPredicate;

/**
 *
 * @author Moritz Becker
 * @since 1.0
 */
public abstract class PredicateManager<T> extends AbstractManager {

    protected final SubqueryInitiatorFactory subqueryInitFactory;
    protected final RootPredicate rootPredicate;
    private RightHandsideSubqueryPredicateBuilder rightSubqueryPredicateBuilderListener;
    private final LeftHandsideSubqueryPredicateBuilderListener leftSubqueryPredicateBuilderListener = new LeftHandsideSubqueryPredicateBuilderListener();
    private SuperExpressionLeftHandsideSubqueryPredicateBuilder superExprLeftSubqueryPredicateBuilderListener;
    private CaseExpressionBuilderListener caseExpressionBuilderListener;
    protected final ExpressionFactory expressionFactory;

    PredicateManager(ResolvingQueryGenerator queryGenerator, ParameterManager parameterManager, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory) {
        super(queryGenerator, parameterManager);
        this.rootPredicate = new RootPredicate(parameterManager);
        this.subqueryInitFactory = subqueryInitFactory;
        this.expressionFactory = expressionFactory;
    }

    RestrictionBuilder<T> restrict(AbstractBaseQueryBuilder<?, ?> builder, Expression expr) {
        return rootPredicate.startBuilder(new RestrictionBuilderImpl<T>((T) builder, rootPredicate, expr, subqueryInitFactory, expressionFactory));
    }

    CaseWhenStarterBuilder<RestrictionBuilder<T>> restrictCase(AbstractBaseQueryBuilder<?, ?> builder) {
        RestrictionBuilder<T> restrictionBuilder = rootPredicate.startBuilder(new RestrictionBuilderImpl<T>((T) builder, rootPredicate, subqueryInitFactory, expressionFactory));
        caseExpressionBuilderListener = new CaseExpressionBuilderListener((RestrictionBuilderImpl<T>) restrictionBuilder);
        return caseExpressionBuilderListener.startBuilder(new CaseWhenBuilderImpl<RestrictionBuilder<T>>(restrictionBuilder, caseExpressionBuilderListener, subqueryInitFactory, expressionFactory));
    }

    SimpleCaseWhenStarterBuilder<RestrictionBuilder<T>> restrictSimpleCase(AbstractBaseQueryBuilder<?, ?> builder, Expression caseOperand) {
        RestrictionBuilder<T> restrictionBuilder = rootPredicate.startBuilder(new RestrictionBuilderImpl<T>((T) builder, rootPredicate, subqueryInitFactory, expressionFactory));
        caseExpressionBuilderListener = new CaseExpressionBuilderListener((RestrictionBuilderImpl<T>) restrictionBuilder);
        return caseExpressionBuilderListener.startBuilder(new SimpleCaseWhenBuilderImpl<RestrictionBuilder<T>>(restrictionBuilder, caseExpressionBuilderListener, expressionFactory, caseOperand));
    }

    SubqueryInitiator<RestrictionBuilder<T>> restrict(AbstractBaseQueryBuilder<?, ?> builder) {
        RestrictionBuilder<T> restrictionBuilder = (RestrictionBuilder<T>) rootPredicate.startBuilder(
                new RestrictionBuilderImpl<T>((T) builder, rootPredicate, subqueryInitFactory, expressionFactory));
        return subqueryInitFactory.createSubqueryInitiator(restrictionBuilder, leftSubqueryPredicateBuilderListener);
    }

    SubqueryInitiator<RestrictionBuilder<T>> restrict(AbstractBaseQueryBuilder<?, ?> builder, String subqueryAlias, String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression);
        superExprLeftSubqueryPredicateBuilderListener = new SuperExpressionLeftHandsideSubqueryPredicateBuilder(subqueryAlias, expr);
        RestrictionBuilder<T> restrictionBuilder = (RestrictionBuilder<T>) rootPredicate.startBuilder(
                new RestrictionBuilderImpl<T>((T) builder, rootPredicate, subqueryInitFactory, expressionFactory));
        return subqueryInitFactory.createSubqueryInitiator(restrictionBuilder, superExprLeftSubqueryPredicateBuilderListener);
    }

    SubqueryInitiator<T> restrictExists(T result) {
        rightSubqueryPredicateBuilderListener = rootPredicate.startBuilder(new RightHandsideSubqueryPredicateBuilder(
                rootPredicate, new ExistsPredicate()));
        return subqueryInitFactory.createSubqueryInitiator(result, rightSubqueryPredicateBuilderListener);
    }

    SubqueryInitiator<T> restrictNotExists(T result) {
        RightHandsideSubqueryPredicateBuilder subqueryListener = rootPredicate.startBuilder(
                new RightHandsideSubqueryPredicateBuilder(rootPredicate, new NotPredicate(new ExistsPredicate())));
        return subqueryInitFactory.createSubqueryInitiator(result, subqueryListener);
    }

    void applyTransformer(ExpressionTransformer transformer) {
        // carry out transformations
        rootPredicate.getPredicate().accept(new TransformationVisitor(transformer, getClauseType()));
    }

    void verifyBuilderEnded() {
        rootPredicate.verifyBuilderEnded();
        leftSubqueryPredicateBuilderListener.verifySubqueryBuilderEnded();
        if (rightSubqueryPredicateBuilderListener != null) {
            rightSubqueryPredicateBuilderListener.verifySubqueryBuilderEnded();
        }
        if (superExprLeftSubqueryPredicateBuilderListener != null) {
            superExprLeftSubqueryPredicateBuilderListener.verifySubqueryBuilderEnded();
        }
        if(caseExpressionBuilderListener != null){
            caseExpressionBuilderListener.verifyBuilderEnded();
        }
    }

    void acceptVisitor(Predicate.Visitor v) {
        rootPredicate.getPredicate().accept(v);
    }

    boolean hasPredicates() {
        return rootPredicate.getPredicate().getChildren().size() > 0;
    }

    void buildClause(StringBuilder sb) {
        queryGenerator.setQueryBuffer(sb);
        if (!hasPredicates()) {
            return;
        }
        sb.append(' ').append(getClauseName()).append(' ');
        applyPredicate(queryGenerator, sb);
    }

    void buildClausePredicate(StringBuilder sb) {
        queryGenerator.setQueryBuffer(sb);
        applyPredicate(queryGenerator, sb);
    }

    protected abstract String getClauseName();

    protected abstract ClauseType getClauseType();
    
    void applyPredicate(ResolvingQueryGenerator queryGenerator, StringBuilder sb) {
        rootPredicate.getPredicate().accept(queryGenerator);
    }

    // TODO: needs equals-hashCode implementation
    static class TransformationVisitor extends VisitorAdapter {

        private final ExpressionTransformer transformer;
        private final ClauseType fromClause;

        public TransformationVisitor(ExpressionTransformer transformer, ClauseType fromClause) {
            this.transformer = transformer;
            this.fromClause = fromClause;
        }

        @Override
        public void visit(BetweenPredicate predicate) {
            predicate.setStart(transformer.transform(predicate.getStart(), fromClause));
            predicate.setLeft(transformer.transform(predicate.getLeft(), fromClause));
            predicate.setEnd(transformer.transform(predicate.getEnd(), fromClause));
        }

        @Override
        public void visit(GePredicate predicate) {
            predicate.setLeft(transformer.transform(predicate.getLeft(), fromClause));
            predicate.setRight(transformer.transform(predicate.getRight(), fromClause));
        }

        @Override
        public void visit(GtPredicate predicate) {
            predicate.setLeft(transformer.transform(predicate.getLeft(), fromClause));
            predicate.setRight(transformer.transform(predicate.getRight(), fromClause));
        }

        @Override
        public void visit(LikePredicate predicate) {
            predicate.setLeft(transformer.transform(predicate.getLeft(), fromClause));
            predicate.setRight(transformer.transform(predicate.getRight(), fromClause));
        }

        @Override
        public void visit(EqPredicate predicate) {
            predicate.setLeft(transformer.transform(predicate.getLeft(), fromClause));
            predicate.setRight(transformer.transform(predicate.getRight(), fromClause));
        }

        @Override
        public void visit(LePredicate predicate) {
            predicate.setLeft(transformer.transform(predicate.getLeft(), fromClause));
            predicate.setRight(transformer.transform(predicate.getRight(), fromClause));
        }

        @Override
        public void visit(LtPredicate predicate) {
            predicate.setLeft(transformer.transform(predicate.getLeft(), fromClause));
            predicate.setRight(transformer.transform(predicate.getRight(), fromClause));
        }

        @Override
        public void visit(InPredicate predicate) {
            predicate.setLeft(transformer.transform(predicate.getLeft(), fromClause));
            predicate.setRight(transformer.transform(predicate.getRight(), fromClause));
        }

        @Override
        public void visit(ExistsPredicate predicate) {
            predicate.setExpression(transformer.transform(predicate.getExpression(), fromClause));
        }

        @Override
        public void visit(MemberOfPredicate predicate) {
            predicate.setLeft(transformer.transform(predicate.getLeft(), fromClause));
            predicate.setRight(transformer.transform(predicate.getRight(), fromClause));
        }

        @Override
        public void visit(IsEmptyPredicate predicate) {
            predicate.setExpression(transformer.transform(predicate.getExpression(), fromClause));
        }

        @Override
        public void visit(IsNullPredicate predicate) {
            predicate.setExpression(transformer.transform(predicate.getExpression(), fromClause));
        }
    }
}
