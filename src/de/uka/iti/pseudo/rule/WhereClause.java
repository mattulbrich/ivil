package de.uka.iti.pseudo.rule;

import java.util.List;
import java.util.Properties;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.Term;
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
            ProofNode goal, Environment env, Properties properties) throws RuleException {
        return whereCondition.applyTo(this, mc, ruleApp, goal, env, properties);
    }
    
    public boolean canApplyTo(TermUnification mc) throws RuleException {
        return whereCondition.canApplyTo(this, mc);
    }
    
    public List<Term> getArguments() {
        return Util.readOnlyArrayList(arguments);
    }

    public WhereCondition getWhereCondition() {
        return whereCondition;
    }

    public boolean checkApplication(TermInstantiator inst,
            RuleApplication ruleApp, ProofNode proofNode, Environment env,
            Properties whereClauseProperties) throws RuleException {
        // TODO Implement WhereClause.checkApplication
        // TODO method documentation
        return true;
    }

}
