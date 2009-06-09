package de.uka.iti.pseudo.proof;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Properties;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.creation.RebuildingTermVisitor;
import de.uka.iti.pseudo.term.creation.TermInstantiator;

// TODO DOC

public class Proof extends Observable {
    
    public static class Instantiater extends RebuildingTermVisitor {
        
    }

    protected ProofNode root;
    
    protected List<ProofNode> openGoals = new LinkedList<ProofNode>();
    
    public void apply(@NonNull RuleApplication ruleApp, Environment env) throws ProofException {
        apply(ruleApp, env, null);
    }
    
    public synchronized void apply(@NonNull RuleApplication ruleApp, Environment env, Properties whereClauseProperties) throws ProofException {
        
        TermInstantiator inst = new TermInstantiator(ruleApp);
        
        int goalno = extractGoalNo(ruleApp);
        ProofNode goal = openGoals.get(goalno);
        
        goal.apply(ruleApp, inst, env, whereClauseProperties);
        
        openGoals.remove(goalno);
        openGoals.addAll(goalno, goal.getChildren());
        fireNodeChanged(goal);        
    }
    
    public Proof(Term initialProblem) throws TermException {
        this(new Sequent(Collections.<Term>emptyList(), Collections.<Term>singletonList(initialProblem)));
    }
    
    public Proof(Sequent initialSequent) {
        root = new ProofNode(this, null, initialSequent);
        openGoals.add(root);
    }

    // needed for mock objects
    protected Proof() { }

    private int extractGoalNo(RuleApplication ruleApp) throws ProofException {
        int goalno = ruleApp.getGoalNumber();
        if(goalno < 0 || goalno >= openGoals.size())
            throw new ProofException("Cannot apply ruleApplication. Illegal goal number in\n" + ruleApp);
        return goalno;
    }

    public ProofNode getRoot() {
        return root;
    }

    private void fireNodeChanged(ProofNode proofNode) {
        setChanged();
        notifyObservers(proofNode);
    }

    public List<ProofNode> getOpenGoals() {
        return Collections.unmodifiableList(openGoals);
    }

    public ProofNode getGoal(int goalNo) {
        return openGoals.get(goalNo);
    }
    
}
