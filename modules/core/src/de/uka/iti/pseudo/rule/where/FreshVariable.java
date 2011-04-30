package de.uka.iti.pseudo.rule.where;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.Variable;
import de.uka.iti.pseudo.term.creation.DefaultTermVisitor;
import de.uka.iti.pseudo.term.creation.TermInstantiator;
import de.uka.iti.pseudo.term.creation.TermMatcher;
import de.uka.iti.pseudo.util.Log;

public class FreshVariable extends WhereCondition {

    public FreshVariable() {
        super("freshVar");
    }
    
    @Override
    public boolean check(Term[] formalArguments, Term[] actualArguments,
            RuleApplication ruleApp, Environment env) throws RuleException {
        
        Term first = actualArguments[0];
        
        if(!(first instanceof Variable)) {
            throw new RuleException("The first argument of freshVar needs to be a variable, not " + first);
        }
        
        Variable var = (Variable) first;
        
        FreeVarFinder finder = new FreeVarFinder();
        try {
            for (int i = 1; i < actualArguments.length; i++) {
                actualArguments[i].visit(finder);
            }
        } catch (TermException e) {
            throw new RuleException(e);
        }
        
        return !finder.freeVariables.contains(var);
    }
    
    /**
     * Try to find a fresh variable instantiation for the first argument.
     * Do nothing if this is not a schema variable. 
     */
    @Override
    public void addInstantiations(TermMatcher termMatcher, Term[] arguments)
            throws RuleException {
        
        // checked in checkSyntax
        assert arguments[0] instanceof SchemaVariable;
        
        SchemaVariable schemaVar = (SchemaVariable) arguments[0];
        String schemaName = schemaVar.getName();
        Map<String, Term> termMap = termMatcher.getTermInstantiation();
        TermInstantiator termInstantiator = termMatcher.getTermInstantiator();
        
        // do nothing if already instantiated
        if(termMap.containsKey(schemaName)) {
            return;
        }
        
        FreeVarFinder finder = new FreeVarFinder();
        try {
            for (int i = 1; i < arguments.length; i++) {
                Term actual = termInstantiator.instantiate(arguments[i]);
                actual.visit(finder);
            }

            String prefix = schemaName.substring(1);
            String varname = freshVarname(finder, prefix);
            Type type = termInstantiator.instantiate(schemaVar.getType());
            
            termMatcher.addInstantiation(schemaVar, Variable.getInst(varname, type));
        
        } catch (TermException e) {
            throw new RuleException(e);
        }

    }

    private String freshVarname(FreeVarFinder finder, String prefix) {
        String name = prefix;
        int count = 1;
        while(finder.allVariableNames.contains(name)) {
            name = prefix + count;
            count ++;
        } 
        
        return name;
    }

    @Override
    public void checkSyntax(Term[] arguments) throws RuleException {
        if(arguments.length == 0) {
            throw new RuleException("freshVar expects at least one argument");
        }
        
        if(!(arguments[0] instanceof SchemaVariable)) {
            throw new RuleException("freshVar expects (schema) variable as first argument");
        }
    }
    
    /**
     * This visitor is used to traverse the term. It calculates the set of free
     * variables.
     * 
     * @see NoFreeVars.FreeVarChecker
     */
    private static class FreeVarFinder extends
            DefaultTermVisitor.DepthTermVisitor {

        /**
         * The bound variables.
         */
        private Set<Variable> boundVariables = new HashSet<Variable>();
        
        /**
         * The free variables.
         */
        Set<Variable> freeVariables = new HashSet<Variable>();
        
        /**
         * The set of all variable names, bound or free
         */
        Set<String> allVariableNames = new HashSet<String>();

        @Override public void visit(Binding binding) throws TermException {
            if (binding.getVariable() instanceof Variable) {
                Variable variable = (Variable) binding.getVariable();
                allVariableNames.add(variable.getName());
                boundVariables.add(variable);
                super.visit(binding);
                boundVariables.remove(variable);
            } else {
                // if schema variable bound
                // LOG if we use logging once
                Log.log(Log.WARNING, "We should actually only check unschematic terms, but: "
                                + binding);
                super.visit(binding);
            }
        }

        @Override public void visit(Variable variable) throws TermException {
            allVariableNames.add(variable.getName());
            if (!boundVariables.contains(variable)) {
                freeVariables.add(variable);
            }
        }
    }

}
