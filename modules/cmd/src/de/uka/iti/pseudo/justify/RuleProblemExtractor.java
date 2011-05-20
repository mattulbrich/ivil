/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.justify;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Map.Entry;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.TypeVariableCollector;
import de.uka.iti.pseudo.environment.creation.RuleFormulaExtractor;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.BindableIdentifier;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.SchemaType;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.TermVisitor;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.Update;
import de.uka.iti.pseudo.term.Variable;
import de.uka.iti.pseudo.term.creation.DefaultTermVisitor;
import de.uka.iti.pseudo.term.creation.TermFactory;
import de.uka.iti.pseudo.term.creation.TermInstantiator;

// http://www.cs.chalmers.se/~philipp/publications/lfm.pdf
// p. 13

/**
 * This class allows the transformation of a rule into its meaning formula.
 * 
 * If this formula can be proven valid, the corresponding rule is a valid
 * inference rule schema.
 * 
 * <h2>Steps</h2>
 * <ol>
 * <li>construct context from <code>assume</code> clauses
 * <li>Using the context create the meaning formula (extract problem)
 * <li>Use a {@link SchemaVariableUseVisitor} to collect information on schema
 * variables in that formula.
 * <li>Turn all free schema types to unused type variables.
 * <li>Turn bound schema variables to logical variables of appropriate type.
 * <li>Turn unbound schema variables to skolem symbols with appropriate
 * parameters.
 * </ol>
 * 
 * <h2>Limitations</h2>
 * 
 * <h3>Type quantification</h3>
 * 
 * Type quantification is not supported. Exceptions are thrown if a rule
 * containing type quantification is to be transformed. However, there are only
 * very few and very fundamental rules dealing with this construct. If at one
 * stage, this is to be implemented, we would need type variables which take
 * parameters. The formula {@code (\T_all %'a; (\forall %x as %'x; %phi))} would
 * have to be translated to {@code (\T_all 'a; (\forall x as 'b('a); phi(x)))}.
 * 
 * The translation {@code (\T_all 'a; (\forall x as 'b; phi(x)))} would not work
 * since this is equivalent to {@code (\forall x as 'b; phi(x))}. That would
 * imply that \T_all can always be dropped - which is not the case.
 * 
 * <p>
 * (Non-type) variable binders are supported, however.
 * 
 * <h3>Where conditions</h3> Some where conditions could be semantically
 * expressed as additional assumptions. {@code notFreeIn %x, %b} with the
 * mappings {@code %x->x, %b->b(x)} (resulting from a quantified formula e.g.)
 * could be translated to {@code (\forall x; b(x) = b(arb))} (or a new skolem
 * constant). Other conditions may be translatable, too.
 * 
 * <h3>Schema updates</h3> If the formula contains a schematic update, it cannot
 * be translated. This could possible be generalised by enumerating all relevant
 * assignables and construct an update {@code a1:=sk_a1 || ... || an:=sk_an}
 * (possibly with bound variables are arguments to the skolem functions.
 * 
 * <h2>Literature</h2>
 * 
 * This is based on this <a
 * href="http://www.cs.chalmers.se/~philipp/publications/lfm.pdf">paper</a> by
 * Bubel, Roth, and Ruemmer.
 * 
 * @author mattias ulbrich
 */
public class RuleProblemExtractor {

    private static final Term FALSE = Environment.getFalse();
    private static final Term TRUE = Environment.getTrue();

    /**
     * The rule to treat.
     */
    private Rule rule;

    /**
     * The combined assumptions are called context in the paper.
     */
    private Term context;

    /**
     * To create terms.
     */
    private TermFactory tf;

    /**
     * The environment in which we are working.
     */
    private Environment env;

    /**
     * The mapping of schema variables to terms. Either map to variables or
     * skolem function symbols.
     */
    private Map<String, Term> mapVars = new HashMap<String, Term>();

