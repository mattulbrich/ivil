/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.proof;

import java.util.List;
import java.util.Map;

import nonnull.NonNull;

import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.Update;
import de.uka.iti.pseudo.util.LinearLookupMap;
import de.uka.iti.pseudo.util.Util;

/**
 * The Class ImmutableRuleApplication is the canonical unmodifiable
 * implementation of the interface {@link RuleApplication}.
 * 
 * <p>Its only constructor accepts another {@link RuleApplication} object and
 * creates a 'snapshot' of its values.
 *
 * <p>
 * All values given to the constructor cannot be changed afterwards. The list of
 * assume selectors is copied into a new array to enforce this. The lookup maps
 * are stored in {@link LinearLookupMap}s which are space optimised
 * implementation of the {@link Map} interface. Their lookup time is bad (O(n))
 * but this does not matter because typical maps are rather short.
 *
 * @author mattias ulbrich
 */
public class ImmutableRuleApplication implements RuleApplication {

    private Rule rule;
    private ProofNode proofNode;
    private TermSelector findSelector;
    private TermSelector[] assumeSelectors;
    private Map<String,Term> schemaVariableMap;
    private Map<String,Update> schemaUpdateMap;
    private Map<String,String> properties;
    private Map<String,Type> typeVariableMap;

    public ImmutableRuleApplication(@NonNull RuleApplication ruleApp) {
        rule = ruleApp.getRule();
        proofNode = ruleApp.getProofNode();
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

    public ProofNode getProofNode() {
        return proofNode;
    }

    public TermSelector getFindSelector() {
        return findSelector;
    }

    public List<TermSelector> getAssumeSelectors() {
        return Util.readOnlyArrayList(assumeSelectors);
    }

    // this is used by the GUI
    @Override
    public String toString() {
        return "Apply " + rule.getName();
    }

    @Override
    public Map<String, Term> getSchemaVariableMapping() {
        return schemaVariableMap;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public Map<String, Type> getTypeVariableMapping() {
        return typeVariableMap;
    }

    @Override
    public boolean hasMutableProperties() {
        return false;
    }

    @Override
    public Map<String, Update> getSchemaUpdateMapping() {
        return schemaUpdateMap;
    }
}