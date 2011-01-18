/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term.statement;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Update;

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
 * <p>
 * Updates and assignment statements keep a list of {@link Assignment} objects.
 * 
 * @see Update
 * @see AssignmentStatement
 */
public class Assignment {
    
    /**
     * The <b>right</b> hand side of the assignment.
     */
    private Term value;
    
    /**
     * The <b>left</b> hand side of the assignment.
     */
    private Term target;

    /**
     * Instantiates a new assignment.
     * 
     * @param target
     *            the target to assign to
     * @param value
     *            the value to be assigned
     * 
     * @throws TermException
     *             if types of target and value are not compatible or if target
     *             is not assignable.
     */
    public Assignment(@NonNull Term target, @NonNull Term value) throws TermException {
        this.target = target;
        this.value = value;
        check();
    }

    /*
     * Check whether the object is well defined: Must have an assignable or
     * schematic target and types must be compatible.
     * Throw an exception otherwise.
     */
    private void check() throws TermException {
        if (getTarget() instanceof Application) {
            Application appl = (Application) getTarget();
            Function func = appl.getFunction();
            if(!func.isAssignable())
                throw new TermException("Target in an assignment needs to be 'assignable'");
        } else if (!(getTarget() instanceof SchemaVariable)) {
            throw new TermException(
                    "Target in an assignment needs to be assignable constant or schema variable, received: " + getTarget());
        }

        // TODO In case of polymorphic symbols, this must be different: modulo renaming
        if(!getTarget().getType().equals(getValue().getType()))
            throw new TermException("target and value need to have identical types: " + getTarget().getType() + ","
                    + getValue().getType());
    }

    @Override
    public String toString() {
        return toString(Term.SHOW_TYPES);
    }
    
    public String toString(boolean typed) {
        return getTarget().toString(false) + " := " + getValue().toString(typed);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Assignment) {
            Assignment ass2 = (Assignment) obj;
            return getTarget().equals(ass2.getTarget()) && getValue().equals(ass2.getValue());
        }
        return false;
    }
    
    /**
     * Gets the target (left hand side) of this assignment.
     * 
     * @return the target
     */
    public @NonNull Term getTarget() {
        return this.target;
    }

    /**
     * Gets the value (right hand side) of this assignment.
     * 
     * @return the value
     */
    public @NonNull Term getValue() {
        return this.value;
    }

}
