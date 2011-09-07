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
package de.uka.iti.pseudo.prettyprint.plugin;

import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.prettyprint.PrettyPrintPlugin;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.TermException;

/**
 * Use this plugin to format load and store function applications for some map
 * type X:
 * 
 * {@code $load_X(a,b,c) --> a[b, c] }
 * {@code $store_X(a,b,c,d) --> a[b, c := d] }
 * 
 * @author timm felden
 */
public class MapPrettyPrinter extends PrettyPrintPlugin {

    @Override public void prettyPrintTerm(Application application) throws TermException {
        
        Function function = application.getFunction();
        String name = function.getName();
        
        if (name.startsWith("$load_")) {
            // map
            printSubterm(application, 0);
            append("[");
            if (application.getSubterms().size() > 1){
                printSubterm(application, 1);
                for (int i = 2; i < application.getSubterms().size(); i++) {
                    append(", ");
                    printSubterm(application, 1);
                }
            }
            append("]");
        } else if (name.startsWith("$store_")) {
            // map
            printSubterm(application, 0);
            append("[");
            if (application.getSubterms().size() - 1 > 1) {
                printSubterm(application, 1);
                for (int i = 2; i < application.getSubterms().size() - 1; i++) {
                    append(", ");
                    printSubterm(application, 1);
                }
            }
            append(" := ");
            printSubterm(application, application.getSubterms().size() - 1);
            append("]");
        }
    }

    @Override public void prettyPrintTerm(Binding term) {
        // do nothing
    }

    @Override
    public String getReplacementName(String name) {
		// nothing to do
        return null;
    }

}
