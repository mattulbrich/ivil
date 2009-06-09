package de.uka.iti.pseudo.gui;

import java.util.List;

import de.uka.iti.pseudo.environment.Binder;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.FixOperator;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.AssignModality;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.CompoundModality;
import de.uka.iti.pseudo.term.IfModality;
import de.uka.iti.pseudo.term.Modality;
import de.uka.iti.pseudo.term.ModalityTerm;
import de.uka.iti.pseudo.term.ModalityVisitor;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.SkipModality;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.TermVisitor;
import de.uka.iti.pseudo.term.Variable;
import de.uka.iti.pseudo.term.WhileModality;
import de.uka.iti.pseudo.util.AnnotatedString;

//TODO DOC
public class PrettyPrint implements TermVisitor, ModalityVisitor {
    
    
    public static AnnotatedString<Term> print(Environment env, Term term) {
        return print(env, term, false, true);
    }

    public static AnnotatedString<Term> print(Environment env, Term term, boolean typed, boolean printFix) {
        PrettyPrint pp = new PrettyPrint(env, typed, printFix);
        
        try {
            term.visit(pp);
        } catch (TermException e) {
            // not thrown in this code
            throw new Error(e);
        }
        
        assert pp.printer.hasEmptyStack();
        
        return pp.printer;
        
    }
    

    private Environment env;
    private boolean typed;
    private boolean printFix;
        
    private PrettyPrint(Environment env, boolean typed, boolean printFix) {
        this.env = env;
        this.typed = typed;
        this.printFix = printFix;
        
        printer = new AnnotatedString<Term>();
    }

    public Environment getEnv() {
        return env;
    }

    public boolean isTyped() {
        return typed;
    }

    private boolean isPrintingFix() {
        return printFix;
    }
    
    private AnnotatedString<Term> printer;
    private boolean inParens;
    
    private void visitMaybeParen(Term subterm, int precedence) throws TermException {
        
        int innerPrecedence = getPrecedence(subterm);
        if( isTyped() && innerPrecedence < Integer.MAX_VALUE || 
           !isTyped() && innerPrecedence < precedence) {
            inParens = true;
            subterm.visit(this);
        } else {
            subterm.visit(this);
        }
        
    }

    private int getPrecedence(Term subterm) {

        if (isPrintingFix() &&  subterm instanceof Application) {
            Application appl = (Application) subterm;
            FixOperator fix = env.getReverseFixOperator(appl.getFunction().getName());
            if(fix != null)
                return fix.getPrecedence();
        }
        
        return Integer.MAX_VALUE;
    }

    public void visit(Variable variable) throws TermException {
        printer.begin(variable).append(variable.toString(isTyped())).end();
    }

    public void visit(ModalityTerm modalityTerm) throws TermException {
        printer.append("[ ");
        modalityTerm.getModality().visit(this);
        printer.append(" ]");
        visitMaybeParen(modalityTerm.getSubterm(0), Integer.MAX_VALUE);
    }

  
    public void visit(Binding binding) throws TermException {
        printer.begin(binding);
        Binder binder = binding.getBinder();
        String bindname = binder.getName();
        printer.append("(").append(bindname).append(" ");
        printer.append(binding.getVariableName());
        if(isTyped())
            printer.append(" as ").append(binding.getType().toString());
        for(Term t : binding.getSubterms()) {
            printer.append("; ");
            t.visit(this);
        }
        printer.append(")");
        printer.end();
    }

    public void visit(Application application) throws TermException {
        printer.begin(application);
        boolean isInParens = inParens;
        inParens = false;
        if(isInParens)
            printer.append("(");
        
        Function function = application.getFunction();
        String fctname = function.getName();
        
        FixOperator fixOperator = null;
        if(printFix)
            fixOperator = env.getReverseFixOperator(fctname);
        
        if(fixOperator != null) {
            if(isTyped())
                printer.append("(");
            
            if(function.getArity() == 1) {
                printPrefix(application, fixOperator);
            } else {
                printInfix(application, fixOperator);
            }
            
            if(isTyped())
                printer.append(") as ").append(application.getType().toString());
            
        } else {
            
            printApplication(application, fctname);
            
        }
        
        if(isInParens)
            printer.append(")");
        printer.end();
    }

    private void printPrefix(Application application, FixOperator fixOperator)
             throws TermException {
        Term subterm = application.getSubterm(0);
        if(printer.length() > 0 && isOperatorChar(printer.getLastCharacter()))
            printer.append(" ");
        printer.append(fixOperator.getOpIdentifier());
        visitMaybeParen(subterm, fixOperator.getPrecedence());
    }
    
    // keep this updated with TermParser.jj
    private boolean isOperatorChar(char c) {
        return "+-<>&|=*/!^".indexOf(c) != -1;
    }

    private void printInfix(Application application, FixOperator fixOperator)
            throws TermException {
        visitMaybeParen(application.getSubterm(0), fixOperator.getPrecedence());
        printer.append(" ").append(fixOperator.getOpIdentifier()).append(" ");
        // TODO explain
        visitMaybeParen(application.getSubterm(1), fixOperator.getPrecedence() + 1);
    }

   

    private void printApplication(Application application, String fctname)
            throws TermException {
        printer.append(fctname);
        List<Term> subterms = application.getSubterms();
        if(subterms.size() > 0) {
            boolean first = true;
            for (Term t : subterms) {
                printer.append(first ? "(" : ", ");
                first = false;
                t.visit(this);
            }
            printer.append(")");
        }
        if(isTyped())
            printer.append(" as " + application.getType());
    }

public void visit(SchemaVariable schemaVariable)
            throws TermException {
        printer.begin(schemaVariable).append(schemaVariable.toString(isTyped())).end();
    }

    
    public void visit(AssignModality assignModality)
            throws TermException {
        printer.append(assignModality.getAssignedConstant().getName()).append(" := ");
        assignModality.getAssignedTerm().visit(this);
    }

    public void visit(CompoundModality compoundModality)
            throws TermException {
        compoundModality.getSubModality(0).visit(this);
        printer.append("; "); 
        compoundModality.getSubModality(1).visit(this);
    }

    public void visit(IfModality ifModality) throws TermException {
        printer.append("if ");
        ifModality.getConditionTerm().visit(this);
        printer.append(" then ");
        ifModality.getThenModality().visit(this);
        Modality elseModality = ifModality.getElseModality();
        if(elseModality != null) {
            elseModality.visit(this);
        }
        printer.append(" end");
        
    }

    public void visit(SkipModality skipModality) throws TermException {
        printer.append("skip");
    }

    public void visit(WhileModality whileModality)
            throws TermException {
        printer.append("while ");
        whileModality.getConditionTerm().visit(this);
        printer.append(" do ");
        whileModality.getBody().visit(this);
        printer.append(" end");
    }
    
}
