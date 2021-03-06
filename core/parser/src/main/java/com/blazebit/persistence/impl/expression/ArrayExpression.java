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

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class ArrayExpression implements PathElementExpression {

    private final PropertyExpression base;
    private final Expression index;

    public ArrayExpression(PropertyExpression base, Expression index) {
        this.base = base;
        this.index = index;
    }

    @Override
    public ArrayExpression clone() {
        return new ArrayExpression(base.clone(), index.clone());
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(ResultVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public PropertyExpression getBase() {
        return base;
    }

    public Expression getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return base.toString() + "[" + index.toString() + "]";
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 61 * hash + (this.base != null ? this.base.hashCode() : 0);
        hash = 61 * hash + (this.index != null ? this.index.hashCode() : 0);
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
        final ArrayExpression other = (ArrayExpression) obj;
        if (this.base != other.base && (this.base == null || !this.base.equals(other.base))) {
            return false;
        }
        if (this.index != other.index && (this.index == null || !this.index.equals(other.index))) {
            return false;
        }
        return true;
    }

}
