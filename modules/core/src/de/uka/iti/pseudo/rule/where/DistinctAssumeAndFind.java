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
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.Term;

/**
 * @ivildoc "Where condition/distinctAssumeAndFind"
 * 
 * <h2>Where condition <tt>distinctAssumeAndFind</tt></h2>
 * 
 * This condition can be used to ensure that the find clause does not match the
 * same as any of the assume clauses.
 * 
 * <h3>Syntax</h3> The where condition expects no arguments.
 * 
 * <h3>Example:</h3>
 * 
 * <pre>
 *   rule replace_known_left
 *   assume  %b |-
 *   find    %b 
 *   where   toplevel
 *   where   distinctAssumeAndFind
 *   replace true
 * </pre>
 * 
 * This prevents from replacing an antecedent
 * 
 * <h3>See also:</h3> <a href="ivil:/Where condition/toplevel">toplevel</a>
 * 
 * <h3>Result:</h3>
 * 
 * <code>true</code> if the find clauses does not match the same as an assume
 * clause. <code>false</code> if the find clause matches against the same term
 * in a sequent as an assumption.
 */
public class DistinctAssumeAndFind extends WhereCondition {

    public DistinctAssumeAndFind() {
        super("distinctAssumeAndFind");
    }

    @Override 
    public boolean check(Term[] formalArguments,
            Term[] actualArguments, RuleApplication ruleApp,
            Environment env) throws RuleException {
        
        TermSelector findSel = ruleApp.getFindSelector().getToplevelSelector();
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
