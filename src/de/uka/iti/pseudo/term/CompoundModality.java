package de.uka.iti.pseudo.term;

public class CompoundModality extends Modality {
    
    public CompoundModality(Modality mod1, Modality mod2) {
        super(mod1, mod2);
    }

    @Override
    public String toString(boolean typed) {
        return getSubModality(0).toString(typed) + "; " + getSubModality(1).toString(typed);
    }

    @Override
    public void visit(ModalityVisitor visitor) throws TermException {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof CompoundModality) {
            CompoundModality com = (CompoundModality) object;
            return com.getSubModality(0).equals(getSubModality(0))
                    && com.getSubModality(1).equals(getSubModality(1));
        }
        return false;
    }

}
