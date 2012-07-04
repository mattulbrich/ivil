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
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeApplication;
import de.uka.iti.pseudo.util.TermUtil;

// TODO DOC
// TODO Move this to pseudo.bytecode

public class JavaHeapPrettyPrinter extends PrettyPrintPlugin {

    @Override public void prettyPrintTerm(Application application) throws TermException {

        Function function = application.getFunction();
        String name = function.getName();

        if ("$load_heap".equals(name)) {

            printSubterm(application, 1);

            // check special case of array access - drop dot then
            if(!TermUtil.isFunctionApplication(application.getSubterm(2), "$array_index")) {
                append(".");
            }

            // field
            printSubterm(application, 2);

            if(!TermUtil.isFunctionApplication(application.getSubterm(0), "$heap")) {
                append("@");
                printSubterm(application, 0);
            }
        } else

        if("$array_index".equals(function.getName())) {
            append("[");
            printSubterm(application, 0);
            append("]");
        }
    }

    /*
     * can this type be unified with "field('a)"?
     */
    private boolean isFieldType(Type type) {
        if (type instanceof TypeApplication) {
            TypeApplication tyApp = (TypeApplication) type;
            if("field".equals(tyApp.getSort().getName())) {
                return true;
            }
        }
        return false;
    }

    @Override public void prettyPrintTerm(Binding term) {
        // do nothing
    }

    @Override
    public String getReplacementName(String name) {
        if (name.startsWith("R_")) {
            int last_ = name.lastIndexOf('_');
            if (last_ > 2) {
                return name.substring(2, last_);
            }
        } else

        if (name.equals("$array_length")) {
            return "length";
        } else

        if (name.equals("$array_index")) {

        }

        return null;
    }

}
