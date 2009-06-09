/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.term.creation;

import java.util.HashMap;
import java.util.Map;

import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.BindableIdentifier;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.Modality;
import de.uka.iti.pseudo.term.SchemaModality;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.UnificationException;
import de.uka.iti.pseudo.util.Util;
// TODO DOC
public class TermInstantiator extends RebuildingTermVisitor {

    private Map<String, Term> termMap;
    private Map<String, Modality> modalityMap;
    private TypeUnification typeMapper;
    

    public Map<String, Term> getTermMap() {
        return termMap;
    }

    public Map<String, Modality> getModalityMap() {
        return modalityMap;
    }

    public TypeUnification getTypeMapper() {
        return typeMapper;
    }
    
    public TermInstantiator() {
        this.termMap = new HashMap<String, Term>();
        this.modalityMap = new HashMap<String, Modality>();
        this.typeMapper = new TypeUnification();
    }

    public TermInstantiator(TermUnification termUnification) {
        this.termMap = termUnification.getTermInstantiation();
        this.modalityMap = termUnification.getModalityInstantiation();
        this.typeMapper = termUnification.getTypeUnification();
    }

    public TermInstantiator(RuleApplication ruleApp) {
        this.termMap = ruleApp.getSchemaVariableMapping();
        this.modalityMap = ruleApp.getSchemaModalityMapping();
        this.typeMapper = new TypeUnification(ruleApp.getTypeVariableMapping());
    }

    public Term instantiate(Term toInst) throws TermException {
        toInst.visit(this);
        if(resultingTerm != null)
            return resultingTerm;
        else
            return toInst;
    }
    
    public Modality instantiate(Modality toInst) throws TermException {
        toInst.visit(this);
        if(resultingModality != null)
            return resultingModality;
        else
            return toInst;
    }
    
    @Override
    protected Type modifyType(Type type) throws TermException {
        if(typeMapper != null)
            return typeMapper.instantiate(type);
        else 
            return type;
    }
    
    @Override
    public void visit(SchemaVariable schemaVariable) throws TermException {
        if(termMap != null) {
            resultingTerm = termMap.get(schemaVariable.getName());
            
            if(resultingTerm != null) {
                Type t1 = modifyType(resultingTerm.getType());
                Type t2 = modifyType(schemaVariable.getType());
                if(!t1.equals(t2))
                    throw new UnificationException("Instantiation failed! Incompatible types", schemaVariable, resultingTerm);
            }
            
        } else {
            resultingTerm = null;
        }
    }
    
    @Override 
    public void visit(SchemaModality schemaModality) throws TermException {
        resultingModality = modalityMap.get(schemaModality.getName());
    }
    
    @Override
    public void visit(Binding binding) throws TermException {
        super.visit(binding);
        if(binding.hasSchemaVariable() && termMap != null) {
            SchemaVariable sv = (SchemaVariable) binding.getVariable(); 
            Term bindingReplacement = termMap.get(sv.getName());
            if(bindingReplacement != null) {
                
                if(!(bindingReplacement instanceof BindableIdentifier)) {
                    throw new UnificationException("Only a variable or schema variable can be instantiated into bindings with schemas", binding, bindingReplacement);
                }
                
                if(resultingTerm != null)
                    binding = (Binding) resultingTerm;
                
                resultingTerm = new Binding(binding.getBinder(),
                        binding.getType(), 
                        (BindableIdentifier)bindingReplacement, 
                        Util.listToArray(binding.getSubterms(), Term.class));
                
            }
        }
    }
    
}
