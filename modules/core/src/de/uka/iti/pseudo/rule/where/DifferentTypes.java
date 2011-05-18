/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.rule.where;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.TypeVariableCollector;
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;

/**
 * The condition DifferentTypes returns true if the two actual arguments of the
 * condition have different ground types.
 * 
 * At the moment, the
 * {@link #check(Term[], Term[], RuleApplication, Environment)} method throws an
 * exception if type variables are encountered. This behaviour will most
 * probably be changed (was due to different semantics in earlier days).
 * 
 * @author mattias ulbrich
 */
public class DifferentTypes extends WhereCondition {

    /**
     * Instantiates a new different types.
     */
    public DifferentTypes() {
        super("differentTypes");
    }

    @Override 
    public boolean check(Term[] formalArguments,
            Term[] actualArguments, RuleApplication ruleApp,
            Environment env) throws RuleException {

        Type type1 = actualArguments[0].getType();
        Type type2 = actualArguments[1].getType();

        if (!TypeVariableCollector.collect(type1).isEmpty()) {
            throw new RuleException("type has free type variables for "
                    + actualArguments[0].toString(true));
        }

        if (!TypeVariableCollector.collect(type2).isEmpty()) {
            throw new RuleException("type has free type variables for "
                    + actualArguments[1].toString(true));
        }

        return ! type1.equals(type2);

    }

    @Override public void checkSyntax(Term[] arguments) throws RuleException {
        if (arguments.length != 2)
            throw new RuleException("differentTypes expects two arguments");
    }

}
