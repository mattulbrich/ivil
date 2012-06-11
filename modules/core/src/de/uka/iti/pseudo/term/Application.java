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

import nonnull.DeepNonNull;
import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.term.creation.TermMatcher;
import de.uka.iti.pseudo.term.creation.TypeMatchVisitor;
import de.uka.iti.pseudo.term.creation.TypeUnification;
import de.uka.iti.pseudo.util.Util;

/**
 * The Class Application encapsulates a term with a toplevel function symbol.
 * There may be argument terms but it may also be a constant w/o arguments.
 *
 * The arguments are stored as subterms in the superclass.
 */
public final class Application extends Term {

    /**
     * The function which is this term's top level symbol.
     */
    private final @NonNull Function function;

    /**
     * Create a new application term.
     *
     * <p>
     * The constructor is not visible. Use the {@code getInst} methods to
     * get/create an object of this Class.
     *
     * @param funct
     *            the function symbol to use
     * @param type
     *            the type to set for this term
     * @param subterms
     *            the arguments to the symbol
     *
     * @throws TermException
     *             if the type check fails.
     */
    private Application(@NonNull Function funct, @NonNull Type type, @DeepNonNull Term[] subterms)
            throws TermException {
        super(subterms, type);
        this.function = funct;
        typeCheck();
    }

    /**
     * Gets an application term.
     *
     * If a term with the given parameters already exists in the system, a
     * reference to it is returned instead of a freshly created one. If not, a
     * new instance is created.
     *
     * @param funct
     *            the function symbol to use
     * @param type
     *            the type to set for this term
     * @param subterms
     *            the arguments to the symbol
     * @return a term with the given parameters. Not necessarily freshly
     *         created.
     * @throws TermException
     *             if the type check fails.
     */
    public static @NonNull Application getInst(@NonNull Function funct, @NonNull Type type,
            @DeepNonNull Term[] subterms) throws TermException {
        return (Application) new Application(funct, type, subterms).intern();
    }

    /**
     * Gets an application term for a constant w/o parameters.
     *
     * If a term with the given parameters already exists in the system, it is
     * returned instead of freshly created one.
     *
     * @param funct
     *            the function symbol to use
     * @param type
     *            the type to set for this term
     * @return a term with the given parameters. Not necessarily freshly
     *         created.
     * @throws TermException
     *             if the type check fails.
     */
    public static Application getInst(@NonNull Function funct, @NonNull Type type)
            throws TermException {
        return getInst(funct, type, NO_ARGUMENTS);
    }

    /**
     * retrieve the top level symbol of this application.
     *
     * @return the top level function symbol
     */
    public @NonNull Function getFunction() {
        return function;
    }

    /**
     * Type check this application term. This includes:
     * <ol>
     * <li>check arity to match symbol definition
     * <li>check that given the argument terms, the term can be typed
     * <li>and that the result type is compatible with the typing result
     * </ol>
     *
     * @throws TermException
     *             the term exception
     */
    private void typeCheck() throws TermException {

        if (countSubterms() != function.getArity()) {
            throw new TermException("Function " + function + " expects "
                    + function.getArity() + " arguments, but got:\n"
                    + Util.listTerms(getSubterms()));
        }

        TypeMatchVisitor matcher = new TypeMatchVisitor(new TermMatcher());
        Type[] argumentTypes = function.getArgumentTypes();

        try {
            for (int i = 0; i < countSubterms(); i++) {
                TypeUnification.makeSchemaVariant(argumentTypes[i]).
                    accept(matcher, getSubterm(i).getType());
            }

            TypeUnification.makeSchemaVariant(function.getResultType()).accept(matcher, getType());
        } catch (UnificationException e) {
            throw new TermException("Term " + toString()
                    + " cannot be typed.\nFunction symbol: " + function
                    + "\nResult type: " + getType()
                    + "\nTypes of subterms:\n" + Util.listTypes(getSubterms()), e);
        }

    }

    @Override
    public String toString(boolean typed) {
        StringBuilder retval = new StringBuilder();
        retval.append(function.getName());
        if (countSubterms() > 0) {
            retval.append("(");
            for (int i = 0; i < countSubterms(); i++) {
                if(i > 0) {
                    retval.append(",");
                }
                retval.append(getSubterm(i).toString(typed));
            }
            retval.append(")");
        }
        if (typed) {
            retval.append(" as ").append(getType());
        }

        return retval.toString();
    }

    @Override
    public void visit(TermVisitor visitor) throws TermException {
        visitor.visit(this);
    }

    /*
     * This term is equal to another term if it is a Application
     * and has the same function symbol and same arguments.
     *
     * Checkstyle: IGNORE EqualsHashCode - defined in Term.java
     */
    @Override
    public boolean equals(@Nullable Object object) {
        if (object instanceof Application) {
            Application app = (Application) object;
            if(app.getFunction() != getFunction()) {
                return false;
            }

            if(!app.getType().equals(getType())) {
                return false;
            }

            assert app.countSubterms() == countSubterms();

            for (int i = 0; i < countSubterms(); i++) {
                if(!app.getSubterm(i).equals(getSubterm(i))) {
                    return false;
                }
            }

            return true;
        }
        return false;
    }

    /*
     * This implementation incorporates the function symbol into the calculation.
     */
    @Override
    protected int calculateHashCode() {
        return super.calculateHashCode() * 31 + getFunction().hashCode();
    }
}
