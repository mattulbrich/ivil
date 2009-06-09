package de.uka.iti.pseudo.proof;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Modality;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.SchemaCollectorVisitor;
import de.uka.iti.pseudo.util.Util;

// TODO DOC

public class ImmutableRuleApplication implements RuleApplication {

    private Rule rule;
    private int goalNumber;
    private TermSelector findSelector;
    private TermSelector[] assumeSelectors;
    private String[] schemaVariables;
    private Term[] termInstantiations;

    private String[] schemaModalities;
    private Modality[] modalityInstantiations;

    private Map<String, String> whereProperties;
    
    public ImmutableRuleApplication(RuleApplication ruleApp) {
        rule = ruleApp.getRule();
        goalNumber = ruleApp.getGoalNumber();
        findSelector = ruleApp.getFindSelector();
        assumeSelectors = Util.listToArray(ruleApp.getAssumeSelectors(), TermSelector.class);
        
        schemaVariables = Util.listToArray(ruleApp.getSchemaVariableNames(), String.class);
        termInstantiations = new Term[schemaVariables.length];
        for (int i = 0; i < termInstantiations.length; i++) {
            termInstantiations[i] = ruleApp.getTermInstantiation(schemaVariables[i]);
        }
        
        schemaModalities = Util.listToArray(ruleApp.getSchemaModalityNames(), String.class);
        modalityInstantiations = new Modality[schemaModalities.length];
        for (int i = 0; i < modalityInstantiations.length; i++) {
            modalityInstantiations[i] = ruleApp.getModalityInstantiation(schemaModalities[i]);
        }
        
        
        // Where properties are rather seldom, we can afford the space for real hashes therefore.
        Collection<String> wp = ruleApp.getWherePropertyNames();
        if(!wp.isEmpty()) {
            whereProperties = new HashMap<String, String>();
            for (String string : wp) {
                whereProperties.put(string, ruleApp.getWhereProperty(string));
            }
        } else {
            whereProperties = null;
        }
    }

    @Deprecated
    public ImmutableRuleApplication(@NonNull Rule rule, int goalNumber,
            @NonNull TermSelector findSelector,
            @Nullable TermSelector[] assumeSelectors,
            @Nullable Map<String, Term> termMapping,
            @Nullable Map<String, Modality> modalityMapping) {
        super();
        this.rule = rule;
        this.goalNumber = goalNumber;
        this.findSelector = findSelector;
        this.assumeSelectors = assumeSelectors;

        SchemaCollectorVisitor scv = new SchemaCollectorVisitor();
        scv.collect(rule);

        schemaVariables = Util.listToArray(termMapping.keySet(), String.class);
        termInstantiations = new Term[schemaVariables.length];
        for (int i = 0; i < termInstantiations.length; i++) {
            termInstantiations[i] = termMapping.get(schemaVariables[i]);
        }

        schemaModalities = Util.listToArray(modalityMapping.keySet(),
                String.class);
        modalityInstantiations = new Modality[schemaModalities.length];
        for (int i = 0; i < modalityInstantiations.length; i++) {
            modalityInstantiations[i] = modalityMapping
                    .get(schemaModalities[i]);
        }

        // TODO check all this ... nö
    }

    public Rule getRule() {
        return rule;
    }

    public int getGoalNumber() {
        return goalNumber;
    }

    public TermSelector getFindSelector() {
        return findSelector;
    }

    public List<TermSelector> getAssumeSelectors() {
        return Util.readOnlyArrayList(assumeSelectors);
    }

    // this is used by the GUI
    @Override public String toString() {
        return "Apply " + rule.getName();
    }

    public String getWhereProperty(String key) {
        if (whereProperties == null)
            return null;
        else
            return whereProperties.get(key);
    }

    public Collection<String> getWherePropertyNames() {
        if(whereProperties == null)
            return Collections.emptySet();
        else
            return whereProperties.keySet();
    }

    public Modality getModalityInstantiation(String schemaModalityName) {

        for (int i = 0; i < schemaModalities.length; i++) {
            if (schemaModalities[i].equals(schemaModalityName))
                return modalityInstantiations[i];
        }
        return null;
    }

    public Collection<String> getSchemaModalityNames() {
        return Util.readOnlyArrayList(schemaModalities);
    }

    public Collection<String> getSchemaVariableNames() {
        return Util.readOnlyArrayList(schemaVariables);
    }

    public Term getTermInstantiation(String schemaVariableName) {
        for (int i = 0; i < schemaVariables.length; i++) {
            if (schemaVariables[i].equals(schemaVariableName))
                return termInstantiations[i];
        }
        return null;
    }
}
