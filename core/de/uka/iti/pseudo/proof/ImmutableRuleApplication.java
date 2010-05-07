/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.proof;

import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.Update;
import de.uka.iti.pseudo.util.LinearLookupMap;
import de.uka.iti.pseudo.util.Util;

// TODO DOC

public class ImmutableRuleApplication implements RuleApplication {

    private Rule rule;
    private int goalNumber;
    private TermSelector findSelector;
    private TermSelector[] assumeSelectors;
    private Map<String,Term> schemaVariableMap;
    private Map<String,Update> schemaUpdateMap;
    private Map<String,String> properties;
    private Map<String,Type> typeVariableMap;
    
    public ImmutableRuleApplication(RuleApplication ruleApp) {
        rule = ruleApp.getRule();
        goalNumber = ruleApp.getGoalNumber();
        findSelector = ruleApp.getFindSelector();
        assumeSelectors = Util.listToArray(ruleApp.getAssumeSelectors(), TermSelector.class);
        
        schemaVariableMap = new LinearLookupMap<String, Term>(ruleApp.getSchemaVariableMapping());
        schemaUpdateMap = new LinearLookupMap<String, Update>(ruleApp.getSchemaUpdateMapping());
        typeVariableMap = new LinearLookupMap<String, Type>(ruleApp.getTypeVariableMapping());
        properties = new LinearLookupMap<String, String>(ruleApp.getProperties());
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

    public Map<String, Term> getSchemaVariableMapping() {
        return schemaVariableMap;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public Map<String, Type> getTypeVariableMapping() {
        return typeVariableMap;
    }
    

    public boolean hasMutableProperties() {
        return false;
    }

    public Map<String, Update> getSchemaUpdateMapping() {
        return schemaUpdateMap;
    }
}
