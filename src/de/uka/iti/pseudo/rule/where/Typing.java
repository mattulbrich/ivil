package de.uka.iti.pseudo.rule.where;

import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.UnificationException;
import de.uka.iti.pseudo.term.creation.TermUnification;

public class Typing extends WhereCondition {

    public Typing() {
        super("typing");
    }

    @Override 
    public void tryToApplyTo(Term[] arguments) throws RuleException {
        if(arguments.length != 1)
            throw new RuleException("typing expects exactly 1 argument");
        Term t = arguments[0];
        if(!(t instanceof SchemaVariable)) 
            throw new RuleException("typing expects a schema variable as argument");
    }

    @Override 
    public boolean applyTo(Term[] arguments, TermUnification mc,
            RuleApplication ruleApp, ProofNode goal) throws RuleException {

        // is ok after tryToApplyTo
        SchemaVariable sv = (SchemaVariable) arguments[0];
        
        try {
            mc.getTypeUnification().unify(sv.getType(), mc.instantiate(sv).getType());
            return true;
        } catch (UnificationException e) {
            return false;
        }

    }


}