    /**
     * The mapping of schema types to type variables.
     */
    private Map<String, Type> mapTypeVars = new HashMap<String, Type>();

    /**
     * The instantiation mechanism using the above maps to instantiate schema
     * instances.
     */
    private TermInstantiator termInst = new TermInstantiator(mapVars,
            mapTypeVars, Collections.<String, Update> emptyMap());

    /**
     * Remember all variable names which are used in quantifications.
     * (we do better not reuse them).
     */
    private Set<String> usedVariableNames = new HashSet<String>();
    
    /**
     * Rember all type variable names which appear (free or bound) in the
     * formulae. (we better not reuse them).
     */
    private Set<String> usedTypeVariableNames = new HashSet<String>();

    /**
     * Visitor to collect {@link #usedVariableNames} and
     * {@link #usedTypeVariableNames}.
     */
    private class VariableCollector extends DefaultTermVisitor.DepthTermVisitor {
        public void visit(Variable variable) throws TermException {
            usedVariableNames.add(variable.getName());
        }
        public void visit(Binding binding) throws TermException {
            super.visit(binding);
            binding.getVariable().visit(this);
        }
        protected void defaultVisitTerm(Term term) throws TermException {
            super.defaultVisitTerm(term);
        }
    }
    
    private static TermVisitor typeQuantDetector = new DefaultTermVisitor.DepthTermVisitor() {
        public void visit(de.uka.iti.pseudo.term.TypeVariableBinding typeVariableBinding) throws TermException {
            throw new TermException("Type quantification not supported at the moment");
        };
    };

    /**
     * Instantiates a new rule problem extractor.
     * 
     * @param rule
     *            the rule to extract
     * @param env
     *            the environment of rule.
     */
    public RuleProblemExtractor(Rule rule, Environment env) {
        super();
        this.rule = rule;
        this.env = env;
        this.tf = new TermFactory(env);
    }

    
    /**
     * Perform the problem extraction.
     * 
     * @return the extracted term.
     * 
     * @throws TermException
     *             if the rule is not suited.
     * @throws RuleException
     *             if the rule is not suited.
     * @throws EnvironmentException
     *             if the rule is not suited.
     */
    public @NonNull Term extractProblem() throws TermException, RuleException,
            EnvironmentException {

        Term problem0 = new RuleFormulaExtractor(env).extractMeaningFormula(rule);
        
        //
        // check for type quantification absence
        problem0.visit(typeQuantDetector);

        //
        // collect info on used variables
        SchemaVariableUseVisitor svuv = new SchemaVariableUseVisitor();
        problem0.visit(svuv);
        
        //
        // collect info on used type variables
        Set<TypeVariable> collectedTypeVars = TypeVariableCollector.collect(problem0);
        for (TypeVariable typeVariable : collectedTypeVars) {
            usedTypeVariableNames.add(typeVariable.getVariableName());
        }

        //
        // schema types to type variables.
        mapTypeVars.clear();
        instantiateTypes(problem0);

        //
        // extract used names. Better do not use function symbols either.
        VariableCollector vc = new VariableCollector();
        problem0.visit(vc);
        for (Function f : env.getAllFunctions()) {
            usedVariableNames.add(f.getName());
        }

        //
        // bound schema variables to variables.
        mapVars.clear();
        mapAllBoundSchemaVars(problem0, svuv.getBoundIdentifiers());

        //
        // then skolemize the remainder with according arity.
        mapRemainingSchemaVars(problem0, svuv.getSeenBindablesMap());

        //
        // instantiate
        Term problem = termInst.instantiate(problem0);

        return problem;
    }

    /**
     * Assign a new type variable to every schema type.
     */
    private void instantiateTypes(@NonNull Term t) throws EnvironmentException,
            TermException {
        Set<SchemaType> typeVars = TypeVariableCollector.collectSchema(t);
        for (SchemaType schType : typeVars) {
            String name = schType.getVariableName();
            if(!mapTypeVars.containsKey(name)) {
                String newName = freshTypeVariableName(name);
                TypeVariable tv = new TypeVariable(newName);
                mapTypeVars.put(name, tv);
            }
        }
    }

