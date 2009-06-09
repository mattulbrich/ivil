package de.uka.iti.pseudo.rule;

import java.util.List;
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

    public boolean applyTo(TermUnification mc, RuleApplication ruleApp,
            ProofNode goal, Environment env, Properties properties, boolean commit) throws RuleException {
        return whereCondition.applyTo(arguments, mc, ruleApp, goal, env, properties, commit);
    }
    
    public List<Term> getArguments() {
        return Util.readOnlyArrayList(arguments);
    }

    public WhereCondition getWhereCondition() {
        return whereCondition;
    }

    public void verify(TermInstantiator termInst, Properties properties) throws RuleException {
        Term[] actual;
        try {
            actual = new Term[arguments.length];
            for (int i = 0; i < actual.length; i++) {
                actual[i] = termInst.instantiate(arguments[i]);
            }
        } catch (TermException e) {
            throw new RuleException("Exception during instantiation", e);
        }
        
        whereCondition.verify(arguments, actual, properties);
    }

}
