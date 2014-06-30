/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.environment.creation.ruleextraction;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nonnull.NonNull;

import de.uka.iti.pseudo.environment.Lemma;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.MetaFunction;
import de.uka.iti.pseudo.environment.TypeVariableCollector;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.rule.RuleTagConstants;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.SchemaProgramTerm;
import de.uka.iti.pseudo.term.SchemaType;
import de.uka.iti.pseudo.term.SchemaUpdateTerm;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.TypeVariableBinding;
import de.uka.iti.pseudo.term.Variable;
import de.uka.iti.pseudo.term.creation.RebuildingTermVisitor;
import de.uka.iti.pseudo.term.creation.RebuildingTypeVisitor;
import de.uka.iti.pseudo.term.creation.TermFactory;


/**
 * The class is used to extract axiomatic lemmas from rules.
 *
 * They may be axioms and need not be proved since the rules give rise to proof
 * obligations already.
 */
public class RuleAxiomExtractor {

    /**
     * The rule tag which indicates that a rule is to be extracted as an axiom.
     */
    private static final String AXIOM_EXTRACT_KEY = RuleTagConstants.KEY_AS_AXIOM;

    /**
     * A map to be used as properties. It contains only one key:
     * {@link RuleTagConstants#KEY_GENERATED_AXIOM}
     */
    private static final Map<String, String> GENERATED =
            Collections.singletonMap(RuleTagConstants.KEY_GENERATED_AXIOM, "");

    /**
     * The environment to operate on.
     */
    private final Environment env;

    /**
     * The termfactory used to construct the terms.
     */
    private final TermFactory tf;

    /**
     * The formula extractor makes the actual formula.
     */
    private final RuleFormulaExtractor formulaExtractor;

    /**
     * The set of used type variables.
     */
    private Set<TypeVariable> usedTypeVariables;

    /**
     * The set of new type variables.
     */
    private final Set<TypeVariable> newTypeVariables = new HashSet<TypeVariable>();

    /**
     * The set of new variables.
     */
    private final Set<Variable> newVariables = new HashSet<Variable>();

    /**
     * Instantiates a new rule axiom extractor.
     *
     * @param env the environment to operate on
     */
    public RuleAxiomExtractor(@NonNull Environment env) {
        this.env = env;
        this.tf = new TermFactory(env);
        this.formulaExtractor = new RuleFormulaExtractor(env);
    }

    /**
     * This visitor takes schema types and replaces them by type variables.
     * These are listed in {@link #newTypeVariables}.
     */
    private class TypeVisitor extends RebuildingTypeVisitor<Void> {

        @Override
        public Type visit(SchemaType schemaType, Void parameter)
                throws TermException {

            String name = "ty_" + schemaType.getVariableName();
            TypeVariable tv = TypeVariable.getInst(name);

            int i = 1;
            while(usedTypeVariables.contains(tv)) {
                tv = TypeVariable.getInst(name + i);
                i++;
            }

            newTypeVariables.add(tv);
            return tv;
        }
    }

    /**
     * The term visitor. It replaces all schema variables and schema types.
     */
    private class TermVisitor extends RebuildingTermVisitor {

        private final TypeVisitor tyv = new TypeVisitor();

        @Override
        protected Type modifyType(Type type) throws TermException {
            return type.accept(tyv, null);
        }

        @Override
        public void visit(SchemaVariable schemaVariable) throws TermException {

            Variable variable = Variable.getInst(schemaVariable.getName().substring(1),
                    modifyType(schemaVariable.getType()));

            newVariables.add(variable);
            resultingTerm = variable;
        }

        @Override
        public void visit(Application application) throws TermException {
            if(application.getFunction() instanceof MetaFunction) {
                throw new TermException("Terms must not contain meta functions: " + application);
            }
            super.visit(application);
        }

        @Override
        public void visit(LiteralProgramTerm literalProgramTerm) throws TermException {
            throw new TermException("Terms must not contain program terms: " + literalProgramTerm);
        }

        @Override
        public void visit(SchemaProgramTerm schemaProgramTerm) throws TermException {
            throw new TermException("Terms must not contain program terms: " + schemaProgramTerm);
        }

        @Override
        public void visit(SchemaUpdateTerm schemaUpdateTerm) throws TermException {
            throw new TermException("Terms must not contain schema updates: " + schemaUpdateTerm);
        }

        @Override
        public void visit(Binding binding) throws TermException {
            throw new TermException("Terms must not contain binders: " + binding);
        }

        @Override
        public void visit(TypeVariableBinding tyVarBinding) throws TermException {
            throw new TermException("Terms must not contain type binders: " + tyVarBinding);
        }

        private Term getResultingTerm() {
            return resultingTerm;
        }
    }

    /**
     * Extract axioms from the environment.
     *
     * The axioms are added to the environment under the same name as the rule.
     * An exception is raised in case of a name clash.
     *
     * @throws ASTVisitException
     *             if a name clash occurs.
     */
    public void extractAxioms() throws ASTVisitException {

        Collection<Rule> rules = env.getLocalRules();
        for (Rule rule : rules) {
            if(rule.getDefinedProperties().contains(AXIOM_EXTRACT_KEY)) {
                try {
                    extractAxiom(rule);
                } catch (Exception e) {
                    throw new ASTVisitException("Cannot extract axiom from rule " +
                            rule.getName() + ": " + e.getMessage(),
                            rule.getDeclaration(), e);
                }
            }
        }
    }

    /*
     * Extract an axiom from a single rule.
     */
    private void extractAxiom(Rule rule) throws EnvironmentException, RuleException, TermException {

        Term term = makeAxiomTerm(rule);

        Map<String, String> properties = GENERATED;
        if(rule.getDefinedProperties().contains(RuleTagConstants.EXCLUDE_FROM_DP)) {
            properties = new HashMap<String, String>(GENERATED);
            properties.put(RuleTagConstants.EXCLUDE_FROM_DP, "");
        }

        Lemma axiom = new Lemma(rule.getName(), term, properties, ASTLocatedElement.CREATED);

        // DEBUG
//        axiom.getTerm().visit(new ToplevelCheckVisitor());
//        Term.SHOW_TYPES = true;
//        axiom.dump();

        env.addLemma(axiom);
    }

    private Term makeAxiomTerm(Rule rule) throws RuleException, TermException {

        if(!rule.getWhereClauses().isEmpty()) {
            throw new RuleException("The rule has where clauses");
        }

        Term meaningFormula = formulaExtractor.extractMeaningFormula(rule);

        usedTypeVariables = TypeVariableCollector.collect(meaningFormula);
        newTypeVariables.clear();
        newVariables.clear();

        TermVisitor v = new TermVisitor();
        meaningFormula.visit(v);
        Term result = v.getResultingTerm();

        // if no changes made: original term
        if(result == null) {
            result = meaningFormula;
        }

        // add the find clause as trigger if there are variables
        if(!newTypeVariables.isEmpty() || !newVariables.isEmpty()) {
            Term find = rule.getFindClause().getTerm();
            find.visit(v);
            if(v.getResultingTerm() != null) {
                result = tf.pattern(v.getResultingTerm(), result);
            }
        }

        for (Variable var : newVariables) {
            result = tf.forall(var, result);
        }

        for (TypeVariable tyv : newTypeVariables) {
            result = tf.typeForall(tyv, result);
        }

        return result;
    }
}
