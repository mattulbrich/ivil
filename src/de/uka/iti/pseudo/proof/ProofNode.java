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
    
    public ProofNode(Proof proof, ProofNode parent, Sequent sequent) {
        this.proof = proof;
        this.parent = parent;
        this.sequent = sequent;
    }
    public ProofNode(Proof proof, ProofNode parent, List<Term> antecedent,
            List<Term> succedent) {
        this(proof, parent, new Sequent(antecedent, succedent));
    }

    protected void setChildren(ProofNode[] children) {
        this.children = children;
        proof.fireNodeChanged(this);
    }
    
    public void prune() {
        setChildren(null);
    }

    public void apply(RuleApplication ruleApp, MatchingContext mc)
            throws ProofException {
        Rule rule = ruleApp.getRule();

        matchFindClause(ruleApp, mc, rule);
        matchAssumeClauses(ruleApp, mc, rule);
        matchWhereClauses(ruleApp, mc, rule);

        setChildren(doAction(ruleApp, mc, rule));

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
        Term oldTerm = sel.selectTopterm(sequent);
        Term newTerm;
        
        if(!sel.isToplevel()) {
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
        Term findSubTerm = findSelector.selectSubterm(sequent);

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
            assert assumeSelectors[i].isToplevel();
            Term assumeTerm = assumeSelectors[i].selectTopterm(sequent);
            LocatedTerm assumption = rule.getAssumptions()[i];
            if (!assumption.isFittingSelect(assumeSelectors[i])) {
                throw new ProofException("Illegal selector for assume (" + i
                        + ")");
            }
            mc.leftMatch(assumption.getTerm(), assumeTerm);
        }
    }
 
    public List<ProofNode> getChildren() {
        if(children != null)
            return Util.readOnlyArrayList(children);
        else
            return null;
    }

    public RuleApplication getAppliedRuleApp() {
        return appliedRuleApp;
    }

    public ProofNode getParent() {
        return parent;
    }

    public Sequent getSequent() {
        return sequent;
    }
    public String getSummaryString() {
        StringBuilder sb = new StringBuilder();
        if(appliedRuleApp != null)
            sb.append("closed ");
        else
            sb.append("open ");
        
        sb.append("node, ")
            .append(sequent.getAntecedent().size())
            .append(" |- ").append(sequent.getSuccedent().size())
            .append(", ").append(getPath());
        
        return sb.toString();
    }
    
    public String getPath() {
        if(parent == null)
            return "";
        else {
            int index = parent.getChildren().indexOf(this);
            assert index != -1;
            return parent.getPath() + index + ".";
        }
    }

}
