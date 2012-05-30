/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;
import de.uka.iti.pseudo.parser.boogie.ast.expression.Expression;
import de.uka.iti.pseudo.parser.boogie.util.ASTConversions;

public final class CallForallStatement extends Statement {
    
    private final String name;
    private final List<Attribute> attr;
    /**
     * arglist can contain wildcard expressions. In this case a suitable
     * ∀-quantified variable has to be used.
     */
    private final List<Expression> arglist;

    public CallForallStatement(Token first, List<Attribute> attr, String name, List<Expression> arglist) {
        super(first);

        this.name = ASTConversions.getEscapedName(name);
        this.arglist = arglist;
        this.attr = attr;

        addChildren(attr);
        addChildren(arglist);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public String getName() {
        return name;
    }

    public List<Expression> getArguments() {
        return arglist;
    }

    public List<Attribute> getAttributes() {
        return attr;
    }

}
