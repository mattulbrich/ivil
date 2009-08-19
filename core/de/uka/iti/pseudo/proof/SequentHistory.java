package de.uka.iti.pseudo.proof;

import java.util.ArrayList;

import de.uka.iti.pseudo.term.Sequent;
// TODO DOC DOC!!!
public class SequentHistory {

    public static class Annotation {
        String text;
        ProofNode creatingProofNode;
        Annotation parentAnnotation;
        public Annotation(String text, ProofNode creatingProofNode,
                Annotation parentAnnotation) {
            this.text = text;
            this.creatingProofNode = creatingProofNode;
            this.parentAnnotation = parentAnnotation;
        }
        public Annotation(String text) {
            this.text = text;
        }
    }

    private String ruleAppText;
    private Annotation reasonAnnotation;
    private ProofNode creatingProofNode;
    
    private boolean fixed;
    
    private ArrayList<Annotation> antecedent; 
    private ArrayList<Annotation> succedent;

    public SequentHistory(String ruleAppText, Annotation reasonAnnotation, ProofNode creatingProofNode) {
        this.ruleAppText = ruleAppText;
        this.reasonAnnotation = reasonAnnotation;
        this.creatingProofNode = creatingProofNode;
        this.antecedent = new ArrayList<Annotation>();
        this.succedent = new ArrayList<Annotation>();
    }

    public SequentHistory(SequentHistory sequentHistory, String ruleAppText, Annotation reasonAnnotation, ProofNode proofNode) {
        this(ruleAppText, reasonAnnotation, proofNode);
        antecedent.addAll(sequentHistory.antecedent);
        succedent.addAll(sequentHistory.succedent);
    }

    public SequentHistory(Sequent sequent, Annotation initialAnnotation) {
        this.antecedent = new ArrayList<Annotation>();
        int len = sequent.getAntecedent().size();
        for (int i = 0; i < len; i++) {
            antecedent.add(initialAnnotation);
        }
        
        this.succedent = new ArrayList<Annotation>();
        len = sequent.getSuccedent().size();
        for (int i = 0; i < len; i++) {
            succedent.add(initialAnnotation);
        }
        
        fix();
    }

    public void fix() {
        fixed = true;
        ruleAppText = null;
        creatingProofNode = null;
        reasonAnnotation = null;
        antecedent.trimToSize();
        succedent.trimToSize();
    }
    
    private void checkNotFixed() {
        if(fixed)
            throw new IllegalStateException("must not change a fixed SequentHistory");
    }

    public boolean sizesAgreeWith(Sequent sequent) {
        return antecedent.size() == sequent.getAntecedent().size() &&
            succedent.size() == sequent.getSuccedent().size();
    }

    public void removed(TermSelector selector) {
        checkNotFixed();
        
        if(selector.isAntecedent()) {
            antecedent.remove(selector.getTermNo());
        } else {
            succedent.remove(selector.getTermNo());
        }
    }

    public void added(boolean side) {
        checkNotFixed();
        
        Annotation ann = new Annotation(ruleAppText, creatingProofNode, reasonAnnotation);
        
        if(side == TermSelector.ANTECEDENT) {
            antecedent.add(ann);
        } else {
            succedent.add(ann);
        }
        
    }

    public Annotation select(TermSelector selector) {
        if(selector == null)
            return null;
        else if(selector.isAntecedent())
            return antecedent.get(selector.getTermNo());
        else
            return succedent.get(selector.getTermNo());
    }

    public void replaced(TermSelector selector) {
        checkNotFixed();
        
        Annotation ann = new Annotation(ruleAppText, creatingProofNode, reasonAnnotation);
        
        if(selector.isAntecedent())
            antecedent.set(selector.getTermNo(), ann);
        else
            succedent.set(selector.getTermNo(), ann);
    }
    
}