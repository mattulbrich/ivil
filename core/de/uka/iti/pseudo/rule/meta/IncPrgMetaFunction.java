/*
 * This file is part of PSEUDO
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.rule.meta;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.MetaFunction;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;

// TODO Documentation needed
public class IncPrgMetaFunction extends MetaFunction {
    
    private static final Type BOOL = Environment.getBoolType();
    
    public IncPrgMetaFunction() {
        super(BOOL, "$$incPrg", BOOL);
    }

    @Override 
    public Term evaluate(Application application, Environment env,
            RuleApplication ruleApp) throws TermException {
        
        Term arg = application.getSubterm(0);
        
        if (arg instanceof LiteralProgramTerm) {
            LiteralProgramTerm progTerm = (LiteralProgramTerm) arg;
            return new LiteralProgramTerm(progTerm.getProgramIndex() + 1, progTerm);
        } else {
            throw new TermException("not a program term " + arg);
        }
        
    }
}
