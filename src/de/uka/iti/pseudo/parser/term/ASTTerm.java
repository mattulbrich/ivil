/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.parser.term;

import java.util.List;

import nonnull.NonNull;

import de.uka.iti.pseudo.term.creation.Typing;
import de.uka.iti.pseudo.util.SelectList;

@NonNull
public abstract class ASTTerm extends ASTElement {
    
    private Typing typing;

    public ASTTerm(List<ASTTerm> subterms) {
        addChildren(subterms);
    }

    public List<ASTTerm> getSubterms() {
        return SelectList.select(ASTTerm.class, getChildren());
    }

    public Typing getTyping() {
        return typing;
    }

    public void setTyping(Typing typing) {
        this.typing = typing;
    }
    
}
