package de.uka.iti.pseudo.term;

public class CompoundModality extends Modality {
    
    public CompoundModality(Modality mod1, Modality mod2) {
        super(mod1, mod2);
    }

    @Override
    public String toString(boolean typed) {
        return getSubModality(0).toString(typed) + "; " + getSubModality(1).toString(typed);
    }

}
