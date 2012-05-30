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
import de.uka.iti.pseudo.parser.boogie.util.ASTConversions;

public final class HavocStatement extends Statement {

    private final List<String> varnames;

    public HavocStatement(Token first, List<Token> vars) {
        super(first);

        varnames = ASTConversions.toEscapedNameList(vars);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public List<String> getVarnames() {
        return varnames;
    }

}
