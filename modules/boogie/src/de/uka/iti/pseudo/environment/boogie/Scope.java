package de.uka.iti.pseudo.environment.boogie;

import de.uka.iti.pseudo.parser.boogie.ast.NamedASTElement;

/**
 * Several boogie constructs create nested scopes. A scope has a parent and a
 * creating node or its the global scope.
 * 
 * @author timm.felden@felden.com
 * 
 */
public class Scope {

    public final Scope parent;
    public final NamedASTElement creator;

    Scope(Scope parent, NamedASTElement creator) {
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
