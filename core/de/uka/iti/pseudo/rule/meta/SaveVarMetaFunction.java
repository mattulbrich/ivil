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

import static de.uka.iti.pseudo.term.TypeVariable.ALPHA;
import static de.uka.iti.pseudo.term.TypeVariable.BETA;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.MetaFunction;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.creation.RebuildingTermVisitor;

// TODO Finish when needed one day ;)
// ????????????????????
public class SaveVarMetaFunction extends MetaFunction {

    public SaveVarMetaFunction() throws EnvironmentException {
        super(ALPHA, "$$saveVar", BETA );
        // TODO Auto-generated constructor stub
    }

    @Override 
    public Term evaluate(Application application, Environment env,
            RuleApplication ruleApp) throws TermException {
        
        Term toReplace = application.getSubterm(0);
        Term replaceWith = application.getSubterm(1);
        Term replaceIn = application.getSubterm(2);
        
        TermReplacer tr = new TermReplacer();
        return tr.replace(toReplace, replaceWith, replaceIn);
        
    }
    
   static class TermReplacer extends RebuildingTermVisitor {
        
        private Term termToReplace;
        private Term replaceWith;
        
        @Override 
        protected void defaultVisitTerm(Term term)
                throws TermException {
            if(term.equals(termToReplace)) {
                resultingTerm = replaceWith;
            } else {
                resultingTerm = null;
            }
        }
        
        @Override public void visit(Binding binding) throws TermException {
            // TODO method documentation
            // TODO make test case for that
            if(binding.getVariable().equals(termToReplace))
                resultingTerm = null;
            else
                super.visit(binding);
        }
        
        public Term replace(Term termToReplace, Term replaceWith, Term replaceIn) throws TermException {
            this.termToReplace = termToReplace;
            this.replaceWith = replaceWith;
            replaceIn.visit(this);
            return resultingTerm == null ? replaceIn : resultingTerm;
        }
        
    }


}
