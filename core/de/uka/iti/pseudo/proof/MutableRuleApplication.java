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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.Update;

// TODO Documentation needed
public class MutableRuleApplication implements RuleApplication {
    
    private Rule rule;
    private List<TermSelector> assumeSelectors;
    private TermSelector findSelector;
    private int proofNodeNumber;
    private Map<String, String> properties;
    private Map<String, Term> schemaVariableMapping;
    private Map<String, Update> schemaUpdateMapping;
    private Map<String, Type> typeVariableMapping;
    
    public MutableRuleApplication(RuleApplication ruleApp) {
        this.rule = ruleApp.getRule();
        this.assumeSelectors = new ArrayList<TermSelector>(ruleApp.getAssumeSelectors());
        this.findSelector = ruleApp.getFindSelector();
        this.proofNodeNumber = ruleApp.getNodeNumber();
        this.properties = new HashMap<String, String>(ruleApp.getProperties());
        this.schemaVariableMapping = new HashMap<String, Term>(ruleApp.getSchemaVariableMapping());
        this.schemaUpdateMapping = new HashMap<String, Update>(ruleApp.getSchemaUpdateMapping());
        this.typeVariableMapping = new HashMap<String, Type>(ruleApp.getTypeVariableMapping());
    }

    public MutableRuleApplication() {
        this.properties = new HashMap<String, String>();
        this.schemaVariableMapping = new HashMap<String, Term>();
        this.schemaUpdateMapping = new HashMap<String, Update>();
        this.typeVariableMapping = new HashMap<String, Type>();
        this.assumeSelectors = new ArrayList<TermSelector>();
        this.proofNodeNumber = 0;
    }

    public void setFindSelector(TermSelector findSelector) {
        this.findSelector = findSelector;
    }

    public void setNodeNumber(int number) {
        this.proofNodeNumber = number;
    }

    public List<TermSelector> getAssumeSelectors() {
        return assumeSelectors;
    }

    public TermSelector getFindSelector() {
        return findSelector;
    }

    public int getNodeNumber() {
        return proofNodeNumber;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public Map<String, Term> getSchemaVariableMapping() {
        return schemaVariableMapping;
    }

    public Map<String, Update> getSchemaUpdateMapping() {
        return schemaUpdateMapping;
    }
    
    public Map<String, Type> getTypeVariableMapping() {
        return typeVariableMapping;
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public boolean hasMutableProperties() {
        return true;
    }

   

}
