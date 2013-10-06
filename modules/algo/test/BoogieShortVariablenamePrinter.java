//package test;
//
//import java.util.List;
//
//import de.uka.iti.pseudo.environment.Function;
//import de.uka.iti.pseudo.prettyprint.PrettyPrintPlugin;
//import de.uka.iti.pseudo.term.Application;
//import de.uka.iti.pseudo.term.Binding;
//import de.uka.iti.pseudo.term.Term;
//import de.uka.iti.pseudo.term.TermException;
//
//public class BoogieShortVariablenamePrinter extends PrettyPrintPlugin {
//
//    @Override
//    public void prettyPrintTerm(Application term) throws TermException {
//
//        Function f = term.getFunction();
//        String name = f.getName();
//        int index = name.lastIndexOf("__");
//        if (index >= 0) {
//            name = name.substring(index + 2);
//            printApplication(term, name);
//        }
//
//    }
//
//    /*
//     * print an application in non-operator prefix form.
//     */
//    private void printApplication(Application application, String fctname)
//            throws TermException {
//        append(fctname);
//        List<Term> subterms = application.getSubterms();
//        if (subterms.size() > 0) {
//            for (int i = 0; i < subterms.size(); i++) {
//                append(i == 0 ? "(" : ", ");
//                printSubterm(application, i);
//            }
//            append(")");
//        }
//        if (isTyped()) {
//            append(" as " + application.getType());
//        }
//    }
//
//    @Override
//    public void prettyPrintTerm(Binding term) throws TermException {
//        // nothing to do
//    }
//
//}
