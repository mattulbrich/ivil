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
package de.uka.iti.pseudo.rule.where;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.NumberLiteral;
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Term;

/**
 * This condition can be used to check whether a term of integer type is a
 * literal.
 * 
 * Technically, it must be an {@link Application} and its
 * {@link Application#getFunction()} must return a {@link NumberLiteral}.
 * 
 * @ivildoc "Where condition/intLiteral"
 * 
 * <h2>Where condition <tt>intLiteral</tt></h2>
 * This condition can be used to ensure that a term of integer type is a
 * number literal.
 * 
 * <h3>Syntax</h3>
 *   The where condition expects exactly one argument of type integer.
 *   This can be a schema variable.
 * 
 * <h3>Example:</h3>
 * <pre>
 *   rule add_literal
 *   find %a + %b
 *   where intLiteral %a
 *         intLiteral %b
 *   replace $$intEval(%a + %b)
 * </pre>
 * 
 * <h3>See also:</h3>
 * <a href="ivil:/Meta function/intEval">intEval</a>
 * 
 * <h3>Result:</h3>
 * 
 * <code>true</code> if the argument is a number literal, 
 * <code>false</code> otherwise,
 * never fails. 
 *  
 * @author mattias ulbrich
 */
public class IntLiteral extends WhereCondition {

    public IntLiteral() {
        super("intLiteral");
    }

    @Override public boolean check(Term[] formalArguments,
            Term[] actualArguments, RuleApplication ruleApp,
            Environment env) throws RuleException {
        
        Term t = actualArguments[0];
        if (t instanceof Application) {
            Application app = (Application) t;
            if (app.getFunction() instanceof NumberLiteral) {
                return true;
            }
        }
        
        return false;
            
    }

    @Override public void checkSyntax(Term[] arguments) throws RuleException {
        if(arguments.length != 1)
            throw new RuleException("intLiteral expects exactly 1 argument");
        
        if(!arguments[0].getType().equals(Environment.getIntType()))
            throw new RuleException("intLiteral expects an argument of type integer");
    }

}
