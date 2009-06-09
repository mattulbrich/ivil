package de.uka.iti.pseudo.term.creation;

import de.uka.iti.pseudo.term.Modality;
import de.uka.iti.pseudo.term.SchemaModality;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
// TODO DOC
public class TermInstantiator extends RebuildingTermVisitor {

    private TermUnification termUnification;

    public TermInstantiator(TermUnification termUnification) {
        this.termUnification = termUnification;
    }

    public Term instantiate(Term toInst) throws TermException {
        toInst.visit(this);
        if(resultingTerm != null)
            return resultingTerm;
        else
            return toInst;
    }
    
    public Modality instantiate(Modality toInst) throws TermException {
        toInst.visit(this);
        if(resultingModality != null)
            return resultingModality;
        else
            return toInst;
    }
    
    @Override
    protected Type modifyType(Type type) throws TermException {
        return termUnification.instantiateType(type);
    }
    
    @Override
    public void visit(SchemaVariable schemaVariable) throws TermException {
        resultingTerm = termUnification.getTermFor(schemaVariable);
    }
    
    @Override 
    public void visit(SchemaModality schemaModality) throws TermException {
        resultingModality = termUnification.getModalityFor(schemaModality);
    }

}
