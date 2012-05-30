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

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.Token;

public abstract class Expression extends ASTElement {

    private final Token first;

    Expression(Token first) {
        this.first = first;
    }

    @Override
    public Token getLocationToken() {
        return first;
    }

    public abstract List<Expression> getOperands();
}
