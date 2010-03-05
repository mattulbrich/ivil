/*
 * This file is part of PSEUDO
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.rule.where;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.UpdateTerm;

/**
 * The {@link WhereCondition} TopLevel ensures that a term is not within the
 * reach of an update.
 */
public class TopLevel extends WhereCondition {

    public TopLevel() {
        super("toplevel");
    }

    @Override 
    public boolean check(Term[] formalArguments,
            Term[] actualArguments, RuleApplication ruleApp, ProofNode goal,
            Environment env) throws RuleException {
        try {
            
            return check(ruleApp.getFindSelector(), goal.getSequent());
            
        } catch (ProofException e) {
            throw new RuleException(e);
        }
        
    }
    
    /* intermediate step to allow testing */
    /**
     * Check that the selected term is not within an {@link UpdateTerm}.
     * 
     * @param select
     *            the term selector to be checked
     * @param sequent
     *            the sequent to check in
     * 
     * @return true if the term is not within the reach of an update
     * 
     * @throws ProofException
     *             never thrown here.
     */
    boolean check(TermSelector select, Sequent sequent) throws ProofException {
        
        Term term = select.selectTopterm(sequent);
        
        for (int p : select.getPath()) {
            if(term instanceof UpdateTerm && p == 0)
                return false;
            term = term.getSubterm(p);
        }
        
        return true;
    }
    
    @Override 
    public void checkSyntax(Term[] arguments) throws RuleException {
        if(arguments.length > 0)
            throw new RuleException("toplevel expects no arguments");
    }
    
}
