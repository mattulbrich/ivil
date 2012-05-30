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

import java.util.Collections;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

public final class ProcedureBody extends ASTElement {

    private final Token first;
    private final List<LocalVariableDeclaration> vars;
    private final List<Statement> statements;

    public ProcedureBody(Token first, List<LocalVariableDeclaration> vars, List<Statement> statements) {
        this.first = first;
        this.vars = vars;
        this.statements = statements;

        addChildren(vars);
        addChildren(statements);
    }

    @Override
    public Token getLocationToken() {
        return first;
    }

    public List<LocalVariableDeclaration> getVariablaDeclarations() {
        return Collections.unmodifiableList(vars);
    }

    public List<Statement> getStatements() {
        return Collections.unmodifiableList(statements);
    }

    public String getName() {
        return first.image;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

}
