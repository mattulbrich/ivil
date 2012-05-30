/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.parser.term;

import java.util.List;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;

public class ASTMapType extends ASTType {
    
    private final Token location;
    private final List<ASTTypeVar> boundVars;
    private final List<ASTType> domain;
    private final ASTType range;

    public ASTMapType(Token location, List<ASTTypeVar> boundVars, List<ASTType> domain, ASTType range) {
        this.location = location;
        this.boundVars = boundVars;
        this.domain = domain;
        this.range = range;
        
        addChildren(boundVars);
        addChildren(domain);
        addChild(range);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }
    
    @Override public Token getLocationToken() {
        return location;
	}

    public List<ASTTypeVar> getBoundVars() {
        return boundVars;
    }

    public List<ASTType> getDomain() {
        return domain;
    }

    public ASTType getRange() {
        return range;
    }

}
