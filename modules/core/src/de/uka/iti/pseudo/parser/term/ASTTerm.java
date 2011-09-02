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

import java.util.List;

import checkers.nullness.quals.LazyNonNull;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.term.creation.Typing;
import de.uka.iti.pseudo.util.SelectList;

/**
 * The Class ASTTerm is the base class for all elements that make up terms.
 * Every ASTTerm has a {@link Typing} element associated which is used to determine
 * the type of the term during the phase of type inference.
 */

public abstract class ASTTerm extends ASTElement {
    
    /**
     * The typing object which is used to infer types.
     * This is null unless set explicitly
     */
    private @LazyNonNull Typing typing = null;

    /**
     * Instantiates a new AST term, the provided subterms are set
     * as sub elements.
     * 
     * @param subterms the subterms
     */
    public ASTTerm(List<ASTTerm> subterms) {
        addChildren(subterms);
    }

    /**
     * retrieve the subterm elements of this term element.
     * The list is provided as a filter result of the list of child elements
     *  
     * @return a list of subterms
     */
    public List<ASTTerm> getSubterms() {
        return SelectList.select(ASTTerm.class, getChildren());
    }

    /**
     * Gets the typing object
     * 
     * @return the typing
     */
    public @NonNull Typing getTyping() throws ASTVisitException {
        if(typing == null)
            throw new ASTVisitException("Retrieving non existing typing", this);
        return typing;
    }

    /**
     * Sets the typing object. This may be called at most once.
     * 
     * @param typing the new typing
     */
    public void setTyping(@NonNull Typing typing) {
        assert this.typing == null;
        this.typing = typing;
    }	
    
    /**
     * Terms always do have a location token, hence the NonNull return type.
     * 
     * @return the location token
     */
    public abstract @NonNull Token getLocationToken();
}
