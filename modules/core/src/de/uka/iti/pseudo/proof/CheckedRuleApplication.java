package de.uka.iti.pseudo.proof;

import java.util.List;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.rule.LocatedTerm;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.rule.WhereClause;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.creation.ProgramComparingTermInstantiator;
import de.uka.iti.pseudo.term.creation.TermInstantiator;
import de.uka.iti.pseudo.util.Log;

public class CheckedRuleApplication extends ImmutableRuleApplication {

    private boolean checked = false;

    public CheckedRuleApplication(RuleApplication ruleApp) {
        super(ruleApp);
    }

    public void check(Environment env) throws ProofException {
        TermInstantiator inst =
                new ProgramComparingTermInstantiator(
                        getSchemaVariableMapping(),
                        getTypeVariableMapping(),
                        getSchemaUpdateMapping(),
                        env);

        matchFindClause(inst);
        matchAssumeClauses(inst);
        verifyWhereClauses(inst, env);

        // no exception until here ...
        checked = true;
    }

    public boolean isApplicable(Environment env) {
        try {
            check(env);
            return true;
        } catch(ProofException ex) {
            return true;
        }
    }

    /*
     * Match find clause against the sequent.
     *
     * If there is no find clause in this rule, just return.
     */
    private void matchFindClause(TermInstantiator inst) throws ProofException {

        LocatedTerm findClause = getRule().getFindClause();
        if(findClause == null) {
            return;
        }

        TermSelector findSelector = getFindSelector();
        Term findSubTerm = findSelector.selectSubterm(getProofNode().getSequent());

        if (!findClause.isFittingSelect(findSelector)) {
            throw new ProofException("Illegal selector for find");
        }

        Term instantiated;
        try {
            instantiated = inst.instantiate(findClause.getTerm());
        } catch (TermException e) {
            throw new ProofException("cannot instantiate find clause", e);
        }

        if(!findSubTerm.equals(instantiated)) {
            throw new ProofException("find clause does not match: \nfind: " + findSubTerm + " \ninstantiated: " + instantiated);
        }

        if(!findClause.isFittingSelect(findSelector)) {
            throw new ProofException("find selector does match find clase: \n" + findClause +
                    "\n" + instantiated + " - " + findSelector);
        }

        OptionalUpdateInstantiationChecker.check(this);
    }

    /*
     * Match assume clauses against the sequent.
     *
     * FIXME start with rule's clauses not with app's clauses
     */
    private void matchAssumeClauses(TermInstantiator inst) throws ProofException {

        List<TermSelector> assumeSelectors = getAssumeSelectors();
        int length = assumeSelectors.size();

        for (int i = 0; i < length; i++) {
            TermSelector assSel = assumeSelectors.get(i);
            assert assSel.isToplevel();
            Term assumeTerm = assSel.selectTopterm(getProofNode().getSequent());
            LocatedTerm assumption = getRule().getAssumptions().get(i);
            if (!assumption.isFittingSelect(assSel)) {
                throw new ProofException("Illegal selector for assume (" + i + ")");
            }
            Term instantiated;
            try {
                instantiated = inst.instantiate(assumption.getTerm());
            } catch (TermException e) {
                throw new ProofException("cannot instantiate assume clause", e);
            }
            if(!assumeTerm.equals(instantiated)) {
                throw new ProofException("assumption clause does not match");
            }
        }
    }

    /*
     * Verify where clauses in a rule application using a terminstantiator.
     */
    private void verifyWhereClauses(TermInstantiator inst, Environment env) throws ProofException {
        for (WhereClause whereClause : getRule().getWhereClauses()) {
            try {
                if(!whereClause.applyTo(inst, this, env)) {
                    Log.log(Log.ERROR, "WhereClause failed: " + whereClause);
                    Log.log(Log.DEBUG, "Term inst: " + inst);
                    throw new ProofException("WhereClause failed: " + whereClause +
                            ", instantiation: " + inst);
                }
            } catch (RuleException e) {
                Log.log(Log.ERROR, "WhereClause failed: " + whereClause);
                Log.log(Log.DEBUG, "Term inst: " + inst);
                throw new ProofException("WhereClause not applicable: " + whereClause, e);
            }
        }
    }



}
