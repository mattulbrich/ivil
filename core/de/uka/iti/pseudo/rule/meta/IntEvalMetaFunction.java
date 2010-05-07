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

import java.math.BigInteger;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.MetaFunction;
import de.uka.iti.pseudo.environment.NumberLiteral;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.TypeVariable;

// TODO Documentation needed
public class IntEvalMetaFunction extends MetaFunction {
    
    public IntEvalMetaFunction() {
        super(TypeVariable.ALPHA, "$$intEval", TypeVariable.ALPHA);
    }

    @Override
    public Term evaluate(Application application, Environment env,
            RuleApplication ruleApp) throws TermException {
        
        Term subterm = application.getSubterm(0);
        if(!(subterm instanceof Application)) {
            throw new TermException("Can only resolve applications, but not " + subterm);
        }
        
        Function f = ((Application)subterm).getFunction();
        
        if(f.getArity() != 2) 
            throw new TermException("Expecting arity 2");
        
        BigInteger val1 = makeNumber(subterm.getSubterm(0));
        BigInteger val2 = makeNumber(subterm.getSubterm(1));
        String fctname = f.getName();
        
        if(fctname.equals("$gt")) {
            BigInteger diff = val1.subtract(val2);
            return makeBool(diff.signum() == 1, env);
        }
        
        if(fctname.equals("$gte")) {
            BigInteger diff = val1.subtract(val2);
            return makeBool(diff.signum() >= 0, env);
        }
        
        if(fctname.equals("$lte")) {
            BigInteger diff = val1.subtract(val2);
            return makeBool(diff.signum() <= 0, env);
        }
        
        if(fctname.equals("$lt")) {
            BigInteger diff = val1.subtract(val2);
            return makeBool(diff.signum() < 0, env);
        }
        
        if(fctname.equals("$plus")) {
            return makeInt(val1.add(val2), env);
        }
        
        if(fctname.equals("$minus")) {
            return makeInt(val1.subtract(val2), env);
        }
        
        throw new TermException("Uncalculable function: " + fctname);
    }

    private Term makeBool(boolean val, Environment env) throws TermException {
        Function f = env.getFunction(Boolean.toString(val));
        return new Application(f, Environment.getBoolType());
    }
    
    private Term makeInt(BigInteger val, Environment env) throws TermException {
        if(val.signum() >= 0) {
            Function f = env.getNumberLiteral(val);
            return new Application(f, Environment.getIntType());
        } else {
            Function f = env.getNumberLiteral(val.negate());
            Term a = new Application(f, Environment.getIntType());
            Function neg = env.getFunction("$neg");
            assert neg != null;
            return new Application(neg, Environment.getIntType(), new Term[] { a });
        }
    }

    private BigInteger makeNumber(Term subterm) throws TermException {
        if (subterm instanceof Application) {
            Application app = (Application) subterm;
            Function fct = app.getFunction();
            if (fct instanceof NumberLiteral) {
                NumberLiteral literal = (NumberLiteral) fct;
                return literal.getValue();
            } else if(fct.getName().equals("$neg")){
                return makeNumber(app.getSubterm(0)).negate();
            }
        }
        throw new TermException("Argument is not a (possibly negated) integer literal");
    }

}
