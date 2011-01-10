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
package de.uka.iti.pseudo.gui;

import de.uka.iti.pseudo.environment.Binder;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.prettyprint.PrettyPrintPlugin;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.statement.Assignment;

public class MockPrettyPrintPlugin extends PrettyPrintPlugin {

    @Override public void prettyPrintTerm(Application term)
            throws TermException {
        Function f = term.getFunction();
        if("f".equals(f.getName())) {
            append("{-");
            printSubterm(term, 0);
            append("-}");
        }
        
        // inverse order of subterms!
        if("g".equals(f.getName())) {
            append("g[");
            printSubterm(term, 1);
            append(", ");
            printSubterm(term, 0);
            append("]");
        }
    }

    @Override public void prettyPrintTerm(Binding term) throws TermException {
        Binder b = term.getBinder();
        if("\\forall".equals(b.getName())) {
            append("ALL ");
            printBoundVariable(term);
            append(" ; ");
            printSubterm(term, 0);
        }
    }

//    @Override public void prettyPrintUpdate(AssignmentStatement assignment)
//            throws TermException {
//        if("i1".equals(assignment.getTarget().toString())) {
//            append("i1 <-- ");
//            printTerm(assignment.getValue());
//        }
//    }

}
