package de.uka.iti.pseudo.parser.boogie.ast;

import de.uka.iti.pseudo.parser.boogie.Token;

/**
 * Named Types are treated different to MapTypes, as maptypes dont need to be
 * defined somewhere.
 * 
 * @author timm.felden@felden.com
 * 
 */
public abstract class NamedType extends Type {

    /**
     * Name of this type
     */
    protected final Token name;

    /**
     * The number of template arguments, this type takes.
     */
    protected final int arity;

    public NamedType(Token name, int arity) {
        this.name = name;
        this.arity = arity;
    }

    @Override
    public Token getLocationToken() {
        return name;
    }

    public int getArity() {
        return arity;
    }
}
