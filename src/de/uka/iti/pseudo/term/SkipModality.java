package de.uka.iti.pseudo.term;

public class SkipModality extends Modality {

    @Override
    public String toString(boolean typed) {
        return "skip";
    }

    @Override public void visit(ModalityVisitor visitor) throws TermException {
        visitor.visit(this);
    }

}
