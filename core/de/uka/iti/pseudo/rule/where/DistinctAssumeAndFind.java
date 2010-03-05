/*
 * This file is part of PSEUDO
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.rule.where;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.Term;

// TODO Documentation needed
public class DistinctAssumeAndFind extends WhereCondition {

    public DistinctAssumeAndFind() {
        super("distinctAssumeAndFind");
    }

    @Override 
    public boolean check(Term[] formalArguments,
            Term[] actualArguments, RuleApplication ruleApp, ProofNode goal,
            Environment env) throws RuleException {
        
        TermSelector findSel = ruleApp.getFindSelector();
        for (TermSelector sel : ruleApp.getAssumeSelectors()) {
            if(findSel.equals(sel)) {
                return false;
            }
        }
        return true;
        
    }

    @Override 
    public void checkSyntax(Term[] arguments) throws RuleException {
        if(arguments.length > 0)
            throw new RuleException("distinctAssumeAndFind does not take arguments");
    }

}
