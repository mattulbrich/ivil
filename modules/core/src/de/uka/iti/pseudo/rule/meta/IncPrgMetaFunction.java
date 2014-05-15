/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.rule.meta;

import de.uka.iti.pseudo.environment.AbstractMetaFunction;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;

/**
 * The Class IncPrgMetaFunction can be used to increment the index of a program
 * term. It is usually used in symbolic execution rules to step to the next
 * statement.
 */
public class IncPrgMetaFunction extends AbstractMetaFunction {
    
    private static final Type BOOL = Environment.getBoolType();
    
    public IncPrgMetaFunction() throws EnvironmentException {
        super(BOOL, "$$incPrg", BOOL);
    }

    @Override 
    public Term evaluate(Application application, Environment env,
            RuleApplication ruleApp) throws TermException {
        
        Term arg = application.getSubterm(0);
        
        if (arg instanceof LiteralProgramTerm) {
            LiteralProgramTerm progTerm = (LiteralProgramTerm) arg;
            return LiteralProgramTerm.getInst(
                    progTerm.getProgramIndex() + 1, 
                    progTerm);
        } else {
            throw new TermException("not a program term " + arg);
        }
        
    }
}
