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


// TODO throw term exception if f == null instead of NPE
/**
 * TermFactory is a collection of static methods to faciliate the
 * construction of terms. They are merely convenience methods
 */
public class TermFactory {

    private Environment env;

    public TermFactory(Environment env) {
        this.env = env;
    }

    public @NonNull Term and(@NonNull Term t1, @NonNull Term t2) throws TermException {
        Function f = env.getFunction("$and");
        return Application.getInst(f, Environment.getBoolType(), new Term[] { t1, t2 });
    }

    public @NonNull Term or(@NonNull Term t1, @NonNull Term t2) throws TermException {
        Function f = env.getFunction("$or");
        return Application.getInst(f, Environment.getBoolType(), new Term[] { t1, t2 });
    }

    public @NonNull Term not(@NonNull Term t1) throws TermException {
        Function f = env.getFunction("$not");
        return Application.getInst(f, Environment.getBoolType(), new Term[] { t1 });
    }

    public @NonNull Term number(int i) throws TermException {
        return Application.getInst(env.getNumberLiteral(BigInteger.valueOf(i)), Environment.getIntType());
    }

    public @NonNull Term gt(@NonNull Term t1, @NonNull Term t2) throws TermException {
        Function f = env.getFunction("$gt");
        return Application.getInst(f, Environment.getBoolType(), new Term[] { t1, t2 });
    }

    public @NonNull Term gte(@NonNull Term t1, @NonNull Term t2) throws TermException {
        Function f = env.getFunction("$gte");
        return Application.getInst(f, Environment.getBoolType(), new Term[] { t1, t2 });
    }

    public @NonNull Term lt(@NonNull Term t1, @NonNull Term t2) throws TermException {
        Function f = env.getFunction("$lt");
        return Application.getInst(f, Environment.getBoolType(), new Term[] { t1, t2 });
    }

    public @NonNull Term cons(Function constantSymbol) throws TermException {
        return Application.getInst(constantSymbol, constantSymbol.getResultType(), new Term[0]);
    }

    public @NonNull Term eq(@NonNull Term t1, @NonNull Term t2) throws TermException {
        Function f = env.getFunction("$eq");
        return Application.getInst(f, Environment.getBoolType(), new Term[] { t1, t2 });
    }

    public @NonNull Term impl(@NonNull Term t1, @NonNull Term t2) throws TermException {
        Function f = env.getFunction("$impl");
        return Application.getInst(f, Environment.getBoolType(), new Term[] { t1, t2 });
    }

    public @NonNull Term forall(@NonNull Variable variable, @NonNull Term term) throws TermException {
        Binder b = env.getBinder("\\forall");
        return Binding.getInst(b, Environment.getBoolType(), variable, new Term[] { term });
    }

    public @NonNull Term typeForall(TypeVariable typeVar, Term term) throws TermException {
        return TypeVariableBinding.getInst(TypeVariableBinding.Kind.ALL, typeVar, term);
    }

    public @NonNull Term typeExists(TypeVariable typeVar, Term term) throws TermException {
        return TypeVariableBinding.getInst(TypeVariableBinding.Kind.EX, typeVar, term);
    }

}
