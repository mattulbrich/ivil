/*
 * This file is part of PSEUDO
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.parser.term;

import java.util.List;

import nonnull.NonNull;
import nonnull.Nullable;

import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.term.creation.Typing;
import de.uka.iti.pseudo.util.SelectList;

/**
 * The Class ASTTerm is the base class for all elements that make up terms.
 * Every ASTTerm has a {@link Typing} element associated which is used to determine
 * the type of the term during the phase of type inference.
 */
@NonNull
public abstract class ASTTerm extends ASTElement {
    
    /**
     * The typing object which is used to infer types.
     * This is null unless set explicitly
     */
    private Typing typing;

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
    public @Nullable Typing getTyping() {
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
}
