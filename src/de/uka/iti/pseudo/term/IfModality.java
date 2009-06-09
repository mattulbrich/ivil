package de.uka.iti.pseudo.term;

public class IfModality extends Modality {

    private Term conditionTerm;

    public IfModality(Term condTerm, Modality thenMod, Modality elseMod) {
        super(thenMod, elseMod);
        this.conditionTerm = condTerm;
    }

    @Override public String toString(boolean typed) {
        return "if " + conditionTerm.toString(typed) + " then "
                + getSubModality(0).toString(typed) + " else "
                + getSubModality(0).toString(typed) + " end";
    }

}
