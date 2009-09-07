package de.uka.iti.pseudo.term.creation;

import java.math.BigInteger;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;


/**
 * TermFactory is a collection of static methods to faciliate the
 * construction of terms. They are merely convenience methods 
 */
public class TermFactory {
    
    private Environment env;

    public TermFactory(Environment env) {
        this.env = env;
    }

    public Term and(Term t1, Term t2) throws TermException {
        Function f = env.getFunction("$and");
        return new Application(f, Environment.getBoolType(), new Term[] { t1, t2 });
    }
    
    public Term or(Term t1, Term t2) throws TermException {
        Function f = env.getFunction("$or");
        return new Application(f, Environment.getBoolType(), new Term[] { t1, t2 });
    }

    public Term number(int i) throws TermException {
        return new Application(env.getNumberLiteral(BigInteger.valueOf(i)), Environment.getIntType());
    }

    public Term gt(Term t1, Term t2) throws TermException {
        Function f = env.getFunction("$gt");
        return new Application(f, Environment.getBoolType(), new Term[] { t1, t2 });
    }

    public Term cons(Function constantSymbol) throws TermException {
        return new Application(constantSymbol, constantSymbol.getResultType(), new Term[0]);
    }

    public Term eq(Term t1, Term t2) throws TermException {
        Function f = env.getFunction("$eq");
        return new Application(f, Environment.getBoolType(), new Term[] { t1, t2 });
    }

    public Term impl(Term t1, Term t2) throws TermException {
        Function f = env.getFunction("$impl");
        return new Application(f, Environment.getBoolType(), new Term[] { t1, t2 });
    }

}
