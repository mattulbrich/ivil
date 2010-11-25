package de.uka.iti.pseudo.rule.where;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermComparator;

//TODO DOC
public class UnorderedTerms extends WhereCondition {

    public UnorderedTerms() {
        super("unorderedTerms");
    }
    
    private TermComparator termComparator;
    
    @Override
    public boolean check(Term[] formalArguments, Term[] actualArguments,
            RuleApplication ruleApp, ProofNode goal, Environment env)
            throws RuleException {
        
        if(termComparator == null) {
            termComparator = new TermComparator(env);
        }
        
        assert termComparator.getEnvironment() == env;
        
        return termComparator.compare(actualArguments[0], actualArguments[1]) < 0;
        
    }

    @Override
    public void checkSyntax(Term[] arguments) throws RuleException {
        if(arguments.length != 2)
            throw new RuleException("orderedTerms expects exactly 2 arguments");
    }

}
