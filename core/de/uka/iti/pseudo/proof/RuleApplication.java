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
package de.uka.iti.pseudo.proof;

import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.Update;

// TODO Documentation needed
public interface RuleApplication {

    public Rule getRule();
    
    public boolean hasMutableProperties();

    public int getGoalNumber();

    public TermSelector getFindSelector();

    public List<TermSelector> getAssumeSelectors();

    public Map<String, Term> getSchemaVariableMapping();
    
    public Map<String, Update> getSchemaUpdateMapping();

    public Map<String, Type> getTypeVariableMapping();

    public Map<String, String> getProperties();
}