package de.uka.iti.pseudo.proof;

import java.util.List;

import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.SubtermCollector;

// TODO DOC DOC!
// TODO Use proofexception?
public class TermSelector {

    public static final boolean ANTECEDENT = true;
    public static final boolean SUCCEDENT = false;
    
    private boolean inAntecedent;
    private int termNo;
    private int subtermNo;
    
    public TermSelector(boolean inAntecedent, int termNo, int subtermNo) {
        this.inAntecedent = inAntecedent;
        this.termNo = termNo;
        this.subtermNo = subtermNo;
        
        assert termNo >= 0;
        assert subtermNo >= 0;
    }
    
    public TermSelector(boolean inAntecendent, int termNo) {
        this(inAntecendent, termNo, 0);
    }
    
    public TermSelector(String descr) throws FormatException {
        String[] sect = descr.split("\\.");
        if(sect.length != 3)
            throw new FormatException("TermSelector", "illegally separated string", descr);
        
        if("A".equals(sect[0])) {
            inAntecedent = true;
        } else if("S".equals(sect[0])) {
            inAntecedent = false;
        } else
            throw new FormatException("TermSelector", "unknown first part: " + sect[0], descr);
        
        try {
            termNo = Integer.parseInt(sect[1]);
            if(termNo < 0)
                throw new FormatException("TermSelector", "negative: " + sect[1], descr);
        } catch (NumberFormatException e) {
            throw new FormatException("TermSelector", "not a number: " + sect[1], descr);
        }
        
        try {
            subtermNo = Integer.parseInt(sect[2]);
            if(subtermNo < 0)
                throw new FormatException("TermSelector", "negative: " + sect[2], descr);
        } catch (NumberFormatException e) {
            throw new FormatException("TermSelector", "not a number: " + sect[2], descr);
        }
    }

    public String toString() {
        return (inAntecedent ? "A." : "S.") + termNo + "." + subtermNo;
    }

    public boolean isAntecedent() {
        return inAntecedent;
    }
    
    public boolean isSuccedent() {
        return !inAntecedent;
    }

    public int getTermNo() {
        return termNo;
    }

    public boolean isToplevel() {
        return subtermNo == 0;
    }
    
    public int getSubtermNo() {
        return subtermNo;
    }

    public TermSelector selectSubterm(int subtermNo) {
        assert subtermNo >= 0;
        return new TermSelector(inAntecedent, termNo, subtermNo);
    }
    
    public Term selectTopterm(Sequent sequent) throws ProofException {
        List<Term> terms;
        if (isAntecedent()) {
            terms = sequent.getAntecedent();
        } else {
            terms = sequent.getSuccedent();
        }

        int termNo = getTermNo();
        if (termNo < 0 || termNo >= terms.size())
            throw new ProofException("Can select " + this);

        return terms.get(termNo);
    }
    
    public Term selectSubterm(Sequent sequent) throws ProofException {
        Term term = selectTopterm(sequent);
        if (isToplevel()) {
            return term;
        } else {
            List<Term> subterms = SubtermCollector.collect(term);

            int subtermNo = getSubtermNo();
            if (subtermNo < 0 || subtermNo >= subterms.size())
                throw new ProofException("Can select " + this);

            return subterms.get(subtermNo);
        }
    }
    
}
