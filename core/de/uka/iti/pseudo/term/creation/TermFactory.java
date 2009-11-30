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
        return new Application(f, Environment.getBoolType(), new Term[] { t1, t2 });
    }
    
    public @NonNull Term or(@NonNull Term t1, @NonNull Term t2) throws TermException {
        Function f = env.getFunction("$or");
        return new Application(f, Environment.getBoolType(), new Term[] { t1, t2 });
    }
    
    public @NonNull Term not(@NonNull Term t1) throws TermException {
        Function f = env.getFunction("$not");
        return new Application(f, Environment.getBoolType(), new Term[] { t1 });
    }

    public @NonNull Term number(int i) throws TermException {
        return new Application(env.getNumberLiteral(BigInteger.valueOf(i)), Environment.getIntType());
    }

    public @NonNull Term gt(@NonNull Term t1, @NonNull Term t2) throws TermException {
        Function f = env.getFunction("$gt");
        return new Application(f, Environment.getBoolType(), new Term[] { t1, t2 });
    }
    
    public @NonNull Term gte(@NonNull Term t1, @NonNull Term t2) throws TermException {
        Function f = env.getFunction("$gte");
        return new Application(f, Environment.getBoolType(), new Term[] { t1, t2 });
    }
    
    public @NonNull Term lt(@NonNull Term t1, @NonNull Term t2) throws TermException {
        Function f = env.getFunction("$lt");
        return new Application(f, Environment.getBoolType(), new Term[] { t1, t2 });
    }

    public @NonNull Term cons(Function constantSymbol) throws TermException {
        return new Application(constantSymbol, constantSymbol.getResultType(), new Term[0]);
    }

    public @NonNull Term eq(@NonNull Term t1, @NonNull Term t2) throws TermException {
        Function f = env.getFunction("$eq");
        return new Application(f, Environment.getBoolType(), new Term[] { t1, t2 });
    }

    public @NonNull Term impl(@NonNull Term t1, @NonNull Term t2) throws TermException {
        Function f = env.getFunction("$impl");
        return new Application(f, Environment.getBoolType(), new Term[] { t1, t2 });
    }

    public @NonNull Term forall(@NonNull Variable variable, @NonNull Term term) throws TermException {
        Binder b = env.getBinder("\\forall");
        return new Binding(b, Environment.getBoolType(), variable, new Term[] { term });
    }

}
