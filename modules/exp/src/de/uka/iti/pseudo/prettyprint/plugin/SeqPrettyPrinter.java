/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.prettyprint.plugin;

import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.prettyprint.PrettyPrintPlugin;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.TermException;

// TODO DOC
// TODO Move this to pseudo.bytecode

public class SeqPrettyPrinter extends PrettyPrintPlugin {

    @Override public void prettyPrintTerm(Application application) throws TermException {

        Function function = application.getFunction();
        String name = function.getName();

        if ("seqGet".equals(name)) {

            printSubterm(application, 0);
            append("[");
            printSubterm(application, 1);
            append("]");
        }
    }

    @Override public void prettyPrintTerm(Binding term) {
        // do nothing
    }

    @Override
    public String getReplacementName(String name) {
        return null;
    }

}
