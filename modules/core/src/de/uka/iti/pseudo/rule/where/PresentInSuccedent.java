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
import de.uka.iti.pseudo.util.ConcatenationList;

/**
 * @ivildoc "Where condition/presentInSuccedent"
 * 
 * <h2>Where condition <tt>presentInSuccedent</tt></h2>
 * 
 * This condition can be used to ensure that a formula is present in 
 * the succedent of the sequent.
 * 
 * <p>
 * A formula is considered to be present if it appears toplevel and
 * syntactically equal on the sequent.
 * 
 * <p>
 * If more than one argument is provided, the check will be performed for all
 * arguments. The result will be <code>true</code> only if all arguments pass
 * it.
 * 
 * <h3>Syntax</h3> The where condition expects one or more boolean terms as
 * arguments.
 * 
 * <h3>Example:</h3>
 * 
 * <pre>
 *   function int card(set('a))
 *   
 *   rule nonneg_cardinality
 *   find card(%x)
 *   where not presentInSuccedent card(%x) < 0
 *   add |- card(%x) < 0 
 * </pre>
 * 
 * <h3>See also:</h3>
 * <a href="ivil:/Where condition/presentInSequent">presentInSequent</a>,
 * <a href="ivil:/Where condition/presentInAntecedent">presentInAntecedent</a>
 * 
 * <h3>Result:</h3>
 * <code>true</code> if the all arguments appear toplevel in the sequent,
 * <code>false</code> otherwise. Does never fail.
 * 
 * @author mattias ulbrich
 */
public class PresentInSuccedent extends PresentWhereCondition {

    public PresentInSuccedent() {
        super("presentInSuccedent");
    }

    @Override
    protected List<Term> chooseFormulas(Sequent seq) {
        return seq.getSuccedent();
    }

}
