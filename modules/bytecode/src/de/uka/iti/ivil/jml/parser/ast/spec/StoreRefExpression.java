/*
 * Copyright (C) 2007 J�lio Vilmar Gesser.
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
/*
 * Created on 05/10/2006
 */
package de.uka.iti.ivil.jml.parser.ast.spec;

import de.uka.iti.ivil.jml.parser.ast.expr.Expression;
import de.uka.iti.ivil.jml.parser.ast.expr.ThisExpr;
import de.uka.iti.ivil.jml.parser.ast.visitor.GenericVisitor;
import de.uka.iti.ivil.jml.parser.ast.visitor.VoidVisitor;

/**
 * @author Timm Felden
 */
public class StoreRefExpression extends Expression {

    private final Expression ref, field;
    private final boolean isArray;

    // defaults to this.*
    public StoreRefExpression() {
        ref = new ThisExpr();
        field = new AnyFieldExpression();
        isArray = false;
    }

    public StoreRefExpression(Expression ref, Expression field, boolean isArray) {
        this.ref = ref;
        this.field = field;
        this.isArray = isArray;
    }

    @Override
    public <R, A> R accept(GenericVisitor<R, A> v, A arg) {
        return v.visit(this, arg);
    }

    @Override
    public <A> void accept(VoidVisitor<A> v, A arg) {
        v.visit(this, arg);
    }

    /**
     * @return return the path to the referenced object or null, if the
     *         reference is implicit (which is this or static)
     */
    public final Expression ref() {
        return ref;
    }

    /**
     * @return expression that leads to a set of fields
     */
    public final Expression field() {
        return field;
    }

    /**
     * @return true iff the field is an array index and not an actual field
     */
    public final boolean isArray() {
        return isArray;
    }
}
