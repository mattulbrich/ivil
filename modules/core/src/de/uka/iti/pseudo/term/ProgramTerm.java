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

import nonnull.DeepNonNull;
import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Environment;

/**
 * The Class ProgramTerm encapsulates a a reference to a program term. The
 * actual content of this program depends on the concrete subclass. A
 * {@link LiteralProgramTerm} has a pair of a program counter value and a
 * reference to a program while a {@link SchemaProgramTerm} contains a reference
 * to a schema variable and possibly a matching statement.
 * 
 * The first subterm is always the term suffixed to the program modality.
 * 
 * Program terms can be terminating or non-terminating.
 */
public abstract class ProgramTerm extends Term {
    
    /**
     * The termination flag.
     */
    private boolean terminating;

    /**
     * Instantiates a new program term with a given termination status and an
     * array of subterms.
     * 
     * @param subterms
     *            the future subterms of the array
     * @param terminating
     *            the termination status
     * @throws TermException
     *             if the program terms does not have a boolean suffixed boolean
     *             term.
     */
    protected ProgramTerm(@DeepNonNull Term[] subterms, boolean terminating) throws TermException {
        super(subterms, Environment.getBoolType());
        this.terminating = terminating;
        
        if(subterms.length == 0 ||
                !subterms[0].getType().equals(Environment.getBoolType())) {
            throw new TermException("Program term needs a boolean suffix term");
        }
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
     * Gets the suffix term, the wrapped formula.
     * 
     * It is the first subterm of this term.
     * 
     * @return the wrapped term in the modality. 
     */
    public @NonNull Term getSuffixTerm() {
        return getSubterm(0);
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
        StringBuilder res = new StringBuilder();
        if(isTerminating())
            res.append("[[").append(getContentString(typed)).append("]]");
        else
            res.append("[").append(getContentString(typed)).append("]");
        
        if (typed)
            res.append("(").append(getSubterm(0).toString(true)).append(") as bool");
        else
            res.append(getSubterm(0).toString(false));
        
        return res.toString();
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

    /**
     * This equality check is used in the {@link Object#equals(Object)}
     * implementation of the concrete subclasses of this abstract class.
     * 
     * It returns true if the two program terms coincide on their termination
     * state and the suffix formula.
     * 
     * @param oherProgramTerm
     *            program term to compare with
     *            
     * @return true if the program terms are equal on the properties of
     *         ProgramTerm.
     */
    protected boolean equalsPartially(@NonNull ProgramTerm otherProgramTerm) {
        return terminating == otherProgramTerm.terminating &&
            getSuffixTerm().equals(otherProgramTerm.getSuffixTerm());
    }
}
