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
package de.uka.iti.ivil.jml.parser.ast.expr;


import java.util.List;

import de.uka.iti.ivil.jml.parser.ast.body.Parameter;
import de.uka.iti.ivil.jml.parser.ast.visitor.GenericVisitor;
import de.uka.iti.ivil.jml.parser.ast.visitor.VoidVisitor;

/**
 * @author Julio Vilmar Gesser
 */
public final class QuantificationExpression extends Expression {

    private final Expression restriction, expression;

    private final List<Parameter> targets;

    private final String quantifier;

    public QuantificationExpression(de.uka.iti.ivil.jml.parser.Token begin, de.uka.iti.ivil.jml.parser.Token end,
            String quantifer, List<Parameter> targets,
            Expression first, Expression second) {
        super(begin.beginLine, begin.beginColumn, end.endLine, end.endColumn);
        this.quantifier = quantifer;
        this.targets = targets;
        if (null == second) {
            restriction = null;
            expression = first;
        } else {
            restriction = first;
            expression = second;
        }
    }

    @Override
    public <R, A> R accept(GenericVisitor<R, A> v, A arg) {
        return v.visit(this, arg);
    }

    @Override
    public <A> void accept(VoidVisitor<A> v, A arg) {
        v.visit(this, arg);
    }

    public String getQuantifier() {
        return quantifier;
    }

    public List<Parameter> getTargets() {
        return targets;
    }

    public Expression getRestriction() {
        return restriction;
    }

    public Expression getExpression() {
        return expression;
    }
}
