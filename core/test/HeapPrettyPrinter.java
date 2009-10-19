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
            Term heap = application.getSubterm(0);
            Term obj = application.getSubterm(1);
            Term field = application.getSubterm(2);

            printSubterm(obj);
            append(".");
            printSubterm(field);

            if(!"h".equals(heap.toString(false))) {
                append("@");
                printSubterm(heap);
            }
        } else

        if ("stor".equals(application.getFunction().getName())) {
            Term heap = application.getSubterm(0);
            Term obj = application.getSubterm(1);
            Term field = application.getSubterm(2);
            Term value = application.getSubterm(3);
            
            append("{");
            printSubterm(obj);
            append(".");
            printSubterm(field);
            append(" := ");
            printSubterm(value);
            append("}@");
            printSubterm(heap);
        }
        
    }

    @Override public void prettyPrintTerm(Binding term) {
        // do nothing
    }

    @Override public void prettyPrintUpdate(AssignmentStatement assignment) throws TermException {
        // do nothing
//        Term lhs = assignment.getTarget();
//        Term rhs = assignment.getValue();
//        Function fct = rhs instanceof Application ? ((Application)rhs).getFunction() : null;
//        
//        if("h".equals(lhs.toString(false)) && fct != null && "stor".equals(fct.getName())) {
//            
//        }
    }

}
