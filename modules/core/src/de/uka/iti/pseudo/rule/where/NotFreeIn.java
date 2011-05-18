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
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Variable;

// TODO DOC l8er
/*
 * Format 
 *    notFreeIn { variable } { term }
 */
@Deprecated
public class NotFreeIn extends WhereCondition {

    public NotFreeIn() {
        super("notFreeIn");
    }

    @Override
    public void checkSyntax(Term[] arguments) throws RuleException {
        if(arguments.length != 2)
            throw new RuleException("notFreeIn expects exactly 2 arguments");
        
        if(!(arguments[0] instanceof SchemaVariable) && !(arguments[0] instanceof Variable))
            throw new RuleException("notFreeIn expects (schema) variable as first argument");
    }
    
    @Override public boolean check(Term[] formalArguments,
            Term[] actualArguments, RuleApplication ruleApp,
            Environment env) throws RuleException {
        
        // TODO Implement NotFreeIn verify
        // get schema variable
        // instantiate
        // make sure it is a variable
        // make sure collect free vars does not find it.
        // throw exception if there is still a schema variable.
        
        return true;
    }

}
