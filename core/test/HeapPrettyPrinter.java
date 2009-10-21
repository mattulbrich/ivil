package test;

import de.uka.iti.pseudo.prettyprint.PrettyPrintPlugin;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.statement.AssignmentStatement;

public class HeapPrettyPrinter extends PrettyPrintPlugin {

    @Override public void prettyPrintTerm(Application application) throws TermException {
        
        if ("sel".equals(application.getFunction().getName())) {

            // obj
            printSubterm(application, 1);
            append(".");
            // field
            printSubterm(application, 2);

            if(!"h".equals(application.toString(false))) {
                append("@");
                // heap
                printSubterm(application, 0);
            }
        } else

        if ("stor".equals(application.getFunction().getName())) {
            append("{");
            // obj
            printSubterm(application, 1);
            append(".");
            // field
            printSubterm(application, 2);
            append(" := ");
            // value
            printSubterm(application, 3);
            append("}@");
            // heap
            printSubterm(application, 0);
        }
        
    }

    @Override public void prettyPrintTerm(Binding term) {
        // do nothing
    }

}
