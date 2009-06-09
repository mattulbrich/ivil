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

public abstract class ASTTerm extends ASTElement {
    
    private List<ASTTerm> subterms;
    
    private Typing typing;

    public ASTTerm(List<ASTTerm> subterms) {
        this.subterms = subterms;
        addChildren(subterms);
    }

    public List<ASTTerm> getSubterms() {
        return subterms;
    }
    
}
