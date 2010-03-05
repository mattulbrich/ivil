/*
 * This file is part of PSEUDO
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.parser.file;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;

public class ASTRuleAdd extends ASTRuleElement {

    public ASTRuleAdd(Token t, ASTLocatedTerm locatedTerm) {
        super(t);
        addChild(locatedTerm);
    }

    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }
    

}
