package de.uka.iti.pseudo.gui;

import de.uka.iti.pseudo.environment.Binder;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.prettyprint.PrettyPrintPlugin;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.statement.AssignmentStatement;

public class MockPrettyPrintPlugin extends PrettyPrintPlugin {

    @Override public void prettyPrintTerm(Application term)
            throws TermException {
        Function f = term.getFunction();
        if("f".equals(f.getName())) {
            append("{-");
            printSubterm(term.getSubterm(0));
            append("-}");
        }
        
        // inverse order of subterms!
        if("g".equals(f.getName())) {
            append("g[");
            printSubterm(term.getSubterm(1));
            append(", ");
            printSubterm(term.getSubterm(0));
            append("]");
        }
    }

    @Override public void prettyPrintTerm(Binding term) throws TermException {
        Binder b = term.getBinder();
        if("\\forall".equals(b.getName())) {
            append("ALL ");
            printBoundVariable(term);
            append(" ; ");
            printSubterm(term.getSubterm(0));
        }
    }

    @Override public void prettyPrintUpdate(AssignmentStatement assignment)
            throws TermException {
        if("i1".equals(assignment.getTarget().toString())) {
            append("i1 <-- ");
            printSubterm(assignment.getValue());
        }
    }

}
