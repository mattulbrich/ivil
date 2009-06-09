package de.uka.iti.pseudo.gui;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import nonnull.NonNull;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.InteractiveRuleApplicationFinder;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.RulePriorityComparator;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Sequent;


// the center of this all
//TODO DOC
public class ProofCenter implements TermSelectionListener {

    private MainWindow mainWindow;
    private Environment env;
    private Proof proof;
    
    private List<Rule> rulesSortedForInteraction;
    
    private List<ProofNodeSelectionListener> listeners = new LinkedList<ProofNodeSelectionListener>();
    private boolean isFiring = false;
    private ProofNode currentProofNode;
    
    public ProofCenter(@NonNull Proof proof, @NonNull Environment env) {
        this.proof = proof;
        this.env = env;
        mainWindow = new MainWindow(this);
        fireSelectedProofNode(proof.getRoot());
        
        prepareRuleLists();
    }

    private void prepareRuleLists() {
        rulesSortedForInteraction = env.getAllRules();
        Collections.sort(rulesSortedForInteraction, new RulePriorityComparator());
        
        // other rule lists: simplifications with priority codes.
    }

    public @NonNull Environment getEnvironment() {
        return env;
    }

    public @NonNull Proof getProof() {
        return proof;
    }

    public @NonNull MainWindow getMainWindow() {
        return mainWindow;
    }
    
    public void addProofNodeSelectionListener(ProofNodeSelectionListener l) {
        listeners.add(l);
    }
    
    public void removeProofNodeSelectionListener(ProofNodeSelectionListener l) {
        listeners.remove(l);
    }
    
    public void fireSelectedProofNode(ProofNode node) {
        if(!isFiring) {
            isFiring = true;
            for (ProofNodeSelectionListener l : listeners) {
                l.proofNodeSelected(node);
            }
            currentProofNode = node;
            isFiring = false;
        }
    }
    
    public void termSelected(Sequent sequent, TermSelector termSelector) {

        int goalNo = proof.getOpenGoals().indexOf(currentProofNode);
        if(goalNo == -1) {
            // current sequent is not a goal.
            return;
        }
        
        InteractiveRuleApplicationFinder iraf = new InteractiveRuleApplicationFinder(proof, goalNo, env);
        try {
            List<RuleApplication> result = iraf.findAll(termSelector, rulesSortedForInteraction);
            getMainWindow().getRuleApplicationComponent().setInteractiveApplications(result);
        } catch (ProofException e) {
            // TODO gescheiter Report!
            e.printStackTrace();
        }
        
    }

    public void apply(RuleApplication ruleApp) throws ProofException {
        ProofNode parent = proof.getGoal(ruleApp.getGoalNumber());
        proof.apply(ruleApp);
        
        // next to select is first child (or self if no children)
        List<ProofNode> children = parent.getChildren();
        ProofNode next;
        if(children != null && !children.isEmpty()) {
            next = children.get(0);
        } else {
            next = parent;
        }
        fireSelectedProofNode(next);
    }
}
