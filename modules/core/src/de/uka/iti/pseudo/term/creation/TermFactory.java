/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term.creation;

import java.math.BigInteger;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Binder;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.TypeVariableBinding;
import de.uka.iti.pseudo.term.Update;
import de.uka.iti.pseudo.term.UpdateTerm;
import de.uka.iti.pseudo.term.Variable;

/**
 * TermFactory is a collection of methods to facilitate the construction of
 * terms.
 *
 * Most methods are wrapper for the corresponding constructor calls.
 */
public class TermFactory {

    private final Environment env;

    /**
     * Instantiates a new term factory for an environment.
     *
     * @param env
     *            the environment to use for construction.
     */
    public TermFactory(Environment env) {
        this.env = env;
    }

    /*
     * Retrieval for function symbols. Throws exception on fail-case.
     */
    private Function getFunction(String name) throws TermException {
        Function f = env.getFunction(name);
        if (f == null) {
            throw new TermException(
                    "The function symbol "
                            + name
                            + " is not available for the term factory. "
                            + "Please ensure that it is present");
        }

        return f;
    }

    /*
     * Retrieval for function symbols. Throws exception on fail-case.
     */
    private Binder getBinder(String name) throws TermException {
        Binder b = env.getBinder(name);
        if (b == null) {
            throw new TermException(
                    "The binder symbol "
                            + name
                            + " is not available for the term factory. "
                            + "Please ensure that it is present");
        }

        return b;
    }

    /**
     * Create a new boolean conjunction.
     *
     * @param t1
     *            the first conjunct
     * @param t2
     *            the second conjunct
     * @return the conjunction {@code $and(t1, t2)}
     * @throws TermException
     *             if the function {@code $and} is not in the environment
     *             or illegally typed arguments
     */
    public @NonNull Term and(@NonNull Term t1, @NonNull Term t2) throws TermException {
        Function f = getFunction("$and");
        return Application.getInst(f, Environment.getBoolType(), new Term[] { t1, t2 });
    }

    /**
     * Create a new boolean disjunction.
     *
     * @param t1
     *            the first disjunct
     * @param t2
     *            the second disjunct
     * @return the disjunction {@code $or(t1, t2)}
     * @throws TermException
     *             if the function {@code $or} is not in the environment
     *             or illegally typed arguments
     */
    public @NonNull Term or(@NonNull Term t1, @NonNull Term t2) throws TermException {
        Function f = getFunction("$or");
        return Application.getInst(f, Environment.getBoolType(), new Term[] { t1, t2 });
    }

    /**
     * Create a new boolean implication.
     *
     * @param t1
     *            the implicant
     * @param t2
     *            the implicate
     * @return the implication {@code $impl(t1, t2)}
     * @throws TermException
     *             if the function {@code $impl} is not in the environment
     *             or illegally typed arguments
     */
    public @NonNull Term impl(@NonNull Term t1, @NonNull Term t2) throws TermException {
        Function f = getFunction("$impl");
        return Application.getInst(f, Environment.getBoolType(), new Term[] { t1, t2 });
    }

    /**
     * Create a new boolean negation.
     *
     * @param t1
     *            the formula to negate
     * @return the negation {@code $not(t1)}
     * @throws TermException
     *             if the function {@code $not} is not in the environment
     *             or illegally typed arguments
     */
    public @NonNull Term not(@NonNull Term t1) throws TermException {
        Function f = getFunction("$not");
        return Application.getInst(f, Environment.getBoolType(), new Term[] { t1 });
    }

    /**
     * Create a new greather-than comparison.
     *
     * @param t1
     *            the left-hand-side operand
     * @param t2
     *            the right-hand-side operand
     * @return the comparison {@code $gt(t1, t2)}
     * @throws TermException
     *             if the function {@code $gt} is not in the environment
     *             or illegally typed arguments
     */
    public @NonNull Term gt(@NonNull Term t1, @NonNull Term t2) throws TermException {
        Function f = getFunction("$gt");
        return Application.getInst(f, Environment.getBoolType(), new Term[] { t1, t2 });
    }

    /**
     * Create a new less-than comparison.
     *
     * @param t1
     *            the left-hand-side operand
     * @param t2
     *            the right-hand-side operand
     * @return the comparison {@code $lt(t1, t2)}
     * @throws TermException
     *             if the function {@code $lt} is not in the environment
     *             or illegally typed arguments
     */
    public @NonNull Term lt(@NonNull Term t1, @NonNull Term t2) throws TermException {
        Function f = getFunction("$lt");
        return Application.getInst(f, Environment.getBoolType(), new Term[] { t1, t2 });
    }

    /**
     * Create a new greather-than-or-equal comparison.
     *
     * @param t1
     *            the left-hand-side operand
     * @param t2
     *            the right-hand-side operand
     * @return the comparison {@code $gte(t1, t2)}
     * @throws TermException
     *             if the function {@code $gte} is not in the environment
     *             or illegally typed arguments
     */
    public @NonNull Term gte(@NonNull Term t1, @NonNull Term t2) throws TermException {
        Function f = getFunction("$gte");
        return Application.getInst(f, Environment.getBoolType(), new Term[] { t1, t2 });
    }

