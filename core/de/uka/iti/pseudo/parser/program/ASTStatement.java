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
