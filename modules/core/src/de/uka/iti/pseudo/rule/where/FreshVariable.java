/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2011 Universitaet Karlsruhe, Germany
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.rule.where;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

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

/**
 * The where condition FreshVariable can be used to create a fresh variable
 * which does not occur in the condition's other arguments.
 *
 * @ivildoc "Where condition/freshVar"
 *
 * <h2>Where condition <tt>freshVar</tt></h2>
 *
 * This condition can be used to ensure that a variable does not occur free nor
 * bound in terms.
 *
 * <p>
 * This is an example of an <em>active</em> condition since it may add variable
 * instantiations. If the first formal parameter is a schema variable, it
 * becomes instantiated with a new variable of the same type which does not
 * occur in the arguments.
 *
 * <p>
 * The resulting variable is named after the first parameter to the condition,
 * possibly with an added number as suffix.
 *
 * <p>
 * This condition can also be used to check whether a variable does not appear
 * freely in a term. See second Example
 *
 * <h3>Syntax</h3> The where condition expects a first parameter which can
 * either be a variable or a schema variable. Any number of arbitrary terms may
 * follow.
 *
 * <h3>Example:</h3>
 *
 * <pre>
 *   sort S
 *   function bool p(S, S)
 *   function bool allP(S)
 *
 *   rule quant_definition
 *    find allP(%s1, %s2)
 *    where freshVar %x, %s2
 *    replace (\forall %x; p(%x, %s2))
 * </pre>
 *
 * and (to check that a variable does not appear)
 *
 *  <pre>
 *   rule forall_remove
 *    find (\forall %x; %b)
 *    where freshVar %x, %b
 *    replace %b
 * </pre>
 *
 * <h3>See also:</h3>
 * <a href="ivil:/Meta function/moFreeVars">noFreeVars</a>
 *
 * <h3>Result:</h3>
 * <code>true</code> if the first argument (or its instantiation) is (or has
 * been chosen by this condition) a variable which does not occur unbound in the
 * remaining arguments. It fails if the first argument is not matched by a
 * variable.
 */
public class FreshVariable extends WhereCondition {

    public FreshVariable() {
        super("freshVar");
    }

    @Override
    public boolean check(Term[] formalArguments, Term[] actualArguments,
            RuleApplication ruleApp, Environment env) throws RuleException {

        Term first = actualArguments[0];

        if (!(first instanceof Variable)) {
            throw new RuleException(
                    "The first argument of freshVar needs to be a variable, not "
                            + first);
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
     * Try to find a fresh variable instantiation for the first argument. Do
     * nothing if this is not a schema variable.
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
        if (termMap.containsKey(schemaName)) {
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

            termMatcher.addInstantiation(schemaVar,
                    Variable.getInst(varname, type));

        } catch (TermException e) {
            throw new RuleException(e);
        }

    }

    private String freshVarname(FreeVarFinder finder, String prefix) {
        String name = prefix;
        int count = 1;
        while (finder.allVariableNames.contains(name)) {
            name = prefix + count;
            count++;
        }

        return name;
    }

    @Override
    public void checkSyntax(Term[] arguments) throws RuleException {
        if (arguments.length == 0) {
            throw new RuleException("freshVar expects at least one argument");
        }

        if (!(arguments[0] instanceof SchemaVariable)) {
            throw new RuleException(
                    "freshVar expects (schema) variable as first argument");
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
        private final Stack<Variable> boundVariables = new Stack<Variable>();

        /**
         * The free variables.
         */
        Set<Variable> freeVariables = new HashSet<Variable>();

        /**
         * The set of all variable names, bound or free
         */
        Set<String> allVariableNames = new HashSet<String>();

        @Override
        public void visit(Binding binding) throws TermException {
            if (binding.getVariable() instanceof Variable) {
                Variable variable = (Variable) binding.getVariable();
                allVariableNames.add(variable.getName());
                boundVariables.push(variable);
                super.visit(binding);
                boundVariables.pop();
            } else {
                // if schema variable bound
                // LOG if we use logging once
                Log.log(Log.WARNING,
                        "We should actually only check unschematic terms, but: "
                                + binding);
                super.visit(binding);
            }
        }

        @Override
        public void visit(Variable variable) throws TermException {
            allVariableNames.add(variable.getName());
            if (!boundVariables.contains(variable)) {
                freeVariables.add(variable);
            }
        }
    }

}
