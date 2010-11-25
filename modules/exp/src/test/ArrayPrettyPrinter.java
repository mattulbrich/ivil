package test;

import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.prettyprint.PrettyPrintPlugin;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.TermException;

public class ArrayPrettyPrinter extends PrettyPrintPlugin {

    @Override
    public void prettyPrintTerm(Application term) throws TermException {
        Function function = term.getFunction();
        String name = function.getName();
        int arity = function.getArity();
        
        if("read".equals(name) && arity == 2) {
            printSubterm(term, 0);
            append("[");
            printSubterm(term, 1);
            append("]");
        } else
            
        if("write".equals(name) && arity == 3) {
            printSubterm(term, 0);
            append("\u2295{");
            // index
            printSubterm(term, 1);
            append("\u21A6");
            // value
            printSubterm(term, 2);
            append("}");
        }
    }

    @Override
    public void prettyPrintTerm(Binding term) throws TermException {
        // do nothing
    }

}
