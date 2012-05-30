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
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.ProgramTerm;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.UpdateTerm;

/**
 * The {@link WhereCondition} TopLevel ensures that the find clause is not
 * within the reach of an update or program modality.
 * 
 * @ivildoc "Where condition/toplevel"
 * 
 * <h2>Where condition <tt>toplevel</tt></h2>
 * 
 * This condition can be used to ensure that the term which matches the find
 * clause is not within the reach of an update or program modality.
 * 
 * <h3>Syntax</h3> The where condition expects no arguments.
 * 
 * <h3>Example:</h3>
 * 
 * <pre>
 *   rule apply_equality 
 *   assume %a=%b |-
 *   where toplevel
 *   find %a
 *   replace %b
 * </pre>
 * 
 * The equality <tt>a=5</tt> must not be applied to replace the second
 * <code>a</code> in the expression <tt>{a:=7}a=5</tt>
 * 
 * <h3>See also:</h3>
 * 
 * <h3>Result:</h3> <code>true</code> if the find clause of the rule matches
 * against a term which is not in the scope of an update or a program modality,
 * <code>false</code> otherwise. Does not fail.
 */
public class TopLevel extends WhereCondition {

    public TopLevel() {
        super("toplevel");
    }

    @Override
    public boolean check(Term[] formalArguments,
            Term[] actualArguments, RuleApplication ruleApp,
            Environment env) throws RuleException {
        try {
            ProofNode goal = ruleApp.getProofNode();
            return check(ruleApp.getFindSelector(), goal.getSequent());

        } catch (ProofException e) {
            throw new RuleException(e);
        }

    }

    /* intermediate step to allow testing */
    /**
     * Check that the selected term is not within an update or program.
     * 
     * @param select
     *            the term selector to be checked
     * @param sequent
     *            the sequent to check in
     * 
     * @return true if the term is not within the reach of an update or program
     *         term
     * 
     * @throws ProofException
     *             never thrown here.
     */
    boolean check(TermSelector select, Sequent sequent) throws ProofException {

        Term term = select.selectTopterm(sequent);

        for (int p : select.getPath()) {
            if (term instanceof UpdateTerm && p == 0)
                return false;
            if (term instanceof ProgramTerm)
                return false;
            term = term.getSubterm(p);
        }

        return true;
    }

    @Override
    public void checkSyntax(Term[] arguments) throws RuleException {
        if (arguments.length > 0)
            throw new RuleException("toplevel expects no arguments");
    }

}
