package de.uka.iti.pseudo.proof;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import de.uka.iti.pseudo.rule.GoalAction;
import de.uka.iti.pseudo.rule.LocatedTerm;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.rule.WhereClause;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.creation.SubtermCollector;
import de.uka.iti.pseudo.term.creation.SubtermReplacer;
import de.uka.iti.pseudo.util.Util;

public class ProofNode {

    private Sequent sequent;

    private Proof proof;

    private ProofNode[] children = null;

    private ProofNode parent;

    private RuleApplication appliedRuleApp;

    public ProofNode(Proof proof, ProofNode parent, List<Term> antecedent,
            List<Term> succedent) {
        this.proof = proof;
        this.parent = parent;
        this.sequent = new Sequent(antecedent, succedent);
    }

    public void makeChildren(ProofNode[] children) {
        assert this.children == null;
        this.children = children;
    }

    public void apply(RuleApplication ruleApp, MatchingContext mc)
            throws ProofException {
        Rule rule = ruleApp.getRule();

        matchFindClause(ruleApp, mc, rule);
        matchAssumeClauses(ruleApp, mc, rule);
        matchWhereClauses(ruleApp, mc, rule);

        children = doAction(ruleApp, mc, rule);

        this.appliedRuleApp = ruleApp;

    }

    private ProofNode[] doAction(RuleApplication ruleApp, MatchingContext mc, Rule rule)
        throws ProofException {
        List<ProofNode> newNodes = new LinkedList<ProofNode>();
        List<Term> antecedent = new ArrayList<Term>();
        List<Term> succedent = new ArrayList<Term>();
        
        for (GoalAction action : rule.getGoalActions()) {
            antecedent.clear();
            succedent.clear();
            switch (action.getKind()) {
            case CLOSE:
                return new ProofNode[0];
            case COPY:
                antecedent.addAll(sequent.getAntecedent());
                succedent.addAll(sequent.getSuccedent());
                break;
            }
            
            Term replaceWith = action.getReplaceWith();
            if(replaceWith != null) {
                TermSelector sel = ruleApp.getFindSelector();
                try {
                    replaceTerm(sel, mc.instantiate(replaceWith), antecedent, succedent);
                } catch (TermException e) {
                    throw new ProofException("Cannot replace term with " + replaceWith);
                }
            }
            
            newNodes.add(new ProofNode(proof, this, antecedent, succedent));
        }

        return Util.listToArray(newNodes, ProofNode.class);
    }

    private void replaceTerm(TermSelector sel, Term replaceWith, List<Term> antecedent, List<Term> succedent) throws ProofException, TermException {
        Term oldTerm = selectTerm(sel);
        Term newTerm;
        
        if(sel.hasSubtermNo()) {
            newTerm = SubtermReplacer.replace(oldTerm, sel.getSubtermNo(), replaceWith);
        } else {
            newTerm = replaceWith;
        }
        
        int index = sel.getTermNo();
        if(sel.isAntecedent()) {
            antecedent.set(index, newTerm);
        } else {
            succedent.set(index, newTerm);
        }
    }

    private void matchWhereClauses(RuleApplication ruleApp, MatchingContext mc,
            Rule rule) throws ProofException {
        for (WhereClause whereClause : rule.getWhereClauses()) {
            try {
                if (!whereClause.applyTo(mc, ruleApp, this))
                    throw new ProofException(
                            "WhereClause does not evaluate to true : "
                                    + whereClause);
            } catch (RuleException e) {
                throw new ProofException("WhereClause not applicable: "
                        + whereClause, e);
            }
        }
    }

    private void matchFindClause(RuleApplication ruleApp, MatchingContext mc,
            Rule rule) throws ProofException {
        TermSelector findSelector = ruleApp.getFindSelector();
        Term findSubTerm = selectSubterm(findSelector);

        LocatedTerm findClause = rule.getFindClause();
        if (!findClause.isFittingSelect(findSelector)) {
            throw new ProofException("Illegal selector for find");
        }
        mc.leftMatch(findClause.getTerm(), findSubTerm);
    }

    private void matchAssumeClauses(RuleApplication ruleApp,
            MatchingContext mc, Rule rule) throws ProofException {
        int length = ruleApp.getAssumeSelectors().length;
        TermSelector[] assumeSelectors = ruleApp.getAssumeSelectors();

        assert length == assumeSelectors.length;

        for (int i = 0; i < length; i++) {
            assert !assumeSelectors[i].hasSubtermNo();
            Term assumeTerm = selectTerm(assumeSelectors[i]);
            LocatedTerm assumption = rule.getAssumptions()[i];
            if (!assumption.isFittingSelect(assumeSelectors[i])) {
                throw new ProofException("Illegal selector for assume (" + i
                        + ")");
            }
            mc.leftMatch(assumption.getTerm(), assumeTerm);
        }
    }

    public Term selectTerm(TermSelector s) throws ProofException {
        List<Term> terms;
        if (s.isAntecedent()) {
            terms = sequent.getAntecedent();
        } else {
            terms = sequent.getSuccedent();
        }

        int termNo = s.getTermNo();
        if (termNo < 0 || termNo >= terms.size())
            throw new ProofException("Can select " + s);

        return terms.get(termNo);
    }

    public Term selectSubterm(TermSelector s) throws ProofException {
        Term term = selectTerm(s);
        if (!s.hasSubtermNo()) {
            return term;
        } else {
            List<Term> subterms = SubtermCollector.collect(term);

            int subtermNo = s.getSubtermNo();
            if (subtermNo < 0 || subtermNo >= subterms.size())
                throw new ProofException("Can select " + s);

            return subterms.get(subtermNo);
        }
    }

}
