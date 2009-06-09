package de.uka.iti.pseudo.term;

public class WhileModality extends Modality {

    private Term conditionTerm;

    public WhileModality(Term conditionTerm, Modality body) {
        super(body);
        this.conditionTerm = conditionTerm;
    }

    @Override
    public String toString(boolean typed) {
        return "while " + conditionTerm.toString(typed) + " do "
                + getSubModality(0).toString(typed) + " end";
    }

    @Override
    public void visit(ModalityVisitor visitor) throws TermException {
        visitor.visit(this);
    }
    
    public Term getConditionTerm() {
        return conditionTerm;
    }
    
    public Modality getBody() {
        return getSubModality(0);
    }

}