    /**
     * Map all bound schema variables to variables.
     */
    private void mapAllBoundSchemaVars(Term term,
            Set<BindableIdentifier> boundIdentifiers) throws TermException {
        for (BindableIdentifier boundvar : boundIdentifiers) {
            if (boundvar instanceof SchemaVariable) {
                SchemaVariable schemaVar = (SchemaVariable) boundvar;
                String schemaName = schemaVar.getName();
                if(!mapVars.containsKey(schemaName)) {
                    // remove leading %
                    String prefix = schemaName.substring(1);
                    // new name is unique
                    String newname = freshName(prefix);
                    Type instType = termInst.instantiate(schemaVar.getType());

                    assert TypeVariableCollector.collectSchema(instType).isEmpty() : 
                        "Ensure no free schema type in instantiation";
                    
                    mapVars.put(schemaName, Variable.getInst(newname, instType));
                }
            }
        }
    }

    private String freshName(String prefix) {
        String newName = prefix;
        int counter = 1;
        while (usedVariableNames.contains(newName)) {
            newName = prefix + counter;
            counter++;
        }
        // mark the new name as used now also.
        usedVariableNames.add(newName);
        return newName;
    }
    
    private String freshTypeVariableName(String prefix) {
        // if the typevariable was internal, make a regulare name of it.
        if(Character.isDigit(prefix.charAt(0))) {
            prefix = "v" + prefix;
        }
        String newName = prefix;
        int counter = 1;
        while (usedTypeVariableNames.contains(newName)) {
            newName = prefix + counter;
            counter++;
        }
        // mark the new name as used now also.
        usedTypeVariableNames.add(newName);
        return newName;
    }

    /**
     * Map all schema entities which are not yet mapped to new skolem symbols.
     */
    private void mapRemainingSchemaVars(Term term,
            Map<SchemaVariable, SortedSet<BindableIdentifier>> seenBindablesMap)
            throws TermException, EnvironmentException {
        for (Entry<SchemaVariable, SortedSet<BindableIdentifier>> entry : 
                seenBindablesMap.entrySet()) {
            SchemaVariable sv = entry.getKey();
            // not yet mapped
            if (!mapVars.containsKey(sv.getName())) {
                SortedSet<BindableIdentifier> deps = entry.getValue();
                skolemizeSchemaVar(sv, deps);
            }
        }
    }

    private void skolemizeSchemaVar(SchemaVariable sv,
            SortedSet<BindableIdentifier> dependencies) throws TermException,
            EnvironmentException {
        Type[] argTypes = new Type[dependencies.size()];
        Term[] argTerms = new Term[dependencies.size()];

        Type instType = termInst.instantiate(sv.getType());

        //
        // make argument array
        int i = 0;
        for (BindableIdentifier dep : dependencies) {
            argTypes[i] = termInst.instantiate(dep.getType());
            if (dep instanceof SchemaVariable) {
                SchemaVariable schema = (SchemaVariable) dep;
                argTerms[i] = mapVars.get(schema.getName());
            } else {
                argTerms[i] = termInst.instantiate(dep);
            }
            i++;
        }

        //
        // make new symbol, stripping leading %
        String name = sv.getName().substring(1);
        name = freshName(name);
        Function f = new Function(name, instType, argTypes, false, false,
                ASTLocatedElement.CREATED);
        env.addFunction(f);

        //
        // build skolemisation (application)
        Term sk = Application.getInst(f, instType, argTerms);
        assert TypeVariableCollector.collectSchema(sk).isEmpty() :
            "Ensure no free schema type in skolemisation";

        mapVars.put(sv.getName(), sk);
    }

}
