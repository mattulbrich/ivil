package test;

import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.prettyprint.PrettyPrintPlugin;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.TermException;

public class PairPrettyPrinter extends PrettyPrintPlugin {

    @Override
    public void prettyPrintTerm(Application term) throws TermException {
        Function function = term.getFunction();
        String name = function.getName();
        int arity = function.getArity();
        
        if("pair".equals(name) && arity == 2) {
            append("\u2329");
            printSubterm(term, 0);
            append(", ");
            printSubterm(term, 1);
            append("\u232a");
        }
    }

    @Override
    public void prettyPrintTerm(Binding term) throws TermException {
        // do nothing
    }

    @Override
    public String getReplacementName(String name) {
        // nothing to do
        return null;
    }

}