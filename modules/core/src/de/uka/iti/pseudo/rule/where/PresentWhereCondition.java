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

import java.util.List;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;

/**
 * This abstract class is superclass to where conditions which check whether a
 * formula is already present on the sequent
 * 
 * @author mattias ulbrich
 */
public abstract class PresentWhereCondition extends WhereCondition {

    protected PresentWhereCondition(String name) {
        super(name);
    }

    @Override
    public void checkSyntax(Term[] arguments) throws RuleException {
        if (arguments.length == 0) {
            throw new RuleException("presentInAntecedent expects at least one argument");
        }
    }

    @Override
    public boolean check(Term[] formalArguments, Term[] actualArguments,
            RuleApplication ruleApp, Environment env) throws RuleException {
        
        Sequent seq = ruleApp.getProofNode().getSequent();
        List<Term> formulas = chooseFormulas(seq);
        for (Term arg : actualArguments) {
            if(formulas.contains(arg)) {
                return true;
            }
        }
        
        return false;
    }

    protected abstract List<Term> chooseFormulas(Sequent seq);

}
