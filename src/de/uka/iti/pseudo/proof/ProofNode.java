package de.uka.iti.pseudo.proof;

import java.util.List;

import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.SubtermCollector;

public class ProofNode {

    private Sequent sequent;
    
    private Proof proof;
    
    private ProofNode[] children = null;
    
    private ProofNode parent;
    
    public void makeChildren(ProofNode[] children) {
        assert this.children == null;
        this.children = children;
    }

    public Term selectTerm(TermSelector s) throws ProofException {
        List<Term> terms;
        if(s.isAntecedent()) {
            terms = sequent.getAntecedent();
        } else {
            terms = sequent.getSuccedent();
        }
        
        int termNo = s.getTermNo();
        if(termNo < 0 || termNo >= terms.size())
            throw new ProofException("Can select " + s);
     
        return terms.get(termNo);
    }

    public Term selectSubterm(TermSelector s) throws ProofException {
        Term term = selectTerm(s);
        if(!s.hasSubtermNo()) {
            return term;
        } else {
            List<Term> subterms = SubtermCollector.collect(term);
            
            int subtermNo = s.getSubtermNo();
            if(subtermNo < 0 || subtermNo >= subterms.size())
                throw new ProofException("Can select " + s);
            
            return subterms.get(subtermNo);
        }
    }
    
}
