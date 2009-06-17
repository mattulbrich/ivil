/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.term.creation;

import java.util.Collections;
import java.util.Map;

import nonnull.NonNull;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.UpdateTerm;
import de.uka.iti.pseudo.term.creation.DefaultTermVisitor.DepthTermVisitor;
import de.uka.iti.pseudo.term.statement.AssignmentStatement;
import de.uka.iti.pseudo.util.AppendMap;

/**
 * The Class TermUnification is the recording instance of a term matching.
 * 
 * <p>The actual matching is performed in class TermMatcher.
 * 
 * @see TermMatcher
 */
public class TermUnification {
    
    /**
     * The type unification is sourced out.
     */
    private TypeUnification typeUnification = new TypeUnification();
    
    /**
     * The mapping from schema variables to their term instantiations.
     * <p> We use {@link AppendMap} here because we ne often need to clone
     * for unification attempts.
     */
    private AppendMap<String, Term> instantiation = new AppendMap<String, Term>();
    
    /**
     * The term matcher performs the actual matching comparisons.
     */
    private TermMatcher termMatcher;
    
    /**
     * The environment too lookup things
     */
    private Environment env;
    
    /**
     * Instantiates a new term unification.
     * 
     * @param env
     *            the environment to look things up.
     */
    public TermUnification(Environment env) {
        termMatcher = new TermMatcher(this, env);
        this.env = env;
    }
    
    /**
     * Unify two terms in one direction.
     * The first argument may contain schema variables while the second may not.
     * 
     * @param adaptingTerm
     *            the matching term which may contain schema entities
     * @param fixTerm
     *            the matched term which may <b>not</b> contain schema entities.
     * 
     * @return true, if successful
     */
    public boolean leftUnify(Term adaptingTerm, Term fixTerm) {
        
        AppendMap<String, Term> copyTermInst = instantiation.clone();

        try {
            
            termMatcher.compare(adaptingTerm, fixTerm);
            return true;
            
        } catch (TermException e) {
            instantiation = copyTermInst;
            return false;
        }
        
    }
    
    /**
     * Adds an instantiation to the mapping.
     * 
     * The schema variable may not already have been instantiated nor may the
     * instantiation contain schema variables.
     * 
     * @param sv
     *            the schema variable to instantiate
     * @param term
     *            the schema-free term to instantiate
     * 
     * @throws TermException
     *             if sv is already instantiated or term contains schema variables.
     */
    public void addInstantiation(@NonNull SchemaVariable sv, @NonNull Term term) throws TermException {
        if(instantiation.get(sv) != null)
            throw new TermException("SchemaVariable " + sv + " already instantiated");
        if(containsSchemaVariables(term))
            throw new TermException("Instantiation " + term + " contains schema variable(s)");
        
        instantiation.put(sv.getName(), term);
    }
 
// // if instantiation CAN contain schema variables
//    public void addInstantiation(SchemaVariable sv, Term term) throws TermException {
//        
//        SchemaCollectorVisitor scv = new SchemaCollectorVisitor();
//        scv.collect(term);
//        
//        if(scv.getSchemaVariables().contains(sv)) {
//            throw new UnificationException("The schema variable cannot be instantiated, occur check failed", sv, term);
//        }
//        
//        assert instantiation.get(sv) == null;
//        
//        instantiation.put(sv.getName(), term);
//        
//        if(containsSchema) {
//            for (String s : instantiation.keySet()) {
//                instantiation.put(s, instantiate(instantiation.get(s)));
//            }
//        }
//        
//        containsSchema |= !scv.isEmpty();
//    }
    
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
    public TermUnification clone() {
        TermUnification retval = new TermUnification(env);
        retval.instantiation.putAll(instantiation);
        retval.typeUnification = typeUnification.clone();
        return retval;
    }

    /**
     * Instantiate a type.
     * This call is delegated to {@link TypeUnification#instantiate(Type)}
     */
    public Type instantiateType(Type type) {
        return typeUnification.instantiate(type);
    }
    
    /**
     * Gets the type unification.
     * 
     * @return the type unification
     */
    public TypeUnification getTypeUnification() {
        return typeUnification;
    }
    
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
        public void visit(UpdateTerm updateTerm) throws TermException {
            for (AssignmentStatement ass : updateTerm.getAssignments()) {
                if(ass.getTarget() instanceof SchemaVariable)
                    throw new TermException("Unexpected schema variable in assignment " + ass);
            }
        }
        public void visit(Binding binding) throws TermException {
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
        return new TermInstantiator(instantiation, typeUnification.getInstantiation());
    }

}
