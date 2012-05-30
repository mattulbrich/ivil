/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.parser.boogie.ast.expression;

import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;

/**
 * This expression creates a new map which satisfies (∀I: rval[I] =
 * (I==arguments)?update:name[I]), thus the type of this expression is
 * typeof(name).
 * 
 * @author timm.felden@felden.com
 */
public final class MapUpdateExpression extends Expression {
    
    private final Expression name, update;
    private final List<Expression> arguments;

    public MapUpdateExpression(Expression rval, List<Expression> arguments, Expression update) {
        super(update.getLocationToken());

        this.name = rval;
        this.arguments = arguments;
        this.update = update;

        addChild(rval);
        addChildren(arguments);
        addChild(update);
    }

    @Override
    public List<Expression> getOperands() {
        return arguments;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public Expression getUpdate() {
        return update;
    }

    public Expression getName() {
        return name;
    }

}
