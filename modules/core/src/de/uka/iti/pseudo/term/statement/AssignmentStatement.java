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
package de.uka.iti.pseudo.term.statement;

import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;

/**
 * Captures an assignment <code>v:=t</code> as it can appear in assignment
 * statements in programs or as it may appear as a basic assignment in an
 * update.
 * 
 * <p>
 * Both the entity to which a value is assigned and the value term which is
 * assigned are considered subterms of the statement. Their terms must
 * conincide.
 * 
 */
public class AssignmentStatement extends Statement {
 // TODO DOC
    public AssignmentStatement(int sourceLineNumber, Term target, Term value) throws TermException {
        super(sourceLineNumber, new Term[] { target, value });
        check();
    }

    public AssignmentStatement(Term target, Term value) throws TermException {
        this(-1, target, value);
    }

    private void check() throws TermException {
        if (getTarget() instanceof Application) {
            Application appl = (Application) getTarget();
            Function func = appl.getFunction();
            if(!func.isAssignable())
                throw new TermException("Target in an assignment needs to be 'assignable'");
        } else if (!(getTarget() instanceof SchemaVariable)) {
            throw new TermException(
                    "Target in an assignment needs to be assignable constant or schema variable");
        }
        
        if(!getTarget().getType().equals(getValue().getType()))
            throw new TermException("target and value need to have identical types");
    }

    public String toString(boolean typed) {
        return getTarget().toString(false) + " := " + getValue().toString(typed);
    }
    
    public void visit(StatementVisitor visitor) throws TermException {
        visitor.visit(this);
    }

    public Term getTarget() {
        return getSubterms().get(0);
    }

    public Term getValue() {
        return getSubterms().get(1);
    }

}
