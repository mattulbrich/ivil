/*
 * Copyright (C) 2012 Timm Felden.
 * 
 * This file is part of Java 1.5 parser and Abstract Syntax Tree.
 *
 * Java 1.5 parser and Abstract Syntax Tree is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Java 1.5 parser and Abstract Syntax Tree is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Java 1.5 parser and Abstract Syntax Tree.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.uka.iti.ivil.jml.parser.ast.expr;

import de.uka.iti.ivil.jml.parser.ast.visitor.GenericVisitor;
import de.uka.iti.ivil.jml.parser.ast.visitor.VoidVisitor;

/**
 * @author Timm Felden
 */
public final class TypeRelationExpression extends Expression {

    private TypeExpression left;

    private TypeExpression right;

    /**
     * if true, the operation is an equality between types. if false, the left
     * type is a subtype of the right type.
     */
    private final boolean isEquality;

    public TypeRelationExpression(TypeExpression left, TypeExpression right, boolean isEquality) {
        this.left = left;
        this.right = right;
        this.isEquality = isEquality;
    }

    @Override
    public <R, A> R accept(GenericVisitor<R, A> v, A arg) {
        return v.visit(this, arg);
    }

    @Override
    public <A> void accept(VoidVisitor<A> v, A arg) {
        v.visit(this, arg);
    }

    public TypeExpression getLeft() {
        return left;
    }

    public boolean isEquality() {
        return isEquality;
    }

    public TypeExpression getRight() {
        return right;
    }
}
