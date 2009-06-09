package de.uka.iti.pseudo.gui;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.FixOperator;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.ModalityTerm;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.TermVisitor;
import de.uka.iti.pseudo.term.Variable;
import de.uka.iti.pseudo.util.AttributedString;

public class PrettyPrint implements TermVisitor {

    private Environment env;
    private boolean typed;
    private boolean printFix;
        
    public PrettyPrint(Environment env) {
        this(env, false);
    }

    public PrettyPrint(Environment env, boolean typed) {
        this.env = env;
        this.typed = typed;
    }

    public Environment getEnv() {
        return env;
    }

    public boolean isTyped() {
        return typed;
    }

    public void setTyped(boolean typed) {
        this.typed = typed;
    }
    
    private AttributedString<Term> printer;

    @Override public void visit(Variable variable) throws TermException {
        printer.begin(variable).append(variable.toString(isTyped())).end();
    }

    @Override public void visit(ModalityTerm modalityTerm) throws TermException {
        // TODO Auto-generated method stub
        
    }

    @Override public void visit(Binding binding) throws TermException {
        // TODO Auto-generated method stub
        
    }

    @Override public void visit(Application application) throws TermException {
        printer.begin(application);
        Function function = application.getFunction();
        String fctname = function.getName();
        
        FixOperator fixOperator = null;
        if(printFix)
            fixOperator = env.getReverseFixOperator(fctname);
        
        if(fixOperator != null) {
            if(function.getArity() == 1) {
                // prefix
            } else {
                // infix;
            }
        } else {
            printer.append(fctname).append("(");
            for (Term t : application.getSubterms()) {
                t.visit(this);
                if(true)
                    printer.append(", ");
            }
            printer.append(")");
            if(isTyped())
                printer.append(" as " + application.getType());
            printer.end();
        }
    }

    @Override public void visit(SchemaVariable schemaVariable)
            throws TermException {
        printer.begin(schemaVariable).append(schemaVariable.toString(isTyped())).end();
    }


}
