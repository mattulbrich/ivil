/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */

package de.uka.iti.pseudo.term;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Function;

/**
 * The Class AssignModality captures assignments in modalities.
 * 
 * Each assignment consists of a target and an assigned term.
 * 
 * An assignment can either be targeted to a nullary function smybol
 * which is explicitly defined as assignable or to a schemaVariable.
 * 
 */
public class AssignModality extends Modality {
    
    /**
     * This interface is used as a marker to {@link Function} and 
     * {@link SchemaVariable}.
     *
     * Those are the syntactical entities that can be used as 
     * targets in an assignment
     */
    public static interface AssignTarget {
        /**
         * get the name of the entity. This is implemented both
         * for {@link Function} and {@link SchemaVariable}
         * 
         * @return identifier describing the entity
         */
        public String getName();

        /**
         * get the type of this entity. Both schema vars and functions
         * do have a dedicated type.
         * 
         * @return type of the target
         */
        public Type getResultType();
    };

    /**
     * The symbol to which something is assigned.
     */
    private AssignTarget target;
    
    /**
     * The term which is assigned to the constant symbol
     */
    private Term assignedTerm;

    private boolean schemaAssignment;

    /**
     * Instantiates a new assign modality element.
     * 
     * Assign modalities can have either schema variables or function
     * symbols as target. In the first case, the function symbol must
     * be assignable.
     * 
     * 
     * @param target
     *            the target to assign to.
     * @param assignedTerm
     *            the term to assign
     * 
     * @throws TermException
     *            if constant is not assignable, or type mismatch
     */
    public AssignModality(@NonNull AssignTarget target, @NonNull Term assignedTerm)
            throws TermException {
        super();
        this.target = target;
        this.assignedTerm = assignedTerm;
        
        this.schemaAssignment = target instanceof SchemaVariable;

        check();
    }
    
    /**
     * does this assignment assign to a schema variable?
     * 
     * @return true if the assignment binds a schme variable
     */
    public boolean isSchemaAssignment() {
        return schemaAssignment;
    }
    
    /**
     * get the target of this assignment entity, 
     * peu important whether it is a 
     * schema variable or a function symbol
     * 
     * @return the assignment target
     */
    public AssignTarget getAssignTarget() {
        return target;
    }

    public Term getAssignedTerm() {
        return assignedTerm;
    }

    /*
     * Check. check the type of assigned functions
     * 
     * fail if types mismatch or the constant is not assignable.
     */
    private void check() throws TermException {
        
        if(!isSchemaAssignment()) {
            Function targetFunction = (Function) target; 

            if (!targetFunction.isAssignable())
                throw new TermException("The assigned symbol " + targetFunction
                        + " is not assignable");
        }
        
        if (!target.getResultType().equals(assignedTerm.getType()))
            throw new TermException(
                    "The types of symbol and term differ.\nSymbol:"
                        + target.getResultType() + "\nTerm:"
                        + assignedTerm.getType());
        
    }

    @Override
    public String toString(boolean typed) {
        return target.getName() + ":=" + assignedTerm.toString(typed);
    }

    @Override
    public void visit(ModalityVisitor visitor) throws TermException {
        visitor.visit(this);
    }

    /**
     * This object is equal to another object if it is an assignment and assigned symbol
     * and term are equal.
     */
    @Override
    public boolean equals(Object object) {
        if (object instanceof AssignModality) {
            AssignModality mod = (AssignModality) object;
            return mod.getAssignTarget().equals(getAssignTarget())
                    && mod.getAssignedTerm().equals(getAssignedTerm());
        }
        return false;
    }

}
