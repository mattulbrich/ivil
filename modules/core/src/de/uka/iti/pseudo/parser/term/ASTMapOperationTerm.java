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
package de.uka.iti.pseudo.parser.term;

import java.util.ArrayList;
import java.util.List;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;

import nonnull.Nullable;

/**
 * Represents a short cut to a sequence of $load_? and $store_? functions. The
 * structure of this is a bit ugly, but needed, as the actual $load_ and $store_
 * functions can only be found, if the type of x is known.
 * 
 * @author timm.felden@felden.com
 */
public class ASTMapOperationTerm extends ASTTerm {
    
    final private ASTTerm assignment, map;
    final private List<ASTTerm> domain;
    final private Token location;

    public ASTMapOperationTerm(Token symbol, ASTTerm map, List<ASTTerm> args, /*@Nullable*/ ASTTerm assignment) {
        super(new ArrayList<ASTTerm>(0));
        location = symbol;
        this.map = map;
        domain = args;
        this.assignment = assignment;

        addChild(map);
        addChildren(domain);
        if (null != assignment)
            addChild(assignment);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    @Override
    public Token getLocationToken() {
        return location;
    }

    public final ASTTerm getMapTerm() {
        return map;
    }
    
    public List<ASTTerm> getDomain() {
        return domain;
    }
    
    public boolean isLoad() {
        return null == assignment;
    }

    public ASTTerm getAssignment() {
        return assignment;
    }
}
