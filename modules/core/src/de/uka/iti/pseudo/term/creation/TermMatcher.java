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

import java.util.Collections;
import java.util.Map;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.SchemaType;
import de.uka.iti.pseudo.term.SchemaUpdateTerm;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeApplication;
import de.uka.iti.pseudo.term.UnificationException;
import de.uka.iti.pseudo.term.Update;
import de.uka.iti.pseudo.term.UpdateTerm;
import de.uka.iti.pseudo.term.creation.DefaultTermVisitor.DepthTermVisitor;
import de.uka.iti.pseudo.term.statement.AssignmentStatement;
import de.uka.iti.pseudo.util.AppendMap;
import de.uka.iti.pseudo.util.AppendSet;

/**
 * The Class TermUnification is the recording instance of a term matching.
 * 
 * <p>The actual matching is performed in class TermMatcher.
 * 
 * @see TermMatchVisitor
 */
public class TermMatcher implements Cloneable {
    
    /**
     * The type unification is sourced out.
     */
    private AppendMap<String, Type> typeInstantiation = new AppendMap<String, Type>();
    
    /**
     * The mapping from schema variables to their term instantiations.
     * <p> We use {@link AppendMap} here because we often need to clone
     * for unification attempts.
     */
    private AppendMap<String, Term> instantiation = new AppendMap<String, Term>();
    
    /**
     * The mapping from schema updates to their instantiations.
     * <p> We use {@link AppendMap} here because we ne often need to clone
     * for unification attempts.
     */
    private AppendMap<String, Update> updateInst = new AppendMap<String, Update>();
    
    /**
     * The term matcher performs the actual matching comparisons.
     */
    private TermMatchVisitor termMatcher;
    
    /**
     * remember those types which are bound in type quantifications.
     * They must not be instantiated with TypeApplications.
     */
    private AppendSet<SchemaType> boundSchemaTypes = new AppendSet<SchemaType>();

    /**
     * Instantiates a new term unification.
     * 
     * @param env
     *            the environment to look things up.
     */
    public TermMatcher(Environment env) {
        termMatcher = new TermMatchVisitor(this, env);
    }
    
    /**
     * Unify two terms in one direction.
     * The first argument may contain schema variables while the second must not.
     * 
     * @param adaptingTerm
     *            the matching term which may contain schema entities
     * @param fixTerm
     *            the matched term which may <b>not</b> contain schema entities.
     * 
     * @return true, if successful
     */
    public boolean leftMatch(Term adaptingTerm, Term fixTerm) {
        
        AppendMap<String, Term> copyTermInst = instantiation.clone();
        AppendMap<String, Update> copyUpdateInst = updateInst.clone();
        AppendSet<SchemaType> copySchemaBoundTypes = boundSchemaTypes.clone();
        try {
            
            termMatcher.compare(adaptingTerm, fixTerm);
            checkBoundSchemaTypes();
            return true;
            
        } catch (TermException e) {
            instantiation = copyTermInst;
            updateInst = copyUpdateInst;
            boundSchemaTypes = copySchemaBoundTypes;
            return false;
        }
        
    }
    
    /*
     * check that all bound schema types are not instantiated with
     * TypeApplications.
     */
    private void checkBoundSchemaTypes() throws UnificationException {
        for (SchemaType schema : boundSchemaTypes) {
            Type inst = typeInstantiation.get(schema.getVariableName());
            if (inst instanceof TypeApplication)
                throw new UnificationException(schema
                        + " is a bound type variable, but instantiated to "
                        + inst);
        }
    }

    /**
     * Adds an instantiation to the mapping.
     * 
     * The schema variable must not already have been instantiated and the
     * instantiation must not contain schema variables.
     * 
     * <p>
     * <i>The latter condition is not mandatory and merely included because
     * this is the case needed in this application. Removing it would require
     * attention because of possible circularities.</i>
     * 
     * @param sv
     *            the schema variable to instantiate
     * @param term
     *            the schema-free term to instantiate
     * 
     * @throws TermException
     *             if sv is already instantiated or term contains schema
     *             variables.
     */
    public void addInstantiation(@NonNull SchemaVariable sv, @NonNull Term term) throws TermException {
        if(instantiation.get(sv.getName()) != null)
            throw new TermException("SchemaVariable " + sv + " already instantiated");
        if(containsSchemaVariables(term))
            throw new TermException("Instantiation " + term + " contains schema variable(s)");
        
        instantiation.put(sv.getName(), term);
    }
    
