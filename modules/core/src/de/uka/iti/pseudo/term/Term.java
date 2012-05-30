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

import java.util.Arrays;
import java.util.List;

import nonnull.DeepNonNull;
import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.util.ObjectCachePool;
import de.uka.iti.pseudo.util.Util;

/**
 * The Class Term is the base class for the term data structure.
 * 
 * It stores the subterms of the term along with its type and provides means for
 * visitation by a {@link TermVisitor}.
 */
@NonNull
public abstract class Term {
    
    /**
     * A constant pool is used to ensure that any term is created only once,
     * hence reusing objects in memory.
     */
    private static final ObjectCachePool termPool = new ObjectCachePool();

    /**
     * The Constant NO_ARGUMENTS is used instead of null when a term has no
     * subterms
     */
    protected static final Term[] NO_ARGUMENTS = new Term[0];

    /**
     * The Constant SHOW_TYPES is read from the system environment and indicates
     * whether terms are to be output with or without types.
     */
    public static boolean SHOW_TYPES = Boolean.getBoolean("pseudo.showtypes");

    /**
     * The array of sub terms
     */
    private Term[] subterms;

    /**
     * The type of this term.
     */
    private Type type;

    /**
     * The hashcode is stored in here once it has been calculated.
     */
    private int storedHashCode = 0;

    /**
     * Instantiates a new term with subterms
     * 
     * @param subterms
     *            the subterms
     * @param type
     *            the type of the term
     */
    protected Term(@DeepNonNull Term[] subterms, @NonNull Type type) {

        assert subterms != null;
        assert type != null;

        this.subterms = subterms;
        this.type = type;
    }

    /**
     * Instantiates a new term without subterms
     * 
     * @param type
     *            the type of the term
     */
    protected Term(@NonNull Type type) {
        this(NO_ARGUMENTS, type);
    }

    /**
     * Gets the type of the term
     * 
     * @return type of this term
     */
    public @NonNull Type getType() {
        return type;
    }

    /**
     * Count the subterms of term
     * 
     * @return a non-negative number
     */
    public int countSubterms() {
        return subterms.length;
    }

    /**
     * Gets a particular subterm.
     * 
     * @param idx
     *            the index of the subterm
     * 
     * @return the subterm
     * 
     * @throws IndexOutOfBoundsException
     *             if <code>idx < 0 </code> or
     *             <code>idx &gt;= countSubterms()</code>
     * @see #countSubterms()
     */
    public @NonNull Term getSubterm(int idx) {
        return subterms[idx];
    }

    /**
     * Gets the subterms as an unmodifiable list.
     * 
     * @return the subterms as list
     */
    public @DeepNonNull List<Term> getSubterms() {
        return Util.readOnlyArrayList(subterms);
    }

    /**
     * Count all direct and indirect subterms. Includes all subterms and
     * subterms of subterms, ...
     * 
     * Return the number of terms including myself.
     * 
     * @return a number > 0
     */
    public int countAllSubterms() {
        int sum = 1;
        for (int i = 0; i < subterms.length; i++) {
            sum += subterms[i].countAllSubterms();
        }
        return sum;
    }

    /**
     * depending on {@link #SHOW_TYPES} print the term with or without typing
     * information
     * 
     * @return string for this term
     */
    @Override
    final public String toString() {
        return toString(SHOW_TYPES);
    }

    /**
     * Depending on the argument give a string representation of this term with
     * or without typing information. If typing is switched on, every subterm
     * will be annotated as in <code>"t as T"</code>.
     * 
     * <p>
     * The resulting term should be parsable again by the term parser resulting
     * in the same term.
     * 
     * @param typed
     *            should the result contain typing information
     * 
     * @return the string for this term, with typing information iff
     *         <code>typed</code> is <code>true</code>.
     */
    public abstract String toString(boolean typed);

    /**
     * {@inheritDoc}
     * 
     * The hash code of a term is calculated using {@link #calculateHashCode()}.
     * Its result is cached in a field so that the calculation does not need to
     * happen a second time
     * 
     * @see #calculateHashCode();
     * 
     * @return the hash code for this
     */
    @Override
    public final int hashCode() {
        if (storedHashCode == 0) {
            storedHashCode = calculateHashCode();
        }
        return storedHashCode;
    }

    /**
     * Calculate the hash code of this term object.
     * 
     * This implementation takes the hashcode of the type and of the subterms
     * and stirs them.
     * 
     * Subclasses can do a different implementation or add more information.
     * 
     * @see Term#hashCode();
     * 
     * @return the hash code of this object.
     */
    protected int calculateHashCode() {
        int result = type.hashCode() + 31 * Arrays.hashCode(subterms) ;
        return result;
    }

    /**
     * Gets the canonical representative for all terms which are equal to this.
     * 
     * @return a term which is {@linkplain Object#equals(Object) equal} to this
     *         term object and of exactly the same class.
     */
    protected final @NonNull Term intern() {
        Term result = termPool.cacheNonNull(this);
        return result;
    }

    /**
     * The equality on terms is the syntactical identity.
     * 
     * Two terms are equal if and only if they are structually the same
     * including their types, that is:<br>
     * 
     * <code>nil as List(int)</code> is not equal to <code>nil as List('a)</code> even if
     * 'a might be instantiated to int.
     * 
     * @param object
     *            an arbitrary object
     * 
     * @return true iff object is a term and structually equal to this term.
     */
    @Override
    public abstract boolean equals(@Nullable Object object);

    /**
     * This is the "accept" method of the visitor pattern.
     * 
     * @param visitor
     *            the visitor to accept
     * 
     * @throws TermException
     *             may be thrown by the visit method of the visitor.
     */
    public abstract void visit(TermVisitor visitor) throws TermException;

}
