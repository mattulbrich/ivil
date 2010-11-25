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
package de.uka.iti.pseudo.term;

import de.uka.iti.pseudo.environment.Environment;

/**
 * The Class ProgramTerm encapsulates a a reference to a program state. 
 * The actual content of this program depends on the concrete subclass. A {@link LiteralProgramTerm}
 * has a pair of a program counter value and a reference to a program while a {@link SchemaProgramTerm}
 * 
 * Program terms can be terminating or non-terminating.
 */
public abstract class ProgramTerm extends Term {
    
    /**
     * The termination flag.
     */
    private boolean terminating;

    /**
     * Instantiates a new program term with a 
     * given termination status and no subterms.
     * 
     * @param terminating
     *            termination status
     */
    protected ProgramTerm(boolean terminating) {
        super(Environment.getBoolType());
        this.terminating = terminating;
    }
    
    /**
     * Instantiates a new program term with a given termination status and
     * an array of subterms.
     * 
     * @param subterms
     *            the future subterms of the array 
     * @param terminating
     *            the termination status
     */
    public ProgramTerm(Term[] subterms, boolean terminating) {
        super(subterms, Environment.getBoolType());
        this.terminating = terminating;
    }

    /**
     * retrieve the termination status of this program term.
     * 
     * @return true, if it is terminating
     */
    public boolean isTerminating() {
        return terminating;
    }
    
    /**
     * {@inheritDoc}
     * 
     * The content strings depends on the implementing class. Terminating
     * program terms are put in '[[' while non-terminating program terms have
     * single '[' delimeters.
     * 
     * @param typed whether or not types are to be made explicit
     */
    public String toString(boolean typed) {
        String res;
        if(isTerminating())
            res =  "[[" + getContentString(typed) + "]]";
        else
            res =  "[" + getContentString(typed) + "]";
        
        if(typed)
            res += " as bool";
        
        return res;
    }

    /**
     * Gets the content string for string representation. This depends on the implementing
     * subclass.
     * 
     * @param typed
     *            true if types are to be printed explicit.
     * 
     * @return the content string
     */
    protected abstract String getContentString(boolean typed);
}
