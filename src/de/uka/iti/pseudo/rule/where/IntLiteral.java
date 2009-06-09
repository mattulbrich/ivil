package de.uka.iti.pseudo.rule.where;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.NumberLiteral;
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Term;

// TODO Documentation needed
public class IntLiteral extends WhereCondition {

    public IntLiteral() {
        super("intLiteral");
        // TODO Auto-generated constructor stub
    }

    @Override public boolean check(Term[] formalArguments,
            Term[] actualArguments, RuleApplication ruleApp, ProofNode goal,
            Environment env) throws RuleException {
        
        Term t = actualArguments[0];
        if (t instanceof Application) {
            Application app = (Application) t;
            if (app.getFunction() instanceof NumberLiteral) {
                return true;
            }
        }
        
        return false;
            
    }

    @Override public void checkSyntax(Term[] arguments) throws RuleException {
        if(arguments.length != 1)
            throw new RuleException("intLiteral expects exactly 1 argument");
    }

}