    /**
     * Create a new is-predecessor-of comparison.
     *
     * @param t1
     *            the left-hand-side operand
     * @param t2
     *            the right-hand-side operand
     * @return the comparison {@code $prec(t1, t2)}
     * @throws TermException
     *             if the function {@code $prec} is not in the environment
     *             or illegally typed arguments
     */
    public @NonNull Term prec(@NonNull Term t1, @NonNull Term t2) throws TermException {
        Function f = getFunction("$prec");
        return Application.getInst(f, Environment.getBoolType(), new Term[] { t1, t2 });
    }

    /**
     * Create a new equality.
     *
     * @param t1
     *            the left-hand-side operand
     * @param t2
     *            the right-hand-side operand
     * @return the comparison {@code $eq(t1, t2)}
     * @throws TermException
     *             if the function {@code $eq} is not in the environment
     *             or illegally typed arguments
     */
    public @NonNull Term eq(@NonNull Term t1, @NonNull Term t2) throws TermException {
        Function f = getFunction("$eq");
        return Application.getInst(f, Environment.getBoolType(), new Term[] { t1, t2 });
    }

    /**
     * Create a new constant term.
     *
     * The argument must be a nullary function symbol.
     *
     * @param constantSymbol
     *            the function symbol to create a term from
     * @return the constant as term
     * @throws TermException
     *             if the function is not in the environment or not a unary
     *             function or illegally typed arguments
     */
    public @NonNull Term cons(@NonNull Function constantSymbol) throws TermException {
        return Application.getInst(constantSymbol, constantSymbol.getResultType(), new Term[0]);
    }

    /**
     * Create a new number literal term.
     *
     * @param value
     *            a non-negative integer value
     * @return the constant as term
     * @throws TermException
     *             if value is negative
     */
    public @NonNull Term number(int value) throws TermException {
        return Application.getInst(env.getNumberLiteral(BigInteger.valueOf(value)),
                Environment.getIntType());
    }

    /**
     * Create a new universally quantified formula.
     *
     * @param variable
     *            the bound variable
     * @param term
     *            the matrix of the quantifier
     * @return the quantified formula {@code (\forall variable; term)}
     * @throws TermException
     *             if quantifier not available or illegally typed arguments
     */
    public @NonNull Term forall(@NonNull Variable variable, @NonNull Term term)
            throws TermException {
        Binder b = getBinder("\\forall");
        return Binding.getInst(b, Environment.getBoolType(), variable, new Term[] { term });
    }

    /**
     * Create a new existential quantified formula.
     *
     * @param variable
     *            the bound variable
     * @param term
     *            the matrix of the quantifier
     * @return the quantified formula {@code (\exists variable; term)}
     * @throws TermException
     *             if quantifier not available or illegally typed arguments
     */
    public @NonNull Term exists(@NonNull Variable variable, @NonNull Term term)
            throws TermException {
        Binder b = getBinder("\\exists");
        return Binding.getInst(b, Environment.getBoolType(), variable, new Term[] { term });
    }

    /**
     * Create a new type-universally quantified formula.
     *
     * @param typeVar
     *            the bound type variable
     * @param term
     *            the matrix of the quantifier
     * @return the quantified formula {@code (\T_all variable; term)}
     * @throws TermException
     *             if quantifier not available or illegally typed arguments
     */
    public @NonNull Term typeForall(TypeVariable typeVar, Term term) throws TermException {
        return TypeVariableBinding.getInst(TypeVariableBinding.Kind.ALL, typeVar, term);
    }

    /**
     * Create a new type-existantial quantified formula.
     *
     * @param typeVar
     *            the bound type variable
     * @param term
     *            the matrix of the quantifier
     * @return the quantified formula {@code (\T_ex variable; term)}
     * @throws TermException
     *             if quantifier not available or illegally typed arguments
     */
    public @NonNull Term typeExists(TypeVariable typeVar, Term term) throws TermException {
        return TypeVariableBinding.getInst(TypeVariableBinding.Kind.EX, typeVar, term);
    }

    /**
     * Create a pattern (trigger) term.
     *
     * @param pattern
     *            the term to be used as pattern
     * @param term
     *            the term to apply to
     * @return the term {@code $pattern(pattern, term)}
     * @throws TermException
     *             if function {@code $pattern} not available or illegally typed
     *             arguments
     */
    public @NonNull Term pattern(@NonNull Term pattern, @NonNull Term term) throws TermException {
        Function tr = getFunction("$pattern");
        return Application.getInst(tr, term.getType(), new Term[] { pattern, term });
    }

    /**
     * Create a new updated term.
     *
     * @param update
     *            the update to apply
     * @param term
     *            the to be updated
     * @return the term {@code {update}term}
     */
    public @NonNull Term upd(@NonNull Update update, @NonNull Term term) {
        return UpdateTerm.getInst(update, term);
    }

}
