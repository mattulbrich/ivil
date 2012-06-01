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
 * Created on 04/11/2006
 */
package de.uka.iti.ivil.jml.parser.ast.spec;

import java.util.List;

import de.uka.iti.ivil.jml.parser.Token;
import de.uka.iti.ivil.jml.parser.ast.expr.Expression;
import de.uka.iti.ivil.jml.parser.ast.stmt.Statement;
import de.uka.iti.ivil.jml.parser.ast.visitor.GenericVisitor;
import de.uka.iti.ivil.jml.parser.ast.visitor.VoidVisitor;

/**
 * @author Timm Felden
 */
public final class JMLStatement extends Statement {

    private final Expression argument, variant;

    private final String type;

    private final List<StoreRefExpression> assignable;

    public JMLStatement() {
        type = "invalid";
        argument = null;
        variant = null;
        assignable = null;
    }

    public JMLStatement(Token begin, Token end, String type, Expression argument, Expression variant,
            List<StoreRefExpression> assignable) {
        super(begin.beginLine, begin.beginColumn, end.endLine, end.endColumn);
        this.argument = argument;
        this.type = type;
        this.variant = variant;
        this.assignable = assignable;

        assert null == variant | type.equals("loop_invariant");
        assert null == assignable | type.equals("loop_invariant");
    }

    @Override
    public <R, A> R accept(GenericVisitor<R, A> v, A arg) {
        return v.visit(this, arg);
    }

    @Override
    public <A> void accept(VoidVisitor<A> v, A arg) {
        v.visit(this, arg);
    }

    public String getType() {
        return type;
    }

    public Expression getArgument() {
        return argument;
    }

    /**
     * may be null. may not be accessed if the type is not loop_invariant
     */
    public Expression getVariant() {
        assert type.equals("loop_invariant") : "you may not access this field for expressions other then loop invariants!";
        return variant;
    }

    public List<StoreRefExpression> getAssignable() {
        return assignable;
    }
}
