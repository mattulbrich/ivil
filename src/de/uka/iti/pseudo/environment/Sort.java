/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */

package de.uka.iti.pseudo.environment;

import nonnull.NonNull;
import de.uka.iti.pseudo.parser.ASTLocatedElement;

/**
 * A sort in the logic.
 * 
 * It has a name and a number of argument types.
 */
public class Sort {

    /**
     * The name of the sort
     */
    private String name;

    /**
     * The number of arguments to apply to the sort.
     */
    private int arity;

    /**
     * The declaration location of this sort.
     */
    private ASTLocatedElement declaration;

    /**
     * Instantiates a new sort.
     * 
     * @param name
     *            the name of the sort
     * @param arity
     *            the number of types expected as parameters
     * @param declaration
     *            the location of the declaration
     */
    public Sort(@NonNull String name, int arity, 
            @NonNull ASTLocatedElement declaration) {
        super();
        this.name = name;
        this.arity = arity;
        this.declaration = declaration;
    }

    /**
     * Gets the name.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the number of expected type parameters.
     * 
     * @return the arity
     */
    public int getArity() {
        return arity;
    }

    /**
     * Gets the location of the declaration.
     * 
     * @return the declaration
     */
    public ASTLocatedElement getDeclaration() {
        return declaration;
    }
    
    public String toString() {
        return "Sort[" + name + ";" + arity + "]";
    }

}
