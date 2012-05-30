/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term.creation;

import java.util.Collections;
import java.util.Map;

import nonnull.NonNull;
import nonnull.Nullable;
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
import de.uka.iti.pseudo.term.statement.Assignment;
import de.uka.iti.pseudo.util.AppendMap;
import de.uka.iti.pseudo.util.AppendSet;

/**
 * The Class TermMatcher is the recording instance of a term matching.
 * 
 * It records instantiations of schema variables, schema type variables and
 * schema updates.
 * 
 * <p>
 * The actual matching is performed in class TermMatcherVisitor.
 * 
 * @see TermMatchVisitor
 */
public class TermMatcher implements Cloneable {

    /**
     * The mapping from schema type variables to types.
     * <p>
     * We use {@link AppendMap} here because we often need to clone for
     * unification attempts.
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
     * <p> We use {@link AppendMap} here because we often need to clone
     * for unification attempts.
     */
    private AppendMap<String, Update> updateInst = new AppendMap<String, Update>();
    
    /**
     * The term matcher visitor performs the actual matching comparisons.
     */
    private TermMatchVisitor termMatcherVisitor;
    
    /**
     * remember those types which are bound in type quantifications.
     * They must not be instantiated with TypeApplications.
     */
    private AppendSet<SchemaType> boundSchemaTypes = new AppendSet<SchemaType>();

    /**
     * A simple visitor which detects schema variables.
     * also in updates / bindings
     * 
     * @see SchemaCollectorVisitor
     */
    private static final DepthTermVisitor schemaFinder = new DepthTermVisitor() {
        protected void defaultVisitTerm(Term term) throws TermException {
            if (term.getType() instanceof SchemaType)
                throw new TermException("Unexpected schema type found: " + term.getType());

            for (Term t : term.getSubterms()) {
                t.visit(this);
            }
        }

        public void visit(SchemaVariable schemaVariable) throws TermException {
            throw new TermException("Unexpected schema variable found: " + schemaVariable);
        }
        public void visit(SchemaUpdateTerm schemaUpdate) throws TermException {
            throw new TermException("Unexpected schema variable found: " + schemaUpdate);
        }
        public void visit(UpdateTerm updateTerm) throws TermException {
            for (Assignment ass : updateTerm.getAssignments()) {
                if(ass.getTarget() instanceof SchemaVariable)
                    throw new TermException("Unexpected schema variable in assignment " + ass);
            }
        }
        public void visit(Binding binding) throws TermException {
            // bugfix:
            super.visit(binding);
            if(binding.getVariable() instanceof SchemaVariable)
                throw new TermException("Unexpected schema variable in binding " + binding);
            binding.getVariable().visit(this);
        }
    };

    /**
     * Checks whether a term contains schema variables, schema updates or schema
     * types.
     * 
     * @param term
     *            term to check
     * 
     * @return true iff there occurs at least one schema object in term
     */
    public static boolean containsSchematic(Term term) {
        try {
            term.visit(schemaFinder);
            return false;
        } catch (TermException e) {
            return true;
        }
    }

    /**
     * Instantiates a new term unification.
     */
    public TermMatcher() {
        termMatcherVisitor = new TermMatchVisitor(this);
    }
    
