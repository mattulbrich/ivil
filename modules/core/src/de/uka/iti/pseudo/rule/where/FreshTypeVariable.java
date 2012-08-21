/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.rule.where;

import java.util.Map;
import java.util.Set;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.TypeVariableCollector;
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.SchemaType;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.creation.TermInstantiator;
import de.uka.iti.pseudo.term.creation.TermMatcher;
import de.uka.iti.pseudo.util.Util;

/**
 * The where condition FreshVariable can be used to create a fresh variable
 * which does not occur in the condition's other arguments.
 *
 * @ivildoc "Where condition/freshTypeVar"
 *
 * ... to be finished ...
 *
 * <h2>Where condition <tt>freshTypeVar</tt></h2>
 *
 * This condition can be used to ensure that a type variable does not occur free nor
 * bound in terms.
 *
 * <p>
 * This is an example of an <em>active</em> condition since it may add schema
 * instantiations. If the first formal parameter has a schema type, this
 * becomes instantiated with a new type variable which does not
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
 *   to be done
 * </pre>
 *
 * and (to check that a variable does not appear)
 *
 *  <pre>
 *   rule T_all_remove
 *    find (\T_all %'x; %b)
 *    where freshTypeVar %'x, %b
 *    replace %b
 * </pre>
 *
 * <h3>See also:</h3>
 * <a href="ivil:/Where condition/freshVar">noFreeVars</a>
 *
 * <h3>Result:</h3>
 * <code>true</code> if the first argument (or its instantiation) is (or has
 * been chosen by this condition) a variable which does not occur unbound in the
 * remaining arguments. It fails if the first argument is not matched by a
 * variable.
 */
public class FreshTypeVariable extends WhereCondition {

    public FreshTypeVariable() {
        super("freshTypeVar");
    }

    @Override
    public boolean check(Term[] formalArguments, Term[] actualArguments,
            RuleApplication ruleApp, Environment env) throws RuleException {

        Term first = actualArguments[0];
        Type type = first.getType();

        if (!(type instanceof TypeVariable)) {
            throw new RuleException(
                    "The first argument of freshTypeVar needs to have a variable type, not "
                            + type);
        }

        Set<TypeVariable> typeVariables = TypeVariableCollector.collectInTerms(
                Util.readOnlyArrayList(actualArguments, 1, actualArguments.length));

        return !typeVariables.contains(type);
    }

    /**
     * Try to find a fresh type variable instantiation for the first argument. Do
     * nothing if this is not a schema variable.
     */
    @Override
    public void addInstantiations(TermMatcher termMatcher, Term[] arguments)
            throws RuleException {

        // checked in checkSyntax
        assert arguments[0].getType() instanceof SchemaType;

        SchemaType schemaType = (SchemaType) arguments[0].getType();
        String schemaTypeName = schemaType.getVariableName();
        Map<String, Type> typeMap = termMatcher.getTypeInstantiation();
        TermInstantiator termInstantiator = termMatcher.getTermInstantiator();

        // do nothing if already instantiated
        if (typeMap.containsKey(schemaTypeName)) {
            return;
        }

        try {
            Term actual[] = new Term[arguments.length - 1];
            for (int i = 0; i < actual.length; i++) {
                actual[i] = termInstantiator.instantiate(arguments[i + 1]);
            }

            Set<TypeVariable> typeVariables =
                    TypeVariableCollector.collectInTerms(Util.readOnlyArrayList(actual));

            String newName = schemaTypeName;

            // bugfix for unnamed variables
            if(schemaType.isTemporary()) {
                newName = "v" + newName;
            }

            int count = 1;
            while (typeVariables.contains(TypeVariable.getInst(newName))) {
                newName = schemaTypeName + count;
                count++;
            }

            termMatcher.addTypeInstantiation(schemaType.getVariableName(),
                    TypeVariable.getInst(newName));

        } catch (TermException e) {
            throw new RuleException(e);
        }

    }

    @Override
    public void checkSyntax(Term[] arguments) throws RuleException {
        if (arguments.length == 0) {
            throw new RuleException("freshVar expects at least one argument");
        }

        if (!(arguments[0].getType() instanceof SchemaType)) {
            throw new RuleException(
                    "freshTypeVar expects first argument with schema type");
        }
    }
}
