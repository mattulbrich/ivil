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

/**
 * An assignment of one or more assignable locations.
 * 
 * @author timm.felden@felden.com
 */
public final class AssignmentStatement extends Statement {

    private final List<SimpleAssignment> assignments;

    public AssignmentStatement(List<SimpleAssignment> assignments) {
        super(assignments.get(0).name);
        
        this.assignments = assignments;

        addChildren(assignments);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public List<SimpleAssignment> getAssignments() {
        return assignments;
    }

}
