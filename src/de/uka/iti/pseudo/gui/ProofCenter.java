package de.uka.iti.pseudo.gui;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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


// the center of this all

public class ProofCenter {

    private MainWindow mainWindow;
    private Environment env;
    private Proof proof;
    
    private List<Rule> rulesSortedForInteraction;
    
    private List<ProofNodeSelectionListener> listeners = new LinkedList<ProofNodeSelectionListener>();
    private boolean isFiring = false;
    
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
            isFiring = false;
        }
    }
    
    public void observe(Object source, Object arg) {
        assert arg instanceof TermSelector;

        TermSelector termSelector = (TermSelector) arg;
        InteractiveRuleApplicationFinder iraf = new InteractiveRuleApplicationFinder(proof, 2, env);
        try {
            List<RuleApplication> result = iraf.findAll(termSelector, rulesSortedForInteraction);
            getMainWindow().getRuleApplicationComponent().setInteractiveApplications(result);
        } catch (ProofException e) {
            // TODO gescheiter Report!
            e.printStackTrace();
        }
    }
}
