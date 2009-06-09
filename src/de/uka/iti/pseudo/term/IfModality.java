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
import nonnull.Nullable;

// TODO: Auto-generated Javadoc
/**
 * The Class IfModality captures a conditional program modality.
 */
public class IfModality extends Modality {

    /**
     * The condition term, must always be of boolean type
     */
    private Term conditionTerm;
    
    /**
     * Does this conditional modality have an else branch
     */
    private boolean hasElseModality;

    /**
     * Instantiates a new modality without else branch.
     * 
     * @param condTerm
     *            the conditional term of boolean type
     * @param thenMod
     *            the modality of the then branch
     */
    public IfModality(Term condTerm, Modality thenMod) 
            throws TermException {
        super(thenMod);
        this.conditionTerm = condTerm;
        this.hasElseModality = false;
        typeCheck();
    }
    
    /**
     * Instantiates a new if modality with else branch
     * 
     * @param condTerm
     *            the conditional term of boolean type
     * @param thenMod
     *            the modality of the then branch
     * @param elseMod
     *            the modality of the else branch
     * @throws TermException
     *             if typing is illegal
     */
    public IfModality(Term condTerm, Modality thenMod, Modality elseMod) throws TermException {
        super(thenMod, elseMod);
        this.conditionTerm = condTerm;
        this.hasElseModality = true;
        typeCheck();
    }

    /*
     * make sure that the type of the conditional expression is bool.
     */
    private void typeCheck() throws TermException {
        if(!conditionTerm.getType().equals(Environment.getBoolType()))
            throw new TermException("Condition term in IfModality must be boolean");
    }

    @Override 
    public String toString(boolean typed) {
        return "if " + conditionTerm.toString(typed) + " then "
                + getSubModality(0).toString(typed) + 
                (hasElseModality ? " else " + getSubModality(1).toString(typed) : "")
                + " end";
    }

    @Override 
    public void visit(ModalityVisitor visitor) throws TermException {
        visitor.visit(this);
    }

    /**
     * Gets the conditional term.
     * 
     * @return the condition term
     */
    public Term getConditionTerm() {
        return conditionTerm;
    }
    
    /**
     * Gets the then modality.
     * 
     * @return the then modality
     */
    public Modality getThenModality() {
        return getSubModality(0);
    }
    
    /**
     * Gets the else modality if there is any, if not, return null
     * 
     * @return the else modality
     */
    public @Nullable Modality getElseModality() {
        if(countModalities() > 1)
            return getSubModality(1);
        else
            return null;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof IfModality) {
            IfModality ifmod = (IfModality) object;
            
            if(hasElseModality != ifmod.hasElseModality)
                return false;
            
            if(!getConditionTerm().equals(ifmod.getConditionTerm()))
                return false;
            
            if(!getThenModality().equals(ifmod.getThenModality()))
                return false;
            
            if(hasElseModality && !getElseModality().equals(ifmod.getElseModality()))
                return false;
            
            return true;
        }
        return false;
    }

}
