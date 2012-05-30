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
 * This is expression is used to read from maps.
 * 
 * @author timm.felden@felden.com
 */
public final class MapAccessExpression extends Expression {
    
    private final Expression name;
    private final List<Expression> arguments;

    public MapAccessExpression(Expression name, List<Expression> arguments) {
        super(name.getLocationToken());

        this.name = name;
        this.arguments = arguments;

        addChild(name);
        addChildren(arguments);
    }

    /**
     * The name expression has to evaluate to a maptype with a domain matching
     * to arguments.
     * 
     * @return the expression that returns the map to be used
     */
    public Expression getName() {
        return name;
    }

    @Override
    public List<Expression> getOperands() {
        return arguments;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

}
