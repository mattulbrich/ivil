/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.parser.file;

import java.util.List;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;

public class ASTGoalAction extends ASTElement {
    
    @Nullable Token goalKindToken;
    @Nullable Token name;

    public ASTGoalAction(@NonNull Token t) {
        goalKindToken = t;
	name = null;
    }

    public ASTGoalAction(Token t, Token name, List<ASTRuleElement> list) {
        goalKindToken = t;
        this.name = name;
	assert list.size() > 0;
        addChildren(list);
    }

    public @Nullable Token getLocationToken() {
        if(goalKindToken != null)
            return goalKindToken;
        else
            return getChildren().get(0).getLocationToken();
    }

    @Override public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public @Nullable Token getGoalKindToken() {
        return goalKindToken;
    }

    public @Nullable Token getName() {
        return name;
    }

}
