package test;

import de.uka.iti.pseudo.environment.Binder;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.prettyprint.PrettyPrintPlugin;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.TermException;

public class SetPrettyPrinter extends PrettyPrintPlugin {

    @Override
    public void prettyPrintTerm(Application term) throws TermException {
        Function function = term.getFunction();
        String name = function.getName();
        int arity = function.getArity();
        
        if("singleton".equals(name) && arity == 1) {
            append("{");
            printSubterm(term, 0);
            append("}");
        }
    }

    @Override
    public void prettyPrintTerm(Binding term) throws TermException {
        Binder binder = term.getBinder();
        String name = binder.getName();
        
        if("\\set".equals(name)) {
            append("{");
            printBoundVariable(term);
            append(" | ");
            printSubterm(term, 0);
            append("}");
        }
    }

    @Override
    public String getReplacementName(String name) {
        // nothing to do
        return null;
    }

}
