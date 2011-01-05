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

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.TypeVariableCollector;
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;

public class DifferentTypesInEq extends WhereCondition {

    public DifferentTypesInEq() {
        super("differentTypesInEq");
    }

    @Override public boolean check(Term[] formalArguments,
            Term[] actualArguments, RuleApplication ruleApp, ProofNode goal,
            Environment env) throws RuleException {

        Type type1 = actualArguments[0].getType();
        Type type2 = actualArguments[1].getType();

        if (!TypeVariableCollector.collect(type1).isEmpty())
            return false;

        if (!TypeVariableCollector.collect(type2).isEmpty())
            return false;

        return ! type1.equals(type2);

    }

    @Override public void checkSyntax(Term[] arguments) throws RuleException {
        if (arguments.length != 2)
            throw new RuleException("differentTypes expects two arguments");
    }

}
