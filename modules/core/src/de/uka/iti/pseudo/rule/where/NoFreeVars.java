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
package de.uka.iti.pseudo.rule.where;

import java.util.HashSet;
import java.util.Set;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Variable;
import de.uka.iti.pseudo.term.creation.DefaultTermVisitor;
import de.uka.iti.pseudo.util.Log;

/**
 * The WhereCondition NoFreeVars ensures that the argument does not contain free
 * variables.
 * 
 * @ivildoc "Where condition/noFreeVars"
 * 
 * <h2>Where condition <tt>noFreeVars</tt></h2>
 * This condition can be used to ensure that a term contains no unbound variables.
 * 
 * <h3>Syntax</h3>
 * The where condition expects exactly one argument of any type.
 * This can be a schema variable.
 * 
 * <h3>Example:</h3>
 * <pre>
 *   rule cut_cond
 *     find cond(%c, %a, %b)
 *   where 
 *     toplevel
 *   where
 *     noFreeVars(%c)
 *   samegoal "Assume true for {%c}"
 *     add %c |-
 *     replace %a
 *   samegoal "Assume false for {%c}"
 *     add |- %c
 * </pre>
 * 
 * <h3>See also:</h3>
 * <a href="ivil:/Meta function/freshVar">freshVar</a>
 * 
 * <h3>Result:</h3>
 * 
 * <code>true</code> if the argument has no free variables, 
 * <code>false</code> otherwise,
 * never fails. 
 */
public class NoFreeVars extends WhereCondition {

    /**
     * This is the simple visitor with which the variable freeness is checked.
     */
    private static final FreeVarChecker FREE_VAR_CHECKER = new FreeVarChecker();

    public NoFreeVars() {
        super("noFreeVars");
    }

    @Override public void checkSyntax(Term[] arguments) throws RuleException {
        if (arguments.length != 1)
            throw new RuleException("noFreeVars expects exactly 1 arguments");

//        // XXX Is this correct? Why is that needed?
//        if (!(arguments[0] instanceof SchemaVariable))
//            throw new RuleException(
//                    "notFreeIn expects (schema) variable as first argument");
    }

    @Override 
    public boolean check(Term[] formalArguments,
            Term[] actualArguments, RuleApplication ruleApp,
            Environment env) throws RuleException {

        try {
            actualArguments[0].visit(FREE_VAR_CHECKER);
            // no exception: no free variable
            return true;
        } catch (TermException e) {
            // exception: free variable encountered
            return false;
        }
    }

    /**
     * This visitor is used to traverse the term. It throws an exception if an
     * unbound var is found.
     * 
     * @see FreshVariable.FreeVarFinder
     */
    private static class FreeVarChecker extends
            DefaultTermVisitor.DepthTermVisitor {

        /**
         * The bound variables.
         */
        private Set<Variable> boundVariables = new HashSet<Variable>();

        /*
         * (non-Javadoc)
         * 
         * @see de.uka.iti.pseudo.term.creation.DefaultTermVisitor#visit(de.uka.iti.pseudo.term.Binding)
         */
        @Override public void visit(Binding binding) throws TermException {
            if (binding.getVariable() instanceof Variable) {
                Variable variable = (Variable) binding.getVariable();
                boundVariables.add(variable);
                super.visit(binding);
                boundVariables.remove(variable);
            } else {
                // if schema variable bound
                // LOG if we use logging once
                Log.log("We should actually only check unschematic terms, but: "
                                + binding);
                super.visit(binding);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see de.uka.iti.pseudo.term.creation.DefaultTermVisitor#visit(de.uka.iti.pseudo.term.Variable)
         */
        @Override public void visit(Variable variable) throws TermException {
            if (!boundVariables.contains(variable)) {
                boundVariables.clear();
                throw new TermException("Unbound variable found: " + variable);
            }
        }
        
        // TODO Implement depth search for variables in programs
        @Override
        public void visit(LiteralProgramTerm literalProgramTerm)
                throws TermException {
            throw new TermException("OVERAPPROXIMATION: Programs may potentially contain unbound variables");
        }
    }

}