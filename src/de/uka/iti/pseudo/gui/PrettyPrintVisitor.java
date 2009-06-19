package de.uka.iti.pseudo.gui;

import java.util.List;

import de.uka.iti.pseudo.environment.Binder;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.FixOperator;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.SchemaProgram;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.TermVisitor;
import de.uka.iti.pseudo.term.UpdateTerm;
import de.uka.iti.pseudo.term.Variable;
import de.uka.iti.pseudo.term.creation.SubtermCollector;
import de.uka.iti.pseudo.term.statement.AssignmentStatement;
import de.uka.iti.pseudo.util.AnnotatedStringWithStyles;

// TODO Documentation needed
/**
 * The Class PrettyPrint provides mean to prettyprint terms while keeping the
 * information about subterms in the resulting string.
 * 
 * Parentheses are introduced only where necessary. This is done using
 * 
 *
* <p>IMPORTANT! Keep the order in this visitor synchronized with the related 
* visitors {@link SubtermCollector}
*/
class PrettyPrintVisitor implements TermVisitor {
    
    private PrettyPrint pp;
    private Environment env;
    private AnnotatedStringWithStyles<Term> printer;
    
    /**
     * Indicator that the current subterm is to be put in parentheses
     */
    private boolean inParens;

    public PrettyPrintVisitor(PrettyPrint pp, AnnotatedStringWithStyles<Term> printer) {
        this.pp = pp;
        this.env = pp.getEnvironment();
        this.printer = printer;
    }
    
    
    
    /*
     * Visit a subterm and put it in parens possibly.
     * 
     * Parens are included if the subterm's precedence is less than
     * the precedence of the surrounding term
     * 
     * If typing is switched on, parentheses are included if the 
     * term has a non-maximal precedence, i.e. if it is a prefixed 
     * or infixed expression
     */
    private void visitMaybeParen(Term subterm, int precedence)
            throws TermException {

        int innerPrecedence = getPrecedence(subterm);
        if ((pp.isTyped() && innerPrecedence < Integer.MAX_VALUE)
                || (!pp.isTyped() && innerPrecedence < precedence)) {
            inParens = true;
            subterm.visit(this);
        } else {
            subterm.visit(this);
        }

    }

    /*
     * Gets the precedence of a term. This is straight forward if it is a fixed term.
     * Then the precedence of the operator is returned.
     * 
     * In any other case the precedence is maximal ({@link Integer#MAX_VALUE}
     * This is because every infix and prefix operator binds less than other term
     * constructions (binders, applications, even modalities)
     */
    private int getPrecedence(Term subterm) {

        if (pp.isPrintingFix() && subterm instanceof Application) {
            Application appl = (Application) subterm;
            FixOperator fix = env.getReverseFixOperator(appl.getFunction()
                    .getName());
            if (fix != null)
                return fix.getPrecedence();
        }

        return Integer.MAX_VALUE;
    }

    /*
     * Prints a term in prefix way.
     * 
     * Possibly insert an extra space if needed, that is if
     * two operators follow directly one another.
     */
    private void printPrefix(Application application, FixOperator fixOperator)
            throws TermException {
        
        assert fixOperator.getArity() == 1;
        
        Term subterm = application.getSubterm(0);
        if (printer.length() > 0 && isOperatorChar(printer.getLastCharacter()))
            printer.append(" ");
        printer.append(fixOperator.getOpIdentifier());
        visitMaybeParen(subterm, fixOperator.getPrecedence());
    }

    // keep this updated with TermParser.jj
    /**
     * Checks if a character is an operator char.
     */
    private boolean isOperatorChar(char c) {
        return "+-<>&|=*/!^".indexOf(c) != -1;
    }

    /*
     * Prints a term in infix way.
     * 
     * The first subterm is visited to be put in parens if the precedence is
     * strictly higher than that of this term.
     * 
     * The second subterm is visited to be put in parens if the precedence is
     * at least as high as that of this term.
     * 
     * Therefore plus(a,plus(b,c)) is put as a + (b + c)
     * and plus(plus(a,b),c) is put as a + b + c
     * 
     * All operators are left associative automatically.
     * 
     */
    private void printInfix(Application application, FixOperator fixOperator)
            throws TermException {
        visitMaybeParen(application.getSubterm(0), fixOperator.getPrecedence());
        printer.append(" ").append(fixOperator.getOpIdentifier()).append(" ");
        visitMaybeParen(application.getSubterm(1),
                fixOperator.getPrecedence() + 1);
    }

