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

package com.blazebit.persistence.impl.expression;

import com.blazebit.persistence.impl.SimpleQueryGenerator;
import com.blazebit.persistence.impl.predicate.Predicate;

/**
 *
 * @author Moritz Becker
 */
public class WhenClauseExpression implements Expression {
    private final Expression condition;
    private Expression result;

    public WhenClauseExpression(Expression condition, Expression result) {
        this.condition = condition;
        this.result = result;
    }

    @Override
    public WhenClauseExpression clone() {
        return new WhenClauseExpression(condition.clone(), result.clone());
    }

    public Expression getCondition() {
        return condition;
    }

    public Expression getResult() {
        return result;
    }

    public void setResult(Expression result) {
        this.result = result;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(ResultVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + (this.condition != null ? this.condition.hashCode() : 0);
        hash = 61 * hash + (this.result != null ? this.result.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WhenClauseExpression other = (WhenClauseExpression) obj;
        if (this.condition != other.condition && (this.condition == null || !this.condition.equals(other.condition))) {
            return false;
        }
        if (this.result != other.result && (this.result == null || !this.result.equals(other.result))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        SimpleQueryGenerator generator = new SimpleQueryGenerator();
        generator.setQueryBuffer(sb);
        generator.visit(this);
        return sb.toString();
    }
}
