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
package de.uka.iti.pseudo.term.creation;

import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.SchemaProgramTerm;
import de.uka.iti.pseudo.term.SchemaType;
import de.uka.iti.pseudo.term.SchemaUpdateTerm;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVisitor;
import de.uka.iti.pseudo.term.UnificationException;
import de.uka.iti.pseudo.term.Update;
import de.uka.iti.pseudo.term.UpdateTerm;
import de.uka.iti.pseudo.term.statement.Assignment;
import de.uka.iti.pseudo.util.Util;

// Instantiation is not applied on the instantiated terms or types.
@SuppressWarnings({"nullness"})
public class TermInstantiator extends RebuildingTermVisitor {

    private Map<String, Term> termMap;
    private Map<String, Type> typeMap;
    private Map<String, Update> updateMap;

    public TermInstantiator(Map<String, Term> termMap,
            Map<String, Type> typeMap, Map<String, Update> updateMap) {
    super();
        this.termMap = termMap;
        this.typeMap = typeMap;
        this.updateMap = updateMap;
    }

    public Term instantiate(Term toInst) throws TermException {
        toInst.visit(this);
        if(resultingTerm != null)
            return resultingTerm;
        else
            return toInst;
    }

    private boolean typesInstantiated;
    
    private TypeVisitor<Type, Void> typeInstantiator = new RebuildingTypeVisitor<Void>() {
        public Type visit(SchemaType stv, Void arg) throws TermException {
            Type t = typeMap.get(stv.getVariableName());
            if(t == null)
                return stv;
            else {
                typesInstantiated = true;
                return t;
            }
        }
    };
    
    public Type instantiate(Type type) throws TermException {
        return modifyType(type);
    }
    
    @Override
    protected Type modifyType(Type type) throws TermException {
        if(typeMap != null) {
            typesInstantiated = false;
            Type newType = type.accept(typeInstantiator, null);
            if(typesInstantiated)
                type = newType;
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
    
    public void visit(SchemaUpdateTerm schemaUpdateTerm)
            throws TermException {
        
        String schemaIdentifier = schemaUpdateTerm.getSchemaIdentifier();
        
        schemaUpdateTerm.getSubterm(0).visit(this);
        
        if(resultingTerm == null) 
        {
            Update resultingUpdate = updateMap.get(schemaIdentifier);
            if(resultingUpdate != null) {
                resultingTerm = new UpdateTerm(resultingUpdate, schemaUpdateTerm.getSubterm(0));
            }
        } else {
            Update resultingUpdate = updateMap.get(schemaIdentifier);
            if(resultingUpdate == null) {
                resultingTerm = new SchemaUpdateTerm(schemaIdentifier, resultingTerm);
            } else {
                resultingTerm = new UpdateTerm(resultingUpdate, resultingTerm);
            }
        }
    }
    
    public void visit(SchemaProgramTerm schemaProgramTerm) throws TermException {
        // see above
        super.visit(schemaProgramTerm);
        
        if(termMap != null) {
            SchemaVariable schemaVariable = schemaProgramTerm.getSchemaVariable();
            Term t  = termMap.get(schemaVariable.getName());
            LiteralProgramTerm progTerm = (LiteralProgramTerm) t;
            
            checkSchemaProgramInstantiation(schemaProgramTerm, progTerm);
            resultingTerm = progTerm;
        }

    }

    /**
     * @param schema
     * @param prog
     * @throws TermException 
     */
    protected void checkSchemaProgramInstantiation(
            SchemaProgramTerm schema, LiteralProgramTerm prog)
            throws TermException {
        if(prog.isTerminating() != schema.isTerminating())
            throw new UnificationException("Instantiation failed! Termination incompatible", 
                    schema, prog);
    }
    
    /*
     * we need to handle this separately since the bound variable may be instantiated. 
     */
    protected void visitBindingVariable(Binding binding)
            throws TermException {
        binding.getVariable().visit(this);
    }
    
    /*
     * we need to handle this separately since the element to which sth is assigned 
     * may be instantiated too. 
     */
    public void visit(UpdateTerm updateTerm) throws TermException {

        updateTerm.getSubterm(0).visit(this);
        Term innerResult = resultingTerm != null ? 
                resultingTerm : updateTerm.getSubterm(0);
        
        Assignment newAssignments[] = null;
        List<Assignment> assignments = updateTerm.getAssignments();
        
        for(int i = 0; i < assignments.size(); i++) {
            Assignment assignment = assignments.get(i);
            
            assignment.getTarget().visit(this);
            Term tgt = resultingTerm;
            
            assignment.getValue().visit(this);
            Term val = resultingTerm;
            
            if(tgt != null || val != null) {
                // restore target if visitation returned null
                if(tgt == null)
                    tgt = assignment.getTarget();
                
                // restore value if visitation returned null
                if(val == null)
                    val = assignment.getValue();
                
                if(newAssignments == null) {
                    newAssignments = Util.listToArray(assignments, Assignment.class);
                }
                newAssignments[i] = new Assignment(tgt, val);
            }
        }
        
        if(newAssignments != null) {
            resultingTerm = new UpdateTerm(new Update(newAssignments), innerResult);
        } else if(innerResult != updateTerm.getSubterm(0)) {
            newAssignments = Util.listToArray(assignments, Assignment.class);
            resultingTerm = new UpdateTerm(new Update(newAssignments), innerResult);
        } else {
            resultingTerm = null;
        }
    }
    
    @Override
    public String toString() {
        return "TermInstantiator[terms=" + termMap + "; types=" + typeMap
                + "; updates=" + updateMap + "]";
    }
}
