package de.uka.iti.pseudo.term;

public class WhileModality extends Modality {

    private Term conditionTerm;

    public WhileModality(Term conditionTerm, Modality body) {
        super(body);
        this.conditionTerm = conditionTerm;
    }

    @Override public String toString(boolean typed) {
        return "while " + conditionTerm.toString(typed) + " do "
                + getSubModality(0).toString(typed) + " end";
    }

}
