package de.uka.iti.pseudo.proof;

import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Modality;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;

// TODO Documentation needed
public interface RuleApplication {

    public Rule getRule();
    
    public boolean isMutable();

    public int getGoalNumber();

    public TermSelector getFindSelector();

    public List<TermSelector> getAssumeSelectors();

    public Map<String, Term> getSchemaVariableMapping();

    public Map<String, Modality> getSchemaModalityMapping();
    
    public Map<String, Type> getTypeVariableMapping();

    public Map<String, String> getProperties();
}