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
import java.util.List;
import java.util.Map;

import nonnull.NonNull;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.BindableIdentifier;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.UnificationException;
import de.uka.iti.pseudo.term.UpdateTerm;
import de.uka.iti.pseudo.term.statement.AssignmentStatement;
import de.uka.iti.pseudo.util.Util;
// TODO DOC
public class TermInstantiator extends RebuildingTermVisitor {

    private Map<String, Term> termMap;
    private Map<String, Type> typeMap;

    public TermInstantiator(Map<String, Term> termMap, Map<String, Type> typeMap) {
        super();
        this.termMap = termMap;
        this.typeMap = typeMap;
    }

    public Term instantiate(Term toInst) throws TermException {
        toInst.visit(this);
        if(resultingTerm != null)
            return resultingTerm;
        else
            return toInst;
    }
    
    /**
     * Replace schema variables and modalities in a string.
     * 
     * They are printed using the pretty printer
     * 
     * For instance <code>Assume %c in &a</code> might become
     * <code>Assume x=3 in y:=2</code>
     * 
     * TODO use pretty printer
     * 
     * @param string the string to instantiate
     * 
     * @param env Environment to use for pretty printing
     * 
     * @return the string with subterms replaced
     */
    public @NonNull String replaceInString(@NonNull String string) {
        
        StringBuilder retval = new StringBuilder();
        StringBuilder curley = new StringBuilder();
        
        int len = string.length();
        
        boolean inCurley = false;
        for (int i = 0; i < len; i++) {
            char c = string.charAt(i);
            switch(c) {
            case '{':
                inCurley = true;
                break;
                
            case '}':
                String lookup = curley.toString();
                Object o = null;
                switch(lookup.charAt(0)) {
                case '%': o = termMap.get(lookup); break;
                }
                retval.append(o == null ? "??" : o);
                inCurley = false;
                curley.setLength(0);
                break;
                
            default:
                if(inCurley)
                    curley.append(c);
                else
                    retval.append(c);
            }
        }
        
        return retval.toString();
    }
    
    @Override
    protected Type modifyType(Type type) throws TermException {
        if(typeMap != null && type instanceof TypeVariable) {
            String typeString = ((TypeVariable)type).getVariableName();
            if(typeMap.containsKey(typeString))
                return typeMap.get(typeString);
        }

        return type;
    }
    
    @Override
    public void visit(SchemaVariable schemaVariable) throws TermException {
        // the schema variable might have to be retyped
        super.visit(schemaVariable);
        
        if(termMap != null) {
            Term t  = termMap.get(schemaVariable.getName());
            
            if(t != null) {
                Type t1 = modifyType(t.getType());
                Type t2 = modifyType(schemaVariable.getType());
                if(!t1.equals(t2))
                    throw new UnificationException("Instantiation failed! Incompatible types", t1, t2);
                resultingTerm = t;
            }
            
        } else {
            resultingTerm = null;
        }
    }
    
    /*
     * we need to handle this separately since the bound variable may be instantiated. 
     */
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
    
    /*
     * we need to handle this separately since the element to which sth is assigned 
     * may be instantiated too. 
     */
    public void visit(UpdateTerm updateTerm) throws TermException {

        updateTerm.getSubterm(0).visit(this);
        Term innerResult = resultingTerm != null ? 
                resultingTerm : updateTerm.getSubterm(0);
        
        AssignmentStatement newAssignments[] = null;
        List<AssignmentStatement> assignments = updateTerm.getAssignments();
        
        for(int i = 0; i < assignments.size(); i++) {
            
            assignments.get(i).getTarget().visit(this);
            Term tgt = resultingTerm;
            
            assignments.get(i).getValue().visit(this);
            Term val = resultingTerm;
            
            if(tgt != null || val != null) {
                if(newAssignments == null) {
                    newAssignments = Util.listToArray(assignments, AssignmentStatement.class);
                }
                newAssignments[i] = new AssignmentStatement(tgt, val);
            }
        }
        
        if(newAssignments != null) {
            resultingTerm = new UpdateTerm(newAssignments, updateTerm.getSubterm(0));
        } else if(innerResult != updateTerm.getSubterm(0)) {
            newAssignments = Util.listToArray(assignments, AssignmentStatement.class);
            resultingTerm = new UpdateTerm(newAssignments, updateTerm.getSubterm(0));
        } else {
            resultingTerm = null;
        }
    }
}
