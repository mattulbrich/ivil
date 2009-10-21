package test;

import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.prettyprint.PrettyPrintPlugin;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeApplication;

public class HeapPrettyPrinter extends PrettyPrintPlugin {

    @Override public void prettyPrintTerm(Application application) throws TermException {
        
        Function function = application.getFunction();
        String name = function.getName();
        
        if ("sel".equals(name)) {

            // obj
            printSubterm(application, 1);
            append(".");
            // field
            printSubterm(application, 2);

            if(!"h".equals(application.getSubterm(0).toString(false))) {
                append("@");
                // heap
                printSubterm(application, 0);
            }
        } else

        if ("stor".equals(name)) {
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
        } else
        
        if(isFieldType(function.getResultType()) && function.getArity() == 0 &&
                name.startsWith("field_")) {
            int last_ = name.lastIndexOf('_');
            append(name.substring(last_+1));
        }
                
        
    }

    /*
     * can this type be unified with "field('a)"?
     */
    private boolean isFieldType(Type type) {
        if (type instanceof TypeApplication) {
            TypeApplication tyApp = (TypeApplication) type;
            if("field".equals(tyApp.getSort().getName()))
                return true;
        }
        return false;
    }

    @Override public void prettyPrintTerm(Binding term) {
        // do nothing
    }

}
