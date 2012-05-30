/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.rule.where;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Term;

/**
 * This where condition checks whether the top level symbol of a term is marked
 * "unique".
 */
public class IsUnique extends WhereCondition {

    public IsUnique() {
        super("isUnique");
    }

    @Override 
    public boolean check(Term[] formalArguments,
            Term[] actualArguments, RuleApplication ruleApp,
            Environment env) throws RuleException {
        
        Term t = actualArguments[0];
        if (t instanceof Application) {
            Application app = (Application) t;
            if (app.getFunction().isUnique()) {
                return true;
            }
        }
        
        return false;
            
    }

    @Override 
    public void checkSyntax(Term[] arguments) throws RuleException {
        if(arguments.length != 1)
            throw new RuleException("isUnique expects exactly 1 argument");
    }

}
