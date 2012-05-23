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
import de.uka.iti.pseudo.term.Variable;

/**
 * TermFactory is a collection of static methods to faciliate the
 * construction of terms. They are merely convenience methods
 */
public class TermFactory {

    private final Environment env;

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

    public @NonNull Term and(@NonNull Term t1, @NonNull Term t2) throws TermException {
        Function f = getFunction("$and");
        return Application.getInst(f, Environment.getBoolType(), new Term[] { t1, t2 });
    }

    public @NonNull Term or(@NonNull Term t1, @NonNull Term t2) throws TermException {
        Function f = getFunction("$or");
        return Application.getInst(f, Environment.getBoolType(), new Term[] { t1, t2 });
    }

    public @NonNull Term not(@NonNull Term t1) throws TermException {
        Function f = getFunction("$not");
        return Application.getInst(f, Environment.getBoolType(), new Term[] { t1 });
    }

    public @NonNull Term number(int i) throws TermException {
        return Application.getInst(env.getNumberLiteral(BigInteger.valueOf(i)), Environment.getIntType());
    }

    public Term prec(@NonNull Term t1, @NonNull Term t2) throws TermException {
        Function f = getFunction("$prec");
        return Application.getInst(f, Environment.getBoolType(), new Term[] { t1, t2 });
    }

    public @NonNull Term gt(@NonNull Term t1, @NonNull Term t2) throws TermException {
        Function f = getFunction("$gt");
        return Application.getInst(f, Environment.getBoolType(), new Term[] { t1, t2 });
    }

    public @NonNull Term gte(@NonNull Term t1, @NonNull Term t2) throws TermException {
        Function f = getFunction("$gte");
        return Application.getInst(f, Environment.getBoolType(), new Term[] { t1, t2 });
    }

    public @NonNull Term lt(@NonNull Term t1, @NonNull Term t2) throws TermException {
        Function f = getFunction("$lt");
        return Application.getInst(f, Environment.getBoolType(), new Term[] { t1, t2 });
    }

    public @NonNull Term cons(Function constantSymbol) throws TermException {
        return Application.getInst(constantSymbol, constantSymbol.getResultType(), new Term[0]);
    }

    public @NonNull Term eq(@NonNull Term t1, @NonNull Term t2) throws TermException {
        Function f = getFunction("$eq");
        return Application.getInst(f, Environment.getBoolType(), new Term[] { t1, t2 });
    }

    public @NonNull Term impl(@NonNull Term t1, @NonNull Term t2) throws TermException {
        Function f = getFunction("$impl");
        return Application.getInst(f, Environment.getBoolType(), new Term[] { t1, t2 });
    }

    public @NonNull Term forall(@NonNull Variable variable, @NonNull Term term) throws TermException {
        Binder b = getBinder("\\forall");
        return Binding.getInst(b, Environment.getBoolType(), variable, new Term[] { term });
    }

    public @NonNull Term typeForall(TypeVariable typeVar, Term term) throws TermException {
        return TypeVariableBinding.getInst(TypeVariableBinding.Kind.ALL, typeVar, term);
    }

    public @NonNull Term typeExists(TypeVariable typeVar, Term term) throws TermException {
        return TypeVariableBinding.getInst(TypeVariableBinding.Kind.EX, typeVar, term);
    }

    public @NonNull Term pattern(Term pattern, Term term) throws TermException {
        Function tr = getFunction("$pattern");
        return Application.getInst(tr, term.getType(), new Term[] { pattern, term });
    }

}
