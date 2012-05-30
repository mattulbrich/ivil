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

import nonnull.Nullable;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermComparator;

/**
 * @ivildoc "Where condition/unorderedTerms"
 *
 * TODO
 *
 * The where condition UnorderedTerms can be used to check whether two terms are
 * in a certain order.
 *
 * It is used in the normalisation of equations
 * <pre>
 * rule equality_order
 *   find %t = %u |-
 *   where
 *     unorderedTerms %t, %u
 *   replace %u = %t
 * </pre>
 *
 * It signals true iff the order value of %t is greater than the one of %u.
 */
public class UnorderedTerms extends WhereCondition {

    /**
     * Instantiates the where condition.
     */
    public UnorderedTerms() {
        super("unorderedTerms");
    }

    /**
     * The term comparator to be used
     */
    private @Nullable TermComparator termComparator;

    /* (non-Javadoc)
     * @see de.uka.iti.pseudo.environment.WhereCondition#check(de.uka.iti.pseudo.term.Term[], de.uka.iti.pseudo.term.Term[], de.uka.iti.pseudo.proof.RuleApplication, de.uka.iti.pseudo.proof.ProofNode, de.uka.iti.pseudo.environment.Environment)
     */
    @Override
    public boolean check(Term[] formalArguments, Term[] actualArguments,
            RuleApplication ruleApp, Environment env)
            throws RuleException {

        if(termComparator == null) {
            termComparator = new TermComparator(env);
        }

        assert termComparator.getEnvironment() == env;

        return termComparator.compare(actualArguments[0], actualArguments[1]) > 0;

    }

    /* (non-Javadoc)
     * @see de.uka.iti.pseudo.environment.WhereCondition#checkSyntax(de.uka.iti.pseudo.term.Term[])
     */
    @Override
    public void checkSyntax(Term[] arguments) throws RuleException {
        if(arguments.length != 2) {
            throw new RuleException("orderedTerms expects exactly 2 arguments");
        }
    }

}