    /**
     * Unify two terms in one direction. The first argument may contain schema
     * variables while the second must not.
     * 
     * <p>
     * The method returns <code>true</code> and updates its schematic
     * instantiations if the match can be performed. If the attempt fails,
     * however, <code>false</code> is returned and the method does not affect
     * the schematic instantiations.
     * 
     * @param adaptingTerm
     *            the matching term which may contain schema entities
     * @param fixTerm
     *            the matched term which <b>must not</b> contain schema
     *            entities.
     * 
     * @return true, if and only if match was successful.
     */
    public boolean leftMatch(Term adaptingTerm, Term fixTerm) {
        
        AppendMap<String, Term> copyTermInst = instantiation.clone();
        AppendMap<String, Type> copyTypeInst = typeInstantiation.clone();
        AppendMap<String, Update> copyUpdateInst = updateInst.clone();
        AppendSet<SchemaType> copySchemaBoundTypes = boundSchemaTypes.clone();
        try {
            
            termMatcherVisitor.compare(adaptingTerm, fixTerm);
            checkBoundSchemaTypes();
            return true;
            
        } catch (TermException e) {
            instantiation = copyTermInst;
            typeInstantiation = copyTypeInst;
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
        if(containsSchematic(term))
            throw new TermException("Instantiation " + term + " contains schema entity");
        
        instantiation.put(sv.getName(), term);
    }
    
    /**
     * Adds an update instantiation to the mapping.
     * 
     * The schema update must not already have been instantiated.
     * 
     * <p>
     * <i>The latter condition is not mandatory and merely included because
     * this is the case needed in this application. Removing it would require
     * attention because of possible circularities.</i>
     * 
     * @param schemaIdentifier
     *            the schema update to instantiate
     * @param update
     *            the update to instantiate
     * 
     * @throws TermException
     *             if schemaIdentifier is already instantiated
     */
    public void addUpdateInstantiation(@NonNull String schemaIdentifier,
            @NonNull Update update) throws TermException {
        if(instantiation.get(schemaIdentifier) != null)
            throw new TermException("SchemaUpdate " + schemaIdentifier + " already instantiated");
        
        updateInst.put(schemaIdentifier, update);
    }

    /**
     * Adds a schema type instantiation to the mapping.
     * 
     * The schema type must not already have been instantiated.
     * <p>
     * <i>The latter condition is not mandatory and merely included because this
     * is the case needed in this application. Removing it would require
     * attention because of possible circularities.</i>
     * 
     * @param varName
     *            the name of the schema type
     * @param type
     *            the type to instantiate
     * 
     * @throws TermException
     *             if varName is already instantiated
     */
    public void addTypeInstantiation(String varName, Type type) throws TermException {
        
        if(typeInstantiation.get(varName) != null)
            throw new TermException("Schema type " + varName + " already instantiated");
        
        typeInstantiation.put(varName, type);
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

    /**
     * Gets the instantiation for a schema variable.
     * 
     * @param sv
     *            the schema variable to look up
     * 
     * @return the instantiation stored in the mapping if there is any, null otherwise.
     */
    public @Nullable Term getTermFor(@NonNull SchemaVariable sv) {
        return instantiation.get(sv.getName());
    }

    /**
     * Gets the instantiation for a schema type.
     * 
     * @param variableName
     *            the schema type to look up
     * 
     * @return the instantiation stored in the mapping if there is any, null
     *         otherwise.
     */
    public @Nullable Type getTypeFor(String variableName) {
        return typeInstantiation.get(variableName);
    }

    /**
     * Gets the instantiation for a schema update.
     * 
     * @param schemaIdentifier
     *            the schema update to look up
     * 
     * @return the instantiation stored in the mapping if there is any, null
     *         otherwise.
     */
    public @Nullable Update getUpdateFor(String schemaIdentifier) {
        return updateInst.get(schemaIdentifier);
    }

    /**
     * Create a deep copy of this object.
     * 
     * The return object is equal to this matcher. However, all maps and sets
     * are are cloned copies of this object. The returned object therefore
     * represents a snapshot of the current state of this object.
     */
    public TermMatcher clone() {
        try {
            TermMatcher retval = (TermMatcher) super.clone();
            retval.instantiation = instantiation.clone();
            retval.typeInstantiation = typeInstantiation.clone();
            retval.updateInst = updateInst.clone();
            retval.boundSchemaTypes = boundSchemaTypes.clone();
            retval.termMatcherVisitor = new TermMatchVisitor(retval);
            return retval;
        } catch (CloneNotSupportedException e) {
            throw new Error(e);
        }
    }
    
    /**
     * Clear and set all maps and sets to empty.
     */
    public void clear() {
        instantiation.clear();
        typeInstantiation.clear();
        updateInst.clear();
        boundSchemaTypes = new AppendSet<SchemaType>();
    }

    /**
     * Gets an unmodifiable copy of the schema-variable-to-term map.
     * 
     * @return a map from schema variable names to terms.
     */
    public Map<String, Term> getTermInstantiation() {
        return Collections.unmodifiableMap(instantiation);
    }

    /**
     * Gets an unmodifiable copy of the schema-type-to-type map.
     * 
     * @return a map from schema types names to types.
     */
    public Map<String, Type> getTypeInstantiation() {
        return  Collections.unmodifiableMap(typeInstantiation);
    }

    /**
     * Gets an unmodifiable copy of the schema-update-to-update map.
     * 
     * @return a map from schema update names to updates.
     */
    public Map<String, Update> getUpdateInstantiation() {
        return Collections.unmodifiableMap(updateInst);
    }

    /**
     * Gets an object which allows the application of the instantiation to
     * arbitrary terms.
     * 
     * @return a freshly created term instantiator visitor.
     */
    public @NonNull TermInstantiator getTermInstantiator() {
        return new TermInstantiator(instantiation, typeInstantiation, updateInst);
    }

    /**
     * Instantiate a term.
     * 
     * This is a convenience method for
     * <pre>getTermInstantiator().instantiate(toInst)</pre>
     * 
     * @param toInst
     *            the term to instantiantiate
     * 
     * @return the instantiated term
     */
    public @NonNull Term instantiate(Term toInst) throws TermException {
        return getTermInstantiator().instantiate(toInst);
    }
    
    /**
     * The string representation shows all used maps and sets
     */
    @Override
    public String toString() {
        return "TermMatcher[terms=" + instantiation + ", types="
                + typeInstantiation + ", updates=" + updateInst
                + ", boundtypes=" + boundSchemaTypes + "]";
    }

    
}