    /**
     * Adds an update instantiation to the mapping.
     * 
     * The schema update must not already have been instantiated.
     * 
     * @param schemaIdentifier
     *            the schema update to instantiate
     * @param update
     *            the update to instantiate
     * 
     * @throws TermException
     *             if sv is already instantiated or term contains schema variables.
     */
    public void addUpdateInstantiation(@NonNull String schemaIdentifier,
            @NonNull Update update) throws TermException {
        if(instantiation.get(schemaIdentifier) != null)
            throw new TermException("SchemaUpdate " + schemaIdentifier + " already instantiated");
        
        updateInst.put(schemaIdentifier, update);
    }
 
    /**
     * Gets the instantiation for a schema variable.
     * 
     * @param sv
     *            the schema variable to look up
     * 
     * @return the instantiation stored in the mapping if there is any, null otherwise.
     */
    public Term getTermFor(@NonNull SchemaVariable sv) {
        return instantiation.get(sv.getName());
    }

    // TODO DOC upto here
    
    public Update getUpdateFor(String schemaIdentifier) {
        return updateInst.get(schemaIdentifier);
    }

    /**
     * Instantiate.
     * 
     * @param toInst
     *            the to inst
     * 
     * @return the term
     * 
     * @throws TermException
     *             the term exception
     */
    public Term instantiate(Term toInst) throws TermException {
        return getTermInstantiator().instantiate(toInst);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public TermMatcher clone() {
        try {
            TermMatcher retval = (TermMatcher) super.clone();
            retval.instantiation = instantiation.clone();
            retval.typeInstantiation = typeInstantiation.clone();
            return retval;
        } catch (CloneNotSupportedException e) {
            throw new Error(e);
        }
    }

    /**
     * Instantiate a type.
     * This call is delegated to {@link TypeUnification#instantiate(Type)}
     */
//    public Type instantiateType(Type type) {
//        return typeInstantiation.instantiate(type);
//    }
    
    /**
     * A simple visitor which detects schema variables.
     * also in updates / bindings
     * 
     * @see SchemaCollectorVisitor
     */
    private static final DepthTermVisitor schemaFinder = new DepthTermVisitor() { 
        public void visit(SchemaVariable schemaVariable) throws TermException {
            throw new TermException("Unexpected schema variable found: " + schemaVariable);
        }
        public void visit(SchemaUpdateTerm schemaUpdate) throws TermException {
            throw new TermException("Unexpected schema variable found: " + schemaUpdate);
        }
        public void visit(UpdateTerm updateTerm) throws TermException {
            for (AssignmentStatement ass : updateTerm.getAssignments()) {
                if(ass.getTarget() instanceof SchemaVariable)
                    throw new TermException("Unexpected schema variable in assignment " + ass);
            }
        }
        public void visit(Binding binding) throws TermException {
            // bugfix:
            super.visit(binding);
            if(binding.getVariable() instanceof SchemaVariable)
                throw new TermException("Unexpected schema variable in binding " + binding);
        }
    };
    
    /**
     * Checks whether a term contains schema variables.
     * 
     * @param term
     *            term to check
     * 
     * @return true iff there occurs at least one schema variable in term 
     */
    public static boolean containsSchemaVariables(Term term) {
        try {
            term.visit(schemaFinder);
            return false;
        } catch (TermException e) {
            return true;
        }
    }
    
    /**
     * Gets an unmodifiable copy of the schemavariable-to-term map.
     * 
     * @return a map from schema variable names to terms.
     */
    public Map<String, Term> getTermInstantiation() {
        return Collections.unmodifiableMap(instantiation);
    }

    /**
     * Gets the term instantiator.
     * 
     * @return the term instantiator
     */
    public TermInstantiator getTermInstantiator() {
        return new TermInstantiator(instantiation, typeInstantiation, updateInst);
    }
    
    /**
     * Gets an unmodifiable copy of the schemaupdate-to-term map.
     * 
     * @return a map from schema update names to updates.
     */
    public Map<String, Update> getUpdateInstantiation() {
        return Collections.unmodifiableMap(updateInst);
    }

    /**
     * Add a schema type to the set of remembered bound schema types.
     * 
     * Those types must not be instantiated with {@link TypeApplication}s. This
     * is checked in {@link #leftMatch(Term, Term)}.
     * 
     * @param schemaType schematic type to add.
     */
    public void addBoundSchemaType(@NonNull SchemaType schemaType) {
        boundSchemaTypes.add(schemaType);
    }

    public Map<String, Type> getTypeInstantiation() {
        return typeInstantiation;
    }

    public Type getTypeFor(String variableName) {
        return typeInstantiation.get(variableName);
    }

    public void addTypeInstantiation(String varName, Type type) {
        typeInstantiation.put(varName, type);
    }

    
}