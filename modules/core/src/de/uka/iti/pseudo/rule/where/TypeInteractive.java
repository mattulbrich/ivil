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
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;

// TODO Documentation needed
public class TypeInteractive extends WhereCondition {

    public static final String INTERACTION = "typeInteract";

    public TypeInteractive() {
        super(INTERACTION);
    }

    @Override
    public void checkSyntax(Term[] arguments) throws RuleException {
        if(arguments.length != 1) {
            throw new RuleException("interact expects exactly 1 argument");
        }

        Type t = arguments[0].getType();
        if(!(t instanceof TypeVariable)) {
            throw new RuleException("interact expects type variable type for its argument");
        }
    }

    @Override
    public boolean check(Term[] formalArguments,
            Term[] actualArguments, RuleApplication ruleApp,
            Environment env) throws RuleException {
        if(ruleApp.hasMutableProperties()) {
            TypeVariable tv = (TypeVariable) formalArguments[0].getType();
            Term actualTerm = actualArguments[0];
            ruleApp.getProperties().put(INTERACTION + "(" + tv.getVariableName() + ")",
                    actualTerm.getType().toString());
        }
        return true;
    }

}
