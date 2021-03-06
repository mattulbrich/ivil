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

import java.util.List;

import nonnull.DeepNonNull;
import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.term.statement.Assignment;

/**
 * The Class UpdateTerm encapsulates the application of an update (a list of
 * assignments) to a term.
 *
 * Subterms are the updated terms and the update values (in this order). Please
 * note that the update targets are not subterms!
 * <p>The subterms of an update term are:
 * <ol>
 * <li>First the updated term
 * <li>Then all assigned values in order from left to right
 * </ol>
 * The updated <em>targets</em> are <b>not</b> subterms of an update term.
 */
public final class UpdateTerm extends Term {

    private final @NonNull Update update;

    /**
     * Instantiates a new update term with the given update and the updated
     * term.
     *
     * <p>
     * The constructor is not visible. Use the {@code getInst} methods to
     * get/create an object of this Class.
     *
     * @param update
     *            the update to apply
     * @param term
     *            the term to be updated
     */
    private UpdateTerm(@NonNull Update update, @NonNull Term term) {
        super(prepareSubterms(term, update), term.getType());
        this.update = update;
    }

    /**
     * Gets an updated term from the given update and the updated term.
     *
     * If a term with the given parameters already exists in the system, a
     * reference to it is returned instead of a freshly created one. If not, a
     * new instance is created.
     *
     * @param update
     *            the update to apply
     * @param term
     *            the term to be updated
     *
     * @return a term with the given parameters. Not necessarily freshly
     *         created.
     */
    public static @NonNull UpdateTerm getInst(@NonNull Update update, @NonNull Term term) {
        return (UpdateTerm) new UpdateTerm(update, term).intern();
    }

    /*
     * prepare the subterms for the super class constructor.
     * First the updated term then all update values in order.
     */
    private static Term[] prepareSubterms(Term term, Update update) {
        List<Assignment> assignments = update.getAssignments();
        Term[] result = new Term[assignments.size() + 1];

        result[0] = term;
        for (int i = 1; i < result.length; i++) {
            result[i] = assignments.get(i-1).getValue();
        }
        return result;
    }

    /*
     * equal to another update term if they have equal assignment sets
     * and equal subterms
     * Checkstyle: IGNORE EqualsHashCode - defined in Term.java
     */
    @Override
    public boolean equals(@Nullable Object object) {
        if (object instanceof UpdateTerm) {
            UpdateTerm ut = (UpdateTerm) object;
            return update.equals(ut.update)
                    && getSubterm(0).equals(ut.getSubterm(0));
        }
        return false;
    }

    /*
     * This implementation incorporates the update into the calculation.
     */
    @Override
    protected int calculateHashCode() {
        // Checkstyle: IGNORE MagicNumber
        return super.calculateHashCode() * 31 + update.hashCode();
    }


    /*
     * we do not print our own typing. the typing of
     * the inner term suffices.
     */
    @Override
    public String toString(boolean typed) {
        StringBuilder sb = new StringBuilder();

        sb.append(update.toString(typed));

        if (typed) {
            sb.append("(").append(getSubterm(0).toString(true)).append(")");
        } else {
            sb.append(getSubterm(0).toString(false));
        }

        return sb.toString();
    }

    @Override
    public void visit(TermVisitor visitor) throws TermException {
        visitor.visit(this);
    }

    /**
     * Gets the assignments of the update of this update term.
     *
     * the call is delegated to the update
     *
     * @return an immutable list of assignments
     */
    public @DeepNonNull List<Assignment> getAssignments() {
        return update.getAssignments();
    }

    /**
     * Gets the immutable update object for this updated term.
     *
     * @return the update object
     */
    public @NonNull Update getUpdate() {
        return update;
    }

}
