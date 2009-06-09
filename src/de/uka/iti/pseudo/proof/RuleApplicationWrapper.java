package de.uka.iti.pseudo.proof;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Modality;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;

//TODO Documentation needed
@Deprecated
public abstract class RuleApplicationWrapper implements RuleApplication {

    private RuleApplication wrappedRuleApplication;
    private Map<String, Term> schemaVariableMapping = null;

    public RuleApplicationWrapper(RuleApplication wrapped) {
        this.wrappedRuleApplication = wrapped;
    }
    
    public void putSchemaInstantiation(String schemaVarName, Term term) {
        if(schemaVariableMapping == null)
            schemaVariableMapping = new HashMap<String, Term>(getSchemaVariableMapping());
        schemaVariableMapping.put(schemaVarName, term);
    }
    
    

    public List<TermSelector> getAssumeSelectors() {
        return wrappedRuleApplication.getAssumeSelectors();
    }

    public TermSelector getFindSelector() {
        return wrappedRuleApplication.getFindSelector();
    }

    public int getGoalNumber() {
        return wrappedRuleApplication.getGoalNumber();
    }

    public Map<String, String> getProperties() {
        return wrappedRuleApplication.getProperties();
    }

    public Rule getRule() {
        return wrappedRuleApplication.getRule();
    }

    public Map<String, Modality> getSchemaModalityMapping() {
        return wrappedRuleApplication.getSchemaModalityMapping();
    }

    public Map<String, Term> getSchemaVariableMapping() {
        if(schemaVariableMapping == null)
            return wrappedRuleApplication.getSchemaVariableMapping();
        else
            return schemaVariableMapping;
    }

    public Map<String, Type> getTypeVariableMapping() {
        return wrappedRuleApplication.getTypeVariableMapping();
    }

   
}
