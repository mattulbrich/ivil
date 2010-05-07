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
package de.uka.iti.pseudo.rule.meta;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.MetaFunction;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.creation.TermFactory;

/**
 * resolve an equality of two unique expressions into a simpler expressions.
 * 
 * Two function application such that for both the function symbol is unique are
 * compare with "=". Since they are unique the result is true iff the function
 * symbol is the same and all arguments are pairwise equal (not necessarily identical).
 */
public class ResolveUniqueMetaFunction extends MetaFunction {
    
    public ResolveUniqueMetaFunction() {
        super(Environment.getBoolType(), "$$resolveUnique", TypeVariable.ALPHA, TypeVariable.BETA);
    }

    @Override
    public Term evaluate(Application application, Environment env,
            RuleApplication ruleApp) throws TermException {
        
        //
        // prepare t1
        Term t1 = application.getSubterm(0);
        if(!(t1 instanceof Application)) {
            throw new TermException("Can only resolve applications, but not " + t1);
        }
        Function f1 = ((Application)t1).getFunction();
        if(!f1.isUnique())
            throw new TermException("Can only resolve unique function applications, but " + f1 + " is not unique");
        
        //
        // prepare t2        
        Term t2 = application.getSubterm(1);
        if(!(t2 instanceof Application)) {
            throw new TermException("Can only resolve applications, but not " + t2);
        }
        Function f2 = ((Application)t2).getFunction();
        if(!f2.isUnique())
            throw new TermException("Can only resolve unique function applications, but " + f2 + " is not unique");

        //
        // check for equality
        if(f1 == f2) {
            
            int len = f1.getArity();
            
            // no arguments --> true
            if(len == 0)
                return Environment.getTrue();
            
            // pairwise equal arguments
            return haveEqualArguments(env, t1, t2, len);
            
        } else {
            // different unique symbols --> false
            return Environment.getFalse();
        }
    }

    /*
     * construct a term which is true iff the arguments two applications t1 and t2
     * are pairwise equal, i.e.
     * 
     *  f(a,b) and g(c,d) yield
     *  
     * a = c & b = d
     */
    private Term haveEqualArguments(Environment env, Term t1, Term t2, int len)
            throws TermException {
        
        assert len > 0;
        
        Term result = null;
        TermFactory tf = new TermFactory(env);
        
        for (int i = 0; i < len; i++) {
            Term eq = tf.eq(t1.getSubterm(i), t2.getSubterm(i));
            if(result == null)
                result = eq;
            else
                result = tf.and(result, eq);
        }
        return result;
    }

}
