package de.uka.iti.pseudo.term;

public class IfModality extends Modality {

    private Term conditionTerm;
    private boolean hasElseModality;

    public IfModality(Term condTerm, Modality thenMod) {
        super(thenMod);
        this.conditionTerm = condTerm;
        this.hasElseModality = false;
    }
    
    public IfModality(Term condTerm, Modality thenMod, Modality elseMod) {
        super(thenMod, elseMod);
        this.conditionTerm = condTerm;
        this.hasElseModality = true;
    }

    @Override public String toString(boolean typed) {
        return "if " + conditionTerm.toString(typed) + " then "
                + getSubModality(0).toString(typed) + 
                (hasElseModality ? " else " + getSubModality(1).toString(typed) : "")
                + " end";
    }

}
