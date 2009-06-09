/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.term;

import nonnull.Nullable;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.util.Util;

/**
 * The Class WhileModality encapsulates a while loop as an object.
 * 
 * A while loop has got another modality as body and a condition term
 * whose type must be boolean.
 * 
 * The body is stored as first submodality.
 */
public class WhileModality extends Modality {

    /**
     * The condition term.
     */
    private Term conditionTerm;
    
    /**
     * If an invariant has been specified for the loop
     */
    private @Nullable Term invariantTerm;

    /**
     * Instantiates a new while modality.
     * 
     * @param conditionTerm
     *            the condition term
     * @param body
     *            the body modaliy
     * @param invariantTerm 
     * 
     * @throws TermException
     *             if conditionTerm is not typed bool
     */
    public WhileModality(Term conditionTerm, Modality body, Term invariantTerm) throws TermException {
        super(body);
        this.conditionTerm = conditionTerm;
        this.invariantTerm = invariantTerm;
        typeCheck();
    }

    /**
     * Type check the while modality.
     * 
     * @throws TermException
     *             thrown if the condition term is not boolean
     */
    private void typeCheck() throws TermException {
        if(!conditionTerm.getType().equals(Environment.getBoolType()))
            throw new TermException("Condition term of a WhileModality must be boolean");
        
        if(invariantTerm != null && !invariantTerm.getType().equals(Environment.getBoolType()))
            throw new TermException("Invariant term of a WhileModality must be boolean");
    }

    /* (non-Javadoc)
     * @see de.uka.iti.pseudo.term.Modality#toString(boolean)
     */
    @Override
    public String toString(boolean typed) {
        StringBuilder sb = new StringBuilder();
        sb.append("while ").append(conditionTerm.toString(typed));
        if(invariantTerm != null)
            sb.append(" inv ").append(invariantTerm.toString(typed));
        sb.append(" do ").append(getSubModality(0).toString(typed)).append(" end");
        return sb.toString();
    }

    /* (non-Javadoc)
     * @see de.uka.iti.pseudo.term.Modality#visit(de.uka.iti.pseudo.term.ModalityVisitor)
     */
    @Override
    public void visit(ModalityVisitor visitor) throws TermException {
        visitor.visit(this);
    }
    
    /**
     * Gets the condition term.
     * 
     * @return the condition term
     */
    public Term getConditionTerm() {
        return conditionTerm;
    }
    
    /**
     * Gets the stored invariant as term.
     * 
     * @return the invariant, null if non specified
     */
    public @Nullable Term getInvariantTerm() {
        return invariantTerm;
    }
    
    /**
     * returns true if an invariant has been specified
     * 
     * @return true if an invariant has been specified
     */
    public boolean hasInvariantTerm() {
        return invariantTerm != null;
    }
    
    /**
     * Gets the while body.
     * 
     * @return the body modality-
     */
    public Modality getBody() {
        return getSubModality(0);
    }

    /* an object is equal if it is a while modality and condition, body and invariant
     * coincide.
     */
    @Override
    public boolean equals(Object object) {
        if (object instanceof WhileModality) {
            WhileModality wmod = (WhileModality) object;
            
            if(!getConditionTerm().equals(wmod.getConditionTerm()))
                return false;
            
            if(!getBody().equals(wmod.getBody()))
                return false;
            
            if(!Util.equalOrNull(getInvariantTerm(), wmod.getInvariantTerm()))
                return false;
            
            return true;
        }
        return false;
    }

   
}