    /*
     * print an application in non-operator prefix form.
     */
    private void printApplication(Application application, String fctname)
            throws TermException {
        printer.append(fctname);
        List<Term> subterms = application.getSubterms();
        if (subterms.size() > 0) {
            boolean first = true;
            for (Term t : subterms) {
                printer.append(first ? "(" : ", ");
                first = false;
                t.visit(this);
            }
            printer.append(")");
        }
        if (pp.isTyped())
            printer.append(" as " + application.getType());
    }
    
    //
    // Visitors
    //
    
    public void visit(Variable variable) throws TermException {
        printer.setStyle("variable");
        printer.begin(variable).append(variable.toString(pp.isTyped())).end();
        printer.resetPreviousStyle();
    }

    public void visit(Binding binding) throws TermException {
        printer.begin(binding);
        Binder binder = binding.getBinder();
        String bindname = binder.getName();
        printer.append("(").append(bindname).append(" ");
        printer.setStyle("variable");
        printer.append(binding.getVariableName());
        if (pp.isTyped())
            printer.append(" as ").append(binding.getType().toString());
        printer.resetPreviousStyle();
        for (Term t : binding.getSubterms()) {
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
        if (isInParens)
            printer.append("(");

        Function function = application.getFunction();
        String fctname = function.getName();

        FixOperator fixOperator = null;
        if (pp.isPrintingFix())
            fixOperator = env.getReverseFixOperator(fctname);

        if (fixOperator != null) {
            if (pp.isTyped())
                printer.append("(");

            if (function.getArity() == 1) {
                printPrefix(application, fixOperator);
            } else {
                printInfix(application, fixOperator);
            }

            if (pp.isTyped())
                printer.append(") as ")
                        .append(application.getType().toString());

        } else {

            printApplication(application, fctname);

        }

        if (isInParens)
            printer.append(")");
        printer.end();
    }

    public void visit(SchemaVariable schemaVariable) throws TermException {
        printer.begin(schemaVariable)
                .append(schemaVariable.toString(pp.isTyped())).end();
    }
    
    public void visit(LiteralProgramTerm litProgTerm) throws TermException {
        printer.begin(litProgTerm);
        printer.setStyle("program");
        printer.append(litProgTerm.isTerminating() ? "[[ " : "[ ");
        printer.append(Integer.toString(litProgTerm.getProgramIndex())
                + "; " + litProgTerm.getProgram());
        printer.append(litProgTerm.isTerminating() ? " ]]" : " ]");
        printer.resetPreviousStyle();
        printer.end();
    }
    
    public void visit(SchemaProgram schemaProgramTerm)
            throws TermException {
        printer.begin(schemaProgramTerm);
        printer.setStyle("program");
        printer.append(schemaProgramTerm.isTerminating() ? "[[ " : "[ ");
        schemaProgramTerm.getSchemaVariable().visit(this);
        if(schemaProgramTerm.hasMatchingStatement()) {
            printer.append(" : " + schemaProgramTerm.getMatchingStatement().toString(pp.isTyped()));
        }
        printer.append(schemaProgramTerm.isTerminating() ? " ]]" : " ]");
        printer.resetPreviousStyle();
        printer.end();
    }
    
    public void visit(UpdateTerm updateTerm) throws TermException {
        printer.begin(updateTerm);
        printer.setStyle("update");
        printer.append("{ ");
        List<AssignmentStatement> assignments = updateTerm.getAssignments();
        for (int i = 0; i < assignments.size(); i++) {
            if(i > 0)
                printer.append(" || ");
            printer.append(assignments.get(i).getTarget().toString());
            printer.append(" := ");
            assignments.get(i).getValue().visit(this);
        }
        printer.append(" }");
        printer.resetPreviousStyle();
        visitMaybeParen(updateTerm.getSubterm(0), Integer.MAX_VALUE);
        printer.end();
    }

    public AnnotatedStringWithStyles<Term> getPrinter() {
        return printer;
    }


}
