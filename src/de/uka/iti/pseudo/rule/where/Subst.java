package de.uka.iti.pseudo.rule.where;

import java.util.List;
import java.util.Properties;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.ImmutableRuleApplication;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.rule.WhereClause;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.creation.RebuildingTermVisitor;
import de.uka.iti.pseudo.term.creation.TermUnification;

// TODO Documentation needed
public class Subst extends WhereCondition {

    public Subst() {
        super("subst");
    }

    @Override
    public boolean applyTo(WhereClause whereClause,
            TermUnification mc) throws RuleException {
        List<Term> arguments = whereClause.getArguments();
        
        // ensured by tryApplyTo
        assert arguments.get(0) instanceof SchemaVariable;
        
        SchemaVariable sv = (SchemaVariable) arguments.get(0);
        Term toReplace = arguments.get(1);
        Term replaceWith = arguments.get(2);
        Term replaceIn = arguments.get(3);
        
        TermReplacer tr = new TermReplacer();
        Term result;
        try {
            result = tr.replace(toReplace, replaceWith, replaceIn);
        } catch (TermException e) {
            throw new RuleException("Cannot substitute", e);
        }
        
        if(!mc.leftUnify(sv, result))
            throw new RuleException("Schema variable already instantiated");
        
        return true;
    }

    @Override 
    public void tryToApplyTo(Term[] arguments) throws RuleException {
        if(arguments.length != 4)
            throw new RuleException("newSkolem expects exactly 4 arguments");
        if(!(arguments[0] instanceof SchemaVariable))
            throw new RuleException("newSkolem expects schema varible as first argument");
    }
    
    private static class TermReplacer extends RebuildingTermVisitor {
        
        private Term termToReplace;
        private  Term replaceWith;
        
        @Override 
        protected void defaultVisitTerm(Term term)
                throws TermException {
            if(term.equals(termToReplace)) {
                resultingTerm = replaceWith;
            }
        }
        
        Term replace(Term termToReplace, Term replaceWith, Term replaceIn) throws TermException {
            this.termToReplace = termToReplace;
            this.replaceWith = replaceWith;
            replaceIn.visit(this);
            return resultingTerm == null ? replaceIn : resultingTerm;
        }
        
    }

}
