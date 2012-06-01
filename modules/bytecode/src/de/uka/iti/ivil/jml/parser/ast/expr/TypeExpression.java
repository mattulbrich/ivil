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

import de.uka.iti.ivil.jml.parser.ast.type.Type;
import de.uka.iti.ivil.jml.parser.ast.visitor.GenericVisitor;
import de.uka.iti.ivil.jml.parser.ast.visitor.VoidVisitor;

/**
 * @author Timm Felden
 */
public final class TypeExpression extends Expression {

    /**
     * one of \\type, \\typeof, \\elemtype
     */
    private final String operator;

    /**
     * iff the type of an expression is to be evaluated, expr are nonnull.
     */
    private final Expression expr;

    /**
     * iff the type of a type is to be evaluated, type will be nonnull.
     */
    private final Type type;

    public TypeExpression(String operator, Expression expr) {
        this.operator = operator;
        this.expr = expr;
        type = null;
    }

    public TypeExpression(Type type) {
        this.type = type;
        operator = "\\type";
        expr = null;
    }

    @Override
    public <R, A> R accept(GenericVisitor<R, A> v, A arg) {
        return v.visit(this, arg);
    }

    @Override
    public <A> void accept(VoidVisitor<A> v, A arg) {
        v.visit(this, arg);
    }

    public String getOperator() {
        return operator;
    }

    public Expression getExpr() {
        return expr;
    }

    public Type getType() {
        return type;
    }
}
