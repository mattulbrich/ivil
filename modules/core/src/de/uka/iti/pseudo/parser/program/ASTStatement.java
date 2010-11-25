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
package de.uka.iti.pseudo.parser.program;

import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.Token;

public abstract class ASTStatement extends ASTElement {
    
    protected Token firstToken;
    private Token textAnnotation;

    public ASTStatement(Token firstToken) {
        this.firstToken = firstToken;
    }
    
    public Token getLocationToken() {
        return firstToken;
    }

    /**
     * Retrieve the optional annotation for a statement.
     * 
     * @return the set annotation, if set. Null otherwise
     */
    public Token getTextAnnotation() {
        return textAnnotation;
    }

    /**
     * Set the optional text annotation for a statement 
     * @param textAnnotation the annotation to set
     */
    public void setTextAnnotation(Token textAnnotation) {
        this.textAnnotation = textAnnotation;
    }

}
