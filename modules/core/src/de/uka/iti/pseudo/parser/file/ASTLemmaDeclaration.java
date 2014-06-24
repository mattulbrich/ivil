/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.parser.file;

import java.util.List;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.term.ASTTerm;
import de.uka.iti.pseudo.util.Pair;

public class ASTLemmaDeclaration extends ASTDeclarationBlock {

    private final Token name;
    private final List<Pair<Token, Token>> properties;

    public ASTLemmaDeclaration(Token first, Token name, ASTTerm term, List<Pair<Token, Token>> properties) {
        super(first);
        this.name = name;
        this.properties = properties;

        addChild(term);
    }

    public List<Pair<Token, Token>> getProperties() {
        return properties;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public Token getName() {
        return name;
    }

    // term may be replaced, so better get it from the children
    public ASTTerm getTerm() {
        return (ASTTerm) getChildren().get(0);
    }

}
