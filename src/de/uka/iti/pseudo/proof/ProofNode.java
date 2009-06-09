package de.uka.iti.pseudo.proof;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import nonnull.Nullable;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.rule.GoalAction;
import de.uka.iti.pseudo.rule.LocatedTerm;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.rule.WhereClause;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.creation.SubtermReplacer;
import de.uka.iti.pseudo.term.creation.TermUnification;
import de.uka.iti.pseudo.util.Util;

// TODO DOC

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
            List<Term> succedent) throws TermException {
        this(proof, parent, new Sequent(antecedent, succedent));
    }

    protected void setChildren(ProofNode[] children) {
        this.children = children;
    }
    
    public void prune() {
        setChildren(null);
    }

    public void apply(RuleApplication ruleApp, TermUnification mc, Environment env, Properties whereClauseProperties)
            throws ProofException {
        
        if(appliedRuleApp != null)
            throw new ProofException("Trying to apply proof to a non-leaf proof node");
        
        Rule rule = ruleApp.getRule();

        matchFindClause(ruleApp, mc, rule);
        matchAssumeClauses(ruleApp, mc, rule);
        matchWhereClauses(ruleApp, mc, rule, env, whereClauseProperties);

        setChildren(doAction(ruleApp, mc, rule));

        this.appliedRuleApp = ruleApp;

    }

    private ProofNode[] doAction(RuleApplication ruleApp, TermUnification mc, Rule rule)
        throws ProofException {
        List<ProofNode> newNodes = new LinkedList<ProofNode>();
        List<Term> antecedent = new ArrayList<Term>();
        List<Term> succedent = new ArrayList<Term>();
        
        try {
            
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
                TermSelector findSelector = ruleApp.getFindSelector();
                if(replaceWith != null) {
                    Term instantiated = mc.instantiate(replaceWith);
                    assert !mc.containsSchemaIdentifier(instantiated);
                    replaceTerm(findSelector, instantiated, antecedent, succedent);
                } else if(action.isRemoveOriginalTerm()) {
                    assert findSelector.isToplevel();
                    if(findSelector.isAntecedent())
                        antecedent.remove(findSelector.getTermNo());
                    else
                        succedent.remove(findSelector.getTermNo());
                }
                
                for (Term add : action.getAddAntecedent()) {
                    antecedent.add(mc.instantiate(add));
                }
                
                for (Term add : action.getAddSuccedent()) {
                    succedent.add(mc.instantiate(add));
                }
                
                newNodes.add(new ProofNode(proof, this, antecedent, succedent));
            }

            return Util.listToArray(newNodes, ProofNode.class);
            
        } catch (TermException e) {
            throw new ProofException("Exception during application of rule", e);
        }
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

    private void matchWhereClauses(RuleApplication ruleApp, TermUnification mc,
            Rule rule, Environment env, Properties whereClauseProperties) throws ProofException {
        for (WhereClause whereClause : rule.getWhereClauses()) {
            try {
                if (!whereClause.applyTo(mc, ruleApp, this, env, whereClauseProperties))
                    throw new ProofException(
                            "WhereClause does not evaluate to true : "
                                    + whereClause);
            } catch (RuleException e) {
                throw new ProofException("WhereClause not applicable: "
                        + whereClause, e);
            }
        }
    }

    private void matchFindClause(RuleApplication ruleApp, TermUnification mc,
            Rule rule) throws ProofException {
        TermSelector findSelector = ruleApp.getFindSelector();
        Term findSubTerm = findSelector.selectSubterm(sequent);

        LocatedTerm findClause = rule.getFindClause();
        if (!findClause.isFittingSelect(findSelector)) {
            throw new ProofException("Illegal selector for find");
        }
        if(!mc.leftUnify(findClause.getTerm(), findSubTerm))
            throw new ProofException("find clause does not match");
    }

    private void matchAssumeClauses(RuleApplication ruleApp,
            TermUnification mc, Rule rule) throws ProofException {
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
            if(!mc.leftUnify(assumption.getTerm(), assumeTerm))
                throw new ProofException("assumption clause does not match");
        }
    }
 
    public List<ProofNode> getChildren() {
        if(children != null)
            return Util.readOnlyArrayList(children);
        else
            return null;
    }

    public @Nullable RuleApplication getAppliedRuleApp() {
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
            return parent.getPath() + (index+1) + ".";
        }
    }
    
    public boolean isClosed() {
        if(children == null)
            return false;
        
        for (ProofNode child : children) {
            if(!child.isClosed())
                return false;
        }
        
        return true;
    }

}
