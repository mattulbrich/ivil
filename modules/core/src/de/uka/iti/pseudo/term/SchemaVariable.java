/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term;

import nonnull.NonNull;
import nonnull.Nullable;

/**
 * The Class SchemaVariable describes a syntactical entity which is used to
 * stand as a placeholder for an other term (possibly containing schema
 * variables itself). The process in which a schema variable is assigned an
 * instantiating term is called matching.
 *
 * <p>
 * The name of schema variables has to begin with a '{@value #SCHEMA_PREFIX}' (
 * {@link #SCHEMA_PREFIX}).
 *
 * <p>
 * Schema variables can be bound in {@link Binding} which is the reason why the
 * class extends {@link BindableIdentifier} rather than {@link Term} directly.
 */
public final class SchemaVariable extends BindableIdentifier {

    /**
     * The prefix with which schema variables' names have to begin.
     */
    public static final String SCHEMA_PREFIX = "%";

    /**
     * The name of the schema variable, including the prefixing '%'.
     */
    private final @NonNull String name;

    /**
     * Instantiates a new schema variable.
     *
     *  <p>
     * The constructor is not visible. Use the {@code getInst} methods to
     * get/create an object of this Class.
     *
     * @param name
     *            the name of the schema variable which has to begin with {@value #SCHEMA_PREFIX}
     * @param type
     *            the type of the schema variable
     *
     * @throws TermException
     *             if the name does not begin with the {@link #SCHEMA_PREFIX}
     */
    private SchemaVariable(@NonNull String name, @NonNull Type type) throws TermException {
        super(type);
        this.name = name;
        if(!name.startsWith(SCHEMA_PREFIX)) {
            throw new TermException("Schema variables need to have a name starting with " +
                    SCHEMA_PREFIX);
        }
    }

    /**
     * Gets an scheme variable term.
     *
     * If a term with the given parameters already exists in the system, a
     * reference to it is returned instead of a freshly created one. If not, a
     * new instance is created.
     *
     * @param name
     *            the name of the schema variable which has to begin with
     *            {@value #SCHEMA_PREFIX}
     * @param type
     *            the type of the schema variable
     *
     * @return a term with the given parameters. Not necessarily freshly
     *         created.
     *
     * @throws TermException
     *             if the name does not begin with the {@link #SCHEMA_PREFIX}
     */

    public static @NonNull SchemaVariable getInst(@NonNull String name, @NonNull Type type)
            throws TermException {
        return (SchemaVariable) new SchemaVariable(name, type).intern();
    }

    /* (non-Javadoc)
     * @see de.uka.iti.pseudo.term.Term#toString(boolean)
     */
    @Override
    public String toString(boolean typed) {
        String retval = name;
        if(typed) {
            retval += " as " + getType();
        }
        return retval;
    }

    /* (non-Javadoc)
     * @see de.uka.iti.pseudo.term.Term#visit(de.uka.iti.pseudo.term.TermVisitor)
     */
    @Override
    public void visit(TermVisitor visitor) throws TermException {
        visitor.visit(this);
    }

    /**
     * Get the name of this schema variable. It begins with an {@value #SCHEMA_PREFIX}
     *
     * @return the name of the schema variable
     */
    @Override
    public String getName() {
        return name;
    }

    /** {@inheritDoc}
     *
     * <p>Two schema variables are equal if they have the same name and the same type.
     */
    // Checkstyle: IGNORE EqualsHashCode
    @Override
    public boolean equals(@Nullable Object object) {
        if (object instanceof SchemaVariable) {
            SchemaVariable sv = (SchemaVariable) object;
            return sv.getName().equals(getName()) && getType().equals(sv.getType());
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

    /*
     * this is for AssignTarget and returns the same as getType()
     */
//    public Type getResultType() {
//        return getType();
//    }

}
