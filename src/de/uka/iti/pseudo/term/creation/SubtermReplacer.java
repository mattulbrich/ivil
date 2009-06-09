package de.uka.iti.pseudo.term.creation;

import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;

// TODO DOC DOC DOC

public class SubtermReplacer extends RebuildingTermVisitor {
    
    private int counter;
    private int replaceNumber;
    private Term replaceWith;
    
    private SubtermReplacer(int replaceNumber, Term replaceWith) {
        this.replaceNumber = replaceNumber;
        this.replaceWith = replaceWith;
    }
    
    public static Term replace(Term oldTerm, int subtermNo, Term replaceWith) throws TermException {
        SubtermReplacer str = new SubtermReplacer(subtermNo, replaceWith);
        oldTerm.visit(str);
        return str.resultingTerm;
    }

    
    @Override 
    protected void defaultVisitTerm(Term term) throws TermException {
        if(counter == replaceNumber)
            resultingTerm = replaceWith;
        else
            resultingTerm = null;
        counter ++;
    }

}
