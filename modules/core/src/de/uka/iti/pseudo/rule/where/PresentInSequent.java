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
 * @ivildoc "Where condition/presentInSequent"
 * 
 * <h2>Where condition <tt>presentInSequent</tt></h2>
 * 
 * This condition can be used to ensure that a formula is present either in the
 * antecedent or the succedent of the sequent.
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
 *   function int f(int)
 *   
 *   rule cut_f_zero
 *   find f(%x)
 *   where not presentInSequent f(%x)
 *   samegoal add f(%x) = 0 |-
 *   samegoal add |- f(%x) = 0
 * </pre>
 * 
 * <h3>See also:</h3>
 * <a href="ivil:/Where condition/presentInAntecedent">presentInAntecedent</a>,
 * <a href="ivil:/Where condition/presentInSuccedent">presentInSuccedent</a>
 * 
 * <h3>Result:</h3>
 * <code>true</code> if the all arguments appear toplevel in the sequent,
 * <code>false</code> otherwise. Does never fail.
 * 
 * @author mattias ulbrich
 */
public class PresentInSequent extends PresentWhereCondition {

    public PresentInSequent() {
        super("presentInSequent");
    }

    @Override
    protected List<Term> chooseFormulas(Sequent seq) {
        return new ConcatenationList<Term>(seq.getAntecedent(), seq.getSuccedent());
    }

}
