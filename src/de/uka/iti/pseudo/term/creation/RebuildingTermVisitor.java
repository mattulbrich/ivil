package de.uka.iti.pseudo.term.creation;

import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.AssignModality;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.CompoundModality;
import de.uka.iti.pseudo.term.IfModality;
import de.uka.iti.pseudo.term.Modality;
import de.uka.iti.pseudo.term.ModalityTerm;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.Variable;
import de.uka.iti.pseudo.term.WhileModality;
import de.uka.iti.pseudo.util.Util;

// TODO DOC

public class RebuildingTermVisitor extends DefaultTermVisitor {

    protected Modality resultingModality;
    protected Term resultingTerm;
    
    @Override 
    protected void defaultVisitModality(Modality modality)
            throws TermException {
        resultingModality = null;
    }
    
    @Override 
    protected void defaultVisitTerm(Term term) throws TermException {
        resultingTerm = null;
    }
    
    protected Type modifyType(Type type) throws TermException {
        return type;
    }
    
    @Override
    public void visit(Variable variable) throws TermException {
        defaultVisitTerm(variable);
        if(resultingTerm == null) {
            Type varType = variable.getType();
            Type type = modifyType(varType);
            if(!type.equals(varType))
                resultingTerm = new Variable(variable.getName(), type);
        }
    }
    
    @Override
    public void visit(SchemaVariable schemaVariable) throws TermException {
        defaultVisitTerm(schemaVariable);
        if(resultingTerm == null) {
            Type varType = schemaVariable.getType();
            Type type = modifyType(varType);
            if(!type.equals(varType))
                resultingTerm = new SchemaVariable(schemaVariable.getName(), type);
        }
    }
    
    @Override
    public void visit(ModalityTerm modalityTerm) throws TermException {
        defaultVisitTerm(modalityTerm);
        if(resultingTerm == null) {
            modalityTerm.getModality().visit(this);
            modalityTerm.getSubterm().visit(this);
            if(resultingModality != null || resultingTerm != null) {
                Modality m = resultingModality == null ? modalityTerm.getModality() : resultingModality;
                Term t = resultingTerm == null ? modalityTerm.getSubterm() : resultingTerm;
                resultingTerm = new ModalityTerm(m,t);
                resultingModality = null;
            }
        }

    }

    @Override
    public void visit(Binding binding) throws TermException {
        defaultVisitTerm(binding);
        if(resultingTerm == null) {
            Term[] args = null;
            for(int i = 0; i < binding.countSubterms(); i++) {
                binding.getSubterm(i).visit(this);
                if(resultingTerm != null) {
                    if(args == null) {
                        args = Util.listToArray(binding.getSubterms(), Term.class);
                    }
                    args[i] = resultingTerm;
                }
            }
            if(args != null) {
                resultingTerm = new Binding(binding.getBinder(), modifyType(binding.getType()),
                        modifyType(binding.getVariableType()), binding.getVariableName(),
                        args);
            } else {
                resultingTerm = null;
            }
        }
    }

    @Override
    public void visit(Application application) throws TermException {
        defaultVisitTerm(application);
        if(resultingTerm == null) {
            Term[] args = null;
            for(int i = 0; i < application.countSubterms(); i++) {
                application.getSubterm(i).visit(this);
                if(resultingTerm != null) {
                    if(args == null) {
                        args = Util.listToArray(application.getSubterms(), Term.class);
                    }
                    args[i] = resultingTerm;
                }
            }
            if(args != null) {
                resultingTerm = new Application(application.getFunction(), 
                        modifyType(application.getType()), args);
            } else {
                resultingTerm = null;
            }
        }
    }

    @Override 
    public void visit(AssignModality assignModality) throws TermException {
        defaultVisitModality(assignModality);
        if(resultingModality == null) {
            defaultVisitTerm(assignModality.getAssignedTerm());
            if(resultingTerm != null) {
                resultingModality = new AssignModality(assignModality.getAssignedConstant(), resultingTerm);
                resultingTerm = null;
            }
        }
    }

    @Override
    public void visit(CompoundModality compoundModality) throws TermException {
        defaultVisitModality(compoundModality);
        if(resultingModality == null) {
            Modality subMod1 = compoundModality.getSubModality(0);
            subMod1.visit(this);
            Modality m1 = resultingModality;
            Modality subMod2 = compoundModality.getSubModality(1);
            subMod2.visit(this);
            Modality m2 = resultingModality;

            if(m1 != null || m2 != null) {
                m1 = m1 == null ? subMod1 : m1;
                m2 = m2 == null ? subMod2 : m2;
                resultingModality = new CompoundModality(m1, m2);
            }
        }
        // else resultingModality = null is implied
    }

    @Override
    public void visit(IfModality ifModality) throws TermException {
        defaultVisitModality(ifModality);
        if(resultingModality == null) {
            defaultVisitTerm(ifModality.getConditionTerm());
            Term c = resultingTerm;
            ifModality.getThenModality().visit(this);
            Modality m1 = resultingModality;

            Modality elseModality = ifModality.getElseModality();
            Modality m2 = null;
            if(elseModality != null) {
                elseModality.visit(this);
                m2 = resultingModality;
            }

            if(c != null || m1 != null || m2 != null) {
                c = c == null ? ifModality.getConditionTerm() : c;
                m1 = m1 == null ? ifModality.getThenModality() : m1;
                m2 = m2 == null ? elseModality : m2;

                resultingModality = new IfModality(c, m1, m2);
            } 

            resultingTerm = null;
        }
    }

    @Override
    public void visit(WhileModality whileModality) throws TermException {
        defaultVisitModality(whileModality);
        if(resultingModality == null) {
            defaultVisitTerm(whileModality.getConditionTerm());
            Term c = resultingTerm;
            whileModality.getBody().visit(this);
            Modality b = resultingModality;

            if(c != null || b != null) {
                c = c == null ? whileModality.getConditionTerm() : c;
                b = b == null ? whileModality.getBody() : b;

                resultingModality = new WhileModality(c, b);
            } 

            resultingTerm = null;
        }
    }
}
