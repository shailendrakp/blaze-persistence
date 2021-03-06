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

package com.blazebit.persistence.impl.predicate;

import com.blazebit.persistence.impl.expression.Expression;

/**
 *
 * @author Moritz Becker
 */
public abstract class NegatableBinaryExpressionPredicate extends BinaryExpressionPredicate implements Negatable {
    protected boolean negated;

    public NegatableBinaryExpressionPredicate(Expression left, Expression right, boolean negated) {
        super(left, right);
        this.negated = negated;
    }

    public NegatableBinaryExpressionPredicate(Expression left, Expression right) {
        this(left, right, false);
    }
    
    @Override
    public abstract NegatableBinaryExpressionPredicate clone();

    @Override
    public boolean isNegated() {
        return negated;
    }

    @Override
    public void setNegated(boolean negated) {
        this.negated = negated;
    }
    
}
