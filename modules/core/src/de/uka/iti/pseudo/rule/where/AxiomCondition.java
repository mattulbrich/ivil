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

import de.uka.iti.pseudo.environment.Axiom;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.Term;

/**
 * The where condition "axiom" can be used to introduce axioms known to the
 * system onto the working sequence.
 * 
 * It is usually used in a context like and triggered not by matching but by
 * separate mechanisms in GUI or automation (hence "autoonly")
 * 
 * When applying a rule, the {@link RuleApplication} needs to have the property
 * {@value #AXIOM_NAME_PROPERTY} set to the name of the axiom to be added.
 * Additionally, the parameter (%b in the example) needs to match the axiom's
 * formula.
 * 
 * <pre>
 * rule `axiom`
 *   where `axiom` %b
 *   add %b |-
 *   tags autoonly
 *        display "Insert axiom {property axiomName}"
 * </pre>
 * 
 * @author mattias ulbrich
 */
public class AxiomCondition extends WhereCondition {

    public static final String AXIOM_NAME_PROPERTY = "axiomName";

    public AxiomCondition() {
        super("axiom");
    }

    /**
     * Extract the axiom name from the rule application. Retrieve the axiom and
     * check that the actual paramter is identical.
     */
    @Override
    public boolean check(Term[] formalArguments, Term[] actualArguments,
            RuleApplication ruleApp, Environment env)
            throws RuleException {
        
        String axiomName = ruleApp.getProperties().get(AXIOM_NAME_PROPERTY);
        if(axiomName == null) {
            throw new RuleException("Property axiomName not set on rule application");
        }
        
        Axiom axiom = env.getAxiom(axiomName);
        if(axiom == null) {
            throw new RuleException("Axiom " + axiomName + " not defined in environment");
        }
        
        Term arg = actualArguments[0];
        if(!arg.equals(axiom.getTerm())) {
            // more detailed error message?
            throw new RuleException("Axiom " + axiomName + "("
                    + axiom.getTerm() + ") is not instantiated, but " + arg);
        }
        
        // rather throw an exception than return false in the other cases 
        return true;
        
    }

    /**
     * Syntax check:
     * We expect exactly one boolean argument.
     */
    @Override
    public void checkSyntax(Term[] arguments) throws RuleException {
        if(arguments.length != 1)
            throw new RuleException("axiom expects exactly 1 argument");
        Term arg = arguments[0];
        if(!Environment.getBoolType().equals(arg.getType()))
            throw new RuleException("axiom expects a boolean argument");
    }

}
