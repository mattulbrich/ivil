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
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.UpdateTerm;

public class UpdSimplMetaFunction extends AbstractUpdSimplMetaFunction {
    
    public UpdSimplMetaFunction() {
        super("$$updSimpl");
    }

    @Override
    public Term evaluate(Application application, Environment env,
            RuleApplication ruleApp) throws TermException {
        
        Term arg = application.getSubterm(0);
        
        if (arg instanceof UpdateTerm) {
            UpdateTerm updTerm = (UpdateTerm) arg;
            
            Term resultTerm = applyUpdate(updTerm);
            if (resultTerm == null)
                throw new TermException("nothing to update");

            return resultTerm;
        } else {
            throw new TermException("Update Simplifier only applicable to update terms");
        }
    }
    
}
