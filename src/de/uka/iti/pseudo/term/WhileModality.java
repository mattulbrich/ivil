/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.term;

import de.uka.iti.pseudo.environment.Environment;

// TODO: Auto-generated Javadoc
/**
 * The Class WhileModality encapsulates 
 */
public class WhileModality extends Modality {

    /**
     * The condition term.
     */
    private Term conditionTerm;

    /**
     * Instantiates a new while modality.
     * 
     * @param conditionTerm
     *            the condition term
     * @param body
     *            the body
     * 
     * @throws TermException
     *             the term exception
     */
    public WhileModality(Term conditionTerm, Modality body) throws TermException {
        super(body);
        this.conditionTerm = conditionTerm;
        typeCheck();
    }

    /**
     * Type check this while modality.
     * 
     * @throws TermException
     *             thrown if the condition term is not boolean
     */
    private void typeCheck() throws TermException {
        if(!conditionTerm.getType().equals(Environment.getBoolType()))
            throw new TermException("Condition term of a WhileModality must be boolean");
    }

    /* (non-Javadoc)
     * @see de.uka.iti.pseudo.term.Modality#toString(boolean)
     */
    @Override
    public String toString(boolean typed) {
        return "while " + conditionTerm.toString(typed) + " do "
                + getSubModality(0).toString(typed) + " end";
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
     * Gets the body.
     * 
     * @return the body
     */
    public Modality getBody() {
        return getSubModality(0);
    }

    /* (non-Javadoc)
     * @see de.uka.iti.pseudo.term.Modality#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object instanceof WhileModality) {
            WhileModality wmod = (WhileModality) object;
            
            if(!getConditionTerm().equals(wmod.getConditionTerm()))
                return false;
            
            if(!getBody().equals(wmod.getBody()))
                return false;
            
            return true;
        }
        return false;
    }
}
