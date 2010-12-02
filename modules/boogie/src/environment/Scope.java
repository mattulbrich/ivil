package de.uka.iti.pseudo.parser.boogie.environment;

import de.uka.iti.pseudo.parser.boogie.ASTElement;

/**
 * Several boogie constructs create nested scopes. A scope has a parent and a
 * creating node or its the global scope.
 * 
 * @author timm.felden@felden.com
 * 
 */
public class Scope {

    public final Scope parent;
    public final ASTElement creator;

    Scope(Scope parent, ASTElement creator) {
        this.parent = parent;
        this.creator = creator;
    }

    @Override
    public String toString() {
        if(null!=parent)
            return creator.toString() + " -> " + parent.toString();
        else
            return "<global>";
    }
}
