package de.uka.iti.pseudo.environment.boogie;

import java.util.regex.Pattern;

import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.prettyprint.PrettyPrintPlugin;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.TermException;

// TODO implement pretty printing for boogie
public final class BoogiePrettyPrinter extends PrettyPrintPlugin {

    private static Pattern LOAD_REGEX = Pattern.compile("map[0-9]+_load");
    private static Pattern STORE_REGEX = Pattern.compile("map[0-9]+_store");
    
    @Override
    public void prettyPrintTerm(Application term) throws TermException {
        Function function = term.getFunction();
        String name = function.getName();
        
        if (LOAD_REGEX.matcher(name).matches()) {

            // map
            printSubterm(term, 0);
            append("[");
            // arguments
            for (int i = 1; i < function.getArity(); i++) {
                if(i > 1) {
                    append(", ");
                }
                printSubterm(term, i);
            }
            append("]");

        } else

        if (STORE_REGEX.matcher(name).matches()) {
            append("{");
            // indices
            for (int i = 1; i < function.getArity()-1; i++) {
                if(i > 1) {
                    append(", ");
                }
                printSubterm(term, i);                
            }
            append(" := ");
            // value
            printSubterm(term, function.getArity() - 1);
            append("}");
            // heap
            printSubterm(term, 0);
        } 
        
    }

    /**
     * replace every Boogie generated name by a name without the generated
     * prefix. For instance the variable <code>var_100_20__x</code> is shortened
     * to just <code>x</code>.
     */
    @Override
    public String getReplacementName(String name) {
        if(name.startsWith("var_") || name.startsWith("old_var_") || name.startsWith("fun__")) {
            int index = name.lastIndexOf("__");
            return name.substring(index+2);
        }
        
        return null;
    }
    
    @Override
    public void prettyPrintTerm(Binding term) throws TermException {
        // nothing to be done
    }

}
