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
package de.uka.iti.pseudo.parser;

/**
 * This interfaces provides the posibilty to obtain a location description for
 * an object of an implmenting clas.
 * 
 * A location is a description of the location of the element within the
 * resources, usually this is a descriptor containing filename, line and/or
 * column number.
 */
public interface ASTLocatedElement {

    /**
     * The BUILTIN Location is used when no resource information is available
     * but the element is always available. For instance the boolean and integer
     * sorts, true and false, ...
     * 
     * Its location description is <code>"#builtin"</code>.
     */
    ASTLocatedElement BUILTIN = new ASTLocatedElement() {
        public String getLocation() {
            return "#builtin";
        }
    };

    /**
     * The BUILTIN Location is used when no resource information is available
     * and the element has been created during the run of the system. For
     * instance skolem symbols.
     * 
     * Its location description is "#created".
     */
    ASTLocatedElement CREATED = new ASTLocatedElement() {
        public String getLocation() {
            return "#created";
        }
    };

    /**
     * Gets a string that describes the location at which this element stood in
     * the sources.
     * 
     * @return the location description
     */
    public String getLocation();
    
}
