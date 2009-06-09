package de.uka.iti.pseudo.proof;

import java.util.Collection;
import java.util.List;

import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Modality;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.util.Pair;

// TODO Documentation needed
public interface RuleApplication {

    public Rule getRule();

    public int getGoalNumber();

    public TermSelector getFindSelector();

    public List<TermSelector> getAssumeSelectors();

    public Collection<String> getSchemaVariableNames();

    public Term getTermInstantiation(String schemaVariableName);
    
    public Collection<String> getSchemaModalityNames();

    public Modality getModalityInstantiation(String schemaModalityName);

    public String getWhereProperty(String key);

    public Collection<String> getWherePropertyNames();

}