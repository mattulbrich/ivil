package de.uka.iti.pseudo.proof;

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
        assert subtermNo >= -1;
    }
    
    public TermSelector(boolean inAntecendent, int termNo) {
        this(inAntecendent, termNo, -1);
    }
    
    public TermSelector(String descr) throws FormatException {
        String[] sect = descr.split("\\.");
        if(sect.length < 2 || sect.length > 3)
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
        
        if(sect.length == 3) {
            try {
                subtermNo = Integer.parseInt(sect[2]);
                if(subtermNo < 0)
                    throw new FormatException("TermSelector", "negative: " + sect[2], descr);
            } catch (NumberFormatException e) {
                throw new FormatException("TermSelector", "not a number: " + sect[2], descr);
            }
        } else {
            subtermNo = -1;
        }
    }

   

    public String toString() {
        return (inAntecedent ? "A." : "S.") + termNo + (subtermNo > 0 ? "."+subtermNo : "");
    }

    public boolean isAntecedent() {
        return inAntecedent;
    }
    
    public boolean isSucedent() {
        return !inAntecedent;
    }

    public int getTermNo() {
        return termNo;
    }

    public boolean hasSubtermNo() {
        return subtermNo >= 0;
    }
    
    public int getSubtermNo() {
        assert subtermNo >= 0;
        return subtermNo;
    }

    public TermSelector selectSubterm(int subtermNo) {
        assert subtermNo >= 0;
        return new TermSelector(inAntecedent, termNo, subtermNo);
    }
    
}
