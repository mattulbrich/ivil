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
package de.uka.iti.pseudo.rule.meta;

import nonnull.Nullable;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.UpdateTerm;
import de.uka.iti.pseudo.term.creation.RebuildingTermVisitor;

public class DeepUpdSimplMetaFunction extends AbstractUpdSimplMetaFunction {

    public DeepUpdSimplMetaFunction() throws EnvironmentException {
        super("$$deepUpdSimpl");
    }

    @Override
    public Term evaluate(Application application, Environment env,
            RuleApplication ruleApp) throws TermException {
        
        Term arg = application.getSubterm(0);
        Visitor visitor = new Visitor();
        
        arg.visit(visitor);
        
        Term result = visitor.getResultingTerm();
        if(result == null)
            throw new TermException("deep update simplification has nothing to do.");
        
        return result;
    }
    
    private static class Visitor extends RebuildingTermVisitor {
        
        @Override
        public void visit(UpdateTerm updateTerm) throws TermException {
            Term applied = applyUpdate(updateTerm);
            
            if(applied == null) {
                // nothing to be changed ... return null
                resultingTerm = null;
            } else {
                applied.visit(this);
                if(resultingTerm == null) {
                    // no further applications
                    resultingTerm = applied;
                }
            }
        }
        
        /**
         * @return the resultingTerm
         */
        public @Nullable Term getResultingTerm() {
            return resultingTerm;
        }

        
    }

}
