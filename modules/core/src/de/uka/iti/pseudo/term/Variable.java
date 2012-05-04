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
package de.uka.iti.pseudo.term;

import nonnull.NonNull;
import nonnull.Nullable;

/**
 * The Class Variable captures a bound variable in a term
 */
public final class Variable extends BindableIdentifier {

    /**
     * The name of the bound variable
     */
    private String name;

    /**
     * Instantiates a new variable.
     * 
     * <p>
     * The constructor is not visible. Use the {@code getInst} methods to
     * get/create an object of this Class.
     * 
     * @param name
     *            the name of the variable
     * @param type
     *            the type of the variable
     */
    private Variable(String name, Type type) {
        super(type);
        this.name = name;
    }

    /**
     * Gets a variable term.
     * 
     * If a term with the given parameters already exists in the system, a
     * reference to it is returned instead of a freshly created one. If not, a
     * new instance is created.
     * 
     * @param name
     *            the name of the variable
     * @param type
     *            the type of the variable
     * @return a term with the given parameters. Not necessarily freshly
     *         created.
     */
    public static @NonNull Variable getInst(@NonNull String name, @NonNull Type type) {
        return (Variable) new Variable(name, type).intern();
    }

    @Override
    public String toString(boolean typed) {
        String retval = "\\var " + name;
        if (typed)
            retval += " as " + getType();
        return retval;
    }

    @Override
    public void visit(TermVisitor visitor) throws TermException {
        visitor.visit(this);
    }

    /**
     * Gets the name of the variable
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (object instanceof Variable) {
            Variable v = (Variable) object;
            return v.getName().equals(getName())
                    && getType().equals(v.getType());
        }
        return false;
    }
    
    /*
     * This implementation takes the hash code from the identifier
     */
    @Override
    protected int calculateHashCode() {
        return name.hashCode();
    }
}
