package de.uka.iti.pseudo.term;

import nonnull.Nullable;

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

    @Override 
    public String toString(boolean typed) {
        return "if " + conditionTerm.toString(typed) + " then "
                + getSubModality(0).toString(typed) + 
                (hasElseModality ? " else " + getSubModality(1).toString(typed) : "")
                + " end";
    }

    @Override 
    public void visit(ModalityVisitor visitor) throws TermException {
        visitor.visit(this);
    }

    public Term getConditionTerm() {
        return conditionTerm;
    }
    
    public Modality getThenModality() {
        return getSubModality(0);
    }
    
    public @Nullable Modality getElseModality() {
        if(countModalities() > 1)
            return getSubModality(1);
        else
            return null;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof IfModality) {
            IfModality ifmod = (IfModality) object;
            
            if(hasElseModality != ifmod.hasElseModality)
                return false;
            
            if(!getConditionTerm().equals(ifmod.getConditionTerm()))
                return false;
            
            if(!getThenModality().equals(ifmod.getThenModality()))
                return false;
            
            if(hasElseModality && !getElseModality().equals(ifmod.getElseModality()))
                return false;
            
            return true;
        }
        return false;
    }

}
