package de.uka.iti.pseudo.rule.where;

import java.util.List;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.SubtermSelector;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.util.Log;

public class ForallQuantifierPrefix extends WhereCondition {

    public ForallQuantifierPrefix() {
        super("forallPrefix");
    }

    @Override
    public void checkSyntax(Term[] arguments) throws RuleException {
        if(arguments.length > 0) {
            throw new RuleException("forallPrefix takes no arguments");
        }
    }

    @Override
    public boolean check(Term[] formalArguments, Term[] actualArguments, RuleApplication ruleApp,
            Environment env) throws RuleException {
        try {
            TermSelector sel = ruleApp.getFindSelector();
            Sequent seq = ruleApp.getProofNode().getSequent();
            Term term = sel.selectTopterm(seq);
            SubtermSelector subSel = sel.getSubtermSelector();

            List<Integer> path = subSel.getPath();
            for (Integer sub : path) {
                if(!isForall(term)) {
                    return false;
                }
                term = term.getSubterm(sub);
            }
            return true;

        } catch (ProofException e) {
            throw new RuleException(e);
        }
    }

    private boolean isForall(Term term) {
        // TODO make this sensible
        if (term instanceof Binding) {
            Binding binding = (Binding) term;
            return binding.getBinder().getName().equals("\\forall");
        }
        return false;
    }

}
