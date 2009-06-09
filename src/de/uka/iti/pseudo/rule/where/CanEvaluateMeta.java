package de.uka.iti.pseudo.rule.where;

import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.rule.meta.MetaEvaluator;
import de.uka.iti.pseudo.term.Modality;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;

// TODO Documentation needed
public class CanEvaluateMeta extends WhereCondition {

    public CanEvaluateMeta() {
        super("canEval");
    }

    @Override 
    public boolean check(Term[] formalArguments,
            Term[] actualArguments, RuleApplication ruleApp, ProofNode goal,
            Environment env) throws RuleException {
        
        MetaEvaluator metaEval = new MetaEvaluator(new ProtectedRuleApplication(ruleApp), env);
        
        try {
            actualArguments[0].visit(metaEval);
            return true;
        } catch (TermException e) {
            // I cannot apply the meta evaluator --> say no
            return false;
        }
    }

    @Override public void checkSyntax(Term[] arguments) throws RuleException {
        if(arguments.length != 1)
            throw new RuleException("canEval expects exactly one argument");
    }
    
    /**
     * This class wraps a rule application and makes its properties immutable. 
     */
    private static class ProtectedRuleApplication implements RuleApplication {
        private RuleApplication wrappedApplication;
        
        public ProtectedRuleApplication(RuleApplication wrappedApplication) {
            this.wrappedApplication = wrappedApplication;
        }

        public List<TermSelector> getAssumeSelectors() {
            return wrappedApplication.getAssumeSelectors();
        }

        public TermSelector getFindSelector() {
            return wrappedApplication.getFindSelector();
        }

        public int getGoalNumber() {
            return wrappedApplication.getGoalNumber();
        }

        public Map<String, String> getProperties() {
            return wrappedApplication.getProperties();
        }

        public Rule getRule() {
            return wrappedApplication.getRule();
        }

        public Map<String, Modality> getSchemaModalityMapping() {
            return wrappedApplication.getSchemaModalityMapping();
        }

        public Map<String, Term> getSchemaVariableMapping() {
            return wrappedApplication.getSchemaVariableMapping();
        }

        public Map<String, Type> getTypeVariableMapping() {
            return wrappedApplication.getTypeVariableMapping();
        }

        public boolean hasMutableProperties() {
            return false;
        }
        
    }
}
