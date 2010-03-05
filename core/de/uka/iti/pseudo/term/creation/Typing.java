/*
 * This file is part of PSEUDO
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term.creation;

import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;
import nonnull.NonNull;

/**
 * The Class Typing is used during the process of type inference.
 * 
 * <p>An instance is assigned to every {@link Term} object during the parsing
 * process. Every instance holds a raw type and a reference to a {@link TypingContext}
 * which allows to make the raw type concrete if needed using the mthod
 * {@link #getType()}.
 * 
 */
@NonNull
public class Typing {

    /**
     * The raw type holds the originally set type
     */
    private Type rawType;

    /**
     * The typing context holds the information to instantiate
     * the {@link #rawType} to produce the current type.
     */
    private TypingContext typingContext;

    /**
     * Instantiates a new typing.
     * 
     * @param rawType the raw type
     * @param instantiation the instantiation
     */
    public Typing(@NonNull Type rawType, @NonNull TypingContext instantiation) {
        super();
        this.rawType = rawType;
        this.typingContext = instantiation;
    }

    /**
     * Gets the raw type. without instantiations.
     * 
     * @return the raw type
     */
    public Type getRawType() {
        return rawType;
    }

    /**
     * Gets the instantiated type. Instantiate the rawtype using the
     * stored instantiation object
     * 
     * @return the type
     */
    public Type getType() {
        return typingContext.instantiate(rawType);
    }
    
    /**
     * the string representation is the raw type
     * 
     * @return rawtype as string
     */
    @Override
    public String toString() {
        return rawType.toString();
    }
}
