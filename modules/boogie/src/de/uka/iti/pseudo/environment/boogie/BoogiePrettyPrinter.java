package de.uka.iti.pseudo.environment.boogie;

import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.prettyprint.PrettyPrintPlugin;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.TermException;

// TODO implement pretty printing for boogie
public final class BoogiePrettyPrinter extends PrettyPrintPlugin {

    @Override
    public void prettyPrintTerm(Application term) throws TermException {
        Function function = term.getFunction();
        String name = function.getName();

        if (name.startsWith("var_")) {
            append(name.substring(name.indexOf("__") + 2, name.length()));
        }
    }

    @Override
    public void prettyPrintTerm(Binding term) throws TermException {
        String name = term.getVariable().getName();

        if (name.startsWith("var_")) {
            // append(name.substring(name.indexOf("__") + 2, name.length()));
        }
    }

}
