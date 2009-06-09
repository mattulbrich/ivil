package de.uka.iti.pseudo.rule.where;

import java.util.Properties;

import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.creation.RebuildingTermVisitor;
import de.uka.iti.pseudo.term.creation.TermUnification;

// TODO Documentation needed
public class Subst extends SimpleWhereCondition {

    public Subst() {
        super("subst");
    }

    @Override
    public boolean applyTo(Term arguments[],
            TermUnification mc) throws RuleException {
        
        // ensured by checkSyntax
        assert arguments[0] instanceof SchemaVariable;
        
        SchemaVariable sv;
        Term toReplace;
        Term replaceWith;
        Term replaceIn;
        try {
            sv = (SchemaVariable) arguments[0];
            toReplace = mc.instantiate(arguments[1]);
            replaceWith = mc.instantiate(arguments[2]);
            replaceIn = mc.instantiate(arguments[3]);
        } catch (TermException e1) {
            throw new RuleException("Exception during instantiations", e1);
        }
        
        TermReplacer tr = new TermReplacer();
        Term result;
        try {
            result = tr.replace(toReplace, replaceWith, replaceIn);
        } catch (TermException e) {
            throw new RuleException("Cannot substitute", e);
        }
        
        if(!mc.leftUnify(sv, result))
            throw new RuleException("Schema variable already instantiated differently");
        
        return true;
    }
    
    @Override 
    public void verify(Term[] formalArguments,
            Term[] actualArguments, Properties properties) throws RuleException {
       Term toReplace = actualArguments[1];
       Term replaceWith = actualArguments[2];
       Term replaceIn = actualArguments[3];
       
       TermReplacer tr = new TermReplacer();
       Term result;
       try {
           result = tr.replace(toReplace, replaceWith, replaceIn);
       } catch (TermException e) {
           throw new RuleException("Cannot substitute", e);
       }
       
       if(!result.equals(actualArguments[0])) {
           throw new RuleException("Unexpected substitution result\nExpected " +
                   actualArguments[0] + "\nbut got: " + result);
       }
    }


    @Override 
    public void checkSyntax(Term[] arguments) throws RuleException {
        if(arguments.length != 4)
            throw new RuleException("subst expects exactly 4 arguments");
        
        if(!(arguments[0] instanceof SchemaVariable))
            throw new RuleException("subst expects schema varible as first argument");
        
        if(!arguments[0].getType().equals(arguments[3].getType()))
            throw new RuleException("In subst first and last (result and initial) need to have same type");
        
        if(!arguments[1].getType().equals(arguments[2].getType()))
            throw new RuleException("In subst second and third (replace and replacewith) need to have same type");
    }
    
    private static class TermReplacer extends RebuildingTermVisitor {
        
        private Term termToReplace;
        private Term replaceWith;
        
        @Override 
        protected void defaultVisitTerm(Term term)
                throws TermException {
            if(term.equals(termToReplace)) {
                resultingTerm = replaceWith;
            }
        }
        
        @Override public void visit(Binding binding) throws TermException {
            // TODO method documentation
            // TODO make test case for that
            if(binding.getVariable().equals(termToReplace))
                resultingTerm = null;
            else
                super.visit(binding);
        }
        
        Term replace(Term termToReplace, Term replaceWith, Term replaceIn) throws TermException {
            this.termToReplace = termToReplace;
            this.replaceWith = replaceWith;
            replaceIn.visit(this);
            return resultingTerm == null ? replaceIn : resultingTerm;
        }
        
    }


}
