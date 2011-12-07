package de.uka.iti.pseudo.environment.creation;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uka.iti.pseudo.environment.Axiom;
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


//not threadsafe
// TODO DOC
public class RuleAxiomExtractor {

    private static final String AXIOM_EXTRACT_KEY = RuleTagConstants.KEY_AS_AXIOM;

    private static final Map<String, String> GENERATED = 
            Collections.singletonMap(RuleTagConstants.KEY_GENERATED_AXIOM, "");

    private Environment env;
    private TermFactory tf;

    private RuleFormulaExtractor formulaExtractor;

    private Set<TypeVariable> usedTypeVariables;
    private Set<TypeVariable> newTypeVariables = new HashSet<TypeVariable>();
    private Set<Variable> newVariables = new HashSet<Variable>();

    public RuleAxiomExtractor(Environment env) {
        this.env = env;
        this.tf = new TermFactory(env);
        this.formulaExtractor = new RuleFormulaExtractor(env);
    }

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

    private class TermVisitor extends RebuildingTermVisitor {

        private TypeVisitor tyv = new TypeVisitor();

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

    public void extractAxioms() throws ASTVisitException {

        List<Rule> rules = env.getLocalRules();
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

    private void extractAxiom(Rule rule) throws EnvironmentException, RuleException, TermException {

        Term term = makeAxiomTerm(rule);

        Axiom axiom = new Axiom(rule.getName(), term, GENERATED, ASTLocatedElement.CREATED);

        // DEBUG
//        axiom.getTerm().visit(new ToplevelCheckVisitor());
//        Term.SHOW_TYPES = true;
//        axiom.dump();
        
        env.addAxiom(axiom);
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

        for (TypeVariable tyv : newTypeVariables) {
            result = tf.typeForall(tyv, result);
        }

        for (Variable var : newVariables) {
            result = tf.forall(var, result);
        }

        return result;
    }



}
