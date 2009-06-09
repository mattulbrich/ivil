/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.parser;

public interface ASTLocatedElement {
    
    ASTLocatedElement BUILTIN = new ASTLocatedElement() {
		public String getLocation() { return "#builtin"; }   	
    };

	public String getLocation();
    
}
