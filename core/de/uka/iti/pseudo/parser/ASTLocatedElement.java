/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.parser;

/**
 * This interfaces provides the posibilty to obtain a location description for an object of an implmenting clas.
 * 
 * A location is a description of the location of the element within the resources, usually this is a descriptor
 * containing filename, line and/or column number.
 */
public interface ASTLocatedElement {
    
    /**
     * The BUILTIN Location is used when no resource information is available (for instance for generated 
     * elements), its location description is "#builtin".
     */
    ASTLocatedElement BUILTIN = new ASTLocatedElement() {
		public String getLocation() { return "#builtin"; }   	
    };

    /**
     * Gets a string that describes the location at which this element
     * stood in the sources.
	 * 
	 * @return the location description
	 */
	public String getLocation();
    
}
