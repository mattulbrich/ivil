package de.uka.iti.pseudo.rule;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.creation.TermInstantiator;
import de.uka.iti.pseudo.term.creation.TermUnification;
import de.uka.iti.pseudo.util.Util;

// TODO DOC l8er

public class WhereClause {
    
    private WhereCondition whereCondition;
    private Term[] arguments;

    public WhereClause(WhereCondition where, Term[] terms) throws RuleException {
        this.arguments = terms;
        this.whereCondition = where;
        
        where.checkSyntax(arguments);
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(whereCondition.getName()).append(" ").append(Util.commatize(getArguments()));
        return sb.toString();
    }

    public boolean applyTo(TermInstantiator inst, RuleApplication ruleApp,
            ProofNode goal, Environment env) throws RuleException {
        Term actualArgs[];
        try {
            actualArgs = new Term[arguments.length];
            for (int i = 0; i < actualArgs.length; i++) {
                actualArgs[i] = inst.instantiate(arguments[i]);
            }
            
        } catch (TermException e) {
            throw new RuleException("Exception during instantiation", e);
        }
        
        return whereCondition.check(arguments, actualArgs, ruleApp, goal, env);
    }
    
    public List<Term> getArguments() {
        return Util.readOnlyArrayList(arguments);
    }

    public WhereCondition getWhereCondition() {
        return whereCondition;
    }

}
