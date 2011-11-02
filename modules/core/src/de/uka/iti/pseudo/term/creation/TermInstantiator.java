/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2011 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term.creation;

import java.util.List;
import java.util.Map;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Modality;
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

    public TermInstantiator(
            @NonNull Map<String, Term> termMap,
            @NonNull Map<String, Type> typeMap, 
            @NonNull Map<String, Update> updateMap) {
        this.termMap = termMap;
        this.typeMap = typeMap;
        this.updateMap = updateMap;
    }

    public @NonNull Term instantiate(Term toInst) throws TermException {
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
        if(!typeMap.isEmpty()) {
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
        // resultingTerm holds retyped variable then
        super.visit(schemaVariable);
        
        Term t  = termMap.get(schemaVariable.getName());
            
        if(t != null) {
            Type t1 = modifyType(t.getType());
            Type t2 = modifyType(schemaVariable.getType());
            if(!t1.equals(t2))
                throw new UnificationException("Instantiation failed! Incompatible types", t1, t2);
            resultingTerm = t;
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
                resultingTerm = UpdateTerm.getInst(resultingUpdate, schemaUpdateTerm.getSubterm(0));
            }
        } else {
            Update resultingUpdate = updateMap.get(schemaIdentifier);
            if(resultingUpdate == null) {
                resultingTerm = SchemaUpdateTerm.getInst(schemaIdentifier, resultingTerm);
            } else {
                resultingTerm = UpdateTerm.getInst(resultingUpdate, resultingTerm);
            }
        }
    }
    
    public void visit(SchemaProgramTerm schemaProgramTerm) throws TermException {
        super.visit(schemaProgramTerm);
        if(resultingTerm != null) {
            // this is guaranteed to be a schema program term here.
            schemaProgramTerm = (SchemaProgramTerm) resultingTerm;
        }
        
        if(termMap != null) {
            SchemaVariable schemaVariable = schemaProgramTerm.getSchemaVariable();
            Term t  = termMap.get(schemaVariable.getName());
            if (!(t instanceof LiteralProgramTerm)) {
                throw new TermException("Tried to instantiate a schema program term " +
                        "with a non-program term: " + t);
            }
            
            LiteralProgramTerm litProgTerm = (LiteralProgramTerm) t;
            
            checkSchemaProgramInstantiation(schemaProgramTerm, litProgTerm);
            
            Program program = litProgTerm.getProgram();
            int index = litProgTerm.getProgramIndex();
            Term suffixTerm = schemaProgramTerm.getSuffixTerm();
            Modality modality = litProgTerm.getModality();

            resultingTerm = LiteralProgramTerm.getInst(index, modality,
                    program, suffixTerm);
        }

    }

    /**
     * Check the instantiation of a schema program term.
     * 
     * In this class, matching statements are not allowed. Subclasses may choose
     * to override this behaviour.
     * 
     * @throws TermException
     *             indicates that the matching term is inappropriate.
     */
    protected void checkSchemaProgramInstantiation(
            SchemaProgramTerm schemaProgramTerm, LiteralProgramTerm litProgTerm) throws TermException {
        
        if(schemaProgramTerm.hasMatchingStatement()) {
            throw new TermException("Tried to instantiate a schema program term " +
                    "with matching statement: " + schemaProgramTerm);
        }
        
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
            resultingTerm = UpdateTerm.getInst(new Update(newAssignments), innerResult);
        } else if(innerResult != updateTerm.getSubterm(0)) {
            newAssignments = Util.listToArray(assignments, Assignment.class);
            resultingTerm = UpdateTerm.getInst(new Update(newAssignments), innerResult);
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
